package com.mustafakara.harcam.domain.usecase

import com.mustafakara.harcam.core.common.Clock
import com.mustafakara.harcam.core.util.DateUtil
import com.mustafakara.harcam.domain.model.CategorySpend
import com.mustafakara.harcam.domain.model.Expense
import com.mustafakara.harcam.domain.model.PeriodStats
import com.mustafakara.harcam.domain.model.ReportPeriod
import com.mustafakara.harcam.domain.model.TrendPoint
import com.mustafakara.harcam.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * Aggregates a [ReportPeriod] window into [PeriodStats]: total, average/day, transaction count,
 * top category, per-category breakdown, and a time-bucketed trend — architecture.md §2.
 * Computed from the reactive date-range expense flow so it updates when expenses change.
 */
class GetPeriodReportUseCase @Inject constructor(
    private val repository: ExpenseRepository,
    private val clock: Clock,
) {
    operator fun invoke(period: ReportPeriod): Flow<PeriodStats> {
        val now = clock.nowMs()
        val range = DateUtil.rangeFor(period, now)
        return repository.observeByDateRange(range.startMs, range.endMs).map { expenses ->
            aggregate(period, range, expenses)
        }
    }

    private fun aggregate(
        period: ReportPeriod,
        range: DateUtil.Range,
        expenses: List<Expense>,
    ): PeriodStats {
        val total = expenses.sumOf { it.amount }
        val days = DateUtil.daysIn(range)
        val byCategory = expenses.groupBy { it.categoryId }
            .map { (catId, list) ->
                CategorySpend(catId, list.sumOf { it.amount }, list.size)
            }
            .sortedByDescending { it.total }

        return PeriodStats(
            period = period,
            total = total,
            averagePerDay = if (days > 0) total / days else 0.0,
            transactionCount = expenses.size,
            topCategoryId = byCategory.firstOrNull()?.categoryId,
            byCategory = byCategory,
            trend = buildTrend(period, expenses),
        )
    }

    /** Buckets expenses into trend points appropriate to the period (oldest→newest). */
    private fun buildTrend(period: ReportPeriod, expenses: List<Expense>): List<TrendPoint> {
        val fmt = when (period) {
            ReportPeriod.DAY -> SimpleDateFormat("HH", Locale.getDefault())
            ReportPeriod.WEEK -> SimpleDateFormat("EEE", Locale.getDefault())
            ReportPeriod.MONTH -> SimpleDateFormat("d", Locale.getDefault())
            ReportPeriod.YEAR -> SimpleDateFormat("MMM", Locale.getDefault())
        }
        val cal = Calendar.getInstance()
        return expenses
            .groupBy { e -> cal.apply { timeInMillis = e.createdAt }.let { fmt.format(it.time) } }
            .toSortedMap()
            .map { (label, list) -> TrendPoint(label, list.sumOf { it.amount }) }
    }
}
