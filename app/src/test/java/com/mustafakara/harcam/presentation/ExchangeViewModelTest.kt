package com.mustafakara.harcam.presentation

import app.cash.turbine.test
import com.mustafakara.harcam.core.util.MoneyFormatter
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.ExchangeRate
import com.mustafakara.harcam.domain.model.ExchangeRates
import com.mustafakara.harcam.domain.repository.ExchangeRatesState
import com.mustafakara.harcam.domain.usecase.ObserveExchangeRatesUseCase
import com.mustafakara.harcam.fakes.FakeClock
import com.mustafakara.harcam.fakes.FakeExchangeRateRepository
import com.mustafakara.harcam.presentation.exchange.ExchangeViewModel
import com.mustafakara.harcam.presentation.exchange.RatesStatus
import com.mustafakara.harcam.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExchangeViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val now = 1_700_000_000_000L
    private val rates = ExchangeRates(
        base = Currency.TRY,
        rates = listOf(ExchangeRate(Currency.USD, 0.03), ExchangeRate(Currency.EUR, 0.028)),
        lastUpdatedEpochMs = now,
    )

    private fun viewModel(states: List<ExchangeRatesState>) = ExchangeViewModel(
        observeRates = ObserveExchangeRatesUseCase(FakeExchangeRateRepository(states)),
        formatter = MoneyFormatter(),
        clock = FakeClock(now),
    )

    @Test
    fun `live emission populates rows and Live status`() = runTest {
        val vm = viewModel(listOf(ExchangeRatesState.Loading, ExchangeRatesState.Live(rates)))
        vm.uiState.test {
            var s = awaitItem()
            while (s.status != RatesStatus.Live) s = awaitItem()
            assertEquals(2, s.rates.size)
            assertEquals("USD", s.rates.first().code)
            // formatRate renders "1 <base> = <rate> <quote>" — base is TRY here.
            assertTrue(s.rates.first().formattedRate.startsWith("1 TRY = "))
            assertTrue(s.rates.first().formattedRate.endsWith(" USD"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error with cache degrades to Cached status keeping rows`() = runTest {
        val vm = viewModel(
            listOf(
                ExchangeRatesState.Loading,
                ExchangeRatesState.Cached(rates),
                ExchangeRatesState.Error(cached = rates),
            ),
        )
        vm.uiState.test {
            var s = awaitItem()
            while (s.status != RatesStatus.Cached || s.rates.isEmpty()) s = awaitItem()
            assertEquals(2, s.rates.size) // rows never discarded on the cached path
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error with no cache surfaces error state`() = runTest {
        val vm = viewModel(listOf(ExchangeRatesState.Loading, ExchangeRatesState.Error(cached = null)))
        vm.uiState.test {
            var s = awaitItem()
            while (s.status != RatesStatus.Error) s = awaitItem()
            assertTrue(s.isError)
            assertTrue(s.rates.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
