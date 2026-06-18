package com.mustafakara.harcam.domain

import app.cash.turbine.test
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.Expense
import com.mustafakara.harcam.domain.model.ReportPeriod
import com.mustafakara.harcam.domain.usecase.GetPeriodReportUseCase
import com.mustafakara.harcam.fakes.FakeClock
import com.mustafakara.harcam.fakes.FakeExpenseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPeriodReportUseCaseTest {

    // Fixed "now" — all fixtures share this timestamp so they land in every period window.
    private val now = 1_700_000_000_000L
    private val clock = FakeClock(now = now)

    private fun expense(id: Long, amount: Double, categoryId: Long) =
        Expense(id = id, amount = amount, currency = Currency.TRY, categoryId = categoryId, note = "", createdAt = now)

    @Test
    fun `aggregates total, count and top category`() = runTest {
        val repo = FakeExpenseRepository(
            listOf(
                expense(1, 100.0, categoryId = 7),
                expense(2, 50.0, categoryId = 7),
                expense(3, 30.0, categoryId = 9),
            ),
        )
        val useCase = GetPeriodReportUseCase(repo, clock)

        useCase(ReportPeriod.DAY).test {
            val stats = awaitItem()
            assertEquals(180.0, stats.total, 0.001)
            assertEquals(3, stats.transactionCount)
            assertEquals(7L, stats.topCategoryId) // 150 > 30
            assertEquals(2, stats.byCategory.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty period reports zeros`() = runTest {
        val useCase = GetPeriodReportUseCase(FakeExpenseRepository(), clock)
        useCase(ReportPeriod.DAY).test {
            val stats = awaitItem()
            assertEquals(0.0, stats.total, 0.001)
            assertEquals(0, stats.transactionCount)
            assertEquals(null, stats.topCategoryId)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
