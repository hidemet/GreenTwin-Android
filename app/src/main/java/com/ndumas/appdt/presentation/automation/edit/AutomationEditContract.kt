package com.ndumas.appdt.presentation.automation.edit

import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.automation.model.AutomationAction
import com.ndumas.appdt.domain.automation.model.AutomationDraft
import com.ndumas.appdt.domain.automation.model.AutomationTrigger
import com.ndumas.appdt.domain.automation.model.SimulationResult
import com.ndumas.appdt.domain.device.model.DeviceDetail
import com.ndumas.appdt.presentation.automation.create.model.RoomGroup

data class AutomationEditUiState(
    val automationId: String = "",
    val draft: AutomationDraft = AutomationDraft(),
    val originalDraft: AutomationDraft = AutomationDraft(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: UiText? = null,
    val availableDeviceGroups: List<RoomGroup> = emptyList(),
    val configuringDevice: DeviceDetail? = null,
    val isDeviceLoading: Boolean = false,
    val simulationResult: SimulationResult? = null,
    val showConflictDialog: Boolean = false,
) {
    val isNextEnabled: Boolean
        get() = draft.hasTriggerAndAction

    val hasChanges: Boolean
        get() = draft != originalDraft
}

sealed interface AutomationEditUiEvent {
    data class UpdateName(
        val name: String,
    ) : AutomationEditUiEvent

    data class UpdateDescription(
        val description: String,
    ) : AutomationEditUiEvent

    data class SetTrigger(
        val trigger: AutomationTrigger,
    ) : AutomationEditUiEvent

    data class AddAction(
        val action: AutomationAction,
    ) : AutomationEditUiEvent

    data class RemoveAction(
        val action: AutomationAction,
    ) : AutomationEditUiEvent

    data class SelectDeviceForAction(
        val deviceId: String,
    ) : AutomationEditUiEvent

    data object LoadActionDevices : AutomationEditUiEvent

    data object SaveAutomation : AutomationEditUiEvent

    data object ForceSaveAutomation : AutomationEditUiEvent

    data object DismissConflictDialog : AutomationEditUiEvent

    data object AutomationSaved : AutomationEditUiEvent

    data class ShowError(
        val message: UiText,
    ) : AutomationEditUiEvent
}
