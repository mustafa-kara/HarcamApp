package com.mustafakara.harcam.domain

import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.usecase.AddExpenseUseCase
import com.mustafakara.harcam.domain.usecase.ExpenseValidation
import com.mustafakara.harcam.domain.usecase.ExpenseValidationError
import com.mustafakara.harcam.fakes.FakeClock
import com.mustafakara.harcam.fakes.FakeExpenseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ExpenseUseCaseTest {

    private val repo = FakeExpenseRepository()
    private val clock = FakeClock(now = 1_000L)
    private val addExpense = AddExpenseUseCase(repo, clock)

    @Test
    fun `add rejects non-positive amount`() = runTest {
        try {
            addExpense(amount = 0.0, categoryId = 1L, currency = Currency.TRY, note = "")
            fail("Expected validation error")
        } catch (e: ExpenseValidation) {
            assertEquals(ExpenseValidationError.AmountNotPositive, e.error)
        }
    }

    @Test
    fun `add rejects missing category`() = runTest {
        try {
            addExpense(amount = 10.0, categoryId = 0L, currency = Currency.TRY, note = "")
            fail("Expected validation error")
        } catch (e: ExpenseValidation) {
            assertEquals(ExpenseValidationError.CategoryMissing, e.error)
        }
    }

    @Test
    fun `add persists with clock timestamp and trims note`() = runTest {
        val id = addExpense(amount = 25.0, categoryId = 3L, currency = Currency.TRY, note = "  lunch  ")
        val saved = repo.getById(id)!!
        assertEquals(25.0, saved.amount, 0.001)
        assertEquals("lunch", saved.note)
        assertEquals(1_000L, saved.createdAt)
    }

    @Test
    fun `add uses explicit createdAt when given`() = runTest {
        val id = addExpense(amount = 5.0, categoryId = 1L, currency = Currency.TRY, note = "", createdAt = 42L)
        assertEquals(42L, repo.getById(id)!!.createdAt)
        assertTrue(repo.items.value.isNotEmpty())
    }
}
