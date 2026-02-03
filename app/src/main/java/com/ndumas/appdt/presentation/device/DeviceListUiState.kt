package com.ndumas.appdt.presentation.device

import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.presentation.home.model.DashboardItem

data class DeviceListUiState(
    val isLoading: Boolean = false,
    val items: List<DashboardItem> = emptyList(),
    val error: UiText? = null,
)

sealed interface DeviceListUiEvent {
    data class NavigateToControl(
        val deviceId: String,
        val room: String?,
    ) : DeviceListUiEvent

    data class ShowToast(
        val message: UiText,
    ) : DeviceListUiEvent
}
