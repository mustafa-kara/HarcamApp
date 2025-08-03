package com.mustafakara.harcam.presentation.state

import com.mustafakara.harcam.data.dao.DailySummary
import com.mustafakara.harcam.data.dao.MonthlySummary
import com.mustafakara.harcam.data.dao.WeeklySummary
import com.mustafakara.harcam.data.dao.YearlySummary
import com.mustafakara.harcam.data.entity.ExpenseEntity

/**
 * Seçili periyoda ait genel istatistikler
 */
data class PeriodStats(
    val totalAmount: Double = 0.0,
    val totalExpenses: Int = 0,
    val averagePerPeriod: Double = 0.0,
    val periodCount: Int = 0,
    val periodName: String = ""
)

data class ReportsUiState(
    val selectedPeriod: ReportPeriod = ReportPeriod.DAILY,
    val dailySummaries: List<DailySummary> = emptyList(),
    val weeklySummaries: List<WeeklySummary> = emptyList(),
    val monthlySummaries: List<MonthlySummary> = emptyList(),
    val yearlySummaries: List<YearlySummary> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    

    val periodStats: PeriodStats = PeriodStats(),
    val selectedPeriodExpenses: List<ExpenseEntity> = emptyList(),
    val editingExpense: ExpenseEntity? = null

)

enum class ReportPeriod(val displayName: String) {
    DAILY("Günlük"),
    WEEKLY("Haftalık"),
    MONTHLY("Aylık"),
    YEARLY("Yıllık")
}

sealed class ReportSummary {
    abstract val period: String
    abstract val totalAmount: Double
    abstract val expenseCount: Int
    
    data class Daily(
        override val period: String,
        override val totalAmount: Double,
        override val expenseCount: Int
    ) : ReportSummary()
    
    data class Weekly(
        override val period: String,
        override val totalAmount: Double,
        override val expenseCount: Int
    ) : ReportSummary()
    
    data class Monthly(
        override val period: String,
        override val totalAmount: Double,
        override val expenseCount: Int
    ) : ReportSummary()
    
    data class Yearly(
        override val period: String,
        override val totalAmount: Double,
        override val expenseCount: Int
    ) : ReportSummary()
}

/**
DAO modellerini dönüştürür
 */
fun DailySummary.toReportSummary(): ReportSummary.Daily {
    return ReportSummary.Daily(
        period = this.date,
        totalAmount = this.totalAmount,
        expenseCount = this.expenseCount
    )
}

fun WeeklySummary.toReportSummary(): ReportSummary.Weekly {
    return ReportSummary.Weekly(
        period = this.week,
        totalAmount = this.totalAmount,
        expenseCount = this.expenseCount
    )
}

fun MonthlySummary.toReportSummary(): ReportSummary.Monthly {
    return ReportSummary.Monthly(
        period = this.month,
        totalAmount = this.totalAmount,
        expenseCount = this.expenseCount
    )
}

fun YearlySummary.toReportSummary(): ReportSummary.Yearly {
    return ReportSummary.Yearly(
        period = this.year,
        totalAmount = this.totalAmount,
        expenseCount = this.expenseCount
    )
}