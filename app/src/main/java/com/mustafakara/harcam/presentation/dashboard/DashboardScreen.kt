package com.mustafakara.harcam.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mustafakara.harcam.core.ui.pressScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakara.harcam.core.ui.components.BudgetProgressBar
import com.mustafakara.harcam.core.ui.components.EmptyState
import com.mustafakara.harcam.core.ui.components.ExpenseRow
import com.mustafakara.harcam.core.ui.components.ScreenHeader
import com.mustafakara.harcam.core.ui.components.SkeletonBlock
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.Radius
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.core.util.DateUtil
import com.mustafakara.harcam.core.util.MoneyFormatter
import com.mustafakara.harcam.domain.model.Currency
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddExpense: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenExchange: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val formatter = remember { MoneyFormatter() }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add expense")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScreenHeader(title = "Dashboard")
            when {
                state.isLoading -> DashboardSkeleton()
                state.isEmpty -> EmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No spending logged yet",
                    description = "Add your first expense to see your month at a glance.",
                    actionText = "Add expense",
                    onAction = onAddExpense,
                )
                else -> DashboardContent(
                    state = state,
                    formatter = formatter,
                    onOpenBudget = onOpenBudget,
                    onOpenCategories = onOpenCategories,
                    onOpenExchange = onOpenExchange,
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    formatter: MoneyFormatter,
    onOpenBudget: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenExchange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.lg, end = Spacing.lg, top = Spacing.sm, bottom = Spacing.xxxl,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item {
            BalanceHeroCard(
                spent = state.monthSpent,
                income = state.monthIncome,
                currency = state.currency,
                maskAmounts = state.maskAmounts,
                budget = state.monthlyBudget,
                formatter = formatter,
                onBudgetClick = onOpenBudget,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                QuickLink("Categories", Icons.Outlined.Category, onOpenCategories, Modifier.weight(1f))
                QuickLink("Exchange", Icons.Outlined.CurrencyExchange, onOpenExchange, Modifier.weight(1f))
            }
        }
        item {
            Text(
                "Recent",
                style = HarcamTheme.type.title,
                color = HarcamTheme.colors.textPrimary,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
        items(state.recent, key = { it.id }) { expense ->
            ExpenseRow(
                expense = expense,
                category = state.categoriesById[expense.categoryId],
                timeLabel = timeFmt.format(expense.createdAt),
                formatter = formatter,
                onClick = {},
            )
        }
    }
}

@Composable
private fun BalanceHeroCard(
    spent: Double,
    income: Double,
    currency: Currency,
    maskAmounts: Boolean,
    budget: com.mustafakara.harcam.domain.model.BudgetStatus?,
    formatter: MoneyFormatter,
    onBudgetClick: () -> Unit,
) {
    val monthLabel = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(DateUtil.startOfMonth(System.currentTimeMillis()))
    }
    val colors = HarcamTheme.colors
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text("Spent · $monthLabel", style = HarcamTheme.type.label, color = colors.textSecondary)
            Text(
                text = if (maskAmounts) "••••" else formatter.format(spent, currency),
                style = HarcamTheme.type.displayLg,
                color = colors.textPrimary,
                modifier = Modifier
                    .padding(top = Spacing.xs)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        if (!maskAmounts) {
                            contentDescription = formatter.semanticLabel(spent, currency, isExpense = true)
                        }
                    },
            )
            // Compact in/out row — design.md §8.3. Income shows only when tracked (> 0).
            if (!maskAmounts) {
                Row(
                    modifier = Modifier.padding(top = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    Text(
                        text = formatter.formatSigned(spent, currency, isExpense = true),
                        style = HarcamTheme.type.amountSm,
                        color = colors.textSecondary,
                    )
                    if (income > 0.0) {
                        Text(
                            text = formatter.formatSigned(income, currency, isExpense = false),
                            style = HarcamTheme.type.amountSm,
                            color = colors.income,
                        )
                    }
                }
            }
            if (budget != null) {
                BudgetProgressBar(
                    status = budget,
                    currency = currency,
                    formatter = formatter,
                    label = "Monthly budget",
                    modifier = Modifier
                        .padding(top = Spacing.lg)
                        .clickable(onClick = onBudgetClick),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickLink(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    androidx.compose.material3.OutlinedCard(
        modifier = modifier.pressScale(interactionSource),
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(Radius.md),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(label, style = HarcamTheme.type.label, color = HarcamTheme.colors.textPrimary)
        }
    }
}

@Composable
private fun DashboardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SkeletonBlock(Modifier.fillMaxWidth().height(120.dp))
        repeat(4) {
            SkeletonBlock(Modifier.fillMaxWidth().height(56.dp))
        }
    }
}
