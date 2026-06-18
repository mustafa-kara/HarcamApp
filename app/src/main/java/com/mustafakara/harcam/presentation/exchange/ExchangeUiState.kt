package com.mustafakara.harcam.presentation.exchange

import com.mustafakara.harcam.domain.model.Currency

/**
 * Exchange screen state — exchange_rates.md §7. [status] drives the four states 1:1; rates always
 * render once a cache exists (never a blank screen on a network failure).
 */
data class ExchangeUiState(
    val isLoading: Boolean = true,
    val base: Currency = Currency.TRY,
    val rates: List<RateUi> = emptyList(),
    val status: RatesStatus = RatesStatus.Loading,
    val lastUpdatedEpochMs: Long? = null,
    val isRefreshing: Boolean = false,
) {
    val isError: Boolean get() = status is RatesStatus.Error && rates.isEmpty()
}

/** Maps the repository's emissions onto an explicit screen status. */
sealed interface RatesStatus {
    data object Loading : RatesStatus
    data object Live : RatesStatus
    data object Cached : RatesStatus
    data object Error : RatesStatus
}

/** One rendered rate row. [changePct]/[direction] are present only when a prior value was known. */
data class RateUi(
    val code: String,
    val name: String,
    val formattedRate: String,
    val changePct: String?,
    val direction: ChangeDirection,
    val isStale: Boolean,
)

enum class ChangeDirection { UP, DOWN, FLAT }
