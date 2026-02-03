package com.ndumas.appdt.presentation.device

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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel
    @Inject
    constructor(
        getDevicesUseCase: GetDevicesUseCase,
        private val callServiceUseCase: CallServiceUseCase,
        private val mapper: DeviceListUiMapper,
    ) : ViewModel() {
        private val _selectedTabIndex = MutableStateFlow(0)

        val uiState: StateFlow<DeviceListUiState> =
            combine(
                getDevicesUseCase(),
                _selectedTabIndex,
            ) { result, tabIndex ->

                val grouping =
                    when (tabIndex) {
                        0 -> DeviceGrouping.ROOM
                        1 -> DeviceGrouping.GROUP
                        2 -> DeviceGrouping.UNASSIGNED
                        else -> DeviceGrouping.ROOM
                    }

                when (result) {
                    is Result.Success -> {

                        val items = mapper.mapToDeviceList(result.data, grouping)
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

        fun onTabSelected(index: Int) {
            _selectedTabIndex.value = index
        }

        fun onRefresh() {
            _selectedTabIndex.value = _selectedTabIndex.value
        }

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
