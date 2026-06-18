package com.mustafakara.harcam.presentation.auth

import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide "unlocked this session" flag (pin_lock.md §7). Held above the nav graph so a
 * background→resume can decide whether to re-show `lock`; reset when the app backgrounds or on
 * process death (this is in-memory only, so a fresh process starts locked).
 */
@Singleton
class SessionLockState @Inject constructor() {
    var isUnlocked: Boolean = false
        private set

    fun markUnlocked() { isUnlocked = true }
    fun lock() { isUnlocked = false }
}
