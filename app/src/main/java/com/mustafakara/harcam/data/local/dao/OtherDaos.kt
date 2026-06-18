package com.mustafakara.harcam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mustafakara.harcam.data.local.entity.BudgetEntity
import com.mustafakara.harcam.data.local.entity.CategoryEntity
import com.mustafakara.harcam.data.local.entity.ExchangeRateEntity
import com.mustafakara.harcam.data.local.entity.RecurringEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE categoryId IS :categoryId AND monthKey IS :monthKey")
    suspend fun clear(categoryId: Long?, monthKey: String?)
}

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring ORDER BY nextDueDate ASC")
    fun observeAll(): Flow<List<RecurringEntity>>

    @Query("SELECT * FROM recurring WHERE id = :id")
    suspend fun getById(id: Long): RecurringEntity?

    @Query("SELECT * FROM recurring WHERE isPaused = 0 AND nextDueDate <= :nowMs")
    suspend fun getDue(nowMs: Long): List<RecurringEntity>

    @Upsert
    suspend fun upsert(recurring: RecurringEntity): Long

    @Query("UPDATE recurring SET nextDueDate = :nextDueMs WHERE id = :id")
    suspend fun advanceNextDue(id: Long, nextDueMs: Long)

    @Query("DELETE FROM recurring WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM exchange_rates WHERE base = :base")
    fun observeForBase(base: String): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates WHERE base = :base")
    suspend fun getForBase(base: String): List<ExchangeRateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rates: List<ExchangeRateEntity>)
}
