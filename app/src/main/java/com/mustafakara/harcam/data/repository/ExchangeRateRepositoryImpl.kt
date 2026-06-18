package com.mustafakara.harcam.data.repository

import com.mustafakara.harcam.core.common.AppError
import com.mustafakara.harcam.core.common.AppResult
import com.mustafakara.harcam.data.local.dao.ExchangeRateDao
import com.mustafakara.harcam.data.local.entity.ExchangeRateEntity
import com.mustafakara.harcam.data.mapper.toDomain
import com.mustafakara.harcam.data.remote.api.ExchangeRateApi
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.ExchangeRate
import com.mustafakara.harcam.domain.model.ExchangeRates
import com.mustafakara.harcam.domain.repository.ExchangeRateRepository
import com.mustafakara.harcam.domain.repository.ExchangeRatesState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exchange-rate repository — the Retrofit + Room-cache-fallback showcase (architecture.md §4/§5).
 *
 * observeRates emits the cache first (Cached) for instant paint, then attempts a refresh:
 * success → Live (and cache is updated); failure → Error(cached) keeping the last-known rates.
 * Errors are mapped to AppError; HttpException/IOException never leak to ViewModels.
 */
@Singleton
class ExchangeRateRepositoryImpl @Inject constructor(
    private val api: ExchangeRateApi,
    private val dao: ExchangeRateDao,
) : ExchangeRateRepository {

    private val quotes = Currency.entries

    override fun observeRates(base: Currency): Flow<ExchangeRatesState> = flow {
        emit(ExchangeRatesState.Loading)

        val cached = readCache(base)
        if (cached != null) emit(ExchangeRatesState.Cached(cached))

        when (val refreshed = refresh(base)) {
            is AppResult.Success -> emit(ExchangeRatesState.Live(refreshed.data))
            is AppResult.Error -> emit(ExchangeRatesState.Error(cached ?: readCache(base)))
        }
    }

    override suspend fun refresh(base: Currency): AppResult<ExchangeRates> = try {
        val dto = api.getLatest(base.code)
        val rawRates = dto.rates
        if (dto.result == "success" && rawRates != null) {
            val updatedMs = (dto.timeLastUpdateUnix ?: 0L) * 1000L
            val entities = quotes
                .filter { it != base && rawRates.containsKey(it.code) }
                .map { quote ->
                    ExchangeRateEntity(
                        pair = "${base.code}_${quote.code}",
                        base = base.code,
                        quote = quote.code,
                        rate = rawRates.getValue(quote.code),
                        lastUpdatedEpochMs = updatedMs,
                    )
                }
            dao.upsertAll(entities)
            AppResult.Success(
                ExchangeRates(
                    base = base,
                    rates = entities.map { ExchangeRate(Currency.fromCode(it.quote), it.rate) },
                    lastUpdatedEpochMs = updatedMs,
                ),
            )
        } else {
            AppResult.Error(AppError.Server(code = 200))
        }
    } catch (e: HttpException) {
        AppResult.Error(AppError.Server(code = e.code()))
    } catch (e: IOException) {
        AppResult.Error(AppError.Network)
    } catch (e: Exception) {
        AppResult.Error(AppError.Unknown(e))
    }

    private suspend fun readCache(base: Currency): ExchangeRates? {
        val rows = dao.getForBase(base.code)
        if (rows.isEmpty()) return null
        return ExchangeRates(
            base = base,
            rates = rows.map { it.toDomain() },
            lastUpdatedEpochMs = rows.maxOf { it.lastUpdatedEpochMs },
        )
    }
}
