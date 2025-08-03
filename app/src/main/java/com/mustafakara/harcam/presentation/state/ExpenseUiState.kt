package com.mustafakara.harcam.presentation.state

import com.mustafakara.harcam.data.entity.ExpenseEntity

/**
 * ViewModel'den UI'a aktarılan state
 */
data class ExpenseUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAddExpenseDialogVisible: Boolean = false,
    val editingExpense: ExpenseEntity? = null
)

data class AddExpenseUiState(
    val description: String = "",
    val amount: String = "",
    val isDescriptionError: Boolean = false,
    val isAmountError: Boolean = false,
    val descriptionErrorMessage: String = "",
    val amountErrorMessage: String = ""
) {
    val isValid: Boolean
        get() = description.isNotBlank() && 
                amount.isNotBlank() && 
                amount.toDoubleOrNull() != null &&
                amount.toDoubleOrNull()!! > 0 &&
                !isDescriptionError && 
                !isAmountError
}