package com.mustafakara.harcam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mustafakara.harcam.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Expense data access. Reactive reads return Flow; period summaries reuse the original
 * SQLite date-bucketing queries (now also used by GetPeriodReportUseCase) — architecture.md §5.
 */
@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Query(
        """
        SELECT * FROM expenses
        WHERE createdAt >= :startMs AND createdAt <= :endMs
        ORDER BY createdAt DESC
        """,
    )
    fun observeByDateRange(startMs: Long, endMs: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun observeByCategory(categoryId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses")
    fun observeTotalAmount(): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0.0) FROM expenses
        WHERE createdAt >= :startMs AND createdAt <= :endMs
        """,
    )
    fun observeTotalByDateRange(startMs: Long, endMs: Long): Flow<Double>

    @Query(
        "SELECT COUNT(*) FROM expenses WHERE recurringId = :recurringId AND occurrenceDate = :occurrenceDateMs",
    )
    suspend fun countForRecurringOccurrence(recurringId: Long, occurrenceDateMs: Long): Int

    // ── Period summaries (date-bucketed) — reused by GetPeriodReportUseCase ──────────────────
    @Query(
        """
        SELECT DATE(createdAt/1000, 'unixepoch', 'localtime') as bucket,
               COALESCE(SUM(amount), 0.0) as totalAmount,
               COUNT(*) as expenseCount
        FROM expenses
        WHERE createdAt >= :startMs
        GROUP BY DATE(createdAt/1000, 'unixepoch', 'localtime')
        ORDER BY bucket ASC
        """,
    )
    suspend fun dailyBuckets(startMs: Long): List<PeriodBucket>

    @Query(
        """
        SELECT strftime('%Y-%m', createdAt/1000, 'unixepoch', 'localtime') as bucket,
               COALESCE(SUM(amount), 0.0) as totalAmount,
               COUNT(*) as expenseCount
        FROM expenses
        WHERE createdAt >= :startMs
        GROUP BY strftime('%Y-%m', createdAt/1000, 'unixepoch', 'localtime')
        ORDER BY bucket ASC
        """,
    )
    suspend fun monthlyBuckets(startMs: Long): List<PeriodBucket>

    @Query(
        """
        SELECT categoryId,
               COALESCE(SUM(amount), 0.0) as totalAmount,
               COUNT(*) as expenseCount
        FROM expenses
        WHERE createdAt >= :startMs AND createdAt <= :endMs
        GROUP BY categoryId
        ORDER BY totalAmount DESC
        """,
    )
    suspend fun categoryTotals(startMs: Long, endMs: Long): List<CategoryTotal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE expenses SET categoryId = :toCategoryId WHERE categoryId = :fromCategoryId")
    suspend fun reassignCategory(fromCategoryId: Long, toCategoryId: Long)
}

/** A time-bucketed spend aggregate (day or month) — drives report trend + stats. */
data class PeriodBucket(
    val bucket: String,
    val totalAmount: Double,
    val expenseCount: Int,
)

/** Per-category spend aggregate within a date range. */
data class CategoryTotal(
    val categoryId: Long,
    val totalAmount: Double,
    val expenseCount: Int,
)
