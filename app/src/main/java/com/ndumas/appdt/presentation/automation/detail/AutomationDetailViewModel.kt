package com.ndumas.appdt.presentation.automation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndumas.appdt.R
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.core.util.asUiText
import com.ndumas.appdt.domain.automation.usecase.DeleteAutomationUseCase
import com.ndumas.appdt.domain.automation.usecase.GetAutomationByIdUseCase
import com.ndumas.appdt.domain.automation.usecase.ToggleAutomationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val UNDO_DELAY_MS = 5000L

@HiltViewModel
class AutomationDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getAutomationByIdUseCase: GetAutomationByIdUseCase,
        private val toggleAutomationUseCase: ToggleAutomationUseCase,
        private val deleteAutomationUseCase: DeleteAutomationUseCase,
    ) : ViewModel() {
        private val automationId: String = checkNotNull(savedStateHandle["automationId"])

        private val _uiState = MutableStateFlow(AutomationDetailUiState())
        val uiState = _uiState.asStateFlow()

        private val _uiEvent = Channel<AutomationDetailUiEvent>()
        val uiEvent = _uiEvent.receiveAsFlow()

        private var pendingDeleteJob: Job? = null

        init {
            loadAutomation()
        }

        fun loadAutomation() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                getAutomationByIdUseCase(automationId).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    automation = result.data,
                                    error = null,
                                )
                            }
                        }

                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.error.asUiText(),
                                )
                            }
                        }
                    }
                }
            }
        }

        fun onToggleActive(isActive: Boolean) {
            viewModelScope.launch {
                _uiState.update { it.copy(isToggling = true) }

                toggleAutomationUseCase(automationId, isActive).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update { state ->
                                state.copy(
                                    isToggling = false,
                                    automation = state.automation?.copy(isActive = isActive),
                                )
                            }
                            val message =
                                if (isActive) {
                                    UiText.StringResource(R.string.automation_activated)
                                } else {
                                    UiText.StringResource(R.string.automation_deactivated)
                                }
                            _uiEvent.send(AutomationDetailUiEvent.ShowSnackbar(message))
                        }

                        is Result.Error -> {
                            _uiState.update { it.copy(isToggling = false) }
                            _uiEvent.send(AutomationDetailUiEvent.ShowSnackbar(result.error.asUiText()))
                        }
                    }
                }
            }
        }

        fun onEditClick() {
            viewModelScope.launch {
                _uiEvent.send(AutomationDetailUiEvent.NavigateToEdit)
            }
        }

        fun onDeleteClick() {
            _uiState.update { it.copy(showDeleteConfirmDialog = true) }
        }

        fun onDismissDeleteDialog() {
            _uiState.update { it.copy(showDeleteConfirmDialog = false) }
        }

        fun onConfirmDelete() {
            _uiState.update { it.copy(showDeleteConfirmDialog = false, isPendingDelete = true) }

            // Mostra Snackbar con undo immediatamente
            viewModelScope.launch {
                _uiEvent.send(
                    AutomationDetailUiEvent.ShowUndoSnackbar(
                        UiText.StringResource(R.string.automation_deleted_pending),
                    ),
                )
            }

            // Avvia il job di eliminazione con delay per permettere undo
            pendingDeleteJob =
                viewModelScope.launch {
                    delay(UNDO_DELAY_MS)

                    // Se arriviamo qui, l'utente non ha annullato
                    _uiState.update { it.copy(isDeleting = true, isPendingDelete = false) }

                    deleteAutomationUseCase(automationId).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update { it.copy(isDeleting = false) }
                                _uiEvent.send(AutomationDetailUiEvent.AutomationDeleted)
                            }

                            is Result.Error -> {
                                _uiState.update { it.copy(isDeleting = false) }
                                _uiEvent.send(AutomationDetailUiEvent.ShowSnackbar(result.error.asUiText()))
                            }
                        }
                    }
                }
        }

        fun cancelDelete() {
            pendingDeleteJob?.cancel()
            pendingDeleteJob = null
            _uiState.update { it.copy(isPendingDelete = false) }
            viewModelScope.launch {
                _uiEvent.send(AutomationDetailUiEvent.DeleteCancelled)
            }
        }

        fun getAutomationId(): String = automationId
    }
