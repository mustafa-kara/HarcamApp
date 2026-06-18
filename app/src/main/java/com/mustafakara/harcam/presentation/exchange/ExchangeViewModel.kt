package com.mustafakara.harcam.presentation.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.core.common.Clock
import com.mustafakara.harcam.core.util.MoneyFormatter
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.ExchangeRate
import com.mustafakara.harcam.domain.model.ExchangeRates
import com.mustafakara.harcam.domain.repository.ExchangeRatesState
import com.mustafakara.harcam.domain.usecase.ObserveExchangeRatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Exchange-rate ViewModel — the Retrofit + cache-fallback showcase (exchange_rates.md §7).
 *
 * [base] and a [refreshToken] are combined and fed through [flatMapLatest]: changing either
 * re-subscribes to [ObserveExchangeRatesUseCase] (cache → live/error), cancelling any in-flight
 * request (§8). The change indicator is computed by diffing a refreshed rate against the value
 * last shown to the user — no fabricated history.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val observeRates: ObserveExchangeRatesUseCase,
    private val formatter: MoneyFormatter,
    private val clock: Clock,
) : ViewModel() {

    private val base = MutableStateFlow(Currency.TRY)
    private val refreshToken = MutableStateFlow(0)
    private val _uiState = MutableStateFlow(ExchangeUiState())
    val uiState: StateFlow<ExchangeUiState> = _uiState.asStateFlow()

    /** Last rate shown per quote code, to compute the next change direction. */
    private var previousRates: Map<String, Double> = emptyMap()

    init {
        combine(base, refreshToken) { b, _ -> b }
            .flatMapLatest { observeRates(it) }
            .onEach(::reduce)
            .launchIn(viewModelScope)
    }

    private fun reduce(state: ExchangeRatesState) {
        when (state) {
            ExchangeRatesState.Loading -> _uiState.update {
                it.copy(isLoading = it.rates.isEmpty(), status = RatesStatus.Loading)
            }
            is ExchangeRatesState.Cached -> emitRates(state.rates, RatesStatus.Cached)
            is ExchangeRatesState.Live -> emitRates(state.rates, RatesStatus.Live)
            is ExchangeRatesState.Error -> {
                val cached = state.cached
                if (cached != null) {
                    emitRates(cached, RatesStatus.Cached)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            status = RatesStatus.Error,
                            rates = emptyList(),
                        )
                    }
                }
            }
        }
    }

    private fun emitRates(rates: ExchangeRates, status: RatesStatus) {
        val now = clock.nowMs()
        val stale = rates.lastUpdatedEpochMs > 0 && now - rates.lastUpdatedEpochMs > STALE_THRESHOLD_MS
        val rows = rates.rates.map { rate -> rate.toUi(rates.base, stale) }
        // Remember the freshly shown values so the next refresh can diff against them.
        previousRates = rates.rates.associate { it.currency.code to it.rate }
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                base = rates.base,
                rates = rows,
                status = status,
                lastUpdatedEpochMs = rates.lastUpdatedEpochMs.takeIf { ms -> ms > 0 },
            )
        }
    }

    private fun ExchangeRate.toUi(base: Currency, stale: Boolean): RateUi {
        val prior = previousRates[currency.code]
        val direction = when {
            prior == null || prior == rate -> ChangeDirection.FLAT
            rate > prior -> ChangeDirection.UP
            else -> ChangeDirection.DOWN
        }
        val changePct = prior?.takeIf { it > 0.0 && it != rate }?.let {
            formatter.formatSignedPercent((rate - it) / it)
        }
        return RateUi(
            code = currency.code,
            name = currency.displayName,
            formattedRate = formatter.formatRate(base, currency, rate),
            changePct = changePct,
            direction = direction,
            isStale = stale,
        )
    }

    fun onBaseChange(currency: Currency) {
        if (currency == base.value) return
        previousRates = emptyMap()
        _uiState.update { it.copy(isLoading = true, rates = emptyList()) }
        base.value = currency
    }

    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        refreshToken.update { it + 1 }
    }

    private val Currency.displayName: String
        get() = when (this) {
            Currency.TRY -> "Turkish Lira"
            Currency.USD -> "US Dollar"
            Currency.EUR -> "Euro"
        }

    companion object {
        private const val STALE_THRESHOLD_MS = 12 * 60 * 60 * 1000L
    }
}
