package com.mustafakara.harcam.fakes

import com.mustafakara.harcam.core.common.AppResult
import com.mustafakara.harcam.core.common.Clock
import com.mustafakara.harcam.domain.model.Budget
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.ExchangeRates
import com.mustafakara.harcam.domain.model.Expense
import com.mustafakara.harcam.domain.model.RecurringExpense
import com.mustafakara.harcam.domain.repository.BudgetRepository
import com.mustafakara.harcam.domain.repository.CategoryRepository
import com.mustafakara.harcam.domain.repository.ExchangeRateRepository
import com.mustafakara.harcam.domain.repository.ExchangeRatesState
import com.mustafakara.harcam.domain.repository.ExpenseRepository
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.repository.RecurringRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Deterministic clock for tests. */
class FakeClock(var now: Long = 0L) : Clock {
    override fun nowMs(): Long = now
}

/** In-memory expense repository backed by a StateFlow so observers see mutations. */
class FakeExpenseRepository(initial: List<Expense> = emptyList()) : ExpenseRepository {
    val items = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1
    val materialized = mutableListOf<Triple<Expense, Long, Long>>()

    override fun observeAll(): Flow<List<Expense>> = items

    override fun observeByDateRange(startMs: Long, endMs: Long): Flow<List<Expense>> =
        items.map { list -> list.filter { it.createdAt in startMs..endMs } }

    override fun observeByCategory(categoryId: Long): Flow<List<Expense>> =
        items.map { list -> list.filter { it.categoryId == categoryId } }

    override suspend fun getById(id: Long): Expense? = items.value.firstOrNull { it.id == id }

    override suspend fun add(expense: Expense): Long {
        val id = nextId++
        items.value = items.value + expense.copy(id = id)
        return id
    }

    override suspend fun update(expense: Expense) {
        items.value = items.value.map { if (it.id == expense.id) expense else it }
    }

    override suspend fun delete(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun existsForRecurringOccurrence(recurringId: Long, occurrenceDateMs: Long): Boolean =
        materialized.any { it.second == recurringId && it.third == occurrenceDateMs }

    override suspend fun materializeOccurrence(expense: Expense, recurringId: Long, occurrenceDateMs: Long): Long {
        materialized += Triple(expense, recurringId, occurrenceDateMs)
        return add(expense)
    }
}

class FakeCategoryRepository(initial: List<Category> = emptyList()) : CategoryRepository {
    val items = MutableStateFlow(initial)
    var seedCount = 0
    override fun observeAll(): Flow<List<Category>> = items
    override suspend fun getById(id: Long): Category? = items.value.firstOrNull { it.id == id }
    override suspend fun upsert(category: Category): Long {
        items.value = items.value.filterNot { it.id == category.id } + category
        return category.id
    }
    override suspend fun delete(id: Long, reassignToId: Long) {
        items.value = items.value.filterNot { it.id == id }
    }
    override suspend fun seedDefaultsIfEmpty() { seedCount++ }
    override suspend fun count(): Int = items.value.size
}

class FakeBudgetRepository(initial: List<Budget> = emptyList()) : BudgetRepository {
    val items = MutableStateFlow(initial)
    override fun observeAll(): Flow<List<Budget>> = items
    override suspend fun setMonthlyBudget(limit: Double, monthKey: String?) {}
    override suspend fun setCategoryBudget(categoryId: Long, limit: Double, monthKey: String?) {}
    override suspend fun clearCategoryBudget(categoryId: Long, monthKey: String?) {}
}

class FakeRecurringRepository(initial: List<RecurringExpense> = emptyList()) : RecurringRepository {
    val items = MutableStateFlow(initial)
    val advanced = mutableListOf<Pair<Long, Long>>()
    override fun observeAll(): Flow<List<RecurringExpense>> = items
    override suspend fun getById(id: Long): RecurringExpense? = items.value.firstOrNull { it.id == id }
    override suspend fun upsert(recurring: RecurringExpense): Long {
        items.value = items.value.filterNot { it.id == recurring.id } + recurring
        return recurring.id.takeIf { it > 0 } ?: (items.value.size.toLong())
    }
    override suspend fun delete(id: Long) { items.value = items.value.filterNot { it.id == id } }
    override suspend fun getDue(nowMs: Long): List<RecurringExpense> = items.value.filter { it.nextDueDate <= nowMs }
    override suspend fun advanceNextDue(id: Long, nextDueMs: Long) {
        advanced += id to nextDueMs
        items.value = items.value.map { if (it.id == id) it.copy(nextDueDate = nextDueMs) else it }
    }
}

class FakePreferencesRepository(
    initial: PreferencesRepository.Preferences = defaultPreferences(),
) : PreferencesRepository {
    val prefs = MutableStateFlow(initial)
    override fun observe(): Flow<PreferencesRepository.Preferences> = prefs
    override suspend fun setOnboarded(value: Boolean) { prefs.value = prefs.value.copy(onboarded = value) }
    override suspend fun setCurrency(currency: Currency) { prefs.value = prefs.value.copy(currency = currency) }
    override suspend fun setThemeMode(mode: PreferencesRepository.ThemeMode) { prefs.value = prefs.value.copy(themeMode = mode) }
    override suspend fun setMaskAmounts(value: Boolean) { prefs.value = prefs.value.copy(maskAmounts = value) }
    override suspend fun setBudgetAlerts(value: Boolean) { prefs.value = prefs.value.copy(budgetAlerts = value) }
    override suspend fun setRecurringReminders(value: Boolean) { prefs.value = prefs.value.copy(recurringReminders = value) }

    companion object {
        fun defaultPreferences() = PreferencesRepository.Preferences(
            onboarded = true,
            currency = Currency.TRY,
            themeMode = PreferencesRepository.ThemeMode.SYSTEM,
            maskAmounts = false,
            budgetAlerts = true,
            recurringReminders = true,
        )
    }
}

/** Scriptable exchange repository: [statesToEmit] is the flow it returns from observeRates. */
class FakeExchangeRateRepository(
    private val statesToEmit: List<ExchangeRatesState>,
    private val refreshResult: AppResult<ExchangeRates>? = null,
) : ExchangeRateRepository {
    override fun observeRates(base: Currency): Flow<ExchangeRatesState> =
        kotlinx.coroutines.flow.flow { statesToEmit.forEach { emit(it) } }
    override suspend fun refresh(base: Currency): AppResult<ExchangeRates> =
        refreshResult ?: AppResult.Error(com.mustafakara.harcam.core.common.AppError.Network)
}
