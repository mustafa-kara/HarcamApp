package com.mustafakara.harcam.presentation.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.core.common.Clock
import com.mustafakara.harcam.core.util.DateUtil
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.usecase.ObserveBudgetStatusUseCase
import com.mustafakara.harcam.domain.usecase.ObserveCategoriesUseCase
import com.mustafakara.harcam.domain.usecase.ObserveCategoryExpensesUseCase
import com.mustafakara.harcam.domain.model.ReportPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Category detail ViewModel — category_detail.md §7. [id] comes from the `categories/{id}` route
 * via [SavedStateHandle]. The selected [ReportPeriod] is local state; the spend total and count
 * are computed from this category's expenses filtered to the period window (the period report use
 * case is not per-category), recomputed reactively as Room flows emit.
 */
@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCategories: ObserveCategoriesUseCase,
    observeCategoryExpenses: ObserveCategoryExpensesUseCase,
    observeBudgetStatus: ObserveBudgetStatusUseCase,
    preferences: PreferencesRepository,
    private val clock: Clock,
) : ViewModel() {

    private val id: Long = checkNotNull(savedStateHandle["id"])

    private val periodFlow = MutableStateFlow(ReportPeriod.MONTH)

    val uiState: StateFlow<CategoryDetailUiState> = combine(
        observeCategories(),
        observeCategoryExpenses(id),
        observeBudgetStatus(),
        preferences.observe(),
        periodFlow,
    ) { categories, expenses, budgetOverview, prefs, period ->
        val range = DateUtil.rangeFor(period, clock.nowMs())
        val periodExpenses = expenses.filter { it.createdAt in range.startMs..range.endMs }

        CategoryDetailUiState(
            isLoading = false,
            category = categories.firstOrNull { it.id == id },
            currency = prefs.currency,
            period = period,
            periodTotal = periodExpenses.sumOf { it.amount },
            transactionCount = periodExpenses.size,
            budget = budgetOverview.perCategory.firstOrNull { it.categoryId == id },
            expenses = periodExpenses,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = CategoryDetailUiState(isLoading = true),
    )

    fun selectPeriod(period: ReportPeriod) {
        periodFlow.value = period
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
