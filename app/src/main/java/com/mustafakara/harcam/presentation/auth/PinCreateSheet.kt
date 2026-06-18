package com.mustafakara.harcam.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.mustafakara.harcam.core.ui.components.PinDots
import com.mustafakara.harcam.core.ui.components.PinPad
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.Spacing

/**
 * Reusable "create + confirm PIN" bottom sheet — used by onboarding and settings (onboarding.md
 * §6). Enter a PIN, confirm it; a mismatch resets and announces. Calls [onConfirmed] with the
 * matched PIN; digits are never echoed (handled by [PinDots]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinCreateSheet(
    onConfirmed: (String) -> Unit,
    onDismiss: () -> Unit,
    pinLength: Int = 4,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var firstPin by remember { mutableStateOf<String?>(null) }
    var entered by remember { mutableStateOf("") }
    var mismatch by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Text(
                text = if (firstPin == null) "Create a PIN" else "Confirm your PIN",
                style = HarcamTheme.type.title,
                color = HarcamTheme.colors.textPrimary,
            )
            PinDots(length = pinLength, entered = entered.length)
            if (mismatch) {
                Text(
                    "PINs don't match",
                    style = HarcamTheme.type.caption,
                    color = HarcamTheme.colors.danger,
                    textAlign = TextAlign.Center,
                )
            }
            PinPad(
                onDigit = { d ->
                    if (entered.length < pinLength) {
                        entered += d
                        if (entered.length == pinLength) {
                            val current = firstPin
                            when {
                                current == null -> {
                                    firstPin = entered
                                    entered = ""
                                    mismatch = false
                                }
                                current == entered -> onConfirmed(current)
                                else -> {
                                    firstPin = null
                                    entered = ""
                                    mismatch = true
                                }
                            }
                        }
                    }
                },
                onBackspace = { if (entered.isNotEmpty()) entered = entered.dropLast(1) },
            )
        }
    }
}
