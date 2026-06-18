package com.mustafakara.harcam.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mustafakara.harcam.core.ui.pressScale
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.IconSize
import com.mustafakara.harcam.core.ui.theme.Spacing

/** Masked PIN dots — design.md §8.15. Filled = entered; count only, never the digits. */
@Composable
fun PinDots(length: Int, entered: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.semantics { contentDescription = "$entered of $length digits entered" },
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        repeat(length) { index ->
            val filled = index < entered
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    ),
            )
        }
    }
}

/**
 * Numeric keypad — design.md §8.15. 0–9 grid, backspace, and an optional fingerprint key. Each
 * key ≥56dp with a press-scale + accessible label; digits are never echoed.
 */
@Composable
fun PinPad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    biometricAvailable: Boolean = false,
    onBiometric: () -> Unit = {},
    enabled: Boolean = true,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
    )
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                row.forEach { digit ->
                    PinKey(label = digit, enabled = enabled, onClick = { onDigit(digit.toInt()) })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            if (biometricAvailable) {
                PinKey(
                    icon = Icons.Outlined.Fingerprint,
                    contentDescription = "Unlock with fingerprint",
                    enabled = enabled,
                    onClick = onBiometric,
                )
            } else {
                Box(modifier = Modifier.size(72.dp))
            }
            PinKey(label = "0", enabled = enabled, onClick = { onDigit(0) })
            PinKey(
                icon = Icons.Outlined.Backspace,
                contentDescription = "Delete",
                enabled = enabled,
                onClick = onBackspace,
            )
        }
    }
}

@Composable
private fun PinKey(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    contentDescription: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(72.dp)
            .pressScale(interactionSource)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material.ripple.rememberRipple(bounded = false),
                enabled = enabled,
                onClick = onClick,
            )
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        when {
            label != null -> Text(label, style = HarcamTheme.type.headline, color = HarcamTheme.colors.textPrimary)
            icon != null -> Icon(
                icon,
                contentDescription = null,
                tint = HarcamTheme.colors.textSecondary,
                modifier = Modifier.size(IconSize.lg),
            )
        }
    }
}
