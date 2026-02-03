package com.ndumas.appdt.data.automation.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SimulationResponseDto(
    @Json(name = "automation") val automation: AutomationDto,
    @Json(name = "conflicts") val conflicts: List<ConflictDto> = emptyList(),
    @Json(name = "suggestions") val suggestions: List<SuggestionDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class ConflictDto(
    @Json(name = "type") val type: String,
    @Json(name = "description") val description: String,
    @Json(name = "days") val days: List<String> = emptyList(),
    @Json(name = "threshold") val threshold: Double? = null,
)

@JsonClass(generateAdapter = true)
data class SuggestionDto(
    @Json(name = "suggestion_type") val type: String,
    @Json(name = "new_activation_time") val newActivationTime: String? = null,
    @Json(name = "monthly_saved_money") val savedMoney: Double? = null,
)
