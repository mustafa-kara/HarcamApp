package com.mustafakara.harcam.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.core.common.Clock
import com.mustafakara.harcam.core.util.DateUtil
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.usecase.ObserveBudgetsUseCase
import com.mustafakara.harcam.domain.usecase.ObserveCategoriesUseCase
import com.mustafakara.harcam.domain.usecase.SetCategoryBudgetUseCase
import com.mustafakara.harcam.domain.usecase.SetMonthlyBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Edit budget ViewModel — seeds the monthly + per-category amount fields from the current limits
 * ONCE, then lets the user edit freely (edit_budget.md §7). User edits live in [monthlyText] and
 * the [categoryTexts] map keyed by categoryId so a reactive Room emit never clobbers in-flight
 * typing; only the (reactively updated) category list itself feeds the rows. Saving writes each
 * non-blank field through the validating use cases for the current month.
 */
@HiltViewModel
class EditBudgetViewModel @Inject constructor(
    observeBudgets: ObserveBudgetsUseCase,
    observeCategories: ObserveCategoriesUseCase,
    preferences: PreferencesRepository,
    private val setMonthlyBudget: SetMonthlyBudgetUseCase,
    private val setCategoryBudget: SetCategoryBudgetUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val monthlyText = MutableStateFlow("")
    private val categoryTexts = MutableStateFlow<Map<Long, String>>(emptyMap())
    private val saved = MutableStateFlow(false)
    private var seeded = false

    val uiState: StateFlow<EditBudgetUiState> = combine(
        observeBudgets(),
        observeCategories(),
        preferences.observe(),
        monthlyText,
        categoryTexts,
    ) { budgets, categories, prefs, monthly, texts ->
        if (!seeded) {
            val monthlyLimit = budgets.firstOrNull { it.categoryId == null }?.limit
            val seedTexts = budgets
                .filter { it.categoryId != null }
                .associate { it.categoryId!! to formatSeed(it.limit) }
            monthlyText.value = monthlyLimit?.let { formatSeed(it) } ?: ""
            categoryTexts.value = seedTexts
            seeded = true
            return@combine EditBudgetUiState(
                isLoading = false,
                currency = prefs.currency,
                monthlyText = monthlyText.value,
                categoryLimits = categories.map {
                    CategoryLimitField(category = it, text = categoryTexts.value[it.id].orEmpty())
                },
                exceedsMonthly = computeExceeds(monthlyText.value, categoryTexts.value, categories.map { it.id }),
                saved = saved.value,
            )
        }

        val fields = categories.map { category ->
            CategoryLimitField(category = category, text = texts[category.id].orEmpty())
        }
        EditBudgetUiState(
            isLoading = false,
            currency = prefs.currency,
            monthlyText = monthly,
            categoryLimits = fields,
            exceedsMonthly = computeExceeds(monthly, texts, categories.map { it.id }),
            saved = saved.value,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = EditBudgetUiState(isLoading = true),
    )

    fun updateMonthly(text: String) {
        monthlyText.value = text
    }

    fun updateCategory(categoryId: Long, text: String) {
        categoryTexts.value = categoryTexts.value.toMutableMap().apply { this[categoryId] = text }
    }

    fun save() {
        viewModelScope.launch {
            val monthKey = DateUtil.monthKey(clock.nowMs())
            parse(monthlyText.value)?.let { setMonthlyBudget(it, monthKey) }
            categoryTexts.value.forEach { (categoryId, text) ->
                parse(text)?.let { setCategoryBudget(categoryId, it, monthKey) }
            }
            saved.value = true
        }
    }

    /** Sum of category limits exceeds the monthly total — a warning, never blocking. */
    private fun computeExceeds(monthly: String, texts: Map<Long, String>, categoryIds: List<Long>): Boolean {
        val monthlyLimit = parse(monthly) ?: 0.0
        val categoryTotal = categoryIds.sumOf { parse(texts[it].orEmpty()) ?: 0.0 }
        return categoryTotal > monthlyLimit
    }

    private fun formatSeed(limit: Double): String =
        if (limit % 1.0 == 0.0) limit.toLong().toString() else limit.toString()

    /** Blank / invalid input parses to null (skipped on save, treated as 0 for totals). */
    private fun parse(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
