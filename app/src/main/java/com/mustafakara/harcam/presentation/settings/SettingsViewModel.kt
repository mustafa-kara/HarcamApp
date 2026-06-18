package com.mustafakara.harcam.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.repository.PreferencesRepository.ThemeMode
import com.mustafakara.harcam.domain.repository.SecurityRepository
import com.mustafakara.harcam.presentation.auth.SessionLockState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Settings ViewModel — settings.md §7. Reactively reflects DataStore preferences + security flags;
 * toggles persist immediately (no Save button). Disabling the lock requires the screen to confirm.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: PreferencesRepository,
    private val security: SecurityRepository,
    private val sessionLock: SessionLockState,
) : ViewModel() {

    private val securityFlags = MutableStateFlow(readSecurity())

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences.observe(),
        securityFlags,
    ) { prefs, sec ->
        SettingsUiState(
            isLoading = false,
            themeMode = prefs.themeMode,
            currency = prefs.currency,
            lockEnabled = sec.lockEnabled,
            biometricEnabled = sec.biometricEnabled,
            maskAmounts = prefs.maskAmounts,
            budgetAlerts = prefs.budgetAlerts,
            recurringReminders = prefs.recurringReminders,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(isLoading = true),
    )

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { preferences.setThemeMode(mode) }

    fun setCurrency(currency: Currency) = viewModelScope.launch { preferences.setCurrency(currency) }

    fun setMaskAmounts(value: Boolean) = viewModelScope.launch { preferences.setMaskAmounts(value) }

    fun setBudgetAlerts(value: Boolean) = viewModelScope.launch { preferences.setBudgetAlerts(value) }

    fun setRecurringReminders(value: Boolean) =
        viewModelScope.launch { preferences.setRecurringReminders(value) }

    /** Enabling lock requires a PIN that the screen captures via the PIN-create sheet. */
    fun enableLock(pin: String) = viewModelScope.launch {
        security.setPin(pin)
        sessionLock.markUnlocked()
        refreshSecurity()
    }

    fun disableLock() = viewModelScope.launch {
        security.disableLock()
        refreshSecurity()
    }

    fun setBiometric(value: Boolean) = viewModelScope.launch {
        security.setBiometricEnabled(value)
        refreshSecurity()
    }

    private data class SecurityFlags(val lockEnabled: Boolean, val biometricEnabled: Boolean)

    private fun readSecurity() = SecurityFlags(security.isLockEnabled(), security.isBiometricEnabled())

    private fun refreshSecurity() {
        securityFlags.value = readSecurity()
    }
}
