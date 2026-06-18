package com.mustafakara.harcam.data

import com.mustafakara.harcam.data.local.entity.ExchangeRateEntity
import com.mustafakara.harcam.data.local.entity.ExpenseEntity
import com.mustafakara.harcam.data.mapper.toDomain
import com.mustafakara.harcam.data.mapper.toEntity
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.Expense
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperTest {

    @Test
    fun `expense entity maps to domain`() {
        val entity = ExpenseEntity(
            id = 5,
            description = "coffee",
            amount = 42.0,
            createdAt = 100L,
            categoryId = 3,
            currency = "USD",
        )
        val domain = entity.toDomain()
        assertEquals(5L, domain.id)
        assertEquals("coffee", domain.note)
        assertEquals(42.0, domain.amount, 0.001)
        assertEquals(Currency.USD, domain.currency)
        assertEquals(3L, domain.categoryId)
    }

    @Test
    fun `expense domain round-trips through entity`() {
        val domain = Expense(id = 1, amount = 9.0, currency = Currency.EUR, categoryId = 2, note = "x", createdAt = 7L)
        val back = domain.toEntity().toDomain()
        assertEquals(domain, back)
    }

    @Test
    fun `exchange rate entity maps quote code to currency`() {
        val entity = ExchangeRateEntity(pair = "TRY_USD", base = "TRY", quote = "USD", rate = 0.03, lastUpdatedEpochMs = 1L)
        val domain = entity.toDomain()
        assertEquals(Currency.USD, domain.currency)
        assertEquals(0.03, domain.rate, 0.0001)
    }
}
