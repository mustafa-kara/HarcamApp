package com.mustafakara.harcam.domain.usecase

import com.mustafakara.harcam.core.common.Clock
import com.mustafakara.harcam.core.util.DateUtil
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.repository.CategoryRepository
import com.mustafakara.harcam.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    operator fun invoke(): Flow<List<Category>> = repository.observeAll()
}

class GetCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(id: Long): Category? = repository.getById(id)
}

class UpsertCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(category: Category): Long = repository.upsert(category)
}

class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    /** Reassigns the category's expenses to [reassignToId] (the "Other" category), then deletes. */
    suspend operator fun invoke(id: Long, reassignToId: Long) = repository.delete(id, reassignToId)
}

class SeedDefaultCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke() = repository.seedDefaultsIfEmpty()
}

/** A category paired with its month-to-date spend — drives the category list. */
data class CategoryWithSpend(
    val category: Category,
    val monthSpent: Double,
    val transactionCount: Int,
)

/** Categories with their current-month spend, recomputed reactively. */
class ObserveCategorySpendUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<List<CategoryWithSpend>> {
        val now = clock.nowMs()
        val start = DateUtil.startOfMonth(now)
        val end = DateUtil.endOfMonth(now)
        return combine(
            categoryRepository.observeAll(),
            expenseRepository.observeByDateRange(start, end),
        ) { categories, monthExpenses ->
            val byCategory = monthExpenses.groupBy { it.categoryId }
            categories.map { category ->
                val items = byCategory[category.id].orEmpty()
                CategoryWithSpend(
                    category = category,
                    monthSpent = items.sumOf { it.amount },
                    transactionCount = items.size,
                )
            }
        }
    }
}
