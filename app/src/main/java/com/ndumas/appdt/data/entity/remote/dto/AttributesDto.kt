package com.ndumas.appdt.data.entity.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AttributesDto(
    // Capability: Brightness (Backend invia 0-255)
    @Json(name = "brightness") val brightness: Int? = null,
    // Capability: RGB Color (Lista di 3 interi [R, G, B])
    @Json(name = "rgb_color") val rgbColor: List<Int>? = null,
    // Capability: Color Temp (Valore corrente in Kelvin)
    @Json(name = "color_temp_kelvin") val colorTempKelvin: Int? = null,
    // Limiti Fisici (Per la logica adattiva)
    @Json(name = "min_color_temp_kelvin") val minTempKelvin: Int? = null,
    @Json(name = "max_color_temp_kelvin") val maxTempKelvin: Int? = null,
    @Json(name = "friendly_name") val friendlyName: String? = null,
    // MEDIA PLAYER Fields
    @Json(name = "volume_level") val volumeLevel: Float? = null, // 0.0 - 1.0
    @Json(name = "is_volume_muted") val isVolumeMuted: Boolean? = null,
    @Json(name = "media_title") val mediaTitle: String? = null,
    @Json(name = "media_artist") val mediaArtist: String? = null,
    @Json(name = "source") val source: String? = null,
)
