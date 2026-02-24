package com.ndumas.appdt.presentation.automation.detail

import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.automation.model.Automation

data class AutomationDetailUiState(
    val isLoading: Boolean = true,
    val automation: Automation? = null,
    val error: UiText? = null,
    val isToggling: Boolean = false,
    val isDeleting: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val isPendingDelete: Boolean = false,
)

sealed interface AutomationDetailUiEvent {
    data object NavigateBack : AutomationDetailUiEvent

    data object NavigateToEdit : AutomationDetailUiEvent

    data class ShowSnackbar(
        val message: UiText,
    ) : AutomationDetailUiEvent

    data object AutomationDeleted : AutomationDetailUiEvent

    data class ShowUndoSnackbar(
        val message: UiText,
    ) : AutomationDetailUiEvent

    data object DeleteCancelled : AutomationDetailUiEvent
}
