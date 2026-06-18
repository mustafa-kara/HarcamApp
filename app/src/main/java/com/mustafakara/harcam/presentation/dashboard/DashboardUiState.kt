package com.mustafakara.harcam.presentation.dashboard

import com.mustafakara.harcam.domain.model.BudgetStatus
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.Expense

/** Immutable dashboard state — design.md §7, four-state mapping via [isLoading]. */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val currency: Currency = Currency.TRY,
    val monthSpent: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthlyBudget: BudgetStatus? = null,
    val recent: List<Expense> = emptyList(),
    val categoriesById: Map<Long, Category> = emptyMap(),
    val maskAmounts: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && recent.isEmpty() && monthSpent == 0.0
}
