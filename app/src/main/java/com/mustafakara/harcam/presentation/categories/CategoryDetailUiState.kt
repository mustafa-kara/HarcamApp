package com.mustafakara.harcam.presentation.categories

import com.mustafakara.harcam.domain.model.BudgetStatus
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.Expense
import com.mustafakara.harcam.domain.model.ReportPeriod

/** Immutable category-detail state — category_detail.md §7, four-state mapping via [isLoading]. */
data class CategoryDetailUiState(
    val isLoading: Boolean = true,
    val category: Category? = null,
    val currency: Currency = Currency.TRY,
    val period: ReportPeriod = ReportPeriod.MONTH,
    val periodTotal: Double = 0.0,
    val transactionCount: Int = 0,
    val budget: BudgetStatus? = null,
    val expenses: List<Expense> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && expenses.isEmpty()
}
