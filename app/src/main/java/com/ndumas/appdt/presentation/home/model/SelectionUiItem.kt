package com.ndumas.appdt.presentation.home.model

import com.ndumas.appdt.domain.device.model.Device

sealed interface SelectionUiItem {
    data class SelectionGroup(
        val roomName: String,
        val items: List<SelectableDeviceItem>,
    )

    data class SelectableDeviceItem(
        val device: Device,
        val isSelected: Boolean = false,
    )
}
