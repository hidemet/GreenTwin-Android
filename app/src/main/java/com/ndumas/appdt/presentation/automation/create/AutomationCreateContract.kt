package com.ndumas.appdt.presentation.automation.create

import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.automation.model.AutomationAction
import com.ndumas.appdt.domain.automation.model.AutomationConflict
import com.ndumas.appdt.domain.automation.model.AutomationDraft
import com.ndumas.appdt.domain.automation.model.AutomationTrigger
import com.ndumas.appdt.domain.automation.model.SimulationResult
import com.ndumas.appdt.domain.device.model.DeviceDetail
import com.ndumas.appdt.presentation.automation.create.model.RoomGroup

data class AutomationCreateUiState(
    val draft: AutomationDraft = AutomationDraft(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val availableDeviceGroups: List<RoomGroup> = emptyList(),
    val configuringDevice: DeviceDetail? = null,
    val isDeviceLoading: Boolean = false,
    val simulationResult: SimulationResult? = null,
    val showConflictDialog: Boolean = false,
) {
    val isNextEnabled: Boolean
        get() = draft.hasTriggerAndAction
}

sealed interface AutomationCreateUiEvent {
    data class UpdateName(
        val name: String,
    ) : AutomationCreateUiEvent

    data object LoadActionDevices : AutomationCreateUiEvent

    data class SetTrigger(
        val trigger: AutomationTrigger,
    ) : AutomationCreateUiEvent

    data class AddAction(
        val action: AutomationAction,
    ) : AutomationCreateUiEvent

    data class RemoveAction(
        val action: AutomationAction,
    ) : AutomationCreateUiEvent

    data class SelectDeviceForAction(
        val deviceId: String,
    ) : AutomationCreateUiEvent

    data object AutomationSaved : AutomationCreateUiEvent

    data object SaveAutomation : AutomationCreateUiEvent

    data object ForceSaveAutomation : AutomationCreateUiEvent

    data object DismissConflictDialog : AutomationCreateUiEvent

    data class ShowConflictsDialog(
        val conflicts: List<AutomationConflict>,
    ) : AutomationCreateUiEvent

    data class ShowError(
        val message: UiText,
    ) : AutomationCreateUiEvent
}
