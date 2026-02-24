package com.ndumas.appdt.presentation.device

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.core.util.asUiText
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.domain.device.model.DeviceType
import com.ndumas.appdt.domain.device.usecase.GetDevicesUseCase
import com.ndumas.appdt.domain.service.usecase.CallServiceUseCase
import com.ndumas.appdt.presentation.device.mapper.DeviceGrouping
import com.ndumas.appdt.presentation.device.mapper.DeviceListUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel
    @Inject
    constructor(
        getDevicesUseCase: GetDevicesUseCase,
        private val callServiceUseCase: CallServiceUseCase,
        private val mapper: DeviceListUiMapper,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _selectedTabIndex = MutableStateFlow(0)

        // Stato per le sezioni espanse (Set di headerId)
        private val _expandedSections = MutableStateFlow<Set<String>?>(null)

        // Flag per forzare il refresh (incrementale per garantire nuova emissione)
        private val _refreshTrigger = MutableStateFlow(0)

        // Parametri di navigazione dalla Home
        private val scrollTarget: String? = savedStateHandle.get<String>("scrollTarget")
        private val isRoomTarget: Boolean = savedStateHandle.get<Boolean>("isRoom") ?: true

        // Target section per espansione (usato nel combine)
        private val _targetSection = MutableStateFlow<String?>(null)

        // Flag per indicare se dobbiamo scrollare a un target (consumabile)
        private var pendingScrollTarget: String? = null

        init {
            // Se arriviamo dalla Home con un target, imposta la tab corretta e il target
            if (scrollTarget != null) {
                _selectedTabIndex.value = if (isRoomTarget) 0 else 1
                _targetSection.value = scrollTarget
                pendingScrollTarget = scrollTarget
            }
        }

        val uiState: StateFlow<DeviceListUiState> =
            combine(
                getDevicesUseCase(),
                _selectedTabIndex,
                _expandedSections,
                _targetSection,
                _refreshTrigger,
            ) { result, tabIndex, expandedSections, targetSection, _ ->

                val grouping =
                    when (tabIndex) {
                        0 -> DeviceGrouping.ROOM
                        1 -> DeviceGrouping.GROUP
                        2 -> DeviceGrouping.UNASSIGNED
                        else -> DeviceGrouping.ROOM
                    }

                when (result) {
                    is Result.Success -> {
                        val items =
                            mapper.mapToDeviceList(
                                devices = result.data,
                                grouping = grouping,
                                expandedSections = expandedSections,
                                targetSection = targetSection,
                            )
                        DeviceListUiState(isLoading = false, items = items, error = null)
                    }

                    is Result.Error -> {
                        DeviceListUiState(isLoading = false, error = result.error.asUiText())
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DeviceListUiState(isLoading = true),
            )

        private val _uiEvent = Channel<DeviceListUiEvent>()
        val uiEvent = _uiEvent.receiveAsFlow()

        fun consumePendingScrollTarget(): String? {
            val target = pendingScrollTarget
            pendingScrollTarget = null
            return target
        }

        fun onTabSelected(index: Int) {
            // Reset delle sezioni espanse quando cambia tab (tutte aperte di default)
            _expandedSections.value = null
            _targetSection.value = null
            pendingScrollTarget = null
            _selectedTabIndex.value = index
        }

        fun onRefresh() {
            // Incrementa il trigger per forzare una nuova emissione del combine
            _refreshTrigger.value++
        }

        /**
         * Toggle dell'espansione di una sezione accordion.
         */
        fun onSectionToggle(headerId: String) {
            // Reset targetSection quando l'utente interagisce manualmente
            _targetSection.value = null

            _expandedSections.update { current ->
                val currentSet = current ?: getAllHeaderIds()
                if (currentSet.contains(headerId)) {
                    currentSet - headerId
                } else {
                    currentSet + headerId
                }
            }
        }

        /**
         * Restituisce tutti gli header ID possibili per lo stato iniziale (tutti espansi).
         */
        private fun getAllHeaderIds(): Set<String> =
            uiState.value.items
                .filterIsInstance<com.ndumas.appdt.presentation.home.model.DashboardItem.SectionHeader>()
                .map { it.id }
                .toSet()

        fun onDeviceClicked(device: Device) {
            when (device.type) {
                DeviceType.LIGHT,
                DeviceType.SWITCH,
                DeviceType.FAN,
                -> {
                    toggleDevice(device)
                }

                else -> {
                    navigateToControl(device)
                }
            }
        }

        fun onDeviceLongClicked(device: Device) {
            navigateToControl(device)
        }

        private fun toggleDevice(device: Device) {
            viewModelScope.launch {
                val service = if (device.isOn) "turn_off" else "turn_on"

                callServiceUseCase(device.id, service).collect { result ->
                    if (result is Result.Error) {
                    }
                }
            }
        }

        private fun navigateToControl(device: Device) {
            viewModelScope.launch {
                _uiEvent.send(DeviceListUiEvent.NavigateToControl(device.id, device.room))
            }
        }
    }
