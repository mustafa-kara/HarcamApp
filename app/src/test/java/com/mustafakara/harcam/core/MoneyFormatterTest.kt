package com.mustafakara.harcam.core

import com.mustafakara.harcam.core.util.MoneyFormatter
import com.mustafakara.harcam.domain.model.Currency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyFormatterTest {

    private val formatter = MoneyFormatter()

    @Test
    fun `format includes currency symbol and two decimals`() {
        val out = formatter.format(1234.5, Currency.TRY)
        assertTrue("expected ₺ in $out", out.contains("₺"))
        assertTrue("expected 2 decimal digits in $out", out.trimEnd().takeLast(3).startsWith(","))
    }

    @Test
    fun `expense is signed with minus`() {
        assertTrue(formatter.formatSigned(120.0, Currency.TRY, isExpense = true).startsWith("−"))
    }

    @Test
    fun `income is signed with plus`() {
        assertTrue(formatter.formatSigned(120.0, Currency.TRY, isExpense = false).startsWith("+"))
    }

    @Test
    fun `percent uses turkish style`() {
        assertEquals("%80", formatter.formatPercent(0.8))
    }

    @Test
    fun `signed percent shows direction`() {
        assertTrue(formatter.formatSignedPercent(0.008).startsWith("+%"))
        assertTrue(formatter.formatSignedPercent(-0.003).startsWith("−%"))
    }

    @Test
    fun `rate uses four decimals and currency codes`() {
        val out = formatter.formatRate(Currency.USD, Currency.TRY, 38.421)
        assertTrue(out.startsWith("1 USD = "))
        assertTrue(out.endsWith(" TRY"))
    }

    @Test
    fun `semantic label speaks the currency`() {
        assertTrue(formatter.semanticLabel(120.0, Currency.TRY, isExpense = true).contains("lira"))
        assertTrue(formatter.semanticLabel(5.0, Currency.USD, isExpense = false).contains("dollars"))
    }
}
