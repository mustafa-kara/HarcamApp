package com.mustafakara.harcam.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mustafakara.harcam.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Harcama verilerine erişim için Data Access Object
 * Room database işlemlerini tanımlar
 */
@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT * FROM expenses 
        WHERE DATE(createdAt/1000, 'unixepoch', 'localtime') = DATE('now', 'localtime')
        ORDER BY createdAt DESC
    """)
    fun getTodayExpenses(): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT * FROM expenses 
        WHERE createdAt >= :startDate AND createdAt <= :endDate
        ORDER BY createdAt DESC
    """)
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses")
    fun getTotalAmount(): Flow<Double>
    

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM expenses 
        WHERE DATE(createdAt/1000, 'unixepoch', 'localtime') = DATE('now', 'localtime')
    """)
    fun getTodayTotalAmount(): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM expenses 
        WHERE createdAt >= :startDate AND createdAt <= :endDate
    """)
    fun getTotalAmountByDateRange(startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT DATE(createdAt/1000, 'unixepoch', 'localtime') as date, 
               COALESCE(SUM(amount), 0.0) as totalAmount,
               COUNT(*) as expenseCount
        FROM expenses 
        WHERE createdAt >= :startDate
        GROUP BY DATE(createdAt/1000, 'unixepoch', 'localtime')
        ORDER BY date DESC
    """)
    suspend fun getDailySummary(startDate: Long): List<DailySummary>
    

    @Query("""
        SELECT strftime('%Y', createdAt/1000, 'unixepoch', 'localtime') || '-W' || 
               CAST((strftime('%j', createdAt/1000, 'unixepoch', 'localtime') - 1) / 7 + 1 AS INTEGER) as week,
               COALESCE(SUM(amount), 0.0) as totalAmount,
               COUNT(*) as expenseCount
        FROM expenses 
        WHERE createdAt >= :startDate
        GROUP BY strftime('%Y', createdAt/1000, 'unixepoch', 'localtime'), 
                 CAST((strftime('%j', createdAt/1000, 'unixepoch', 'localtime') - 1) / 7 + 1 AS INTEGER)
        ORDER BY week DESC
    """)
    suspend fun getWeeklySummary(startDate: Long): List<WeeklySummary>
    

    @Query("""
        SELECT strftime('%Y-%m', createdAt/1000, 'unixepoch', 'localtime') as month,
               COALESCE(SUM(amount), 0.0) as totalAmount,
               COUNT(*) as expenseCount
        FROM expenses 
        WHERE createdAt >= :startDate
        GROUP BY strftime('%Y-%m', createdAt/1000, 'unixepoch', 'localtime')
        ORDER BY month DESC
    """)
    suspend fun getMonthlySummary(startDate: Long): List<MonthlySummary>
    

    @Query("""
        SELECT strftime('%Y', createdAt/1000, 'unixepoch', 'localtime') as year,
               COALESCE(SUM(amount), 0.0) as totalAmount,
               COUNT(*) as expenseCount
        FROM expenses 
        GROUP BY strftime('%Y', createdAt/1000, 'unixepoch', 'localtime')
        ORDER BY year DESC
    """)
    suspend fun getYearlySummary(): List<YearlySummary>
    

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
    

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Long)
    

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}


data class DailySummary(
    val date: String,
    val totalAmount: Double,
    val expenseCount: Int
)


data class WeeklySummary(
    val week: String,
    val totalAmount: Double,
    val expenseCount: Int
)


data class MonthlySummary(
    val month: String,
    val totalAmount: Double,
    val expenseCount: Int
)

data class YearlySummary(
    val year: String,
    val totalAmount: Double,
    val expenseCount: Int
)