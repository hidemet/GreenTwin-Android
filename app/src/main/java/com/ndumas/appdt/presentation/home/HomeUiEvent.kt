package com.ndumas.appdt.presentation.home

import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.presentation.home.model.DashboardSectionType

sealed interface HomeUiEvent {
    data class NavigateToDeviceControl(
        val entityId: String,
        val roomName: String?,
        val groupName: String?,
    ) : HomeUiEvent

    data class OpenAddWidgetSheet(
        val type: DashboardSectionType,
    ) : HomeUiEvent

    data class ShowToast(
        val message: UiText,
    ) : HomeUiEvent

    data class NavigateToDevicesWithScroll(
        val targetName: String,
        val isRoom: Boolean,
    ) : HomeUiEvent
}
