package com.ndumas.appdt.presentation.automation.create.mapper

import com.ndumas.appdt.domain.device.model.DeviceType
import javax.inject.Inject

class DeviceUiMapper
    @Inject
    constructor() {
        fun mapDomainToDeviceType(domain: String): DeviceType =
            when (domain) {
                "light" -> DeviceType.LIGHT
                "switch" -> DeviceType.SWITCH
                "climate" -> DeviceType.THERMOSTAT
                "fan" -> DeviceType.FAN
                "cover" -> DeviceType.BLINDS
                "lock" -> DeviceType.LOCK
                "media_player" -> DeviceType.MEDIA_PLAYER
                "camera" -> DeviceType.CAMERA
                "sensor", "binary_sensor" -> DeviceType.SENSOR
                "weather" -> DeviceType.SENSOR
                "button" -> DeviceType.BUTTON
                else -> DeviceType.OTHER
            }
    }
