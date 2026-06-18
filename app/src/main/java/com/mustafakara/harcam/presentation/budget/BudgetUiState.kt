package com.mustafakara.harcam.presentation.budget

import com.mustafakara.harcam.domain.model.BudgetStatus
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency

/** Immutable budget-overview state — budget_overview.md §7, four-state mapping via [isLoading]. */
data class BudgetUiState(
    val isLoading: Boolean = true,
    val currency: Currency = Currency.TRY,
    val monthly: BudgetStatus? = null,
    val categoryRows: List<CategoryBudgetRow> = emptyList(),
) {
    /** Empty when there is no monthly budget AND no per-category budgets set (budget_overview.md §8). */
    val isEmpty: Boolean get() = !isLoading && monthly == null && categoryRows.isEmpty()
}

/** A category paired with its derived budget status — one row in the "By category" list. */
data class CategoryBudgetRow(
    val category: Category,
    val status: BudgetStatus,
)
