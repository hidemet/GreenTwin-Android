package com.ndumas.appdt.presentation.home

import HomeUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.core.common.RootError // Aggiunto import
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.core.util.asUiText
import com.ndumas.appdt.domain.automation.model.Automation
import com.ndumas.appdt.domain.automation.usecase.GetAutomationsUseCase
import com.ndumas.appdt.domain.automation.usecase.GetAvailableAutomationsUseCase
import com.ndumas.appdt.domain.consumption.model.EnergySummary // Aggiunto import
import com.ndumas.appdt.domain.consumption.usecase.GetEnergySummaryUseCase
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.domain.device.model.DeviceType
import com.ndumas.appdt.domain.device.usecase.GetAvailableDevicesUseCase
import com.ndumas.appdt.domain.device.usecase.GetDevicesUseCase
import com.ndumas.appdt.domain.device.usecase.GetHiddenSectionsUseCase
import com.ndumas.appdt.domain.device.usecase.GetSavedOrderUseCase
import com.ndumas.appdt.domain.device.usecase.SaveDashboardOrderUseCase
import com.ndumas.appdt.domain.device.usecase.SaveHiddenSectionsUseCase
import com.ndumas.appdt.domain.service.usecase.CallServiceUseCase
import com.ndumas.appdt.presentation.home.mapper.DashboardUiMapper
import com.ndumas.appdt.presentation.home.mapper.SelectionUiMapper
import com.ndumas.appdt.presentation.home.model.DashboardItem
import com.ndumas.appdt.presentation.home.model.DashboardSectionType
import com.ndumas.appdt.presentation.home.model.SelectableAutomationItem
import com.ndumas.appdt.presentation.home.model.SelectionUiItem.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
        private val getAutomationsUseCase: GetAutomationsUseCase,
        private val dashboardMapper: DashboardUiMapper,
        private val callServiceUseCase: CallServiceUseCase,
        private val getAvailableDevicesUseCase: GetAvailableDevicesUseCase,
        private val getAvailableAutomationsUseCase: GetAvailableAutomationsUseCase,
        private val selectionUiMapper: SelectionUiMapper,
        private val getHiddenSectionsUseCase: GetHiddenSectionsUseCase,
        private val saveHiddenSectionsUseCase: SaveHiddenSectionsUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
        val uiState = _uiState.asStateFlow()

        private val _uiEvent = Channel<HomeUiEvent>()
        val uiEvent = _uiEvent.receiveAsFlow()

        private val _selectionGroups = MutableStateFlow<List<SelectionGroup>>(emptyList())
        val selectionGroups = _selectionGroups.asStateFlow()

        private val _automationSelectionItems = MutableStateFlow<List<SelectableAutomationItem>>(emptyList())
        val automationSelectionItems = _automationSelectionItems.asStateFlow()

        private val _roomGroupSelectionItems =
            MutableStateFlow<List<com.ndumas.appdt.presentation.home.model.SelectableRoomGroupItem>>(emptyList())
        val roomGroupSelectionItems = _roomGroupSelectionItems.asStateFlow()

        // Aggiungiamo questi due StateFlow per gestire la reattività
        private val _isEditModeFlow = MutableStateFlow(false)
        private val _hiddenSectionsFlow = MutableStateFlow<Set<DashboardSectionType>>(emptySet())

        init {
            viewModelScope.launch {
                // Carica le sezioni nascoste salvate
                getHiddenSectionsUseCase().collect { savedHiddenSections ->
                    _hiddenSectionsFlow.value = savedHiddenSections
                }
            }
            viewModelScope.launch {
                observeDashboardData()
            }
        }

        private suspend fun observeDashboardData() {
            combine(
                getDevicesUseCase(),
                getAutomationsUseCase(),
                getSavedOrderUseCase(),
                getEnergySummaryUseCase(),
                _isEditModeFlow,
                _hiddenSectionsFlow,
            ) { args: Array<Any?> ->
                @Suppress("UNCHECKED_CAST")
                val devicesResult = args[0] as Result<List<Device>, RootError>

                @Suppress("UNCHECKED_CAST")
                val automationsResult = args[1] as Result<List<Automation>, RootError>

                @Suppress("UNCHECKED_CAST")
                val savedOrder = args[2] as List<String>

                @Suppress("UNCHECKED_CAST")
                val summaryResult = args[3] as Result<EnergySummary?, RootError>
                val isEditMode = args[4] as Boolean

                @Suppress("UNCHECKED_CAST")
                val hiddenSections = args[5] as Set<DashboardSectionType>

                val devices = if (devicesResult is Result.Success) devicesResult.data else emptyList()
                val automations = if (automationsResult is Result.Success) automationsResult.data else emptyList()
                val energySummary = if (summaryResult is Result.Success) summaryResult.data else null
                val error = if (devicesResult is Result.Error) devicesResult.error.asUiText() else null

                val items =
                    dashboardMapper.mapToDashboardItems(
                        devices = devices,
                        automations = automations,
                        savedOrder = savedOrder,
                        energySummary = energySummary,
                        hiddenSections = hiddenSections,
                        isEditMode = isEditMode,
                    )

                HomeUiStatePayload(items, error, false, isEditMode, hiddenSections)
            }.collectLatest { payload ->
                _uiState.update {
                    it.copy(
                        isLoading = payload.isLoading,
                        dashboardItems = payload.items,
                        error = payload.error,
                        isEditMode = payload.isEditMode,
                        hiddenSections = payload.hiddenSections,
                    )
                }
            }
        }

        /**
         * Carica i dispositivi disponibili e li mappa in SelectionItem.
         */
        fun loadAvailableDevices() {
            viewModelScope.launch {
                val result = getDevicesUseCase().first()
                if (result is Result.Success) {
                    val groups = selectionUiMapper.mapToSelectionGroups(result.data)
                    _selectionGroups.value = groups
                }
            }
        }

        fun onAddWidgetClick(type: DashboardSectionType) {
            viewModelScope.launch {
                _uiEvent.send(HomeUiEvent.OpenAddWidgetSheet(type))
            }
        }

        fun removeWidget(itemId: String) {
            val currentItems = _uiState.value.dashboardItems

            val newOrderIds =
                currentItems
                    .filter {
                        it is DashboardItem.DeviceWidget || it is DashboardItem.AutomationWidget ||
                            it is DashboardItem.RoomGroupWidget
                    }.filter { it.id != itemId }
                    .map { it.id }

            val newUiList = currentItems.filter { it.id != itemId }
            _uiState.update { it.copy(dashboardItems = newUiList) }

            viewModelScope.launch {
                saveDashboardOrderUseCase(newOrderIds)
                _uiEvent.send(HomeUiEvent.ShowToast(UiText.DynamicString("Widget rimosso")))
            }
        }

        fun toggleSelection(deviceId: String) {
            val currentGroups =
                _selectionGroups.value.map { group ->
                    // Cerca se il device è in questo gruppo
                    val updatedItems =
                        group.items.map { item ->
                            if (item.device.id == deviceId) {
                                item.copy(isSelected = !item.isSelected)
                            } else {
                                item
                            }
                        }
                    group.copy(items = updatedItems)
                }
            _selectionGroups.value = currentGroups
        }

        fun saveSelectedWidgets() {
            val selectedIds =
                _selectionGroups.value
                    .flatMap { it.items }
                    .filter { it.isSelected }
                    .map { it.device.id }

            if (selectedIds.isNotEmpty()) {
                addWidgetsToDashboardInternal(selectedIds)
            }
        }

        fun loadAvailableAutomations() {
            viewModelScope.launch {
                getAvailableAutomationsUseCase().collect { result ->
                    if (result is Result.Success) {
                        val items =
                            result.data.map { automation ->
                                SelectableAutomationItem(
                                    automation = automation,
                                    isSelected = false,
                                )
                            }
                        _automationSelectionItems.value = items
                    }
                }
            }
        }

        fun toggleAutomationSelection(automationId: String) {
            _automationSelectionItems.update { currentItems ->
                currentItems.map { item ->
                    if (item.automation.id == automationId) {
                        item.copy(isSelected = !item.isSelected)
                    } else {
                        item
                    }
                }
            }
        }

        /**
         * Salva le automazioni selezionate nella dashboard.
         */
        fun saveSelectedAutomations() {
            val selectedIds =
                _automationSelectionItems.value
                    .filter { it.isSelected }
                    .map { it.automation.id }

            if (selectedIds.isNotEmpty()) {
                addAutomationsToDashboardInternal(selectedIds)
            }
        }

        /**
         * Carica le stanze/gruppi disponibili (non ancora nella dashboard).
         */
        fun loadAvailableRoomsGroups() {
            viewModelScope.launch {
                val result = getDevicesUseCase().first()
                if (result is Result.Success) {
                    val devices = result.data
                    val roomsAndGroups = dashboardMapper.extractRoomsAndGroups(devices)

                    // Filtra quelle già presenti nella dashboard
                    val currentIds =
                        _uiState.value.dashboardItems
                            .filter { it is DashboardItem.RoomGroupWidget }
                            .map { it.id }
                            .toSet()

                    val availableItems =
                        roomsAndGroups
                            .filter { it.id !in currentIds }
                            .sortedByDescending { it.deviceCount }
                            .map { info ->
                                com.ndumas.appdt.presentation.home.model.SelectableRoomGroupItem(
                                    roomGroupInfo = info,
                                    isSelected = false,
                                )
                            }

                    _roomGroupSelectionItems.value = availableItems
                }
            }
        }

        /**
         * Toggle selezione di una stanza/gruppo.
         */
        fun toggleRoomGroupSelection(id: String) {
            _roomGroupSelectionItems.update { currentItems ->
                currentItems.map { item ->
                    if (item.roomGroupInfo.id == id) {
                        item.copy(isSelected = !item.isSelected)
                    } else {
                        item
                    }
                }
            }
        }

        /**
         * Salva le stanze/gruppi selezionate nella dashboard.
         */
        fun saveSelectedRoomsGroups() {
            val selectedIds =
                _roomGroupSelectionItems.value
                    .filter { it.isSelected }
                    .map { it.roomGroupInfo.id }

            if (selectedIds.isNotEmpty()) {
                addRoomsGroupsToDashboardInternal(selectedIds)
            }
        }

        private fun addRoomsGroupsToDashboardInternal(newIds: List<String>) {
            val currentItems = _uiState.value.dashboardItems

            val currentIds =
                currentItems
                    .filter {
                        it is DashboardItem.DeviceWidget || it is DashboardItem.AutomationWidget ||
                            it is DashboardItem.RoomGroupWidget
                    }.map { it.id }
                    .toMutableList()

            val uniqueNewIds = newIds.filter { !currentIds.contains(it) }

            if (uniqueNewIds.isNotEmpty()) {
                currentIds.addAll(uniqueNewIds)

                viewModelScope.launch {
                    saveDashboardOrderUseCase(currentIds)
                    _uiEvent.send(HomeUiEvent.ShowToast(UiText.DynamicString("Stanze/Gruppi aggiunti!")))
                }
                // Rendi visibile la sezione ROOMS_GROUPS se era nascosta
                makeSectionVisible(DashboardSectionType.ROOMS_GROUPS)
            }
        }

        private fun addAutomationsToDashboardInternal(newAutomationIds: List<String>) {
            val currentItems = _uiState.value.dashboardItems

            val currentIds =
                currentItems
                    .filter {
                        it is DashboardItem.DeviceWidget || it is DashboardItem.AutomationWidget ||
                            it is DashboardItem.RoomGroupWidget
                    }.map { it.id }
                    .toMutableList()

            val uniqueNewIds = newAutomationIds.filter { !currentIds.contains(it) }

            if (uniqueNewIds.isNotEmpty()) {
                currentIds.addAll(uniqueNewIds)

                viewModelScope.launch {
                    saveDashboardOrderUseCase(currentIds)
                    _uiEvent.send(HomeUiEvent.ShowToast(UiText.DynamicString("Automazioni aggiunte!")))
                }
                // Rendi visibile la sezione AUTOMATIONS se era nascosta
                makeSectionVisible(DashboardSectionType.AUTOMATIONS)
            }
        }

        private fun addWidgetsToDashboardInternal(newDeviceIds: List<String>) {
            val currentItems = _uiState.value.dashboardItems

            val currentIds =
                currentItems
                    .filter {
                        it is DashboardItem.DeviceWidget || it is DashboardItem.AutomationWidget ||
                            it is DashboardItem.RoomGroupWidget
                    }.map { it.id }
                    .toMutableList()

            val uniqueNewIds = newDeviceIds.filter { !currentIds.contains(it) }

            if (uniqueNewIds.isNotEmpty()) {
                currentIds.addAll(uniqueNewIds)

                viewModelScope.launch {
                    saveDashboardOrderUseCase(currentIds)
                    _uiEvent.send(HomeUiEvent.ShowToast(UiText.DynamicString("Widget aggiunti!")))
                }
                // Rendi visibile la sezione DEVICES se era nascosta
                makeSectionVisible(DashboardSectionType.DEVICES)
            }
        }

        /**
         * Rende visibile una sezione se era nascosta.
         */
        private fun makeSectionVisible(sectionType: DashboardSectionType) {
            if (_hiddenSectionsFlow.value.contains(sectionType)) {
                _hiddenSectionsFlow.update { currentHidden ->
                    val newHidden = currentHidden.toMutableSet()
                    newHidden.remove(sectionType)
                    viewModelScope.launch {
                        saveHiddenSectionsUseCase(newHidden)
                    }
                    newHidden
                }
            }
        }

        fun onRefresh() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                delay(100)
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        // Aggiorniamo toggleEditMode per usare il Flow
        fun toggleEditMode() {
            _isEditModeFlow.update { !it }
        }

        fun updateLocalOrder(newItems: List<DashboardItem>) {
            _uiState.update { it.copy(dashboardItems = newItems) }
        }

        fun saveEditModeChanges() {
            val currentItems = _uiState.value.dashboardItems
            val orderIds =
                currentItems
                    .filter {
                        it is DashboardItem.DeviceWidget || it is DashboardItem.AutomationWidget ||
                            it is DashboardItem.RoomGroupWidget
                    }.map { it.id }

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

        /**
         * Gestisce il click su un widget stanza/gruppo.
         * Naviga alla pagina dispositivi con scroll automatico alla sezione corrispondente.
         */
        fun onRoomGroupClicked(roomGroupWidget: DashboardItem.RoomGroupWidget) {
            viewModelScope.launch {
                _uiEvent.send(
                    HomeUiEvent.NavigateToDevicesWithScroll(
                        targetName = roomGroupWidget.name,
                        isRoom = roomGroupWidget.isRoom,
                    ),
                )
            }
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
                            HomeUiEvent.NavigateToDeviceControl(
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

        // Aggiorniamo toggleSectionVisibility per usare il Flow e salvare in DataStore
        fun toggleSectionVisibility(type: DashboardSectionType) {
            _hiddenSectionsFlow.update { currentHidden ->
                val newHidden = currentHidden.toMutableSet()
                if (newHidden.contains(type)) {
                    newHidden.remove(type)
                } else {
                    newHidden.add(type)
                }
                // Salva le nuove impostazioni in DataStore
                viewModelScope.launch {
                    saveHiddenSectionsUseCase(newHidden)
                }
                newHidden
            }
        }

        // Aggiorniamo HomeUiStatePayload
        private data class HomeUiStatePayload(
            val items: List<DashboardItem>,
            val error: UiText?,
            val isLoading: Boolean,
            val isEditMode: Boolean,
            val hiddenSections: Set<DashboardSectionType>,
        )
    }
