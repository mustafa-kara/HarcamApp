package com.mustafakara.harcam.presentation.expenses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.core.common.Clock
import com.mustafakara.harcam.domain.model.Expense
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.usecase.AddExpenseUseCase
import com.mustafakara.harcam.domain.usecase.DeleteExpenseUseCase
import com.mustafakara.harcam.domain.usecase.ExpenseValidation
import com.mustafakara.harcam.domain.usecase.ExpenseValidationError
import com.mustafakara.harcam.domain.usecase.GetExpenseUseCase
import com.mustafakara.harcam.domain.usecase.ObserveCategoriesUseCase
import com.mustafakara.harcam.domain.usecase.UpdateExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Add/Edit expense ViewModel — add_edit_expense.md §7. Reads the optional expense [id] from
 * [SavedStateHandle] (route `expenses/edit?id={id}`; absent or <= 0 = create). In edit mode it
 * loads the existing expense; it observes categories + the user's currency for the form, validates
 * via [AddExpenseUseCase]/[UpdateExpenseUseCase] (mapping [ExpenseValidation] to field errors),
 * and flags [AddEditExpenseUiState.saved] for the screen to navigate back.
 */
@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCategories: ObserveCategoriesUseCase,
    preferences: PreferencesRepository,
    private val getExpense: GetExpenseUseCase,
    private val addExpense: AddExpenseUseCase,
    private val updateExpense: UpdateExpenseUseCase,
    private val deleteExpense: DeleteExpenseUseCase,
    clock: Clock,
) : ViewModel() {

    private val expenseId: Long = savedStateHandle.get<Long>("id")?.takeIf { it > 0L } ?: 0L
    private val isEditing: Boolean = expenseId > 0L

    private val _uiState = MutableStateFlow(
        AddEditExpenseUiState(
            isEditing = isEditing,
            dateMs = clock.nowMs(),
            isLoading = isEditing,
        ),
    )
    val uiState: StateFlow<AddEditExpenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
        viewModelScope.launch {
            preferences.observe().collect { prefs ->
                _uiState.update { it.copy(currency = prefs.currency) }
            }
        }
        if (isEditing) {
            viewModelScope.launch {
                val expense = getExpense(expenseId)
                if (expense != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            amount = expense.amount,
                            amountText = formatAmountInput(expense.amount),
                            categoryId = expense.categoryId,
                            note = expense.note,
                            dateMs = expense.createdAt,
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun updateAmount(text: String) {
        val parsed = text.replace(',', '.').toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(amountText = text, amount = parsed, amountError = false) }
    }

    fun selectCategory(id: Long) {
        _uiState.update { it.copy(categoryId = id, categoryError = false) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun updateDate(dateMs: Long) {
        _uiState.update { it.copy(dateMs = dateMs) }
    }

    fun toggleRecurring() {
        _uiState.update { it.copy(makeRecurring = !it.makeRecurring) }
    }

    fun save() {
        val current = _uiState.value
        viewModelScope.launch {
            try {
                if (isEditing) {
                    updateExpense(
                        Expense(
                            id = expenseId,
                            amount = current.amount,
                            currency = current.currency,
                            categoryId = current.categoryId ?: 0L,
                            note = current.note,
                            createdAt = current.dateMs,
                        ),
                    )
                } else {
                    addExpense(
                        amount = current.amount,
                        categoryId = current.categoryId ?: 0L,
                        currency = current.currency,
                        note = current.note,
                        createdAt = current.dateMs,
                    )
                }
                _uiState.update { it.copy(saved = true) }
            } catch (validation: ExpenseValidation) {
                _uiState.update {
                    when (validation.error) {
                        ExpenseValidationError.AmountNotPositive -> it.copy(amountError = true)
                        ExpenseValidationError.CategoryMissing -> it.copy(categoryError = true)
                    }
                }
            }
        }
    }

    fun delete() {
        if (!isEditing) return
        viewModelScope.launch {
            deleteExpense(expenseId)
            _uiState.update { it.copy(saved = true) }
        }
    }

    private fun formatAmountInput(amount: Double): String =
        if (amount == amount.toLong().toDouble()) amount.toLong().toString() else amount.toString()
}
