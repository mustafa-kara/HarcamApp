package com.mustafakara.harcam.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.core.util.DateUtil
import com.mustafakara.harcam.domain.model.Expense
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.usecase.AddExpenseUseCase
import com.mustafakara.harcam.domain.usecase.DeleteExpenseUseCase
import com.mustafakara.harcam.domain.usecase.ObserveCategoriesUseCase
import com.mustafakara.harcam.domain.usecase.ObserveExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * Expense list ViewModel — composes all expenses (optionally filtered by category), the category
 * lookup, and the user's currency into one reactive [ExpenseListUiState] grouped by day with
 * per-day signed totals (expense_list.md §7, architecture.md §2/§4). Reads observe Room, so adds,
 * edits, and deletes elsewhere refresh the list automatically. Local Room reads do not error, so
 * there is no error state to map here.
 */
@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    observeExpenses: ObserveExpensesUseCase,
    observeCategories: ObserveCategoriesUseCase,
    preferences: PreferencesRepository,
    private val addExpense: AddExpenseUseCase,
    private val deleteExpense: DeleteExpenseUseCase,
) : ViewModel() {

    private val selectedCategoryId = MutableStateFlow<Long?>(null)
    private val recentlyDeleted = MutableStateFlow<Expense?>(null)

    val uiState: StateFlow<ExpenseListUiState> = combine(
        observeExpenses(),
        observeCategories(),
        preferences.observe(),
        selectedCategoryId,
        recentlyDeleted,
    ) { expenses, categories, prefs, selectedId, deleted ->
        val filtered = if (selectedId == null) expenses else expenses.filter { it.categoryId == selectedId }
        ExpenseListUiState(
            isLoading = false,
            currency = prefs.currency,
            days = groupByDay(filtered),
            categoriesById = categories.associateBy { it.id },
            selectedCategoryId = selectedId,
            allCategories = categories,
            recentlyDeleted = deleted,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = ExpenseListUiState(isLoading = true),
    )

    fun selectCategory(id: Long?) {
        selectedCategoryId.value = id
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            val snapshot = uiState.value.days.flatMap { it.items }.firstOrNull { it.id == id }
            deleteExpense.invoke(id)
            recentlyDeleted.value = snapshot
        }
    }

    fun undoDelete() {
        val snapshot = recentlyDeleted.value ?: return
        recentlyDeleted.value = null
        viewModelScope.launch {
            addExpense(
                amount = snapshot.amount,
                categoryId = snapshot.categoryId,
                currency = snapshot.currency,
                note = snapshot.note,
                createdAt = snapshot.createdAt,
            )
        }
    }

    /** Group [expenses] by their local calendar day, newest day first, with a signed day total. */
    private fun groupByDay(expenses: List<Expense>): List<ExpenseDayGroup> =
        expenses
            .groupBy { DateUtil.startOfDay(it.createdAt) }
            .toSortedMap(compareByDescending { it })
            .map { (dayStart, items) ->
                ExpenseDayGroup(
                    dateLabel = dayLabelFormat.format(dayStart),
                    dayTotal = items.sumOf { it.amount },
                    items = items.sortedByDescending { it.createdAt },
                )
            }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        val dayLabelFormat = SimpleDateFormat("EEEE · d MMM", Locale.getDefault())
    }
}
