package com.mustafakara.harcam.data.remote.api

import com.mustafakara.harcam.data.remote.dto.ExchangeRateDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Exchange-rate REST endpoint (open.er-api.com) — the app's single remote integration
 * (architecture.md §4/§5). Read-only, non-sensitive.
 */
interface ExchangeRateApi {
    @GET("latest/{base}")
    suspend fun getLatest(@Path("base") base: String): ExchangeRateDto
}
