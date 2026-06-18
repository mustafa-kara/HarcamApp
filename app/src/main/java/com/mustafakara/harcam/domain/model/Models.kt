package com.mustafakara.harcam.domain.model

/**
 * Domain models — architecture.md §1 (domain/model). Pure Kotlin, framework-free; the data
 * layer maps Room entities / Retrofit DTOs to these, and the presentation layer consumes them.
 */

/** Supported currencies — design.md §4.3 (user-selectable). */
enum class Currency(val code: String, val symbol: String) {
    TRY("TRY", "₺"),
    USD("USD", "$"),
    EUR("EUR", "€");

    companion object {
        fun fromCode(code: String): Currency = entries.firstOrNull { it.code == code } ?: TRY
    }
}

/**
 * A spending category. [colorKey] selects a slot in the design.md §2.1 category palette;
 * [iconKey] selects a Material icon. Both are stable keys, not raw colors/icons (design.md §7).
 */
data class Category(
    val id: Long,
    val name: String,
    val colorKey: String,
    val iconKey: String,
    val isDefault: Boolean = false,
)

/** A logged expense (money out). Amount is always positive; direction is implied by type. */
data class Expense(
    val id: Long,
    val amount: Double,
    val currency: Currency,
    val categoryId: Long,
    val note: String,
    val createdAt: Long,
)

/** A budget limit — either the overall monthly budget ([categoryId] == null) or per-category. */
data class Budget(
    val id: Long,
    val categoryId: Long?,
    val limit: Double,
    /** Month key "yyyy-MM" the limit applies to, or null for an ongoing/default limit. */
    val monthKey: String?,
)

/** Budget consumption level — drives BudgetProgressBar states (design.md §8.4). */
enum class BudgetLevel { NORMAL, WARNING, OVER }

/** Derived budget status for a scope (monthly or a category) — architecture.md §4. */
data class BudgetStatus(
    val categoryId: Long?,
    val spent: Double,
    val limit: Double,
) {
    val ratio: Double get() = if (limit <= 0.0) 0.0 else spent / limit
    val remaining: Double get() = limit - spent
    val level: BudgetLevel
        get() = when {
            ratio >= 1.0 -> BudgetLevel.OVER
            ratio >= 0.8 -> BudgetLevel.WARNING
            else -> BudgetLevel.NORMAL
        }
}

/** How often a recurring expense repeats. */
enum class RecurrenceCadence { WEEKLY, MONTHLY, YEARLY }

/** A recurring/subscription expense template — materialized into real expenses by RecurringWorker. */
data class RecurringExpense(
    val id: Long,
    val name: String,
    val amount: Double,
    val currency: Currency,
    val categoryId: Long,
    val cadence: RecurrenceCadence,
    val nextDueDate: Long,
    val reminderDaysBefore: Int,
    val isPaused: Boolean = false,
)

/** A single currency's rate relative to a base — exchange screen (architecture.md §4). */
data class ExchangeRate(
    val currency: Currency,
    val rate: Double,
)

/** Cached exchange-rate snapshot with freshness metadata for the cache-fallback flow. */
data class ExchangeRates(
    val base: Currency,
    val rates: List<ExchangeRate>,
    val lastUpdatedEpochMs: Long,
)

/** Report period tabs — design.md §8.8 PeriodTabBar. */
enum class ReportPeriod { DAY, WEEK, MONTH, YEAR }

/** Per-category aggregate within a report period. */
data class CategorySpend(
    val categoryId: Long,
    val total: Double,
    val transactionCount: Int,
)

/** Aggregated report for a period — produced by GetPeriodReportUseCase (architecture.md §2). */
data class PeriodStats(
    val period: ReportPeriod,
    val total: Double,
    val averagePerDay: Double,
    val transactionCount: Int,
    val topCategoryId: Long?,
    val byCategory: List<CategorySpend>,
    /** Time-bucketed totals for the trend line (label + amount), oldest→newest. */
    val trend: List<TrendPoint>,
)

data class TrendPoint(
    val label: String,
    val total: Double,
)
