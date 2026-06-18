package com.mustafakara.harcam.presentation.expenses

import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency

/** Immutable add/edit-expense form state — add_edit_expense.md §7. */
data class AddEditExpenseUiState(
    val isEditing: Boolean = false,
    val amountText: String = "",
    val amount: Double = 0.0,
    val categoryId: Long? = null,
    val note: String = "",
    val dateMs: Long = 0L,
    val makeRecurring: Boolean = false,
    val categories: List<Category> = emptyList(),
    val currency: Currency = Currency.TRY,
    val amountError: Boolean = false,
    val categoryError: Boolean = false,
    val saved: Boolean = false,
    val isLoading: Boolean = false,
)
