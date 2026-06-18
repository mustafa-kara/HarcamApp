package com.mustafakara.harcam.data.repository

import com.mustafakara.harcam.data.local.dao.BudgetDao
import com.mustafakara.harcam.data.local.dao.CategoryDao
import com.mustafakara.harcam.data.local.dao.ExpenseDao
import com.mustafakara.harcam.data.local.dao.RecurringDao
import com.mustafakara.harcam.data.local.entity.BudgetEntity
import com.mustafakara.harcam.data.local.entity.CategoryEntity
import com.mustafakara.harcam.data.mapper.toDomain
import com.mustafakara.harcam.data.mapper.toEntity
import com.mustafakara.harcam.domain.model.Budget
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Expense
import com.mustafakara.harcam.domain.model.RecurringExpense
import com.mustafakara.harcam.domain.repository.BudgetRepository
import com.mustafakara.harcam.domain.repository.CategoryRepository
import com.mustafakara.harcam.domain.repository.ExpenseRepository
import com.mustafakara.harcam.domain.repository.RecurringRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao,
) : ExpenseRepository {
    override fun observeAll(): Flow<List<Expense>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeByDateRange(startMs: Long, endMs: Long): Flow<List<Expense>> =
        dao.observeByDateRange(startMs, endMs).map { list -> list.map { it.toDomain() } }

    override fun observeByCategory(categoryId: Long): Flow<List<Expense>> =
        dao.observeByCategory(categoryId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Expense? = dao.getById(id)?.toDomain()

    override suspend fun add(expense: Expense): Long = dao.insert(expense.toEntity())

    override suspend fun update(expense: Expense) = dao.update(expense.toEntity())

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun existsForRecurringOccurrence(
        recurringId: Long,
        occurrenceDateMs: Long,
    ): Boolean = dao.countForRecurringOccurrence(recurringId, occurrenceDateMs) > 0

    override suspend fun materializeOccurrence(
        expense: Expense,
        recurringId: Long,
        occurrenceDateMs: Long,
    ): Long = dao.insert(
        expense.toEntity().copy(recurringId = recurringId, occurrenceDate = occurrenceDateMs),
    )
}

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
    private val expenseDao: ExpenseDao,
) : CategoryRepository {
    override fun observeAll(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Category? = dao.getById(id)?.toDomain()

    override suspend fun upsert(category: Category): Long = dao.upsert(category.toEntity())

    override suspend fun delete(id: Long, reassignToId: Long) {
        expenseDao.reassignCategory(fromCategoryId = id, toCategoryId = reassignToId)
        dao.deleteById(id)
    }

    override suspend fun count(): Int = dao.count()

    override suspend fun seedDefaultsIfEmpty() {
        if (dao.count() > 0) return
        dao.insertAll(DEFAULT_CATEGORIES)
    }

    companion object {
        /** Default categories seeded on first run — design.md §2.1 category palette keys. */
        val DEFAULT_CATEGORIES = listOf(
            CategoryEntity(name = "Food", colorKey = "food", iconKey = "restaurant", isDefault = true),
            CategoryEntity(name = "Transport", colorKey = "transport", iconKey = "directions_bus", isDefault = true),
            CategoryEntity(name = "Bills", colorKey = "bills", iconKey = "receipt_long", isDefault = true),
            CategoryEntity(name = "Shopping", colorKey = "shopping", iconKey = "shopping_bag", isDefault = true),
            CategoryEntity(name = "Health", colorKey = "health", iconKey = "favorite", isDefault = true),
            CategoryEntity(name = "Entertainment", colorKey = "entertainment", iconKey = "sports_esports", isDefault = true),
            CategoryEntity(name = "Other", colorKey = "other", iconKey = "category", isDefault = true),
        )
    }
}

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao,
) : BudgetRepository {
    override fun observeAll(): Flow<List<Budget>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun setMonthlyBudget(limit: Double, monthKey: String?) {
        dao.upsert(BudgetEntity(categoryId = null, amountLimit = limit, monthKey = monthKey))
    }

    override suspend fun setCategoryBudget(categoryId: Long, limit: Double, monthKey: String?) {
        dao.upsert(BudgetEntity(categoryId = categoryId, amountLimit = limit, monthKey = monthKey))
    }

    override suspend fun clearCategoryBudget(categoryId: Long, monthKey: String?) {
        dao.clear(categoryId, monthKey)
    }
}

@Singleton
class RecurringRepositoryImpl @Inject constructor(
    private val dao: RecurringDao,
) : RecurringRepository {
    override fun observeAll(): Flow<List<RecurringExpense>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): RecurringExpense? = dao.getById(id)?.toDomain()

    override suspend fun upsert(recurring: RecurringExpense): Long = dao.upsert(recurring.toEntity())

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun getDue(nowMs: Long): List<RecurringExpense> =
        dao.getDue(nowMs).map { it.toDomain() }

    override suspend fun advanceNextDue(id: Long, nextDueMs: Long) = dao.advanceNextDue(id, nextDueMs)
}
