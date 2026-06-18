package com.mustafakara.harcam.presentation.reports

import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.PeriodStats
import com.mustafakara.harcam.domain.model.ReportPeriod

/** Immutable reports state — design.md §7; four-state via [isLoading]/[isEmpty]. */
data class ReportsUiState(
    val isLoading: Boolean = true,
    val period: ReportPeriod = ReportPeriod.MONTH,
    val currency: Currency = Currency.TRY,
    val stats: PeriodStats? = null,
    val categoriesById: Map<Long, Category> = emptyMap(),
) {
    val isEmpty: Boolean get() = !isLoading && (stats?.transactionCount ?: 0) == 0
    val topCategory: Category? get() = stats?.topCategoryId?.let { categoriesById[it] }
}
