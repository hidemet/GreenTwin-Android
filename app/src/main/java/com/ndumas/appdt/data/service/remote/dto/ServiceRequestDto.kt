package com.ndumas.appdt.data.service.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ServiceRequestDto(
    @Json(name = "entity_id") val entityId: String,
    @Json(name = "service") val service: String,
    @Json(name = "data") val data: Map<String, Any>? = null,
    @Json(name = "user") val user: String? = "android_client", // Default temporaneo per tracciare i log
)
