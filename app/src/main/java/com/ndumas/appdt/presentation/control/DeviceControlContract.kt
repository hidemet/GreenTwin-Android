package com.ndumas.appdt.presentation.control

import com.ndumas.appdt.core.ui.UiText

sealed interface DeviceControlEvent {
    data object OnRefresh : DeviceControlEvent

    data object OnBackClick : DeviceControlEvent

    data object Toggle : DeviceControlEvent

    data object OnPaletteClick : DeviceControlEvent

    data class OnBrightnessChanged(
        val value: Float,
    ) : DeviceControlEvent

    data class OnColorSelected(
        val color: Int,
    ) : DeviceControlEvent

    data class OnTemperatureChanged(
        val value: Float,
    ) : DeviceControlEvent

    data class OnPresetSelected(
        val color: Int,
    ) : DeviceControlEvent

    data object OnPlayPauseClick : DeviceControlEvent

    data object OnSkipNextClick : DeviceControlEvent

    data object OnSkipPrevClick : DeviceControlEvent

    data class OnVolumeChanged(
        val value: Float,
    ) : DeviceControlEvent
}

sealed interface DeviceControlUiEvent {
    data class ShowError(
        val message: UiText,
    ) : DeviceControlUiEvent

    data object NavigateBack : DeviceControlUiEvent

    data object OpenPalette : DeviceControlUiEvent
}
