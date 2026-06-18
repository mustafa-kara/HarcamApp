package com.mustafakara.harcam.presentation.expenses

import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.Expense

/** A single day's expenses with its running signed total — expense_list.md §7 (DayGroup). */
data class ExpenseDayGroup(
    val dateLabel: String,
    val dayTotal: Double,
    val items: List<Expense>,
)

/** Immutable expense-list state — expense_list.md §7, four-state mapping via [isLoading]. */
data class ExpenseListUiState(
    val isLoading: Boolean = true,
    val currency: Currency = Currency.TRY,
    val days: List<ExpenseDayGroup> = emptyList(),
    val categoriesById: Map<Long, Category> = emptyMap(),
    val selectedCategoryId: Long? = null,
    val allCategories: List<Category> = emptyList(),
    val recentlyDeleted: Expense? = null,
) {
    val isEmpty: Boolean get() = !isLoading && days.isEmpty()
}
