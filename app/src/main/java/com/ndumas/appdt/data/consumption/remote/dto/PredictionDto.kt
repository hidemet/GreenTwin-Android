package com.ndumas.appdt.data.consumption.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PredictionResponseDto(
    @Json(name = "cached") val cached: Boolean,
    @Json(name = "data") val data: List<PredictionDataDto>,
    @Json(name = "timestamp") val timestamp: Long,
)

@JsonClass(generateAdapter = true)
data class PredictionDataDto(
    @Json(name = "date") val date: String,
    @Json(name = "energy_consumption") val energyConsumption: Double,
    @Json(name = "energy_consumption_unit") val energyConsumptionUnit: String,
)
