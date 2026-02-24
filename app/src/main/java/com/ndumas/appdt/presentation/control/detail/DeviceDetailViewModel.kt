package com.ndumas.appdt.presentation.control.detail

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndumas.appdt.R
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.core.ui.device.DeviceUiStyle
import com.ndumas.appdt.core.ui.device.getUiStyle
import com.ndumas.appdt.domain.device.model.DeviceDetail
import com.ndumas.appdt.domain.device.model.DeviceType
import com.ndumas.appdt.domain.device.usecase.GetDeviceDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceDetailUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val deviceType: String = "",
    @DrawableRes val deviceIconRes: Int = R.drawable.ic_devices,
    @ColorRes val deviceColorRes: Int = R.color.tw_gray_400,
    val room: String? = null,
    val group: String? = null,
)

@HiltViewModel
class DeviceDetailViewModel
    @Inject
    constructor(
        private val getDeviceDetailUseCase: GetDeviceDetailUseCase,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val deviceId: String = checkNotNull(savedStateHandle["deviceId"])

        private val _uiState = MutableStateFlow(DeviceDetailUiState())
        val uiState: StateFlow<DeviceDetailUiState> = _uiState.asStateFlow()

        init {
            loadDeviceDetails()
        }

        private fun loadDeviceDetails() {
            viewModelScope.launch {
                getDeviceDetailUseCase(deviceId).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            val detail = result.data
                            val uiStyle = getDeviceUiStyle(detail)
                            _uiState.value =
                                DeviceDetailUiState(
                                    isLoading = false,
                                    name = detail.name,
                                    deviceType = mapDeviceTypeToString(detail),
                                    deviceIconRes = uiStyle.iconRes,
                                    deviceColorRes = uiStyle.activeColorRes,
                                    room = detail.room,
                                    group = detail.group,
                                )
                        }

                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
                }
            }
        }

        private fun getDeviceUiStyle(detail: DeviceDetail): DeviceUiStyle {
            val deviceType = detail.type ?: DeviceType.OTHER
            return deviceType.getUiStyle()
        }

        private fun mapDeviceTypeToString(detail: DeviceDetail): String =
            when (detail) {
                is DeviceDetail.Light -> {
                    "Luce"
                }

                is DeviceDetail.MediaPlayer -> {
                    "Media Player"
                }

                is DeviceDetail.Sensor -> {
                    "Sensore"
                }

                is DeviceDetail.Generic -> {
                    when (detail.type) {
                        DeviceType.SWITCH -> "Interruttore"
                        DeviceType.FAN -> "Ventilatore"
                        DeviceType.THERMOSTAT -> "Termostato"
                        DeviceType.AIR_CONDITIONER -> "Condizionatore"
                        DeviceType.LOCK -> "Serratura"
                        DeviceType.BLINDS -> "Tapparella"
                        DeviceType.TV -> "TV"
                        DeviceType.SPEAKER -> "Altoparlante"
                        DeviceType.CAMERA -> "Videocamera"
                        DeviceType.DOOR -> "Porta"
                        DeviceType.DOORBELL -> "Campanello"
                        DeviceType.WASHING_MACHINE -> "Lavatrice"
                        DeviceType.REFRIGERATOR -> "Frigorifero"
                        DeviceType.DISHWASHER -> "Lavastoviglie"
                        DeviceType.OVEN -> "Forno"
                        DeviceType.MICROWAVE -> "Microonde"
                        DeviceType.WINDOW -> "Finestra"
                        DeviceType.DESKTOP -> "Computer"
                        else -> "Dispositivo generico"
                    }
                }
            }
    }
