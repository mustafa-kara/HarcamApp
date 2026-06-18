package com.mustafakara.harcam.presentation.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakara.harcam.core.ui.components.BudgetProgressBar
import com.mustafakara.harcam.core.ui.components.EmptyState
import com.mustafakara.harcam.core.ui.components.ExpenseRow
import com.mustafakara.harcam.core.ui.components.PeriodTabBar
import com.mustafakara.harcam.core.ui.components.SkeletonBlock
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.core.util.MoneyFormatter
import com.mustafakara.harcam.domain.model.ReportPeriod
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val formatter = remember { MoneyFormatter() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.category?.name ?: "Category",
                        style = HarcamTheme.type.headline,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            PeriodTabBar(
                selected = state.period,
                onSelect = viewModel::selectPeriod,
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            )
            when {
                state.isLoading -> CategoryDetailSkeleton()
                state.isEmpty -> Column(Modifier.fillMaxSize()) {
                    CategoryDetailHeader(state = state, formatter = formatter)
                    EmptyState(
                        icon = Icons.Outlined.Category,
                        title = "No spending in this period",
                        description = "Add an expense to this category to see it here.",
                    )
                }
                else -> CategoryDetailContent(
                    state = state,
                    formatter = formatter,
                )
            }
        }
    }
}

@Composable
private fun CategoryDetailContent(
    state: CategoryDetailUiState,
    formatter: MoneyFormatter,
    modifier: Modifier = Modifier,
) {
    val timeFmt = remember { SimpleDateFormat("HH:mm · d MMM", Locale.getDefault()) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.xxxl),
    ) {
        item {
            CategoryDetailHeader(state = state, formatter = formatter)
        }
        item {
            Text(
                "Expenses",
                style = HarcamTheme.type.title,
                color = HarcamTheme.colors.textPrimary,
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            )
        }
        items(state.expenses, key = { it.id }) { expense ->
            ExpenseRow(
                expense = expense,
                category = state.category,
                timeLabel = timeFmt.format(expense.createdAt),
                formatter = formatter,
                onClick = {},
            )
        }
    }
}

@Composable
private fun CategoryDetailHeader(
    state: CategoryDetailUiState,
    formatter: MoneyFormatter,
    modifier: Modifier = Modifier,
) {
    val colors = HarcamTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            "Spent this ${state.period.periodLabel()}",
            style = HarcamTheme.type.label,
            color = colors.textSecondary,
        )
        Text(
            text = formatter.formatSigned(state.periodTotal, state.currency, isExpense = true),
            style = HarcamTheme.type.amountLg,
            color = colors.expense,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        val budget = state.budget
        if (budget != null) {
            BudgetProgressBar(
                status = budget,
                currency = state.currency,
                formatter = formatter,
                label = "Budget",
                modifier = Modifier.padding(top = Spacing.md),
            )
        }
    }
}

@Composable
private fun CategoryDetailSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SkeletonBlock(Modifier.fillMaxWidth().height(48.dp))
        SkeletonBlock(Modifier.fillMaxWidth().height(24.dp))
        repeat(4) {
            SkeletonBlock(Modifier.fillMaxWidth().height(56.dp))
        }
    }
}

private fun ReportPeriod.periodLabel(): String = when (this) {
    ReportPeriod.DAY -> "day"
    ReportPeriod.WEEK -> "week"
    ReportPeriod.MONTH -> "month"
    ReportPeriod.YEAR -> "year"
}
