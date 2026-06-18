package com.mustafakara.harcam.domain

import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.RecurrenceCadence
import com.mustafakara.harcam.domain.model.RecurringExpense
import com.mustafakara.harcam.domain.usecase.MaterializeDueRecurringUseCase
import com.mustafakara.harcam.fakes.FakeClock
import com.mustafakara.harcam.fakes.FakeExpenseRepository
import com.mustafakara.harcam.fakes.FakeRecurringRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MaterializeDueRecurringUseCaseTest {

    private val day = 86_400_000L
    private val now = 1_700_000_000_000L

    private fun template(nextDue: Long, paused: Boolean = false) = RecurringExpense(
        id = 1,
        name = "Netflix",
        amount = 50.0,
        currency = Currency.TRY,
        categoryId = 6,
        cadence = RecurrenceCadence.MONTHLY,
        nextDueDate = nextDue,
        reminderDaysBefore = 1,
        isPaused = paused,
    )

    @Test
    fun `materializes a due template and advances next due`() = runTest {
        val recurring = FakeRecurringRepository(listOf(template(nextDue = now - day)))
        val expenses = FakeExpenseRepository()
        val useCase = MaterializeDueRecurringUseCase(recurring, expenses, FakeClock(now))

        val created = useCase()

        assertEquals(1, created)
        assertEquals(1, expenses.items.value.size)
        assertTrue("next-due should advance past now", recurring.advanced.last().second > now)
    }

    @Test
    fun `is idempotent — second run creates nothing new`() = runTest {
        val recurring = FakeRecurringRepository(listOf(template(nextDue = now - day)))
        val expenses = FakeExpenseRepository()
        val useCase = MaterializeDueRecurringUseCase(recurring, expenses, FakeClock(now))

        useCase()
        val before = expenses.items.value.size
        // Re-arm the same occurrence to prove the existence guard blocks a duplicate.
        recurring.items.value = listOf(template(nextDue = now - day))
        val createdAgain = useCase()

        assertEquals(0, createdAgain)
        assertEquals(before, expenses.items.value.size)
    }

    @Test
    fun `paused template is skipped`() = runTest {
        val recurring = FakeRecurringRepository(listOf(template(nextDue = now - day, paused = true)))
        val expenses = FakeExpenseRepository()
        val useCase = MaterializeDueRecurringUseCase(recurring, expenses, FakeClock(now))

        assertEquals(0, useCase())
        assertTrue(expenses.items.value.isEmpty())
    }

    @Test
    fun `future template is not yet due`() = runTest {
        val recurring = FakeRecurringRepository(listOf(template(nextDue = now + day)))
        val expenses = FakeExpenseRepository()
        val useCase = MaterializeDueRecurringUseCase(recurring, expenses, FakeClock(now))

        assertEquals(0, useCase())
    }
}
