package com.mustafakara.harcam.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mustafakara.harcam.data.local.dao.BudgetDao
import com.mustafakara.harcam.data.local.dao.CategoryDao
import com.mustafakara.harcam.data.local.dao.ExchangeRateDao
import com.mustafakara.harcam.data.local.dao.ExpenseDao
import com.mustafakara.harcam.data.local.dao.RecurringDao
import com.mustafakara.harcam.data.local.entity.BudgetEntity
import com.mustafakara.harcam.data.local.entity.CategoryEntity
import com.mustafakara.harcam.data.local.entity.ExchangeRateEntity
import com.mustafakara.harcam.data.local.entity.ExpenseEntity
import com.mustafakara.harcam.data.local.entity.RecurringEntity

/**
 * App database — architecture.md §1/§5. Version 2 adds categories/budgets/recurring/exchange
 * and extends the original `expenses` table. A real migration replaces the previous
 * fallbackToDestructiveMigration so existing user data survives the refactor.
 */
@Database(
    entities = [
        ExpenseEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        RecurringEntity::class,
        ExchangeRateEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class HarcamDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringDao(): RecurringDao
    abstract fun exchangeRateDao(): ExchangeRateDao

    companion object {
        const val NAME = "harcam_database"

        /** v1 (single `expenses` table) → v2 (category/currency/recurring + new tables). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Extend expenses (existing rows get defaults; categoryId 0 = unassigned → "Other").
                db.execSQL("ALTER TABLE expenses ADD COLUMN categoryId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE expenses ADD COLUMN currency TEXT NOT NULL DEFAULT 'TRY'")
                db.execSQL("ALTER TABLE expenses ADD COLUMN recurringId INTEGER")
                db.execSQL("ALTER TABLE expenses ADD COLUMN occurrenceDate INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_categoryId ON expenses(categoryId)")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_expenses_recurringId_occurrenceDate ON expenses(recurringId, occurrenceDate)",
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        colorKey TEXT NOT NULL,
                        iconKey TEXT NOT NULL,
                        isDefault INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent(),
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS budgets (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        categoryId INTEGER,
                        amountLimit REAL NOT NULL,
                        monthKey TEXT
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_budgets_categoryId_monthKey ON budgets(categoryId, monthKey)",
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recurring (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        currency TEXT NOT NULL DEFAULT 'TRY',
                        categoryId INTEGER NOT NULL,
                        cadence TEXT NOT NULL,
                        nextDueDate INTEGER NOT NULL,
                        reminderDaysBefore INTEGER NOT NULL DEFAULT 1,
                        isPaused INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent(),
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS exchange_rates (
                        pair TEXT NOT NULL PRIMARY KEY,
                        base TEXT NOT NULL,
                        quote TEXT NOT NULL,
                        rate REAL NOT NULL,
                        lastUpdatedEpochMs INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
