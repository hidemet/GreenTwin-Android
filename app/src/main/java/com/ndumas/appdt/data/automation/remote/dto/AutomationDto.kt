// filename: AutomationDto.kt
package com.ndumas.appdt.data.automation.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AutomationRequestDto(
    @Json(name = "automation") val automation: AutomationDto,
)

@JsonClass(generateAdapter = true)
data class AutomationDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "alias") val alias: String? = null,
    @Json(name = "name") val name: String? = null,
    val description: String = "",
    val mode: String = "single",
    val trigger: List<TriggerDto>,
    val condition: List<Map<String, Any>> = emptyList(),
    val action: List<ActionDto>,
)

@JsonClass(generateAdapter = true)
data class TriggerDto(
    val platform: String? = null,
    @Json(name = "trigger") val triggerType: String? = null,
    val at: String? = null,
    val event: String? = null,
    val offset: Long? = null,
    @Json(name = "device_id") val deviceId: String? = null,
    val domain: String? = null,
    val type: String? = null,
    val subtype: String? = null,
    val entity_id: String? = null,
    val above: Double? = null,
    val below: Double? = null,
    val for_duration: String? = null,
)

@JsonClass(generateAdapter = true)
data class ActionDto(
    val service: String,
    val target: TargetDto? = null,
    val data: Map<String, Any>? = null,
    @Json(name = "device_id") val flatDeviceId: String? = null,
    @Json(name = "domain") val domain: String? = null,
)

@JsonClass(generateAdapter = true)
data class TargetDto(
    @Json(name = "device_id") val deviceId: List<String>? = null,
    @Json(name = "entity_id") val entityId: List<String>? = null,
)
