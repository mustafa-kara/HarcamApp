package com.mustafakara.harcam.domain.usecase

import com.mustafakara.harcam.core.common.AppResult
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.ExchangeRates
import com.mustafakara.harcam.domain.repository.ExchangeRateRepository
import com.mustafakara.harcam.domain.repository.ExchangeRatesState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observe exchange rates for a base currency — exchange_rates.md §7. Emits cache first then the
 * fresh remote result (Live / Cached / Error), the cache-fallback contract owned by the repo.
 */
class ObserveExchangeRatesUseCase @Inject constructor(
    private val repository: ExchangeRateRepository,
) {
    operator fun invoke(base: Currency): Flow<ExchangeRatesState> = repository.observeRates(base)
}

/** Force a refresh (Retry / manual refresh) — returns the AppResult so the caller can react. */
class RefreshExchangeRatesUseCase @Inject constructor(
    private val repository: ExchangeRateRepository,
) {
    suspend operator fun invoke(base: Currency): AppResult<ExchangeRates> = repository.refresh(base)
}
