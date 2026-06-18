package com.mustafakara.harcam.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.repository.SecurityRepository
import com.mustafakara.harcam.domain.usecase.SeedDefaultCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryPreview(val name: String, val colorKey: String, val iconKey: String)

data class OnboardingUiState(
    val selectedCurrency: Currency = Currency.TRY,
    val lockEnabled: Boolean = false,
    val pinConfirmed: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val defaultCategories: List<CategoryPreview> = DEFAULTS

    companion object {
        val DEFAULTS = listOf(
            CategoryPreview("Food", "food", "restaurant"),
            CategoryPreview("Transport", "transport", "directions_bus"),
            CategoryPreview("Bills", "bills", "receipt_long"),
            CategoryPreview("Shopping", "shopping", "shopping_bag"),
            CategoryPreview("Health", "health", "favorite"),
            CategoryPreview("Entertainment", "entertainment", "sports_esports"),
            CategoryPreview("Other", "other", "category"),
        )
    }
}

/**
 * Onboarding ViewModel — picks currency, seeds default categories, optionally sets a PIN lock, and
 * flips the onboarded flag (onboarding.md §7). Seeding is idempotent so Retry is safe.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val seedDefaults: SeedDefaultCategoriesUseCase,
    private val preferences: PreferencesRepository,
    private val security: SecurityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _done = Channel<Unit>(Channel.BUFFERED)
    val done = _done.receiveAsFlow()

    fun selectCurrency(currency: Currency) = _uiState.update { it.copy(selectedCurrency = currency) }

    fun toggleLock(enabled: Boolean) = _uiState.update {
        it.copy(lockEnabled = enabled, pinConfirmed = if (enabled) it.pinConfirmed else false)
    }

    /** Called when the PIN-create sheet confirms a matching PIN. */
    fun onPinConfirmed(pin: String) = _uiState.update { it.copy(pinConfirmed = true, lockEnabled = true).also { _pendingPin = pin } }

    private var _pendingPin: String? = null

    fun getStarted() {
        val s = _uiState.value
        if (s.lockEnabled && !s.pinConfirmed) {
            _uiState.update { it.copy(errorMessage = "Confirm your PIN to enable the lock") }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                seedDefaults()
                preferences.setCurrency(s.selectedCurrency)
                if (s.lockEnabled) {
                    _pendingPin?.let { security.setPin(it) }
                }
                preferences.setOnboarded(true)
                _done.send(Unit)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = "Couldn't finish setup. Try again.") }
            }
        }
    }
}
