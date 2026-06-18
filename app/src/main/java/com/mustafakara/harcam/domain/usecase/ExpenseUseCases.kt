package com.mustafakara.harcam.domain.usecase

import com.mustafakara.harcam.core.common.Clock
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.Expense
import com.mustafakara.harcam.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Reactive list of all expenses (newest first). */
class ObserveExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository,
) {
    operator fun invoke(): Flow<List<Expense>> = repository.observeAll()
}

/** Expenses for one category (category detail). */
class ObserveCategoryExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository,
) {
    operator fun invoke(categoryId: Long): Flow<List<Expense>> =
        repository.observeByCategory(categoryId)
}

/** Validation error surfaced to the add/edit screen. */
sealed interface ExpenseValidationError {
    data object AmountNotPositive : ExpenseValidationError
    data object CategoryMissing : ExpenseValidationError
}

/** Thrown by add/update on invalid input — ViewModels map it to a field error. */
class ExpenseValidation(val error: ExpenseValidationError) : Exception()

private fun validate(amount: Double, categoryId: Long) {
    if (amount <= 0.0) throw ExpenseValidation(ExpenseValidationError.AmountNotPositive)
    if (categoryId <= 0L) throw ExpenseValidation(ExpenseValidationError.CategoryMissing)
}

class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository,
    private val clock: Clock,
) {
    /** Returns the new id, or throws [ExpenseValidation] if invalid. */
    suspend operator fun invoke(
        amount: Double,
        categoryId: Long,
        currency: Currency,
        note: String,
        createdAt: Long? = null,
    ): Long {
        validate(amount, categoryId)
        return repository.add(
            Expense(
                id = 0,
                amount = amount,
                currency = currency,
                categoryId = categoryId,
                note = note.trim(),
                createdAt = createdAt ?: clock.nowMs(),
            ),
        )
    }
}

class UpdateExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository,
) {
    suspend operator fun invoke(expense: Expense) {
        validate(expense.amount, expense.categoryId)
        repository.update(expense.copy(note = expense.note.trim()))
    }
}

class DeleteExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository,
) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}

class GetExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository,
) {
    suspend operator fun invoke(id: Long): Expense? = repository.getById(id)
}
