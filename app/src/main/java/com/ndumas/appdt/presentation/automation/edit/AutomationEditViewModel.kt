package com.ndumas.appdt.presentation.automation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.core.util.asUiText
import com.ndumas.appdt.domain.automation.model.AutomationDraft
import com.ndumas.appdt.domain.automation.usecase.GetAutomationByIdUseCase
import com.ndumas.appdt.domain.automation.usecase.SimulateAutomationUseCase
import com.ndumas.appdt.domain.automation.usecase.UpdateAutomationUseCase
import com.ndumas.appdt.domain.device.model.DeviceType
import com.ndumas.appdt.domain.device.usecase.GetDeviceDetailUseCase
import com.ndumas.appdt.domain.device.usecase.GetDevicesUseCase
import com.ndumas.appdt.presentation.automation.create.model.RoomGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutomationEditViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getAutomationByIdUseCase: GetAutomationByIdUseCase,
        private val updateAutomationUseCase: UpdateAutomationUseCase,
        private val simulateAutomationUseCase: SimulateAutomationUseCase,
        private val getDevicesUseCase: GetDevicesUseCase,
        private val getDeviceDetailUseCase: GetDeviceDetailUseCase,
    ) : ViewModel() {
        private val automationId: String = checkNotNull(savedStateHandle["automationId"])

        private val _uiState = MutableStateFlow(AutomationEditUiState(automationId = automationId))
        val uiState = _uiState.asStateFlow()

        private val _uiEvent = Channel<AutomationEditUiEvent>()
        val uiEvent = _uiEvent.receiveAsFlow()

        init {
            loadAutomation()
        }

        private fun loadAutomation() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                getAutomationByIdUseCase(automationId).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val automation = result.data
                            val draft =
                                AutomationDraft(
                                    name = automation.name,
                                    description = automation.description,
                                    trigger = automation.triggers.firstOrNull(),
                                    actions = automation.actions,
                                    isActive = automation.isActive,
                                )
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    draft = draft,
                                    originalDraft = draft,
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

        fun onEvent(event: AutomationEditUiEvent) {
            when (event) {
                is AutomationEditUiEvent.UpdateName -> {
                    _uiState.update { it.copy(draft = it.draft.copy(name = event.name)) }
                }

                is AutomationEditUiEvent.UpdateDescription -> {
                    _uiState.update { it.copy(draft = it.draft.copy(description = event.description)) }
                }

                is AutomationEditUiEvent.SetTrigger -> {
                    _uiState.update { it.copy(draft = it.draft.copy(trigger = event.trigger)) }
                }

                is AutomationEditUiEvent.AddAction -> {
                    // In modalità edit, sostituiamo l'azione esistente (supportiamo solo 1 azione)
                    _uiState.update {
                        it.copy(draft = it.draft.copy(actions = listOf(event.action)))
                    }
                }

                is AutomationEditUiEvent.RemoveAction -> {
                    _uiState.update {
                        val newActions = it.draft.actions - event.action
                        it.copy(draft = it.draft.copy(actions = newActions))
                    }
                }

                AutomationEditUiEvent.LoadActionDevices -> {
                    loadDevices()
                }

                is AutomationEditUiEvent.SelectDeviceForAction -> {
                    loadDeviceDetail(event.deviceId)
                }

                AutomationEditUiEvent.SaveAutomation -> {
                    simulateAndSave()
                }

                AutomationEditUiEvent.ForceSaveAutomation -> {
                    performRealSave()
                }

                AutomationEditUiEvent.DismissConflictDialog -> {
                    _uiState.update { it.copy(showConflictDialog = false, simulationResult = null) }
                }

                else -> {}
            }
        }

        private fun simulateAndSave() {
            val draft = _uiState.value.draft

            if (draft.name.isBlank()) {
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }

                simulateAutomationUseCase(draft).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val simResult = result.data
                            if (simResult.hasConflicts) {
                                _uiState.update {
                                    it.copy(
                                        isSaving = false,
                                        simulationResult = simResult,
                                        showConflictDialog = true,
                                    )
                                }
                            } else {
                                performRealSave()
                            }
                        }

                        is Result.Error -> {
                            _uiState.update { it.copy(isSaving = false) }
                            _uiEvent.send(AutomationEditUiEvent.ShowError(result.error.asUiText()))
                        }
                    }
                }
            }
        }

        private fun performRealSave() {
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, showConflictDialog = false, simulationResult = null) }

                updateAutomationUseCase(automationId, _uiState.value.draft).collect { result ->
                    _uiState.update { it.copy(isSaving = false) }

                    when (result) {
                        is Result.Success -> {
                            _uiEvent.send(AutomationEditUiEvent.AutomationSaved)
                        }

                        is Result.Error -> {
                            _uiEvent.send(AutomationEditUiEvent.ShowError(result.error.asUiText()))
                        }
                    }
                }
            }
        }

        private fun loadDeviceDetail(deviceId: String) {
            viewModelScope.launch {
                _uiState.update { it.copy(isDeviceLoading = true, configuringDevice = null) }

                getDeviceDetailUseCase(deviceId).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(
                                    isDeviceLoading = false,
                                    configuringDevice = result.data,
                                )
                            }
                        }

                        is Result.Error -> {
                            _uiState.update { it.copy(isDeviceLoading = false) }
                            _uiEvent.send(AutomationEditUiEvent.ShowError(result.error.asUiText()))
                        }
                    }
                }
            }
        }

        private fun loadDevices() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                getDevicesUseCase().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val actionableDevices =
                                result.data.filter { device ->
                                    device.type in
                                        setOf(
                                            DeviceType.LIGHT,
                                            DeviceType.SWITCH,
                                            DeviceType.MEDIA_PLAYER,
                                            DeviceType.TV,
                                            DeviceType.AIR_CONDITIONER,
                                            DeviceType.FAN,
                                            DeviceType.THERMOSTAT,
                                        )
                                }

                            val groupedList =
                                actionableDevices
                                    .groupBy { it.room ?: "Non assegnato" }
                                    .map { (roomName, devices) ->
                                        RoomGroup(roomName, devices.sortedBy { it.name })
                                    }.sortedWith { g1, g2 ->
                                        if (g1.roomName == "Non assegnato") {
                                            1
                                        } else if (g2.roomName == "Non assegnato") {
                                            -1
                                        } else {
                                            g1.roomName.compareTo(g2.roomName)
                                        }
                                    }

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    availableDeviceGroups = groupedList,
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
    }
