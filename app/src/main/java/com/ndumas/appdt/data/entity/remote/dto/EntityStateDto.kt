package com.ndumas.appdt.data.entity.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EntityStateDto(
    @Json(name = "entity_id") val entityId: String,
    @Json(name = "state") val state: String,
    @Json(name = "attributes") val attributes: AttributesDto? = null,
)
