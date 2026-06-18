package com.mustafakara.harcam.core.util

import com.mustafakara.harcam.domain.model.Currency
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Locale-aware money / percentage / rate formatting — design.md §4.3. Single source so every
 * screen formats consistently. Default locale tr_TR; currency is user-selectable.
 */
@Singleton
class MoneyFormatter @Inject constructor() {

    private val locale: Locale = Locale("tr", "TR")

    /** "₺12.450,00" — absolute amount with the currency symbol, no sign. */
    fun format(amount: Double, currency: Currency): String {
        val nf = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "${currency.symbol}${nf.format(abs(amount))}"
    }

    /** "− ₺120,00" / "+ ₺3.000,00" — signed for expense (out) / income (in). */
    fun formatSigned(amount: Double, currency: Currency, isExpense: Boolean = true): String {
        val sign = if (isExpense) "−" else "+"
        return "$sign ${format(amount, currency)}"
    }

    /** "₺1.640 / ₺2.000" — budget progress. */
    fun formatBudget(spent: Double, limit: Double, currency: Currency): String =
        "${format(spent, currency)} / ${format(limit, currency)}"

    /** "1 USD = 38,4210 TRY" — exchange rate with 4 decimals. */
    fun formatRate(base: Currency, quote: Currency, rate: Double): String {
        val nf = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 4
            maximumFractionDigits = 4
        }
        return "1 ${base.code} = ${nf.format(rate)} ${quote.code}"
    }

    /** "%80" — percent (Turkish style). */
    fun formatPercent(ratio: Double): String = "%${(ratio * 100).toInt()}"

    /** "+%0,8" / "−%0,3" — signed percent change for exchange rates (1 decimal, tr style). */
    fun formatSignedPercent(ratio: Double): String {
        val sign = if (ratio >= 0) "+" else "−"
        val nf = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }
        return "$sign%${nf.format(abs(ratio) * 100)}"
    }

    /**
     * Screen-reader label for a signed amount — design.md §4.3/§12. e.g. "− ₺120,00" →
     * "spent 120 lira" / "+ ₺3.000,00" → "received 3000 lira". Avoids the symbol-only reading.
     */
    fun semanticLabel(amount: Double, currency: Currency, isExpense: Boolean = true): String {
        val verb = if (isExpense) "spent" else "received"
        val nf = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }
        return "$verb ${nf.format(abs(amount))} ${currency.spokenName}"
    }
}

/** Spoken currency name for accessibility labels. */
private val Currency.spokenName: String
    get() = when (this) {
        Currency.TRY -> "lira"
        Currency.USD -> "dollars"
        Currency.EUR -> "euros"
    }
