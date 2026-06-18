package com.mustafakara.harcam.presentation.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakara.harcam.core.ui.components.PinDots
import com.mustafakara.harcam.core.ui.components.PinPad
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.IconSize
import com.mustafakara.harcam.core.ui.theme.Spacing

/**
 * PIN / biometric lock (pin_lock.md). PinPad + masked dots; auto-verifies on full entry. If
 * biometric is enrolled and enabled, the prompt auto-launches and the fingerprint key re-launches
 * it. Unlock fades through to the dashboard.
 */
@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LockViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val canBiometric = state.biometricEnabled && activity != null && biometricReady(context)

    LaunchedEffect(Unit) { viewModel.unlocked.collect { onUnlocked() } }

    LaunchedEffect(canBiometric) {
        if (canBiometric && activity != null) {
            promptBiometric(activity, onSuccess = viewModel::onBiometricSuccess)
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Outlined.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(IconSize.xl),
        )
        Text(
            "Enter your PIN",
            style = HarcamTheme.type.title,
            color = HarcamTheme.colors.textPrimary,
            modifier = Modifier.padding(top = Spacing.lg),
        )
        PinDots(
            length = state.pinLength,
            entered = state.enteredCount,
            modifier = Modifier.padding(top = Spacing.xl),
        )
        if (state.wrongPin) {
            Text(
                text = "Incorrect PIN · ${state.remainingAttempts} left",
                style = HarcamTheme.type.caption,
                color = HarcamTheme.colors.danger,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = Spacing.sm)
                    .semantics { liveRegion = LiveRegionMode.Assertive },
            )
        }
        PinPad(
            onDigit = viewModel::onDigit,
            onBackspace = viewModel::onBackspace,
            biometricAvailable = canBiometric,
            onBiometric = { activity?.let { promptBiometric(it, viewModel::onBiometricSuccess) } },
            enabled = !state.isVerifying,
            modifier = Modifier.padding(top = Spacing.xxl),
        )
    }
}

private fun biometricReady(context: android.content.Context): Boolean =
    BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
        BiometricManager.BIOMETRIC_SUCCESS

private fun promptBiometric(activity: FragmentActivity, onSuccess: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
            // Failure / cancel falls back silently to PIN — no error scream (pin_lock.md §5).
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Harcam")
        .setSubtitle("Use your fingerprint or face")
        .setNegativeButtonText("Use PIN")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        .build()
    prompt.authenticate(info)
}
