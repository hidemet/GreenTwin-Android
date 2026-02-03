package com.ndumas.appdt.presentation.home

import HomeUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.core.util.asUiText
import com.ndumas.appdt.domain.consumption.usecase.GetEnergySummaryUseCase
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.domain.device.model.DeviceType
import com.ndumas.appdt.domain.device.usecase.GetAvailableDevicesUseCase
import com.ndumas.appdt.domain.device.usecase.GetDevicesUseCase
import com.ndumas.appdt.domain.device.usecase.GetSavedOrderUseCase
import com.ndumas.appdt.domain.device.usecase.SaveDashboardOrderUseCase
import com.ndumas.appdt.domain.service.usecase.CallServiceUseCase
import com.ndumas.appdt.presentation.home.mapper.DashboardUiMapper
import com.ndumas.appdt.presentation.home.mapper.SelectionUiMapper // <--- Assicurati che questo import esista
import com.ndumas.appdt.presentation.home.model.DashboardItem
import com.ndumas.appdt.presentation.home.model.SelectionItem // <--- Assicurati che questo import esista
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val getDevicesUseCase: GetDevicesUseCase,
        private val getSavedOrderUseCase: GetSavedOrderUseCase,
        private val saveDashboardOrderUseCase: SaveDashboardOrderUseCase,
        private val getEnergySummaryUseCase: GetEnergySummaryUseCase,
        private val dashboardMapper: DashboardUiMapper,
        private val callServiceUseCase: CallServiceUseCase,
        private val getAvailableDevicesUseCase: GetAvailableDevicesUseCase,
        private val selectionUiMapper: SelectionUiMapper,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
        val uiState = _uiState.asStateFlow()

        private val _uiEvent = Channel<HomeUiEvent>()
        val uiEvent = _uiEvent.receiveAsFlow()

        private val _selectionItems = MutableStateFlow<List<SelectionItem>>(emptyList())
        val selectionItems = _selectionItems.asStateFlow()

        init {
            viewModelScope.launch {
                observeDashboardData()
            }
        }

        private suspend fun observeDashboardData() {
            combine(
                getDevicesUseCase(),
                getSavedOrderUseCase(),
                getEnergySummaryUseCase(),
            ) { devicesResult, savedOrder, summaryResult ->

                val devices = if (devicesResult is Result.Success) devicesResult.data else emptyList()
                val energySummary = if (summaryResult is Result.Success) summaryResult.data else null
                val error = if (devicesResult is Result.Error) devicesResult.error.asUiText() else null

                val items =
                    dashboardMapper.mapToDashboardItems(
                        devices = devices,
                        savedOrder = savedOrder,
                        energySummary = energySummary,
                    )

                HomeUiStatePayload(items, error, false)
            }.collectLatest { payload ->
                _uiState.update {
                    it.copy(
                        isLoading = payload.isLoading,
                        dashboardItems = payload.items,
                        error = payload.error,
                    )
                }
            }
        }

        /**
         * Carica i dispositivi disponibili e li mappa in SelectionItem.
         */
        fun loadAvailableDevices() {
            viewModelScope.launch {
                getAvailableDevicesUseCase().collect { result ->
                    if (result is Result.Success) {
                        val items = selectionUiMapper.mapToSelectionList(result.data)
                        _selectionItems.value = items
                    } else {
                        _selectionItems.value = emptyList()
                    }
                }
            }
        }

        fun removeWidget(itemId: String) {
            val currentItems = _uiState.value.dashboardItems

            val newOrderIds =
                currentItems
                    .filter { it is DashboardItem.DeviceWidget || it is DashboardItem.AutomationWidget }
                    .filter { it.id != itemId }
                    .map { it.id }

            val newUiList = currentItems.filter { it.id != itemId }
            _uiState.update { it.copy(dashboardItems = newUiList) }

            viewModelScope.launch {
                saveDashboardOrderUseCase(newOrderIds)
                _uiEvent.send(HomeUiEvent.ShowToast(UiText.DynamicString("Widget rimosso")))
            }
        }

        /**
         * Gestisce il click su un dispositivo nella lista di selezione.
         */
        fun toggleSelection(deviceId: String) {
            val currentList = _selectionItems.value.toMutableList()

            val index =
                currentList.indexOfFirst {
                    it is SelectionItem.SelectableDevice && it.device.id == deviceId
                }

            if (index != -1) {
                val item = currentList[index] as SelectionItem.SelectableDevice

                currentList[index] = item.copy(isSelected = !item.isSelected)
                _selectionItems.value = currentList
            }
        }

        /**
         * Salva i widget selezionati.
         */
        fun saveSelectedWidgets() {
            val selectedIds =
                _selectionItems.value
                    .filterIsInstance<SelectionItem.SelectableDevice>()
                    .filter { it.isSelected }
                    .map { it.device.id }

            if (selectedIds.isNotEmpty()) {
                addWidgetsToDashboardInternal(selectedIds)
            }
        }

        private fun addWidgetsToDashboardInternal(newDeviceIds: List<String>) {
            val currentItems = _uiState.value.dashboardItems

            val currentIds =
                currentItems
                    .filter { it is DashboardItem.DeviceWidget || it is DashboardItem.AutomationWidget }
                    .map { it.id }
                    .toMutableList()

            val uniqueNewIds = newDeviceIds.filter { !currentIds.contains(it) }

            if (uniqueNewIds.isNotEmpty()) {
                currentIds.addAll(uniqueNewIds)

                viewModelScope.launch {
                    saveDashboardOrderUseCase(currentIds)
                    _uiEvent.send(HomeUiEvent.ShowToast(UiText.DynamicString("Widget aggiunti!")))
                }
            }
        }

        fun onRefresh() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                delay(500)
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        fun toggleEditMode() {
            _uiState.update { it.copy(isEditMode = !it.isEditMode) }
        }

        fun updateLocalOrder(newItems: List<DashboardItem>) {
            _uiState.update { it.copy(dashboardItems = newItems) }
        }

        fun saveEditModeChanges() {
            val currentItems = _uiState.value.dashboardItems
            val orderIds =
                currentItems
                    .filter { it is DashboardItem.DeviceWidget || it is DashboardItem.AutomationWidget }
                    .map { it.id }

            viewModelScope.launch {
                saveDashboardOrderUseCase(orderIds)
                toggleEditMode()
            }
        }

        fun onDeviceToggleClick(device: Device) {
            when (device.type) {
                DeviceType.LIGHT, DeviceType.SWITCH, DeviceType.FAN -> toggleDeviceState(device)

                DeviceType.MEDIA_PLAYER, DeviceType.TV, DeviceType.SPEAKER,
                DeviceType.THERMOSTAT, DeviceType.AIR_CONDITIONER,
                DeviceType.SENSOR, DeviceType.CAMERA,
                -> navigateToDetail(device)

                else -> navigateToDetail(device)
            }
        }

        fun onDeviceLongClicked(device: Device) {
            navigateToDetail(device)
        }

        private fun toggleDeviceState(device: Device) {
            val service = if (device.isOn) "turn_off" else "turn_on"
            viewModelScope.launch {
                callServiceUseCase(entityId = device.id, service = service).collect { result ->
                    if (result is Result.Error) {
                        _uiEvent.send(HomeUiEvent.ShowToast(result.error.asUiText()))
                    }
                }
            }
        }

        private fun navigateToDetail(device: Device) {
            viewModelScope.launch {
                when (device.type) {
                    DeviceType.LIGHT, DeviceType.SWITCH, DeviceType.SENSOR,
                    DeviceType.MEDIA_PLAYER, DeviceType.TV, DeviceType.SPEAKER,
                    DeviceType.DESKTOP, DeviceType.OVEN, DeviceType.FAN,
                    DeviceType.AIR_CONDITIONER, DeviceType.THERMOSTAT,
                    -> {
                        _uiEvent.send(
                            HomeUiEvent.NavigateToLightControl(
                                entityId = device.id,
                                roomName = device.room,
                                groupName = null,
                            ),
                        )
                    }

                    else -> {
                        _uiEvent.send(HomeUiEvent.ShowToast(UiText.DynamicString("Dettagli non disponibili per ${device.type}")))
                    }
                }
            }
        }

        private data class HomeUiStatePayload(
            val items: List<DashboardItem>,
            val error: UiText?,
            val isLoading: Boolean,
        )
    }
