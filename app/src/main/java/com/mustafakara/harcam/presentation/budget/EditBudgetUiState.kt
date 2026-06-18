package com.mustafakara.harcam.presentation.budget

import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency

/** Immutable edit-budget form state — edit_budget.md §7, four-state mapping via [isLoading]. */
data class EditBudgetUiState(
    val isLoading: Boolean = true,
    val currency: Currency = Currency.TRY,
    val monthlyText: String = "",
    val categoryLimits: List<CategoryLimitField> = emptyList(),
    /** Warning (not blocking): sum of category limits exceeds the monthly total. */
    val exceedsMonthly: Boolean = false,
    val saved: Boolean = false,
)

/** A category paired with its editable limit text — blank means "no limit". */
data class CategoryLimitField(
    val category: Category,
    val text: String,
)
