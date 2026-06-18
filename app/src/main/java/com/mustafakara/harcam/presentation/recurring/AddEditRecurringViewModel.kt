package com.mustafakara.harcam.presentation.recurring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.core.common.Clock
import com.mustafakara.harcam.domain.model.RecurrenceCadence
import com.mustafakara.harcam.domain.model.RecurringExpense
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.usecase.DeleteRecurringUseCase
import com.mustafakara.harcam.domain.usecase.GetRecurringUseCase
import com.mustafakara.harcam.domain.usecase.ObserveCategoriesUseCase
import com.mustafakara.harcam.domain.usecase.RecurringValidationError
import com.mustafakara.harcam.domain.usecase.UpsertRecurringUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Add/Edit recurring ViewModel — add_edit_recurring.md §7. Reads optional [id] from the route
 * `recurring/edit?id={id}` (absent/<=0 = create). Validates via [UpsertRecurringUseCase].
 */
@HiltViewModel
class AddEditRecurringViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCategories: ObserveCategoriesUseCase,
    preferences: PreferencesRepository,
    private val getRecurring: GetRecurringUseCase,
    private val upsertRecurring: UpsertRecurringUseCase,
    private val deleteRecurring: DeleteRecurringUseCase,
    clock: Clock,
) : ViewModel() {

    private val recurringId: Long = savedStateHandle.get<Long>("id")?.takeIf { it > 0L } ?: 0L
    private val isEditing: Boolean = recurringId > 0L

    private val _uiState = MutableStateFlow(
        AddEditRecurringUiState(
            isEditing = isEditing,
            isLoading = isEditing,
            nextDueMs = clock.nowMs(),
        ),
    )
    val uiState: StateFlow<AddEditRecurringUiState> = _uiState.asStateFlow()

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
                val r = getRecurring(recurringId)
                if (r != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = r.name,
                            amount = r.amount,
                            amountText = formatAmountInput(r.amount),
                            categoryId = r.categoryId,
                            cadence = r.cadence,
                            nextDueMs = r.nextDueDate,
                            reminderDaysBefore = r.reminderDaysBefore,
                            isPaused = r.isPaused,
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value, nameError = false) }

    fun updateAmount(text: String) {
        val parsed = text.replace(',', '.').toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(amountText = text, amount = parsed, amountError = false) }
    }

    fun selectCategory(id: Long) = _uiState.update { it.copy(categoryId = id, categoryError = false) }

    fun selectCadence(cadence: RecurrenceCadence) = _uiState.update { it.copy(cadence = cadence) }

    fun updateNextDue(ms: Long) = _uiState.update { it.copy(nextDueMs = ms) }

    fun togglePaused() = _uiState.update { it.copy(isPaused = !it.isPaused) }

    fun save() {
        val s = _uiState.value
        viewModelScope.launch {
            val result = upsertRecurring(
                RecurringExpense(
                    id = recurringId,
                    name = s.name.trim(),
                    amount = s.amount,
                    currency = s.currency,
                    categoryId = s.categoryId ?: 0L,
                    cadence = s.cadence,
                    nextDueDate = s.nextDueMs,
                    reminderDaysBefore = s.reminderDaysBefore,
                    isPaused = s.isPaused,
                ),
            )
            when (result) {
                is UpsertRecurringUseCase.Result.Success -> _uiState.update { it.copy(saved = true) }
                is UpsertRecurringUseCase.Result.Invalid -> _uiState.update {
                    when (result.error) {
                        RecurringValidationError.BLANK_NAME -> it.copy(nameError = true)
                        RecurringValidationError.NON_POSITIVE_AMOUNT -> it.copy(amountError = true)
                        RecurringValidationError.NO_CATEGORY -> it.copy(categoryError = true)
                    }
                }
            }
        }
    }

    fun delete() {
        if (!isEditing) return
        viewModelScope.launch {
            deleteRecurring(recurringId)
            _uiState.update { it.copy(saved = true) }
        }
    }

    private fun formatAmountInput(amount: Double): String =
        if (amount == amount.toLong().toDouble()) amount.toLong().toString() else amount.toString()
}
