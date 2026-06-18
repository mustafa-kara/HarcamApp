package com.mustafakara.harcam.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response DTO for open.er-api.com `/latest/{base}` — data/remote/dto.
 * Example: { "result": "success", "base_code": "USD", "time_last_update_unix": 1718....,
 *            "rates": { "USD": 1, "EUR": 0.92, "TRY": 38.42 } }
 */
@JsonClass(generateAdapter = true)
data class ExchangeRateDto(
    @Json(name = "result") val result: String?,
    @Json(name = "base_code") val baseCode: String?,
    @Json(name = "time_last_update_unix") val timeLastUpdateUnix: Long?,
    @Json(name = "rates") val rates: Map<String, Double>?,
)
