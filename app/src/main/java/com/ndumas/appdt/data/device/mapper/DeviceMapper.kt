package com.ndumas.appdt.data.device.mapper

import com.ndumas.appdt.data.device.remote.dto.DeviceDto
import com.ndumas.appdt.data.device.remote.dto.EntityDto
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.domain.device.model.DeviceDetail
import com.ndumas.appdt.domain.device.model.DeviceGroup
import com.ndumas.appdt.domain.device.model.DeviceType
import com.ndumas.appdt.domain.device.model.SensorAttribute
import com.ndumas.appdt.domain.device.model.SensorIconType

/**
 * Trasforma un DeviceDto completo (dalla lista /device) in un modello di dettaglio parziale.
 * Per le luci, mancheranno ancora i limiti min/max (che vanno presi con chiamata extra).
 */

fun DeviceDto.toDomain(): Device {
    val type = mapCategoryToType(this.category)
    val isOn = this.state.lowercase() in setOf("on", "playing", "unlocked")

    val powerValue =
        this.powerEntityId?.let { pid ->
            this.entities
                .find { it.entityId == pid }
                ?.state
                ?.toDoubleOrNull()
        } ?: 0.0

    return Device(
        id = this.deviceId,
        entityId = this.stateEntityId,
        name = this.name,
        type = type,
        model = this.model,
        manufacturer = this.manufacturer,
        isOn = isOn,
        currentPower = powerValue,
        room = this.mapData?.room,
        isOnline = this.state.lowercase() != "unavailable",
        groups = this.groups.map { DeviceGroup(it.groupId, it.name) },
    )
}

fun DeviceDto.toDeviceDetail(): DeviceDetail {
    val id = this.deviceId
    val name = this.name
    val isOn = this.state.lowercase() in setOf("on", "playing", "unlocked")
    val isOnline = this.state.lowercase() != "unavailable"

    val powerValue =
        this.powerEntityId?.let { pid ->
            this.entities
                .find { it.entityId == pid }
                ?.state
                ?.toDoubleOrNull()
        }

    val sensors = this.entities.mapNotNull { mapEntityToSensor(it) }

    val roomName = this.mapData?.room

    val groupName =
        if (this.groups.isNotEmpty()) {
            this.groups.joinToString(", ") { it.name }
        } else {
            null
        }

    val realType = mapCategoryToType(this.category)

    return when (this.category.lowercase()) {
        "light" -> {
            DeviceDetail.Light(
                id = id,
                entityId = this.stateEntityId,
                type = realType,
                name = name,
                isOn = isOn,
                isOnline = isOnline,
                currentPowerW = powerValue,
                sensorList = sensors,
                room = roomName,
                group = groupName,
                brightness = if (isOn) 100 else 0,
                colorTemp = 4000,
                rgbColor = null,
                minTemp = 2000,
                maxTemp = 6500,
                supportsBrightness = false,
                supportsColor = false,
                supportsTemp = false,
            )
        }

        "tv", "media_player", "speaker" -> {
            val stateLower = this.state.lowercase()
            val isMediaOn = stateLower in setOf("on", "playing", "paused", "buffering", "idle")
            val isPlaying = stateLower == "playing"

            DeviceDetail.MediaPlayer(
                id = id,
                entityId = this.stateEntityId,
                type = realType,
                name = name,
                isOn = isMediaOn,
                isOnline = isOnline,
                currentPowerW = powerValue,
                sensorList = sensors,
                room = roomName,
                group = groupName,
                volume = 0,
                isMuted = false,
                isPlaying = isPlaying,
                trackTitle = null,
                trackArtist = null,
            )
        }

        "sensor" -> {
            DeviceDetail.Sensor(
                id = id,
                entityId = this.stateEntityId,
                type = realType,
                name = name,
                isOnline = isOnline,
                currentPowerW = powerValue,
                sensorList = sensors,
                room = roomName,
                group = groupName,
                activeAutomations = 0,
            )
        }

        else -> {
            DeviceDetail.Generic(
                id = id,
                entityId = this.stateEntityId,
                type = realType,
                name = name,
                isOn = isOn,
                isOnline = isOnline,
                currentPowerW = powerValue,
                sensorList = sensors,
                room = roomName,
                group = groupName,
            )
        }
    }
}

private fun mapEntityToSensor(entity: EntityDto): SensorAttribute? {
    if (entity.entityId.startsWith("switch.") || entity.entityId.startsWith("light.")) return null
    if (entity.state == "unavailable" || entity.state == "unknown") return null

    val icon =
        when (entity.entityClass?.lowercase()) {
            "temperature" -> SensorIconType.TEMPERATURE
            "humidity" -> SensorIconType.HUMIDITY
            "battery" -> SensorIconType.BATTERY
            "power", "energy" -> SensorIconType.POWER
            "voltage" -> SensorIconType.VOLTAGE
            "current" -> SensorIconType.CURRENT
            else -> SensorIconType.GENERIC
        }

    if (icon == SensorIconType.GENERIC && entity.unitOfMeasurement == null) return null

    val label =
        entity.name ?: entity.entityId
            .split(".")
            .last()
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }

    return SensorAttribute(
        label = label,
        value = entity.state,
        unit = entity.unitOfMeasurement,
        icon = icon,
    )
}

private fun mapCategoryToType(category: String): DeviceType =
    when (category.lowercase()) {
        "air_conditioner", "ac" -> DeviceType.AIR_CONDITIONER
        "thermostat", "climate" -> DeviceType.THERMOSTAT
        "fan" -> DeviceType.FAN
        "refrigerator", "fridge" -> DeviceType.REFRIGERATOR
        "dishwasher" -> DeviceType.DISHWASHER
        "induction_stove", "stove" -> DeviceType.INDUCTION_STOVE
        "microwave" -> DeviceType.MICROWAVE
        "oven" -> DeviceType.OVEN
        "light", "bulb" -> DeviceType.LIGHT
        "switch", "plug", "outlet" -> DeviceType.SWITCH
        "button" -> DeviceType.BUTTON
        "tv", "television" -> DeviceType.TV
        "media_player", "speaker" -> DeviceType.MEDIA_PLAYER
        "desktop", "computer" -> DeviceType.DESKTOP
        "camera" -> DeviceType.CAMERA
        "door" -> DeviceType.DOOR
        "doorbell" -> DeviceType.DOORBELL
        "lock" -> DeviceType.LOCK
        "washing_machine" -> DeviceType.WASHING_MACHINE
        "blinds", "cover" -> DeviceType.BLINDS
        "window" -> DeviceType.WINDOW
        "sensor", "temperature", "humidity" -> DeviceType.SENSOR
        "room" -> DeviceType.ROOM
        "group" -> DeviceType.GROUP
        else -> DeviceType.OTHER
    }

private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
