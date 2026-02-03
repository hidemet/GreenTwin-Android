package com.ndumas.appdt.domain.device.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Categorie semantiche per la rappresentazione UI dei dispositivi.
 * Mappatura 1:1 con i tipi definiti nel frontend web
 */
enum class DeviceType {
    // Clima
    AIR_CONDITIONER,
    THERMOSTAT,
    FAN,

    // Elettrodomestici Cucina
    REFRIGERATOR,
    DISHWASHER,
    INDUCTION_STOVE,
    MICROWAVE,
    OVEN,

    // Luci e Interruttori
    LIGHT,
    SWITCH,
    BUTTON,

    // Multimedia
    TV,
    MEDIA_PLAYER,
    SPEAKER,
    DESKTOP,

    // Sicurezza e Accessi
    CAMERA,
    DOOR,
    DOORBELL,
    LOCK,

    // Casa e Sensori
    WASHING_MACHINE,
    BLINDS,
    WINDOW,
    SENSOR,

    // Aggregazioni
    ROOM,
    GROUP,

    // Fallback
    OTHER,
}

@Parcelize
data class DeviceGroup(
    val id: Int,
    val name: String,
) : Parcelable

@Parcelize
data class Device(
    val id: String,
    val entityId: String?,
    val name: String,
    val type: DeviceType,
    val model: String?,
    val manufacturer: String?,
    val isOn: Boolean,
    val currentPower: Double, // In Watt
    val room: String?,
    val isOnline: Boolean = true,
    val groups: List<DeviceGroup> = emptyList(),
) : Parcelable
