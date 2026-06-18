package com.mustafakara.harcam.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entities — architecture.md §1 (data/local/entity). The original `expenses` table is
 * extended with category/currency/recurring fields (migration 1→2 in HarcamDatabase).
 */

@Entity(
    tableName = "expenses",
    indices = [Index("categoryId"), Index(value = ["recurringId", "occurrenceDate"])],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amount: Double,
    val createdAt: Long = System.currentTimeMillis(),
    // Added in v2:
    val categoryId: Long = 0,
    val currency: String = "TRY",
    /** Non-null when this expense was materialized from a recurring template (idempotency). */
    val recurringId: Long? = null,
    /** The occurrence date (day granularity, epoch ms) for the recurring materialization guard. */
    val occurrenceDate: Long? = null,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorKey: String,
    val iconKey: String,
    val isDefault: Boolean = false,
)

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["categoryId", "monthKey"], unique = true)],
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Null = overall monthly budget; non-null = per-category. */
    val categoryId: Long? = null,
    val amountLimit: Double,
    /** "yyyy-MM" or null for an ongoing default limit. */
    val monthKey: String? = null,
)

@Entity(tableName = "recurring")
data class RecurringEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val currency: String = "TRY",
    val categoryId: Long,
    val cadence: String,
    val nextDueDate: Long,
    val reminderDaysBefore: Int = 1,
    val isPaused: Boolean = false,
)

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    /** Composite-ish key: "<base>_<quote>" so cache is per base+quote pair. */
    @PrimaryKey val pair: String,
    val base: String,
    val quote: String,
    val rate: Double,
    val lastUpdatedEpochMs: Long,
)
