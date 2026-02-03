package com.ndumas.appdt.data.consumption.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConsumptionDto(
    @Json(name = "date") val date: String,
    @Json(name = "energy_consumption") val energyConsumption: Double,
    @Json(name = "energy_consumption_unit") val energyConsumptionUnit: String,
    @Json(name = "cost") val cost: Double? = 0.0,
    @Json(name = "cost_unit") val costUnit: String? = null,
    @Json(name = "entity") val entityId: String? = null,
)
