package com.mustafakara.harcam.presentation.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakara.harcam.core.ui.components.BarChart
import com.mustafakara.harcam.core.ui.components.ChartSlice
import com.mustafakara.harcam.core.ui.components.DonutChart
import com.mustafakara.harcam.core.ui.components.EmptyState
import com.mustafakara.harcam.core.ui.components.PeriodTabBar
import com.mustafakara.harcam.core.ui.components.SkeletonBlock
import com.mustafakara.harcam.core.ui.components.ScreenHeader
import com.mustafakara.harcam.core.ui.components.StatCard
import com.mustafakara.harcam.core.ui.components.TrendLineChart
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.Radius
import com.mustafakara.harcam.core.ui.theme.Spacing
import com.mustafakara.harcam.core.util.MoneyFormatter
import com.mustafakara.harcam.domain.model.PeriodStats

/**
 * Reports — the flagship analytics screen (design.md screens/reports). Period tabs drive a stat
 * grid, a donut (category proportion), a bar chart (category comparison), and a spending trend
 * line. Charts carry a content summary for accessibility (design.md §12 chart rule).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val formatter = remember { MoneyFormatter() }

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ScreenHeader(title = "Reports")
            PeriodTabBar(
                selected = state.period,
                onSelect = viewModel::selectPeriod,
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            )
            when {
                state.isLoading -> ReportsSkeleton()
                state.isEmpty -> EmptyState(
                    icon = Icons.Outlined.BarChart,
                    title = "No spending in this period",
                    description = "Log some expenses to see where your money goes.",
                )
                state.stats != null -> ReportsContent(state = state, stats = state.stats!!, formatter = formatter)
            }
        }
    }
}

@Composable
private fun ReportsContent(
    state: ReportsUiState,
    stats: PeriodStats,
    formatter: MoneyFormatter,
) {
    val colors = HarcamTheme.colors
    val catColorFor: (Long) -> androidx.compose.ui.graphics.Color = { id ->
        val key = state.categoriesById[id]?.colorKey ?: "other"
        colors.category.byKey(key).base
    }
    val nameFor: (Long) -> String = { id -> state.categoriesById[id]?.name ?: "Other" }

    val slices = remember(stats, state.categoriesById) {
        stats.byCategory.map { ChartSlice(nameFor(it.categoryId), it.total, catColorFor(it.categoryId)) }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // design.md §13: expanded (≥600) shows the donut and the comparison breakdown side by side.
        val expanded = maxWidth >= 600.dp

        val donutCard: @Composable () -> Unit = {
            ChartCard(title = "By category") {
                DonutChart(
                    slices = slices,
                    centerLabel = "Total",
                    centerValue = formatter.format(stats.total, state.currency),
                    contentSummary = donutSummary(slices, stats.total, formatter, state.currency),
                    valueFormatter = { formatter.format(it, state.currency) },
                )
            }
        }
        val comparisonCard: @Composable () -> Unit = {
            ChartCard(title = "Comparison") {
                BarChart(
                    bars = slices,
                    valueFormatter = { formatter.format(it, state.currency) },
                    contentSummary = "Category comparison bar chart.",
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (expanded) Spacing.xl else Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            // Stat grid (2 columns)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
                StatCard("Total", formatter.format(stats.total, state.currency), Modifier.weight(1f))
                StatCard("Avg / day", formatter.format(stats.averagePerDay, state.currency), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
                StatCard("Top category", state.topCategory?.name ?: "—", Modifier.weight(1f))
                StatCard("Transactions", stats.transactionCount.toString(), Modifier.weight(1f))
            }

            if (slices.isNotEmpty()) {
                if (expanded) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg), modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(1f)) { donutCard() }
                        Box(Modifier.weight(1f)) { comparisonCard() }
                    }
                } else {
                    donutCard()
                    comparisonCard()
                }
            }

            if (stats.trend.size >= 2) {
                ChartCard(title = "Trend") {
                    TrendLineChart(
                        points = stats.trend.map { it.total },
                        labels = stats.trend.map { it.label },
                        lineColor = MaterialTheme.colorScheme.primary,
                        contentSummary = "Spending trend over the period.",
                        granularityLabel = trendGranularity(state.period),
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.xl),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(title, style = HarcamTheme.type.title, color = HarcamTheme.colors.textPrimary)
            Column(modifier = Modifier.padding(top = Spacing.md)) { content() }
        }
    }
}

@Composable
private fun ReportsSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
            SkeletonBlock(Modifier.weight(1f).height(72.dp))
            SkeletonBlock(Modifier.weight(1f).height(72.dp))
        }
        SkeletonBlock(Modifier.fillMaxWidth().height(180.dp))
    }
}

/** x-axis time granularity for the trend chart, matching the active period — design.md §8.12. */
private fun trendGranularity(period: com.mustafakara.harcam.domain.model.ReportPeriod): String =
    when (period) {
        com.mustafakara.harcam.domain.model.ReportPeriod.DAY -> "by hour"
        com.mustafakara.harcam.domain.model.ReportPeriod.WEEK -> "by day"
        com.mustafakara.harcam.domain.model.ReportPeriod.MONTH -> "by day"
        com.mustafakara.harcam.domain.model.ReportPeriod.YEAR -> "by month"
    }

private fun donutSummary(
    slices: List<ChartSlice>,
    total: Double,
    formatter: MoneyFormatter,
    currency: com.mustafakara.harcam.domain.model.Currency,
): String {
    val parts = slices.joinToString(", ") { "${it.label} ${formatter.format(it.value, currency)}" }
    return "Spending by category. Total ${formatter.format(total, currency)}. $parts"
}
