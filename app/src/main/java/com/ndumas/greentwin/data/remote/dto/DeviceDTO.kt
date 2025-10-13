package com.ndumas.greentwin.data.remote.dto

import com.squareup.moshi.Json

/**
 * Data Transfer Object per un singolo dispositivo, come restituito dall'endpoint GET /device.
 * Questa classe mappa fedelmente la struttura JSON della risposta API.
 */
data class DeviceDTO(
    @field:Json(name = "device_id") val deviceId: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "model") val model: String?,
    @field:Json(name = "manufacturer") val manufacturer: String?,
    @field:Json(name = "state") val state: String,
    @field:Json(name = "category") val category: String,
    @field:Json(name = "show") val show: Boolean,
    @field:Json(name = "energy_entity_id") val energyEntityId: String?,
    @field:Json(name = "power_entity_id") val powerEntityId: String?,
    @field:Json(name = "state_entity_id") val stateEntityId: String?,
    @field:Json(name = "list_of_entities") val entities: List<EntityDTO>,
    @field:Json(name = "map_data") val mapData: MapDataDTO?,
    @field:Json(name = "groups") val groups: List<GroupDTO>
)

/**
 * DTO per le entità annidate all'interno di un dispositivo.
 */
data class EntityDTO(
    @field:Json(name = "entity_id") val entityId: String,
    @field:Json(name = "state") val state: String,
    @field:Json(name = "entity_class") val entityClass: String?,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "unit_of_measurement") val unitOfMeasurement: String?
)

/**
 * DTO per i dati di posizionamento sulla mappa.
 */
data class MapDataDTO(
    @field:Json(name = "x") val x: Int,
    @field:Json(name = "y") val y: Int,
    @field:Json(name = "floor") val floor: Int,
    @field:Json(name = "room") val room: String?
)

/**
 * DTO per i gruppi a cui un dispositivo appartiene.
 */
data class GroupDTO(
    @field:Json(name = "group_id") val groupId: Int,
    @field:Json(name = "name") val name: String
)