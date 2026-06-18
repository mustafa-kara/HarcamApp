package com.mustafakara.harcam.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.domain.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LockUiState(
    val pinLength: Int = 4,
    val entered: String = "",
    val biometricEnabled: Boolean = false,
    val isVerifying: Boolean = false,
    val wrongPin: Boolean = false,
    val remainingAttempts: Int = MAX_ATTEMPTS,
) {
    val enteredCount: Int get() = entered.length

    companion object {
        const val MAX_ATTEMPTS = 5
    }
}

/**
 * Lock ViewModel — verifies the entered PIN against the stored hash (pin_lock.md §7); never
 * compares plaintext (hashing lives in [SecurityRepository]). Emits [Unlocked] on success so the
 * screen can navigate and mark the session unlocked.
 */
@HiltViewModel
class LockViewModel @Inject constructor(
    private val security: SecurityRepository,
    private val sessionLock: SessionLockState,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LockUiState(biometricEnabled = security.isBiometricEnabled()),
    )
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    private val _events = Channel<Unit>(Channel.BUFFERED)
    /** Emits once when unlocked. */
    val unlocked = _events.receiveAsFlow()

    fun onDigit(digit: Int) {
        val s = _uiState.value
        if (s.isVerifying || s.entered.length >= s.pinLength) return
        val next = s.entered + digit
        _uiState.update { it.copy(entered = next, wrongPin = false) }
        if (next.length == s.pinLength) verify(next)
    }

    fun onBackspace() {
        _uiState.update { if (it.entered.isEmpty()) it else it.copy(entered = it.entered.dropLast(1), wrongPin = false) }
    }

    fun onBiometricSuccess() {
        unlock()
    }

    private fun verify(pin: String) {
        _uiState.update { it.copy(isVerifying = true) }
        viewModelScope.launch {
            val ok = security.verifyPin(pin)
            if (ok) {
                unlock()
            } else {
                _uiState.update {
                    it.copy(
                        isVerifying = false,
                        entered = "",
                        wrongPin = true,
                        remainingAttempts = (it.remainingAttempts - 1).coerceAtLeast(0),
                    )
                }
            }
        }
    }

    private fun unlock() {
        sessionLock.markUnlocked()
        _uiState.update { it.copy(isVerifying = false) }
        viewModelScope.launch { _events.send(Unit) }
    }
}
