package com.mustafakara.harcam.presentation.auth

import androidx.lifecycle.ViewModel
import com.mustafakara.harcam.domain.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Exposes the resume-lock decision to the NavHost (pin_lock.md §7 cross-feature state). When the
 * app returns from background with the lock enabled and the session no longer unlocked, the host
 * routes back to `lock`.
 */
@HiltViewModel
class LockGateViewModel @Inject constructor(
    private val security: SecurityRepository,
    private val sessionLock: SessionLockState,
) : ViewModel() {
    fun shouldRelock(): Boolean = security.isLockEnabled() && !sessionLock.isUnlocked
}
