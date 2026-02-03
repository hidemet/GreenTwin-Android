package com.ndumas.appdt.data.device.repository

import android.graphics.Color
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.domain.device.model.DeviceDetail
import com.ndumas.appdt.domain.device.model.DeviceGroup
import com.ndumas.appdt.domain.device.model.DeviceType
import com.ndumas.appdt.domain.device.model.SensorAttribute
import com.ndumas.appdt.domain.device.model.SensorIconType
import com.ndumas.appdt.domain.device.repository.DeviceRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeDeviceRepository
    @Inject
    constructor() : DeviceRepository {
        private val _devicesFlow = MutableStateFlow<List<DeviceDetail>>(emptyList())

        init {
            preloadData()
        }

        override fun getDevices(): Flow<Result<List<Device>, DataError>> =
            _devicesFlow.map { details ->
                val devices =
                    details.map { detail ->

                        val groups =
                            if (detail.group != null) {
                                listOf(DeviceGroup(1, detail.group!!))
                            } else {
                                emptyList()
                            }

                        Device(
                            id = detail.id,
                            entityId = detail.entityId,
                            name = detail.name,
                            type = mapDetailToType(detail),
                            model = "Mock Model",
                            manufacturer = "Mock Factory",
                            isOn = detail.isOn,
                            currentPower = detail.currentPowerW ?: 0.0,
                            room = detail.room,
                            isOnline = detail.isOnline,
                            groups = groups,
                        )
                    }
                Result.Success(devices)
            }

        override fun getDeviceDetail(deviceId: String): Flow<Result<DeviceDetail, DataError>> =
            _devicesFlow.map { list ->
                val device = list.find { it.id == deviceId }
                if (device != null) {
                    Result.Success(device)
                } else {
                    Result.Error(DataError.Auth.USER_NOT_FOUND)
                }
            }

        fun updateDeviceState(
            id: String,
            mutator: (DeviceDetail) -> DeviceDetail,
        ) {
            val currentList = _devicesFlow.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == id }

            if (index != -1) {
                val currentItem = currentList[index]
                val updatedItem = mutator(currentItem)
                currentList[index] = updatedItem

                _devicesFlow.value = currentList
            }
        }

        private fun preloadData() {
            val list = mutableListOf<DeviceDetail>()

            list.add(
                DeviceDetail.Light(
                    id = "uuid_fake_1",
                    entityId = "light.rgb_strip_sala",
                    type = DeviceType.LIGHT,
                    name = "Striscia LED Sala",
                    isOn = true,
                    isOnline = true,
                    room = "Sala",
                    group = "Luci Ambiente",
                    currentPowerW = 12.5,
                    brightness = 80,
                    colorTemp = 4000,
                    rgbColor = Color.MAGENTA,
                    minTemp = 2000,
                    maxTemp = 6500,
                    supportsBrightness = true,
                    supportsColor = true,
                    supportsTemp = true,
                ),
            )
            list.add(
                DeviceDetail.Light(
                    id = "uuid_fake_2",
                    entityId = "light.white_bulb_cucina",
                    type = DeviceType.LIGHT,
                    name = "Lampada Lettura",
                    isOn = true,
                    isOnline = true,
                    room = "Cucina",
                    group = "Luci",
                    currentPowerW = 8.0,
                    brightness = 100,
                    colorTemp = 3000,
                    rgbColor = null,
                    minTemp = 2700,
                    maxTemp = 6500,
                    supportsBrightness = false,
                    supportsColor = false,
                    supportsTemp = false,
                ),
            )

            list.add(
                DeviceDetail.MediaPlayer(
                    id = "uuid_fake_3",
                    entityId = "media_player.tv_sala",
                    type = DeviceType.MEDIA_PLAYER,
                    name = "TV Sala",
                    isOn = true,
                    isOnline = true,
                    room = "Sala",
                    group = "Intrattenimento",
                    currentPowerW = 120.0,
                    volume = 25,
                    isMuted = false,
                    isPlaying = true,
                    trackTitle = "Sanremo 2025",
                    trackArtist = "Rai 1",
                ),
            )

            list.add(
                DeviceDetail.Generic(
                    id = "uuid_fake_4",
                    entityId = "switch.smart_plug_forno",
                    type = DeviceType.SWITCH,
                    name = "Presa Forno",
                    isOn = true,
                    isOnline = true,
                    room = "Cucina",
                    group = "Elettrodomestici",
                    currentPowerW = 2500.0,
                ),
            )

            list.add(
                DeviceDetail.Sensor(
                    id = "uuid_fake_5",
                    entityId = "sensor.temp_hum_cucina",
                    type = DeviceType.SENSOR,
                    name = "Sensore H&T cucina",
                    isOnline = true,
                    room = "Cucina",
                    group = null,
                    activeAutomations = 1,
                    sensorList =
                        listOf(
                            SensorAttribute("Temperatura", "17.6", "°C", SensorIconType.TEMPERATURE),
                            SensorAttribute("Umidità", "60.5", "%", SensorIconType.HUMIDITY),
                            SensorAttribute("Batteria", "81", "%", SensorIconType.BATTERY),
                        ),
                ),
            )

            list.add(
                DeviceDetail.MediaPlayer(
                    id = "uuid_fake_6",
                    entityId = "media_player.speaker_sala",
                    type = DeviceType.MEDIA_PLAYER,
                    name = "Speaker Sala",
                    isOn = true,
                    isOnline = true,
                    room = "Sala",
                    group = "Intrattenimento",
                    currentPowerW = 15.0,
                    volume = 65,
                    isMuted = false,
                    isPlaying = true,
                    trackTitle = "Bohemian Rhapsody",
                    trackArtist = "Queen",
                ),
            )

            list.add(
                DeviceDetail.MediaPlayer(
                    id = "uuid_fake_7",
                    entityId = "media_player.tv_camera",
                    type = DeviceType.MEDIA_PLAYER,
                    name = "TV Camera",
                    isOn = true,
                    isOnline = true,
                    room = null,
                    group = "Intrattenimento",
                    currentPowerW = 80.0,
                    volume = 20,
                    isMuted = true,
                    isPlaying = false,
                    trackTitle = "Netflix",
                    trackArtist = null,
                ),
            )

            _devicesFlow.value = list
        }

        private fun mapDetailToType(detail: DeviceDetail): DeviceType =
            when (detail) {
                is DeviceDetail.Light -> DeviceType.LIGHT
                is DeviceDetail.Generic -> DeviceType.SWITCH
                is DeviceDetail.Sensor -> DeviceType.SENSOR
                is DeviceDetail.MediaPlayer -> DeviceType.MEDIA_PLAYER
            }
    }
