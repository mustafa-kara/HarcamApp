package com.mustafakara.harcam.domain.usecase

import com.mustafakara.harcam.core.common.Clock
import com.mustafakara.harcam.core.util.DateUtil
import com.mustafakara.harcam.domain.model.Budget
import com.mustafakara.harcam.domain.model.BudgetStatus
import com.mustafakara.harcam.domain.repository.BudgetRepository
import com.mustafakara.harcam.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveBudgetsUseCase @Inject constructor(
    private val repository: BudgetRepository,
) {
    operator fun invoke(): Flow<List<Budget>> = repository.observeAll()
}

class SetMonthlyBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository,
) {
    suspend operator fun invoke(limit: Double, monthKey: String? = null) =
        repository.setMonthlyBudget(limit, monthKey)
}

class SetCategoryBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository,
) {
    suspend operator fun invoke(categoryId: Long, limit: Double, monthKey: String? = null) =
        repository.setCategoryBudget(categoryId, limit, monthKey)
}

/** The full budget picture for the current month — overall + per-category statuses. */
data class BudgetOverview(
    val monthly: BudgetStatus?,
    val perCategory: List<BudgetStatus>,
)

/**
 * Combines budget limits with current-month spend into derived [BudgetStatus] values
 * (architecture.md §4). A budget with limit applies if its monthKey is null (ongoing) or
 * matches the current month.
 */
class ObserveBudgetStatusUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<BudgetOverview> {
        val now = clock.nowMs()
        val monthKey = DateUtil.monthKey(now)
        val start = DateUtil.startOfMonth(now)
        val end = DateUtil.endOfMonth(now)
        return combine(
            budgetRepository.observeAll(),
            expenseRepository.observeByDateRange(start, end),
        ) { budgets, monthExpenses ->
            val applicable = budgets.filter { it.monthKey == null || it.monthKey == monthKey }
            val totalSpent = monthExpenses.sumOf { it.amount }
            val spentByCategory = monthExpenses.groupBy { it.categoryId }
                .mapValues { (_, list) -> list.sumOf { it.amount } }

            val monthly = applicable.firstOrNull { it.categoryId == null }?.let {
                BudgetStatus(categoryId = null, spent = totalSpent, limit = it.limit)
            }
            val perCategory = applicable
                .filter { it.categoryId != null }
                .map { budget ->
                    BudgetStatus(
                        categoryId = budget.categoryId,
                        spent = spentByCategory[budget.categoryId] ?: 0.0,
                        limit = budget.limit,
                    )
                }
            BudgetOverview(monthly = monthly, perCategory = perCategory)
        }
    }
}
