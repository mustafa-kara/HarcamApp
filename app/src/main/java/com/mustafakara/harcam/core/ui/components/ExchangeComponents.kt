package com.mustafakara.harcam.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingFlat
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.IconSize
import com.mustafakara.harcam.core.ui.theme.ListRowMinHeight
import com.mustafakara.harcam.core.ui.theme.Radius
import com.mustafakara.harcam.core.ui.theme.Spacing

/** Direction of a rate change — UP=income green ▲, DOWN=expense coral ▼, FLAT=neutral. */
enum class RateChangeDirection { UP, DOWN, FLAT }

/**
 * Exchange-rate row — design.md §8.14. Monogram + code/name on the left; rate (tnum) + signed
 * change on the right. Stale rows dim to 60% and say "Not updated" (text, never color alone).
 */
@Composable
fun ExchangeRateRow(
    code: String,
    name: String,
    formattedRate: String,
    changePct: String?,
    direction: RateChangeDirection,
    isStale: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = HarcamTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ListRowMinHeight)
            .alpha(if (isStale) 0.6f else 1f)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(HarcamTheme.colors.info.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(code.take(2), style = HarcamTheme.type.label, color = colors.info)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Spacing.md),
        ) {
            Text(code, style = HarcamTheme.type.bodyStrong, color = colors.textPrimary)
            Text(
                text = if (isStale) "$name · Not updated" else name,
                style = HarcamTheme.type.caption,
                color = if (isStale) colors.warning else colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formattedRate, style = HarcamTheme.type.amount, color = colors.textPrimary)
            if (changePct != null && direction != RateChangeDirection.FLAT) {
                val tint = if (direction == RateChangeDirection.UP) colors.income else colors.expense
                val arrow = if (direction == RateChangeDirection.UP) {
                    Icons.Outlined.TrendingUp
                } else {
                    Icons.Outlined.TrendingDown
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = arrow,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier
                            .size(IconSize.sm)
                            .padding(end = Spacing.xs),
                    )
                    Text(changePct, style = HarcamTheme.type.amountSm, color = tint)
                }
            } else if (changePct == null) {
                Icon(
                    imageVector = Icons.Outlined.TrendingFlat,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(IconSize.sm),
                )
            }
        }
    }
}

/** Inline status banner (cached fallback / error) — design.md §8 (warning banner + Retry). */
@Composable
fun StatusBanner(
    icon: ImageVector,
    message: String,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(Radius.md))
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(IconSize.md))
        Text(
            text = message,
            style = HarcamTheme.type.caption,
            color = tint,
            modifier = Modifier.weight(1f),
        )
        if (actionText != null && onAction != null) {
            TextButton(
                onClick = onAction,
                colors = ButtonDefaults.textButtonColors(contentColor = tint),
            ) {
                Text(text = actionText, style = HarcamTheme.type.label)
            }
        }
    }
}

/** Convenience: a "warning" banner used for the cached-rates fallback. */
@Composable
fun CachedRatesBanner(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    StatusBanner(
        icon = Icons.Filled.Warning,
        message = message,
        tint = HarcamTheme.colors.warning,
        background = HarcamTheme.colors.warningBg,
        actionText = "Retry",
        onAction = onRetry,
        modifier = modifier,
    )
}
