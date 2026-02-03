package com.ndumas.appdt.presentation.control

import androidx.annotation.ColorInt
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.device.model.DeviceType
import com.ndumas.appdt.domain.device.model.SensorAttribute

/**
 * Stato della UI per la schermata di controllo. Ogni sottoclasse rappresenta un layout diverso.
 */
sealed interface DeviceControlUiState {
    val entityId: String? get() = null
    val type: DeviceType?

    data object Loading : DeviceControlUiState {
        override val entityId: String? = null
        override val type: DeviceType? = null
    }

    data class Error(
        val message: UiText,
    ) : DeviceControlUiState {
        override val entityId: String? = null
        override val type: DeviceType? = null
    }

    data class LightControl(
        val id: String,
        override val entityId: String?,
        override val type: DeviceType,
        val name: String,
        val roomName: String?,
        val groupName: String?,
        val isOnline: Boolean,
        val isOn: Boolean,
        val currentPowerW: Double?,
        val activeAutomations: Int,
        val brightness: Int,
        val colorTemp: Int,
        @ColorInt val rgbColor: Int?,
        val minTemp: Int,
        val maxTemp: Int,
        // Capabilities
        val supportsBrightness: Boolean,
        val supportsColor: Boolean,
        val supportsTemp: Boolean,
        val activeMode: LightMode = LightMode.WHITE_MODE,
    ) : DeviceControlUiState

    data class MediaControl(
        val id: String,
        override val entityId: String?,
        override val type: DeviceType,
        val name: String,
        val roomName: String?,
        val groupName: String?,
        val isOnline: Boolean,
        val isOn: Boolean,
        val currentPowerW: Double?,
        val activeAutomations: Int,
        val volume: Int, // 0-100
        val isPlaying: Boolean,
        val isMuted: Boolean,
        val trackTitle: String?,
        val trackArtist: String?,
    ) : DeviceControlUiState

    // Stato generico (Switch on/off semplice)
    data class GenericControl(
        val id: String?,
        override val entityId: String?,
        override val type: DeviceType,
        val name: String,
        val roomName: String?,
        val groupName: String?,
        val isOnline: Boolean,
        val isOn: Boolean,
        val currentPowerW: Double?,
        val activeAutomations: Int,
    ) : DeviceControlUiState

    data class SensorControl(
        val id: String,
        override val entityId: String?,
        override val type: DeviceType,
        val name: String,
        val roomName: String?,
        val groupName: String?,
        val isOnline: Boolean,
        val sensors: List<SensorAttribute>,
        val activeAutomations: Int,
    ) : DeviceControlUiState
}

enum class LightMode {
    WHITE_MODE,
    COLOR_MODE,
}
