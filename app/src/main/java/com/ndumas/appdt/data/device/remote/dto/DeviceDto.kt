package com.ndumas.appdt.data.device.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object per un singolo dispositivo, come restituito dall'endpoint GET /device.
 * Questa classe mappa fedelmente la struttura JSON della risposta API.
 */
@JsonClass(generateAdapter = true)
data class DeviceDto(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "name") val name: String,
    @Json(name = "model") val model: String? = null,
    @Json(name = "manufacturer") val manufacturer: String? = null,
    @Json(name = "state") val state: String,
    @Json(name = "category") val category: String,
    @Json(name = "show") val show: Boolean = true,
    @Json(name = "energy_entity_id") val energyEntityId: String? = null,
    @Json(name = "power_entity_id") val powerEntityId: String? = null,
    @Json(name = "state_entity_id") val stateEntityId: String? = null,
    @Json(name = "list_of_entities") val entities: List<EntityDto> = emptyList(),
    @Json(name = "map_data") val mapData: MapDataDto? = null,
    @Json(name = "groups") val groups: List<GroupDto> = emptyList(),
)

/**
 * DTO per le entità annidate all'interno di un dispositivo.
 */
@JsonClass(generateAdapter = true)
data class EntityDto(
    @Json(name = "entity_id") val entityId: String,
    @Json(name = "state") val state: String,
    @Json(name = "entity_class") val entityClass: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "unit_of_measurement") val unitOfMeasurement: String? = null,
)

/**
 * DTO per i dati di posizionamento sulla mappa.
 */
@JsonClass(generateAdapter = true)
data class MapDataDto(
    @Json(name = "x") val x: Double,
    @Json(name = "y") val y: Double,
    @Json(name = "floor") val floor: Int,
    @Json(name = "room") val room: String? = null,
)

/**
 * DTO per i gruppi a cui un dispositivo appartiene.
 */
@JsonClass(generateAdapter = true)
data class GroupDto(
    @Json(name = "group_id") val groupId: Int,
    @Json(name = "name") val name: String,
)
