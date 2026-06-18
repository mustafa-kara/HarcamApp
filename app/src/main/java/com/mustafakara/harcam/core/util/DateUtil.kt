package com.mustafakara.harcam.core.util

import com.mustafakara.harcam.domain.model.ReportPeriod
import java.util.Calendar

/**
 * Date-range and bucketing helpers for reports / budgets / dashboard. Kept framework-light
 * (java.util.Calendar) and pure so use cases stay testable with an injected "now".
 */
object DateUtil {

    data class Range(val startMs: Long, val endMs: Long)

    /** Start of the day containing [ms] (local time). */
    fun startOfDay(ms: Long): Long = cal(ms).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun endOfDay(ms: Long): Long = cal(ms).apply {
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    /** First millisecond of the month containing [ms]. */
    fun startOfMonth(ms: Long): Long = cal(ms).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun endOfMonth(ms: Long): Long = cal(ms).apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    /** "yyyy-MM" key for the month containing [ms] — used by Budget.monthKey. */
    fun monthKey(ms: Long): String = cal(ms).let {
        "%04d-%02d".format(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1)
    }

    /** The [ReportPeriod] window ending at [now]. */
    fun rangeFor(period: ReportPeriod, now: Long): Range = when (period) {
        ReportPeriod.DAY -> Range(startOfDay(now), endOfDay(now))
        ReportPeriod.WEEK -> {
            val start = cal(now).apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            Range(start, endOfDay(now))
        }
        ReportPeriod.MONTH -> Range(startOfMonth(now), endOfMonth(now))
        ReportPeriod.YEAR -> {
            val start = cal(now).apply {
                set(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            Range(start, now)
        }
    }

    /** Number of inclusive days in [range] (≥1) — for average-per-day. */
    fun daysIn(range: Range): Int {
        val dayMs = 24L * 60 * 60 * 1000
        return ((range.endMs - range.startMs) / dayMs).toInt().coerceAtLeast(0) + 1
    }

    /** Advance [ms] by one [cadenceField] step (Calendar.WEEK_OF_YEAR / MONTH / YEAR). */
    fun advance(ms: Long, calendarField: Int, amount: Int = 1): Long =
        cal(ms).apply { add(calendarField, amount) }.timeInMillis

    private fun cal(ms: Long) = Calendar.getInstance().apply { timeInMillis = ms }
}
