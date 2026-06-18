package com.mustafakara.harcam.presentation.settings

import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.repository.PreferencesRepository.ThemeMode

/** Settings state — settings.md §7. Local preferences; defaults always present (no empty state). */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currency: Currency = Currency.TRY,
    val lockEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val maskAmounts: Boolean = false,
    val budgetAlerts: Boolean = true,
    val recurringReminders: Boolean = true,
)
