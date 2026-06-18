package com.mustafakara.harcam

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import com.mustafakara.harcam.core.navigation.HarcamNavHost
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.repository.PreferencesRepository.ThemeMode
import com.mustafakara.harcam.domain.repository.SecurityRepository
import com.mustafakara.harcam.presentation.auth.SessionLockState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Single-activity host. Extends [FragmentActivity] so the lock screen can launch BiometricPrompt.
 * Clears the session-unlock flag when the app goes to background so a return re-shows the lock
 * (pin_lock.md §2/§8) — the NavHost observes this and routes to `lock`.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var sessionLock: SessionLockState

    @Inject lateinit var security: SecurityRepository

    @Inject lateinit var preferences: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themeFlow = preferences.observe().map { it.themeMode }
        setContent {
            val themeMode by themeFlow.collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            HarcamTheme(darkTheme = darkTheme) {
                HarcamNavHost()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Re-lock on next foreground if the lock is enabled.
        if (security.isLockEnabled()) sessionLock.lock()
    }
}
