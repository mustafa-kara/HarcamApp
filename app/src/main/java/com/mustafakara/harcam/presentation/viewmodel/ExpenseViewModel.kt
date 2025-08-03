package com.mustafakara.harcam.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.data.entity.ExpenseEntity
import com.mustafakara.harcam.data.repository.ExpenseRepository
import com.mustafakara.harcam.presentation.state.AddExpenseUiState
import com.mustafakara.harcam.presentation.state.ExpenseUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Harcama ekranı için ViewModel
 * viewModel katmanı
 * UI state management ve business logici içerir
 */
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    // Ana ekran UI state
    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    // Harcama ekleme form state
    private val _addExpenseState = MutableStateFlow(AddExpenseUiState())
    val addExpenseState: StateFlow<AddExpenseUiState> = _addExpenseState.asStateFlow()

    init {
        startReactiveDataFlow()
    }

    /**
     * Repositoryden veri akışını başlatır
     * Veritabanında herhangi bir değişiklik olduğunda otomatik güncellenir
     */
    private fun startReactiveDataFlow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                combine(
                    repository.getTodayExpenses(),
                    repository.getTodayTotalAmount()
                ) { expenses, total ->
                    ExpenseUiState(
                        expenses = expenses,
                        totalAmount = total ?: 0.0,
                        isLoading = false,
                        errorMessage = null,
                        isAddExpenseDialogVisible = _uiState.value.isAddExpenseDialogVisible,
                        editingExpense = _uiState.value.editingExpense
                    )
                }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Veriler yüklenirken hata oluştu: ${exception.message}"
                    )
                }
                .collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen hata: ${e.message}"
                )
            }
        }
    }

    /**
     * Eski
     */
    private fun loadExpenseData() {

    }

    fun showAddExpenseDialog() {
        _uiState.value = _uiState.value.copy(isAddExpenseDialogVisible = true)
        // Form state'ini temizle
        _addExpenseState.value = AddExpenseUiState()
    }

    fun hideAddExpenseDialog() {
        _uiState.value = _uiState.value.copy(isAddExpenseDialogVisible = false)

        _addExpenseState.value = AddExpenseUiState()
    }

    fun updateDescription(description: String) {
        val isError = description.isBlank()
        val errorMessage = if (isError) "Açıklama boş bırakılamaz" else ""
        
        _addExpenseState.value = _addExpenseState.value.copy(
            description = description,
            isDescriptionError = isError,
            descriptionErrorMessage = errorMessage
        )
    }

    fun updateAmount(amount: String) {
        val numericAmount = amount.toDoubleOrNull()
        val isError = amount.isBlank() || numericAmount == null || numericAmount <= 0
        val errorMessage = when {
            amount.isBlank() -> "Tutar boş bırakılamaz"
            numericAmount == null -> "Geçerli bir sayı giriniz"
            numericAmount <= 0 -> "Tutar 0'dan büyük olmalıdır"
            else -> ""
        }
        
        _addExpenseState.value = _addExpenseState.value.copy(
            amount = amount,
            isAmountError = isError,
            amountErrorMessage = errorMessage
        )
    }

    fun addExpense() {
        val currentState = _addExpenseState.value
        
        if (!currentState.isValid) {
            return
        }

        viewModelScope.launch {
            try {
                repository.addExpense(
                    description = currentState.description,
                    amount = currentState.amount.toDouble()
                )
                hideAddExpenseDialog()
                // Reactive flow otomatik günceller, manuel refresh gerekmiyor
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Harcama eklenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense)
                // eski
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Harcama silinirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            try {
                repository.updateExpense(expense)
                hideEditExpenseDialog()
                // eski
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Harcama güncellenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun showEditExpenseDialog(expense: ExpenseEntity) {
        _uiState.value = _uiState.value.copy(editingExpense = expense)
    }

    fun hideEditExpenseDialog() {
        _uiState.value = _uiState.value.copy(editingExpense = null)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun refreshData() {
        loadExpenseData()
    }
}