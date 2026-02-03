package com.ndumas.appdt.presentation.home.model

import com.ndumas.appdt.domain.device.model.Device

sealed interface SelectionItem {
    data class Header(
        val name: String,
    ) : SelectionItem

    data class SelectableDevice(
        val device: Device,
        val isSelected: Boolean = false,
    ) : SelectionItem
}
