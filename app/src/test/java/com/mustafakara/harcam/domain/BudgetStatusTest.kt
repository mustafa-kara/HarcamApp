package com.mustafakara.harcam.domain

import com.mustafakara.harcam.domain.model.BudgetLevel
import com.mustafakara.harcam.domain.model.BudgetStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/** Budget level thresholds — architecture.md §9 (79% NORMAL / 80% WARNING / 100% OVER). */
class BudgetStatusTest {

    private fun status(spent: Double, limit: Double = 100.0) =
        BudgetStatus(categoryId = null, spent = spent, limit = limit)

    @Test
    fun `79 percent is normal`() {
        assertEquals(BudgetLevel.NORMAL, status(79.0).level)
    }

    @Test
    fun `exactly 80 percent is warning`() {
        assertEquals(BudgetLevel.WARNING, status(80.0).level)
    }

    @Test
    fun `99 percent is warning`() {
        assertEquals(BudgetLevel.WARNING, status(99.0).level)
    }

    @Test
    fun `exactly 100 percent is over`() {
        assertEquals(BudgetLevel.OVER, status(100.0).level)
    }

    @Test
    fun `over limit is over`() {
        val s = status(120.0)
        assertEquals(BudgetLevel.OVER, s.level)
        assertEquals(-20.0, s.remaining, 0.001)
    }

    @Test
    fun `zero limit never divides by zero`() {
        val s = status(50.0, limit = 0.0)
        assertEquals(0.0, s.ratio, 0.001)
        assertEquals(BudgetLevel.NORMAL, s.level)
    }
}
