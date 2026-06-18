package com.mustafakara.harcam.domain.usecase

import com.mustafakara.harcam.core.common.Clock
import com.mustafakara.harcam.core.util.DateUtil
import com.mustafakara.harcam.domain.model.Expense
import com.mustafakara.harcam.domain.model.RecurrenceCadence
import com.mustafakara.harcam.domain.model.RecurringExpense
import com.mustafakara.harcam.domain.repository.ExpenseRepository
import com.mustafakara.harcam.domain.repository.RecurringRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

/** Observe all recurring expense templates — recurring_list.md §7. */
class ObserveRecurringUseCase @Inject constructor(
    private val repository: RecurringRepository,
) {
    operator fun invoke(): Flow<List<RecurringExpense>> = repository.observeAll()
}

class GetRecurringUseCase @Inject constructor(
    private val repository: RecurringRepository,
) {
    suspend operator fun invoke(id: Long): RecurringExpense? = repository.getById(id)
}

/** Validate + upsert a recurring template. Returns the row id (or a validation error). */
class UpsertRecurringUseCase @Inject constructor(
    private val repository: RecurringRepository,
) {
    sealed interface Result {
        data class Success(val id: Long) : Result
        data class Invalid(val error: RecurringValidationError) : Result
    }

    suspend operator fun invoke(recurring: RecurringExpense): Result {
        if (recurring.name.isBlank()) return Result.Invalid(RecurringValidationError.BLANK_NAME)
        if (recurring.amount <= 0.0) return Result.Invalid(RecurringValidationError.NON_POSITIVE_AMOUNT)
        if (recurring.categoryId <= 0L) return Result.Invalid(RecurringValidationError.NO_CATEGORY)
        return Result.Success(repository.upsert(recurring))
    }
}

enum class RecurringValidationError { BLANK_NAME, NON_POSITIVE_AMOUNT, NO_CATEGORY }

class DeleteRecurringUseCase @Inject constructor(
    private val repository: RecurringRepository,
) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}

/**
 * Materializes due recurring templates into real expenses (architecture.md §7/§9). For each
 * non-paused template whose next-due has passed, inserts a tagged expense for every missed
 * occurrence (idempotent via [ExpenseRepository.existsForRecurringOccurrence]) and advances the
 * next-due past now. Returns the number of expenses created.
 */
class MaterializeDueRecurringUseCase @Inject constructor(
    private val recurringRepository: RecurringRepository,
    private val expenseRepository: ExpenseRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Int {
        val now = clock.nowMs()
        var created = 0
        recurringRepository.getDue(now).filterNot { it.isPaused }.forEach { template ->
            var occurrence = template.nextDueDate
            while (occurrence <= now) {
                if (!expenseRepository.existsForRecurringOccurrence(template.id, occurrence)) {
                    expenseRepository.materializeOccurrence(
                        expense = template.toExpense(occurrence),
                        recurringId = template.id,
                        occurrenceDateMs = occurrence,
                    )
                    created++
                }
                occurrence = DateUtil.advance(occurrence, template.cadence.calendarField())
            }
            recurringRepository.advanceNextDue(template.id, occurrence)
        }
        return created
    }

    private fun RecurringExpense.toExpense(occurrenceMs: Long) = Expense(
        id = 0L,
        amount = amount,
        currency = currency,
        categoryId = categoryId,
        note = name,
        createdAt = occurrenceMs,
    )

    private fun RecurrenceCadence.calendarField(): Int = when (this) {
        RecurrenceCadence.WEEKLY -> Calendar.WEEK_OF_YEAR
        RecurrenceCadence.MONTHLY -> Calendar.MONTH
        RecurrenceCadence.YEARLY -> Calendar.YEAR
    }
}
