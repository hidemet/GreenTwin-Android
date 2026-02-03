package com.ndumas.appdt.domain.device.model

import androidx.annotation.ColorInt

// Enum  per il dominio (Icone)
enum class SensorIconType {
    TEMPERATURE,
    HUMIDITY,
    BATTERY,
    POWER,
    VOLTAGE,
    CURRENT,
    GENERIC,
}

//  Modello del singolo attributo sensore
data class SensorAttribute(
    val label: String,
    val value: String,
    val unit: String?,
    val icon: SensorIconType,
)

sealed interface DeviceDetail {
    val id: String
    val entityId: String?
    val type: DeviceType?
    val name: String
    val isOn: Boolean
    val isOnline: Boolean

    val currentPowerW: Double? // Consumo istantaneo
    val sensorList: List<SensorAttribute> // Dati extra (Temp, Batt, ecc.)

    val room: String?
    val group: String?

    data class Light(
        override val id: String,
        override val entityId: String?,
        override val type: DeviceType,
        override val name: String,
        override val isOn: Boolean,
        override val isOnline: Boolean,
        override val currentPowerW: Double? = null,
        override val sensorList: List<SensorAttribute> = emptyList(),
        override val room: String?,
        override val group: String?,
        val brightness: Int,
        val colorTemp: Int,
        @ColorInt val rgbColor: Int?,
        val minTemp: Int,
        val maxTemp: Int,
        val supportsBrightness: Boolean,
        val supportsColor: Boolean,
        val supportsTemp: Boolean,
    ) : DeviceDetail

    data class MediaPlayer(
        override val id: String,
        override val entityId: String?,
        override val type: DeviceType,
        override val name: String,
        override val isOn: Boolean, // true se state == "playing" o "on"
        override val isOnline: Boolean,
        override val currentPowerW: Double? = null,
        override val sensorList: List<SensorAttribute> = emptyList(),
        override val room: String?,
        override val group: String?,
        // Specifici
        val volume: Int, // Normalizzato 0-100
        val isMuted: Boolean,
        val isPlaying: Boolean, // Specifico per play/pause UI
        val trackTitle: String?,
        val trackArtist: String?,
    ) : DeviceDetail

    data class Generic(
        override val id: String,
        override val entityId: String?,
        override val type: DeviceType,
        override val name: String,
        override val isOn: Boolean,
        override val isOnline: Boolean,
        override val currentPowerW: Double? = null,
        override val sensorList: List<SensorAttribute> = emptyList(),
        override val room: String?,
        override val group: String?,
    ) : DeviceDetail

    data class Sensor(
        override val id: String,
        override val entityId: String?,
        override val type: DeviceType,
        override val name: String,
        override val isOnline: Boolean,
        override val currentPowerW: Double? = null,
        override val sensorList: List<SensorAttribute>,
        override val room: String?,
        override val group: String?,
        val activeAutomations: Int = 0,
    ) : DeviceDetail {
        override val isOn: Boolean = true
    }
}
