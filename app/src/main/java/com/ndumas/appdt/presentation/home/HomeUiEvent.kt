package com.ndumas.appdt.presentation.home

import com.ndumas.appdt.core.ui.UiText

sealed interface HomeUiEvent {
    data class NavigateToLightControl(
        val entityId: String,
        val roomName: String?,
        val groupName: String?,
    ) : HomeUiEvent

    data class ShowToast(
        val message: UiText,
    ) : HomeUiEvent
}
