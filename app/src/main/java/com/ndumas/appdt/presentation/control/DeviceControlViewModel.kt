package com.ndumas.appdt.presentation.control

import android.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.core.util.asUiText
import com.ndumas.appdt.domain.device.model.DeviceDetail
import com.ndumas.appdt.domain.device.usecase.GetDeviceDetailUseCase
import com.ndumas.appdt.domain.service.usecase.CallServiceUseCase
import com.ndumas.appdt.presentation.control.logic.DeviceActionResolver
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
import kotlin.math.roundToInt

@HiltViewModel
class DeviceControlViewModel
    @Inject
    constructor(
        private val getDeviceDetailUseCase: GetDeviceDetailUseCase,
        private val callServiceUseCase: CallServiceUseCase,
        private val actionResolver: DeviceActionResolver,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val deviceId: String = checkNotNull(savedStateHandle["deviceId"])

        private val _uiState = MutableStateFlow<DeviceControlUiState>(DeviceControlUiState.Loading)
        val uiState = _uiState.asStateFlow()

        private val _uiEvent = Channel<DeviceControlUiEvent>()
        val uiEvent = _uiEvent.receiveAsFlow()

        private var actionJob: Job? = null

        init {
            loadData()
        }

        fun onEvent(event: DeviceControlEvent) {
            when (event) {
                DeviceControlEvent.OnRefresh -> loadData()
                DeviceControlEvent.OnBackClick -> viewModelScope.launch { _uiEvent.send(DeviceControlUiEvent.NavigateBack) }
                DeviceControlEvent.Toggle -> toggleDevice()
                is DeviceControlEvent.OnBrightnessChanged -> setBrightness(event.value)
                is DeviceControlEvent.OnColorSelected -> setColor(event.color)
                is DeviceControlEvent.OnTemperatureChanged -> setTemperature(event.value)
                DeviceControlEvent.OnPaletteClick -> viewModelScope.launch { _uiEvent.send(DeviceControlUiEvent.OpenPalette) }
                is DeviceControlEvent.OnPresetSelected -> setColor(event.color)
                DeviceControlEvent.OnPlayPauseClick -> togglePlayPause()
                DeviceControlEvent.OnSkipNextClick -> performAction("media_next_track")
                DeviceControlEvent.OnSkipPrevClick -> performAction("media_previous_track")
                is DeviceControlEvent.OnVolumeChanged -> setVolume(event.value)
            }
        }

        private fun loadData() {
            viewModelScope.launch {
                _uiState.value = DeviceControlUiState.Loading
                getDeviceDetailUseCase(deviceId).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.value = mapDomainToUiState(result.data)
                        }

                        is Result.Error -> {
                            _uiState.value = DeviceControlUiState.Error(result.error.asUiText())
                        }
                    }
                }
            }
        }

        private fun mapDomainToUiState(detail: DeviceDetail): DeviceControlUiState =
            when (detail) {
                is DeviceDetail.Light -> {
                    val currentMode =
                        (_uiState.value as? DeviceControlUiState.LightControl)?.activeMode
                            ?: if (detail.supportsColor && !detail.supportsTemp) LightMode.COLOR_MODE else LightMode.WHITE_MODE

                    DeviceControlUiState.LightControl(
                        id = detail.id,
                        entityId = detail.entityId,
                        type = detail.type,
                        name = detail.name,
                        roomName = detail.room,
                        groupName = detail.group,
                        isOnline = detail.isOnline,
                        isOn = detail.isOn,
                        currentPowerW = detail.currentPowerW,
                        activeAutomations = 0,
                        brightness = detail.brightness,
                        colorTemp = detail.colorTemp ?: detail.minTemp,
                        rgbColor = detail.rgbColor ?: Color.WHITE,
                        minTemp = detail.minTemp,
                        maxTemp = detail.maxTemp,
                        supportsBrightness = detail.supportsBrightness,
                        supportsColor = detail.supportsColor,
                        supportsTemp = detail.supportsTemp,
                        activeMode = currentMode,
                    )
                }

                is DeviceDetail.MediaPlayer -> {
                    DeviceControlUiState.MediaControl(
                        id = detail.id,
                        entityId = detail.entityId,
                        type = detail.type,
                        name = detail.name,
                        roomName = detail.room,
                        groupName = detail.group,
                        isOnline = detail.isOnline,
                        isOn = detail.isOn,
                        currentPowerW = detail.currentPowerW,
                        activeAutomations = 0,
                        volume = detail.volume,
                        isPlaying = detail.isPlaying,
                        isMuted = detail.isMuted,
                        trackTitle = detail.trackTitle,
                        trackArtist = detail.trackArtist,
                    )
                }

                is DeviceDetail.Generic -> {
                    DeviceControlUiState.GenericControl(
                        id = detail.id,
                        entityId = detail.entityId,
                        type = detail.type,
                        name = detail.name,
                        roomName = detail.room,
                        groupName = detail.group,
                        isOnline = detail.isOnline,
                        isOn = detail.isOn,
                        currentPowerW = detail.currentPowerW,
                        activeAutomations = 2,
                    )
                }

                is DeviceDetail.Sensor -> {
                    DeviceControlUiState.SensorControl(
                        id = detail.id,
                        entityId = detail.entityId,
                        type = detail.type,
                        name = detail.name,
                        roomName = detail.room,
                        groupName = detail.group,
                        isOnline = detail.isOnline,
                        sensors = detail.sensorList,
                        activeAutomations = 1,
                    )
                }
            }

        private fun toggleDevice() {
            val state = _uiState.value

            val entityId = state.entityId
            val deviceType = state.type

            val isCurrentlyOn =
                when (state) {
                    is DeviceControlUiState.LightControl -> state.isOn
                    is DeviceControlUiState.GenericControl -> state.isOn
                    is DeviceControlUiState.MediaControl -> state.isOn
                    else -> return
                }

            if (entityId != null && deviceType != null) {
                val service = actionResolver.resolveToggleService(deviceType, isCurrentlyOn)

                updateStateIsOn(!isCurrentlyOn)

                performAction(service)
            }
        }

        private fun togglePlayPause() {
            val state = _uiState.value as? DeviceControlUiState.MediaControl ?: return

            _uiState.update { state.copy(isPlaying = !state.isPlaying) }

            val service = actionResolver.resolvePlayPauseService(state.isPlaying)

            performAction(service)
        }

        private fun setVolume(value: Float) {
            val volumeLevel = value / 100f

            performAction(
                service = "volume_set",
                params = mapOf("volume_level" to volumeLevel),
                debounceMs = 200,
            )
        }

        private fun setBrightness(value: Float) {
            val currentState = _uiState.value
            if (currentState is DeviceControlUiState.LightControl) {
                val intVal = value.roundToInt()
                _uiState.update { currentState.copy(brightness = intVal) }
                performAction("turn_on", mapOf("brightness_pct" to intVal), debounceMs = 500)
            }
        }

        private fun setColor(color: Int) {
            val currentState = _uiState.value
            if (currentState is DeviceControlUiState.LightControl) {
                _uiState.update { currentState.copy(rgbColor = color, activeMode = LightMode.COLOR_MODE) }
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)
                performAction("turn_on", mapOf("rgb_color" to listOf(r, g, b)), debounceMs = 500)
            }
        }

        private fun setTemperature(value: Float) {
            val currentState = _uiState.value
            if (currentState is DeviceControlUiState.LightControl) {
                val intVal = value.roundToInt()
                _uiState.update { currentState.copy(colorTemp = intVal, activeMode = LightMode.WHITE_MODE) }
                performAction("turn_on", mapOf("kelvin" to intVal), debounceMs = 500)
            }
        }

        private fun updateStateIsOn(isOn: Boolean) {
            _uiState.update { state ->
                when (state) {
                    is DeviceControlUiState.LightControl -> state.copy(isOn = isOn)
                    is DeviceControlUiState.GenericControl -> state.copy(isOn = isOn)
                    is DeviceControlUiState.MediaControl -> state.copy(isOn = isOn)
                    else -> state
                }
            }
        }

        private fun performAction(
            service: String,
            params: Map<String, Any>? = null,
            debounceMs: Long = 0,
        ) {
            actionJob?.cancel()

            actionJob =
                viewModelScope.launch {
                    if (debounceMs > 0) delay(debounceMs)

                    val targetId = _uiState.value.entityId

                    if (targetId != null) {
                        callServiceUseCase(targetId, service, params).collect { result ->
                            when (result) {
                                is Result.Success -> {
                                }

                                is Result.Error -> {
                                    _uiEvent.send(DeviceControlUiEvent.ShowError(result.error.asUiText()))
                                    loadData()
                                }
                            }
                        }
                    } else {
                        _uiEvent.send(
                            DeviceControlUiEvent.ShowError(
                                com.ndumas.appdt.core.ui.UiText
                                    .DynamicString("Impossibile eseguire: ID non trovato"),
                            ),
                        )
                        loadData()
                    }
                }
        }
    }
