package com.mustafakara.harcam.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.IconSize
import com.mustafakara.harcam.core.ui.theme.ListRowMinHeight
import com.mustafakara.harcam.core.ui.theme.Motion
import com.mustafakara.harcam.core.ui.theme.Radius
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.core.util.MoneyFormatter
import com.mustafakara.harcam.domain.model.BudgetLevel
import com.mustafakara.harcam.domain.model.BudgetStatus
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.Expense

/** Expense list row — design.md §8.5 (avatar + note/category/time + signed amount). */
@Composable
fun ExpenseRow(
    expense: Expense,
    category: Category?,
    timeLabel: String,
    formatter: MoneyFormatter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HarcamTheme.colors
    val catColor = category?.let { colors.category.byKey(it.colorKey) } ?: colors.category.other
    val icon = CategoryIcons.forKey(category?.iconKey ?: "category")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ListRowMinHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryAvatar(
            color = catColor,
            icon = icon,
            contentDescription = category?.name?.let { "$it category" },
            size = 40.dp,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Spacing.md),
        ) {
            Text(
                text = expense.note.ifBlank { category?.name ?: "Expense" },
                style = HarcamTheme.type.body,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(category?.name, timeLabel).joinToString(" · "),
                style = HarcamTheme.type.caption,
                color = colors.textSecondary,
            )
        }
        // Neutral amount: in an expense list every row is an expense, so coloring them all red
        // carries no information — it's just noise. The signed "−" prefix marks direction; color is
        // reserved for genuine risk signals (budget warning/over).
        Text(
            text = formatter.formatSigned(expense.amount, expense.currency, isExpense = true),
            style = HarcamTheme.type.amount,
            color = colors.textPrimary,
            modifier = Modifier.semantics {
                contentDescription = formatter.semanticLabel(expense.amount, expense.currency, isExpense = true)
            },
        )
    }
}

/** Budget progress bar — design.md §8.4. NORMAL/WARNING(≥80%)/OVER(≥100%) with icon + text. */
@Composable
fun BudgetProgressBar(
    status: BudgetStatus,
    currency: Currency,
    formatter: MoneyFormatter,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val colors = HarcamTheme.colors
    val fillColor = when (status.level) {
        BudgetLevel.NORMAL -> MaterialTheme.colorScheme.primary
        BudgetLevel.WARNING -> colors.warning
        BudgetLevel.OVER -> colors.danger
    }
    val animatedColor by animateColorAsState(fillColor, tween(Motion.BASE), label = "budgetColor")
    val targetFraction = status.ratio.coerceIn(0.0, 1.0).toFloat()
    val animatedFraction by animateFloatAsState(targetFraction, tween(Motion.BASE), label = "budgetFill")

    val statusIcon = when (status.level) {
        BudgetLevel.NORMAL -> Icons.Filled.CheckCircle
        BudgetLevel.WARNING, BudgetLevel.OVER -> Icons.Filled.Warning
    }
    val statusText = when (status.level) {
        BudgetLevel.NORMAL -> formatter.formatBudget(status.spent, status.limit, currency)
        BudgetLevel.WARNING -> "${formatter.formatPercent(status.ratio)} used · ${formatter.formatBudget(status.spent, status.limit, currency)}"
        BudgetLevel.OVER -> "Over by ${formatter.format(-status.remaining, currency)}"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (label != null) {
                Text(label, style = HarcamTheme.type.label, color = colors.textSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = fillColor,
                    modifier = Modifier
                        .size(IconSize.sm)
                        .padding(end = Spacing.xs),
                )
                Text(
                    text = statusText,
                    style = HarcamTheme.type.caption,
                    color = if (status.level == BudgetLevel.NORMAL) colors.textSecondary else fillColor,
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(top = Spacing.sm)
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(Radius.pill)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(8.dp)
                    .background(animatedColor, RoundedCornerShape(Radius.pill)),
            )
        }
    }
}

/** Compact metric card — design.md §8.9. */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Radius.lg),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(label, style = HarcamTheme.type.caption, color = HarcamTheme.colors.textSecondary)
            Text(
                text = value,
                style = HarcamTheme.type.amount,
                color = valueColor ?: HarcamTheme.colors.textPrimary,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
    }
}

/** Recurring/subscription row — design.md §8.13 (avatar + name + cadence/next-due + amount). */
@Composable
fun RecurringRow(
    name: String,
    category: Category?,
    cadenceLabel: String,
    amount: String,
    dueSoon: Boolean,
    isPaused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HarcamTheme.colors
    val catColor = category?.let { colors.category.byKey(it.colorKey) } ?: colors.category.other
    val icon = CategoryIcons.forKey(category?.iconKey ?: "category")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ListRowMinHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryAvatar(color = catColor, icon = icon, contentDescription = category?.name, size = 40.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Spacing.md),
        ) {
            Text(name, style = HarcamTheme.type.body, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(cadenceLabel, style = HarcamTheme.type.caption, color = colors.textSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(amount, style = HarcamTheme.type.amount, color = colors.textPrimary)
            when {
                isPaused -> StatusPill("Paused", colors.textTertiary, MaterialTheme.colorScheme.surfaceVariant)
                dueSoon -> StatusPill("Due soon", colors.warning, colors.warningBg)
            }
        }
    }
}

/** Small status pill — design.md §8.13/§5 (pill, status color + soft bg, text not color alone). */
@Composable
private fun StatusPill(text: String, contentColor: Color, background: Color) {
    Box(
        modifier = Modifier
            .padding(top = Spacing.xs)
            .background(background, RoundedCornerShape(Radius.pill))
            .padding(horizontal = Spacing.sm, vertical = 2.dp),
    ) {
        Text(text, style = HarcamTheme.type.caption, color = contentColor)
    }
}
