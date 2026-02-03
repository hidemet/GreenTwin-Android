package com.ndumas.appdt.presentation.home.model

import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.device.model.Device

sealed interface DashboardItem {
    val id: String

    data class EnergyWidget(
        override val id: String = "widget_energy_header",
        val currentPowerKw: Double,
        val yesterdayPowerKw: Double,
        val trend: Int,
        val lastUpdate: Long,
    ) : DashboardItem

    data class DeviceWidget(
        val device: Device,
    ) : DashboardItem {
        override val id: String = device.id
    }

    data class AutomationWidget(
        override val id: String,
        val name: String,
        val description: String?,
        val isActive: Boolean,
    ) : DashboardItem

    data class SectionHeader(
        override val id: String,
        val title: String,
    ) : DashboardItem

    data class EmptyState(
        override val id: String = "empty_state",
        val title: UiText,
        val description: UiText,
        val iconRes: Int,
    ) : DashboardItem
}
