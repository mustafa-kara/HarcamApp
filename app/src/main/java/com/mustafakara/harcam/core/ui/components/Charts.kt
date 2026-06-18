package com.mustafakara.harcam.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakara.harcam.core.ui.entranceProgress
import com.mustafakara.harcam.core.ui.theme.HarcamTheme
import com.mustafakara.harcam.core.ui.theme.Spacing

/**
 * Compose-native charts (Canvas) — design.md §8.10–8.12. Donut ≤6 slices + "Other"; legend always
 * shows %% + amount so meaning never relies on color alone; every chart takes a
 * [contentSummary] string for the screen-reader / data-table-alternative requirement.
 */

/** One chart datum: a label, value, and the color it renders in. */
data class ChartSlice(
    val label: String,
    val value: Double,
    val color: Color,
)

/** Donut chart with center total + side legend. Folds slices beyond [maxSlices] into "Other". */
@Composable
fun DonutChart(
    slices: List<ChartSlice>,
    centerLabel: String,
    centerValue: String,
    contentSummary: String,
    valueFormatter: (Double) -> String,
    modifier: Modifier = Modifier,
    maxSlices: Int = 6,
) {
    val folded = foldSlices(slices, maxSlices)
    val total = folded.sumOf { it.value }.toFloat().coerceAtLeast(0.0001f)
    val sweepProgress = entranceProgress(label = "donutSweep")
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = contentSummary },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .padding(Spacing.sm),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                val stroke = Stroke(width = size.minDimension * 0.16f, cap = StrokeCap.Butt)
                val inset = stroke.width / 2
                val arcSize = Size(size.minDimension - stroke.width, size.minDimension - stroke.width)
                val topLeft = Offset(inset, inset)
                // Track
                drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = stroke)
                var startAngle = -90f
                folded.forEach { slice ->
                    val sweep = (slice.value.toFloat() / total) * 360f * sweepProgress
                    drawArc(slice.color, startAngle, sweep, false, topLeft, arcSize, style = stroke)
                    startAngle += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(centerLabel, style = HarcamTheme.type.caption, color = HarcamTheme.colors.textSecondary)
                Text(centerValue, style = HarcamTheme.type.amount, color = HarcamTheme.colors.textPrimary)
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            folded.forEach { slice ->
                val pct = (slice.value / total * 100).toInt()
                LegendRow(
                    slice = slice,
                    percent = "%$pct",
                    amount = valueFormatter(slice.value),
                )
            }
        }
    }
}

@Composable
private fun LegendRow(slice: ChartSlice, percent: String, amount: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(slice.color, CircleShape),
        )
        Text(
            text = slice.label,
            style = HarcamTheme.type.caption,
            color = HarcamTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.sm),
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(amount, style = HarcamTheme.type.amountSm, color = HarcamTheme.colors.textPrimary)
            Text(percent, style = HarcamTheme.type.caption, color = HarcamTheme.colors.textSecondary)
        }
    }
}

/** Horizontal bars with value labels, sorted by the caller. Each bar has a ≥48dp-friendly row. */
@Composable
fun BarChart(
    bars: List<ChartSlice>,
    valueFormatter: (Double) -> String,
    contentSummary: String,
    modifier: Modifier = Modifier,
) {
    val max = bars.maxOfOrNull { it.value }?.toFloat()?.coerceAtLeast(0.0001f) ?: 1f
    val progress = entranceProgress(label = "barGrow")
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = contentSummary },
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        bars.forEach { bar ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        bar.label,
                        style = HarcamTheme.type.caption,
                        color = HarcamTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        valueFormatter(bar.value),
                        style = HarcamTheme.type.amountSm,
                        color = HarcamTheme.colors.textSecondary,
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = Spacing.xs)
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(trackColor, RoundedCornerShape(percent = 50)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((bar.value.toFloat() / max) * progress)
                            .height(8.dp)
                            .background(bar.color, RoundedCornerShape(percent = 50)),
                    )
                }
            }
        }
    }
}

/** Single-series trend line with soft area fill. [points] oldest→newest. */
@Composable
fun TrendLineChart(
    points: List<Double>,
    labels: List<String>,
    lineColor: Color,
    contentSummary: String,
    modifier: Modifier = Modifier,
    granularityLabel: String? = null,
) {
    val progress = entranceProgress(label = "lineDraw")
    val gridColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = contentSummary },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(vertical = Spacing.sm),
        ) {
            if (points.size < 2) return@Canvas
            val maxV = (points.maxOrNull() ?: 0.0).toFloat().coerceAtLeast(0.0001f)
            val stepX = size.width / (points.size - 1)
            val pts = points.mapIndexed { i, v ->
                Offset(i * stepX, size.height - (v.toFloat() / maxV) * size.height)
            }
            // baseline
            drawLine(gridColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1f)

            val visibleCount = (pts.size * progress).toInt().coerceIn(2, pts.size)
            val visible = pts.take(visibleCount)

            val linePath = Path().apply {
                moveTo(visible.first().x, visible.first().y)
                visible.drop(1).forEach { lineTo(it.x, it.y) }
            }
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(visible.last().x, size.height)
                lineTo(visible.first().x, size.height)
                close()
            }
            drawPath(fillPath, lineColor.copy(alpha = 0.12f))
            drawPath(linePath, lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
        }
        if (labels.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(labels.first(), style = HarcamTheme.type.caption, color = HarcamTheme.colors.textTertiary)
                if (granularityLabel != null) {
                    Text(granularityLabel, style = HarcamTheme.type.caption, color = HarcamTheme.colors.textSecondary)
                }
                if (labels.size > 1) {
                    Text(labels.last(), style = HarcamTheme.type.caption, color = HarcamTheme.colors.textTertiary)
                }
            }
        }
    }
}

/** Keeps the largest (maxSlices-1) slices and folds the rest into a single "Other" slice. */
private fun foldSlices(slices: List<ChartSlice>, maxSlices: Int): List<ChartSlice> {
    if (slices.size <= maxSlices) return slices
    val sorted = slices.sortedByDescending { it.value }
    val head = sorted.take(maxSlices - 1)
    val tail = sorted.drop(maxSlices - 1)
    val otherColor = tail.lastOrNull()?.color ?: head.last().color
    return head + ChartSlice("Other", tail.sumOf { it.value }, otherColor)
}
