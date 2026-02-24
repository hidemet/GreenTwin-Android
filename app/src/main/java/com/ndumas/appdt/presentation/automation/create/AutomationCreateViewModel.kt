package com.ndumas.appdt.presentation.automation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.core.util.asUiText
import com.ndumas.appdt.domain.automation.usecase.CreateAutomationUseCase
import com.ndumas.appdt.domain.automation.usecase.SimulateAutomationUseCase
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
class AutomationCreateViewModel
    @Inject
    constructor(
        private val getDevicesUseCase: GetDevicesUseCase,
        private val getDeviceDetailUseCase: GetDeviceDetailUseCase,
        private val createAutomationUseCase: CreateAutomationUseCase,
        private val simulateAutomationUseCase: SimulateAutomationUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AutomationCreateUiState())
        val uiState = _uiState.asStateFlow()

        private val _uiEvent = Channel<AutomationCreateUiEvent>()
        val uiEvent = _uiEvent.receiveAsFlow()

        fun onEvent(event: AutomationCreateUiEvent) {
            when (event) {
                is AutomationCreateUiEvent.UpdateName -> {
                    _uiState.update { it.copy(draft = it.draft.copy(name = event.name)) }
                }

                is AutomationCreateUiEvent.SetTrigger -> {
                    _uiState.update { it.copy(draft = it.draft.copy(trigger = event.trigger)) }
                }

                is AutomationCreateUiEvent.AddAction -> {
                    _uiState.update {
                        val newActions = it.draft.actions + event.action
                        it.copy(draft = it.draft.copy(actions = newActions))
                    }
                }

                is AutomationCreateUiEvent.RemoveAction -> {
                    _uiState.update {
                        val newActions = it.draft.actions - event.action
                        it.copy(draft = it.draft.copy(actions = newActions))
                    }
                }

                AutomationCreateUiEvent.LoadActionDevices -> {
                    loadDevices()
                }

                AutomationCreateUiEvent.SaveAutomation -> {
                    simulateAndSave()
                }

                is AutomationCreateUiEvent.SelectDeviceForAction -> {
                    loadDeviceDetail(event.deviceId)
                }

                AutomationCreateUiEvent.ForceSaveAutomation -> {
                    performRealSave()
                }

                AutomationCreateUiEvent.DismissConflictDialog -> {
                    _uiState.update { it.copy(showConflictDialog = false, simulationResult = null) }
                }

                AutomationCreateUiEvent.CancelAutomationCreation -> {
                    _uiState.update { AutomationCreateUiState() }
                    viewModelScope.launch {
                        _uiEvent.send(AutomationCreateUiEvent.NavigateBackToAutomations)
                    }
                }

                else -> {}
            }
        }

        private fun simulateAndSave() {
            val draft = _uiState.value.draft
            android.util.Log.d("DEBUG_VM", "Tentativo simulazione per: ${draft.name}")

            if (draft.name.isBlank()) {
                // TODO: da sistemare
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                simulateAutomationUseCase(draft).collect { result ->
                    android.util.Log.d("DEBUG_VM", "Risultato simulazione: $result")

                    when (result) {
                        is Result.Success -> {
                            val simResult = result.data
                            android.util.Log.d("DEBUG_VM", "Has Conflicts? ${simResult.hasConflicts}")

                            if (simResult.hasConflicts) {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        simulationResult = simResult,
                                        showConflictDialog = true,
                                    )
                                }
                            } else {
                                performRealSave()
                            }
                        }

                        is Result.Error -> {
                            android.util.Log.e("DEBUG_VM", "Errore simulazione: ${result.error}")
                            _uiState.update { it.copy(isLoading = false) }
                            _uiEvent.send(AutomationCreateUiEvent.ShowError(result.error.asUiText()))
                        }
                    }
                }
            }
        }

        private fun performRealSave() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, showConflictDialog = false, simulationResult = null) }

                createAutomationUseCase(_uiState.value.draft).collect { result ->
                    _uiState.update { it.copy(isLoading = false) }

                    when (result) {
                        is Result.Success -> {
                            _uiEvent.send(AutomationCreateUiEvent.AutomationSaved)
                        }

                        is Result.Error -> {
                            _uiEvent.send(AutomationCreateUiEvent.ShowError(result.error.asUiText()))
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
                            _uiEvent.send(AutomationCreateUiEvent.ShowError(result.error.asUiText()))
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
                                    }.sortedWith(
                                        Comparator { g1, g2 ->

                                            if (g1.roomName == "Non assegnato") {
                                                1
                                            } else if (g2.roomName == "Non assegnato") {
                                                -1
                                            } else {
                                                g1.roomName.compareTo(g2.roomName)
                                            }
                                        },
                                    )

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
