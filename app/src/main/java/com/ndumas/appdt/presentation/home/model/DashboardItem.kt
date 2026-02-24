package com.ndumas.appdt.presentation.home.model

import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.presentation.consumption.model.PredictionState

enum class DashboardSectionType {
    DEVICES,
    ROOMS_GROUPS,
    AUTOMATIONS,
    INFO, // Aggiunto nuovo tipo
}

sealed interface DashboardItem {
    val id: String

    data class EnergyWidget(
        override val id: String = "widget_energy_header",
        val currentConsumptionKwh: Double,
        val trendPercentage: Double,
        val trendState: PredictionState,
        val sectionType: DashboardSectionType = DashboardSectionType.INFO,
    ) : DashboardItem

    data class DeviceWidget(
        val device: Device,
        val sectionType: DashboardSectionType = DashboardSectionType.DEVICES,
    ) : DashboardItem {
        override val id: String = device.id
    }

    data class AutomationWidget(
        override val id: String,
        val name: String,
        val description: String?,
        val isActive: Boolean,
        val sectionType: DashboardSectionType = DashboardSectionType.AUTOMATIONS,
    ) : DashboardItem

    data class RoomGroupWidget(
        override val id: String,
        val name: String,
        val deviceCount: Int,
        val isRoom: Boolean,
        val sectionType: DashboardSectionType = DashboardSectionType.ROOMS_GROUPS,
    ) : DashboardItem

    data class SectionHeader(
        override val id: String,
        val title: String,
        val sectionType: DashboardSectionType,
        val isVisible: Boolean = true,
        val isExpanded: Boolean = true,
    ) : DashboardItem

    data class AddPlaceholder(
        override val id: String,
        val text: UiText,
        val sectionType: DashboardSectionType,
    ) : DashboardItem

    data class EmptyState(
        override val id: String = "empty_state",
        val title: UiText,
        val description: UiText,
        val iconRes: Int,
    ) : DashboardItem
}
