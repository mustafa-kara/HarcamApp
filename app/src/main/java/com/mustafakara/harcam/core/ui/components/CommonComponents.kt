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

/** Circular category avatar — design.md §8.6 (color + icon, never color alone). */
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
            .background(color.container, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color.base,
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
