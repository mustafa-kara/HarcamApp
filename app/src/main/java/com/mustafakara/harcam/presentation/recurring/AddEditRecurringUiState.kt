package com.mustafakara.harcam.presentation.recurring

import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.RecurrenceCadence

/** Add/Edit recurring form state — add_edit_recurring.md §7. */
data class AddEditRecurringUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val name: String = "",
    val amountText: String = "",
    val amount: Double = 0.0,
    val currency: Currency = Currency.TRY,
    val categoryId: Long? = null,
    val cadence: RecurrenceCadence = RecurrenceCadence.MONTHLY,
    val nextDueMs: Long = 0L,
    val reminderDaysBefore: Int = 1,
    val isPaused: Boolean = false,
    val categories: List<Category> = emptyList(),
    val nameError: Boolean = false,
    val amountError: Boolean = false,
    val categoryError: Boolean = false,
    val saved: Boolean = false,
)
