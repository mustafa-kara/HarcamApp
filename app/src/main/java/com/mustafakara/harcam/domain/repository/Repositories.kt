package com.mustafakara.harcam.domain.repository

import com.mustafakara.harcam.core.common.AppResult
import com.mustafakara.harcam.domain.model.Budget
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.ExchangeRates
import com.mustafakara.harcam.domain.model.Expense
import com.mustafakara.harcam.domain.model.RecurringExpense
import kotlinx.coroutines.flow.Flow

/**
 * Repository interfaces — the seam (architecture.md §5). Presentation/domain depend ONLY on
 * these; implementations live in data/repository and are bound in di/RepositoryModule.
 * Local reads expose Flow; remote reads return AppResult.
 */

interface ExpenseRepository {
    fun observeAll(): Flow<List<Expense>>
    fun observeByDateRange(startMs: Long, endMs: Long): Flow<List<Expense>>
    fun observeByCategory(categoryId: Long): Flow<List<Expense>>
    suspend fun getById(id: Long): Expense?
    suspend fun add(expense: Expense): Long
    suspend fun update(expense: Expense)
    suspend fun delete(id: Long)
    /** Used by RecurringWorker to avoid double-materializing the same occurrence. */
    suspend fun existsForRecurringOccurrence(recurringId: Long, occurrenceDateMs: Long): Boolean

    /** Inserts an expense materialized from a recurring template, tagged for idempotency. */
    suspend fun materializeOccurrence(expense: Expense, recurringId: Long, occurrenceDateMs: Long): Long
}

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    suspend fun getById(id: Long): Category?
    suspend fun upsert(category: Category): Long
    /** Reassigns the category's expenses to [reassignToId] (default "Other"), then deletes it. */
    suspend fun delete(id: Long, reassignToId: Long)
    suspend fun seedDefaultsIfEmpty()
    suspend fun count(): Int
}

interface BudgetRepository {
    fun observeAll(): Flow<List<Budget>>
    suspend fun setMonthlyBudget(limit: Double, monthKey: String?)
    suspend fun setCategoryBudget(categoryId: Long, limit: Double, monthKey: String?)
    suspend fun clearCategoryBudget(categoryId: Long, monthKey: String?)
}

interface RecurringRepository {
    fun observeAll(): Flow<List<RecurringExpense>>
    suspend fun getById(id: Long): RecurringExpense?
    suspend fun upsert(recurring: RecurringExpense): Long
    suspend fun delete(id: Long)
    suspend fun getDue(nowMs: Long): List<RecurringExpense>
    suspend fun advanceNextDue(id: Long, nextDueMs: Long)
}

interface ExchangeRateRepository {
    /**
     * Emits cached rates first (if any) then the fresh remote result; on remote failure keeps
     * the cache and surfaces the error — architecture.md §4 (Retrofit + cache fallback).
     */
    fun observeRates(base: Currency): Flow<ExchangeRatesState>
    suspend fun refresh(base: Currency): AppResult<ExchangeRates>
}

/** Three-way state for the exchange screen: live, cached fallback, or error. */
sealed interface ExchangeRatesState {
    data object Loading : ExchangeRatesState
    data class Live(val rates: ExchangeRates) : ExchangeRatesState
    data class Cached(val rates: ExchangeRates) : ExchangeRatesState
    data class Error(val cached: ExchangeRates?) : ExchangeRatesState
}

interface PreferencesRepository {
    data class Preferences(
        val onboarded: Boolean,
        val currency: Currency,
        val themeMode: ThemeMode,
        val maskAmounts: Boolean,
        val budgetAlerts: Boolean,
        val recurringReminders: Boolean,
    )

    enum class ThemeMode { LIGHT, DARK, SYSTEM }

    fun observe(): Flow<Preferences>
    suspend fun setOnboarded(value: Boolean)
    suspend fun setCurrency(currency: Currency)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setMaskAmounts(value: Boolean)
    suspend fun setBudgetAlerts(value: Boolean)
    suspend fun setRecurringReminders(value: Boolean)
}

interface SecurityRepository {
    fun isLockEnabled(): Boolean
    suspend fun setPin(pin: String)
    suspend fun verifyPin(pin: String): Boolean
    suspend fun disableLock()
    fun isBiometricEnabled(): Boolean
    suspend fun setBiometricEnabled(value: Boolean)
}
