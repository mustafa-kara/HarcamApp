package com.mustafakara.harcam.presentation.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakara.harcam.core.ui.components.BudgetProgressBar
import com.mustafakara.harcam.core.ui.components.CategoryAvatar
import com.mustafakara.harcam.core.ui.components.CategoryIcons
import com.mustafakara.harcam.core.ui.components.EmptyState
import com.mustafakara.harcam.core.ui.components.ScreenHeader
import com.mustafakara.harcam.core.ui.components.PrimaryButton
import com.mustafakara.harcam.core.ui.components.SkeletonBlock
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.ListRowMinHeight
import com.mustafakara.harcam.core.ui.theme.Radius
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.domain.model.BudgetStatus
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.core.util.MoneyFormatter

/**
 * Budget overview — the calm at-a-glance screen (budget_overview.md). Renders the monthly budget
 * bar plus a per-category list, each bar escalating NORMAL → WARNING (≥80%) → OVER (≥100%).
 *
 * WorkManager note: a `BudgetReminderWorker` (daily) posts a notification the first time the
 * monthly or a category budget crosses 80% / 100% and deep-links into this screen (Phase 3,
 * architecture.md §4/§8). It is not rendered here; this screen is only its navigation target.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val formatter = remember { MoneyFormatter() }

    Scaffold(modifier = modifier) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScreenHeader(title = "Budget")
            when {
                state.isLoading -> BudgetSkeleton()
                state.isEmpty -> EmptyState(
                    icon = Icons.Outlined.Savings,
                    title = "No budget set",
                    description = "Set a monthly budget to track your spending.",
                    actionText = "Set budget",
                    onAction = onEdit,
                )
                else -> BudgetContent(
                    state = state,
                    formatter = formatter,
                    onEdit = onEdit,
                )
            }
        }
    }
}

@Composable
private fun BudgetContent(
    state: BudgetUiState,
    formatter: MoneyFormatter,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.lg, end = Spacing.lg, top = Spacing.sm, bottom = Spacing.xxxl,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        val monthly = state.monthly
        if (monthly != null) {
            item {
                MonthlyBudgetCard(
                    monthly = monthly,
                    currency = state.currency,
                    formatter = formatter,
                )
            }
        }
        if (state.categoryRows.isNotEmpty()) {
            item {
                Text(
                    "By category",
                    style = HarcamTheme.type.title,
                    color = HarcamTheme.colors.textPrimary,
                    modifier = Modifier.padding(top = Spacing.sm),
                )
            }
            items(state.categoryRows, key = { it.category.id }) { row ->
                CategoryBudgetCard(
                    row = row,
                    currency = state.currency,
                    formatter = formatter,
                )
            }
        }
        item {
            PrimaryButton(
                text = "Edit budgets",
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md),
            )
        }
    }
}

@Composable
private fun MonthlyBudgetCard(
    monthly: BudgetStatus,
    currency: Currency,
    formatter: MoneyFormatter,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                "Monthly budget",
                style = HarcamTheme.type.label,
                color = HarcamTheme.colors.textSecondary,
            )
            Text(
                text = formatter.format(monthly.remaining, currency),
                style = HarcamTheme.type.amountLg,
                color = HarcamTheme.colors.textPrimary,
                modifier = Modifier
                    .padding(top = Spacing.xs)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
            BudgetProgressBar(
                status = monthly,
                currency = currency,
                formatter = formatter,
                modifier = Modifier.padding(top = Spacing.lg),
            )
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    row: CategoryBudgetRow,
    currency: Currency,
    formatter: MoneyFormatter,
) {
    val colors = HarcamTheme.colors
    val catColor = colors.category.byKey(row.category.colorKey)
    val icon = CategoryIcons.forKey(row.category.iconKey)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = ListRowMinHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryAvatar(
                    color = catColor,
                    icon = icon,
                    contentDescription = null,
                    size = 32.dp,
                )
                Text(
                    text = row.category.name,
                    style = HarcamTheme.type.body,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(start = Spacing.md),
                )
            }
            BudgetProgressBar(
                status = row.status,
                currency = currency,
                formatter = formatter,
                modifier = Modifier.padding(top = Spacing.md),
            )
        }
    }
}

@Composable
private fun BudgetSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SkeletonBlock(Modifier.fillMaxWidth().height(120.dp))
        repeat(4) {
            SkeletonBlock(Modifier.fillMaxWidth().height(72.dp))
        }
    }
}
