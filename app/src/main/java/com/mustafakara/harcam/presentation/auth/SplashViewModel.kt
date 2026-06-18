package com.mustafakara.harcam.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Where splash should route after reading app state (splash.md). */
enum class SplashDestination { ONBOARDING, LOCK, DASHBOARD }

/**
 * Splash gate — reads onboarded + lock state once, then emits the destination (splash.md §7).
 * Not-onboarded → onboarding; lock enabled and session not unlocked → lock; else → dashboard.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferences: PreferencesRepository,
    private val security: SecurityRepository,
    private val sessionLock: SessionLockState,
) : ViewModel() {

    private val _destination = Channel<SplashDestination>(Channel.BUFFERED)
    val destination = _destination.receiveAsFlow()

    init {
        viewModelScope.launch {
            val prefs = preferences.observe().first()
            val target = when {
                !prefs.onboarded -> SplashDestination.ONBOARDING
                security.isLockEnabled() && !sessionLock.isUnlocked -> SplashDestination.LOCK
                else -> SplashDestination.DASHBOARD
            }
            _destination.send(target)
        }
    }
}
