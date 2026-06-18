package com.mustafakara.harcam.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mustafakara.harcam.core.ui.theme.CategoryColor
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.IconSize
import com.mustafakara.harcam.core.ui.theme.Radius
import com.mustafakara.harcam.core.ui.theme.Spacing

/**
 * Compact inline screen title for top-level destinations.
 *
 * Replaces the Material [androidx.compose.material3.TopAppBar], whose fixed 64dp height plus its own
 * insets left a large empty band above the title. This is just a title row laid out as the first
 * item of the screen — same horizontal rhythm as the content, far less vertical cost. An optional
 * [trailing] slot hosts a single action (e.g. an edit affordance).
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.lg, bottom = Spacing.sm),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text = title, style = HarcamTheme.type.headline, color = HarcamTheme.colors.textPrimary)
        if (trailing != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) { trailing() }
        }
    }
}

/** Primary CTA — design.md §8.16. One per screen. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(Radius.md),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(text = text, style = HarcamTheme.type.label)
        }
    }
}

/** Secondary outlined button — design.md §8.16. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(Radius.md),
    ) {
        Text(text = text, style = HarcamTheme.type.label)
    }
}

/** Destructive text action — design.md §8.16 (destructive variant). */
@Composable
fun DestructiveTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(contentColor = HarcamTheme.colors.danger),
    ) {
        Text(text = text, style = HarcamTheme.type.label)
    }
}

/** Empty / error state — design.md §8.18. Each screen supplies its own copy. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HarcamTheme.colors.textTertiary,
            modifier = Modifier.size(64.dp),
        )
        Text(
            text = title,
            style = HarcamTheme.type.title,
            color = HarcamTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.lg),
        )
        Text(
            text = description,
            style = HarcamTheme.type.body,
            color = HarcamTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.sm),
        )
        if (actionText != null && onAction != null) {
            PrimaryButton(
                text = actionText,
                onClick = onAction,
                modifier = Modifier.padding(top = Spacing.xl),
            )
        }
    }
}

/**
 * Circular category avatar — design.md §8.6.
 *
 * Intentionally monochrome: a list of rows with one saturated tint per category reads as visual
 * noise ("AI slop"). Category color is meaningful only where it encodes data — the report charts
 * and their legends — so here the avatar uses neutral surface + secondary-text tokens, and the
 * category icon alone carries identity. The [color] parameter is kept for call-site compatibility
 * (and so a future per-screen accent remains a one-line change) but is not rendered.
 */
@Composable
fun CategoryAvatar(
    color: CategoryColor,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = HarcamTheme.colors.textSecondary,
            modifier = Modifier.size(IconSize.lg),
        )
    }
}

/** A single shimmer-less skeleton block; screens compose these to mirror their success layout. */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = Radius.sm,
) {
    Box(
        modifier = modifier
            .clearAndSetSemantics { }
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(cornerRadius),
            ),
    )
}
