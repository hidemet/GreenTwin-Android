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

            // ========== CUCINA (4 dispositivi) ==========
            list.add(
                DeviceDetail.Light(
                    id = "light_cucina_1",
                    entityId = "light.ceiling_cucina",
                    type = DeviceType.LIGHT,
                    name = "Luce Soffitto",
                    isOn = true,
                    isOnline = true,
                    room = "Cucina",
                    group = "Luci Piano Terra",
                    currentPowerW = 15.0,
                    brightness = 80,
                    colorTemp = 4000,
                    rgbColor = null,
                    minTemp = 2700,
                    maxTemp = 6500,
                    supportsBrightness = true,
                    supportsColor = false,
                    supportsTemp = true,
                ),
            )

            list.add(
                DeviceDetail.Light(
                    id = "light_cucina_2",
                    entityId = "light.under_cabinet_cucina",
                    type = DeviceType.LIGHT,
                    name = "LED Sottopensile",
                    isOn = false,
                    isOnline = true,
                    room = "Cucina",
                    group = "Luci Piano Terra",
                    currentPowerW = 0.0,
                    brightness = 60,
                    colorTemp = 3500,
                    rgbColor = null,
                    minTemp = 2700,
                    maxTemp = 6500,
                    supportsBrightness = true,
                    supportsColor = false,
                    supportsTemp = true,
                ),
            )

            list.add(
                DeviceDetail.Generic(
                    id = "switch_cucina_1",
                    entityId = "switch.coffee_machine",
                    type = DeviceType.SWITCH,
                    name = "Macchina Caffè",
                    isOn = true,
                    isOnline = true,
                    room = "Cucina",
                    group = "Elettrodomestici",
                    currentPowerW = 1200.0,
                ),
            )

            // Forno - Dispositivo energivoro per test conflitti
            list.add(
                DeviceDetail.Generic(
                    id = "switch_cucina_forno",
                    entityId = "switch.oven",
                    type = DeviceType.SWITCH,
                    name = "Forno",
                    isOn = false,
                    isOnline = true,
                    room = "Cucina",
                    group = "Elettrodomestici",
                    currentPowerW = 0.0,
                ),
            )

            // Lavastoviglie - Dispositivo energivoro per test conflitti
            list.add(
                DeviceDetail.Generic(
                    id = "switch_cucina_lavastoviglie",
                    entityId = "switch.dishwasher",
                    type = DeviceType.SWITCH,
                    name = "Lavastoviglie",
                    isOn = false,
                    isOnline = true,
                    room = "Cucina",
                    group = "Elettrodomestici",
                    currentPowerW = 0.0,
                ),
            )

            list.add(
                DeviceDetail.Sensor(
                    id = "sensor_cucina_1",
                    entityId = "sensor.temp_hum_cucina",
                    type = DeviceType.SENSOR,
                    name = "Sensore Temperatura",
                    isOnline = true,
                    room = "Cucina",
                    group = null,
                    activeAutomations = 1,
                    sensorList =
                        listOf(
                            SensorAttribute("Temperatura", "22.5", "°C", SensorIconType.TEMPERATURE),
                            SensorAttribute("Umidità", "55", "%", SensorIconType.HUMIDITY),
                        ),
                ),
            )

            // ========== SOGGIORNO (5 dispositivi) ==========
            list.add(
                DeviceDetail.Light(
                    id = "light_soggiorno_1",
                    entityId = "light.rgb_strip_tv",
                    type = DeviceType.LIGHT,
                    name = "Striscia LED TV",
                    isOn = true,
                    isOnline = true,
                    room = "Soggiorno",
                    group = "Luci Piano Terra",
                    currentPowerW = 12.0,
                    brightness = 70,
                    colorTemp = 4000,
                    rgbColor = Color.rgb(138, 43, 226), // Purple
                    minTemp = 2000,
                    maxTemp = 6500,
                    supportsBrightness = true,
                    supportsColor = true,
                    supportsTemp = true,
                ),
            )

            list.add(
                DeviceDetail.Light(
                    id = "light_soggiorno_2",
                    entityId = "light.floor_lamp",
                    type = DeviceType.LIGHT,
                    name = "Lampada da Terra",
                    isOn = true,
                    isOnline = true,
                    room = "Soggiorno",
                    group = "Luci Piano Terra",
                    currentPowerW = 10.0,
                    brightness = 100,
                    colorTemp = 3000,
                    rgbColor = null,
                    minTemp = 2700,
                    maxTemp = 6500,
                    supportsBrightness = true,
                    supportsColor = false,
                    supportsTemp = false,
                ),
            )

            list.add(
                DeviceDetail.MediaPlayer(
                    id = "media_soggiorno_1",
                    entityId = "media_player.tv_soggiorno",
                    type = DeviceType.MEDIA_PLAYER,
                    name = "Smart TV",
                    isOn = true,
                    isOnline = true,
                    room = "Soggiorno",
                    group = "Intrattenimento",
                    currentPowerW = 95.0,
                    volume = 35,
                    isMuted = false,
                    isPlaying = false, // Cambiato: parte in pausa (mostra play)
                    trackTitle = "So What",
                    trackArtist = "Miles Davis",
                ),
            )

            list.add(
                DeviceDetail.MediaPlayer(
                    id = "media_soggiorno_2",
                    entityId = "media_player.soundbar",
                    type = DeviceType.MEDIA_PLAYER,
                    name = "Soundbar",
                    isOn = true,
                    isOnline = true,
                    room = "Soggiorno",
                    group = "Intrattenimento",
                    currentPowerW = 25.0,
                    volume = 45,
                    isMuted = false,
                    isPlaying = false, // Cambiato: parte in pausa (mostra play)
                    trackTitle = "Kind of Blue",
                    trackArtist = "Miles Davis",
                ),
            )

            list.add(
                DeviceDetail.Generic(
                    id = "switch_soggiorno_1",
                    entityId = "switch.fan",
                    type = DeviceType.SWITCH,
                    name = "Ventilatore",
                    isOn = false,
                    isOnline = true,
                    room = "Soggiorno",
                    group = null,
                    currentPowerW = 0.0,
                ),
            )

            // ========== CAMERA DA LETTO (3 dispositivi) ==========
            list.add(
                DeviceDetail.Light(
                    id = "light_camera_1",
                    entityId = "light.ceiling_camera",
                    type = DeviceType.LIGHT,
                    name = "Luce Principale",
                    isOn = false,
                    isOnline = true,
                    room = "Camera da Letto",
                    group = "Luci Piano Notte",
                    currentPowerW = 0.0,
                    brightness = 100,
                    colorTemp = 3200,
                    rgbColor = null,
                    minTemp = 2700,
                    maxTemp = 6500,
                    supportsBrightness = true,
                    supportsColor = false,
                    supportsTemp = true,
                ),
            )

            list.add(
                DeviceDetail.Light(
                    id = "light_camera_2",
                    entityId = "light.bedside_lamp",
                    type = DeviceType.LIGHT,
                    name = "Lampada Comodino",
                    isOn = true,
                    isOnline = true,
                    room = "Camera da Letto",
                    group = "Luci Piano Notte",
                    currentPowerW = 5.0,
                    brightness = 30,
                    colorTemp = 2700,
                    rgbColor = Color.rgb(255, 147, 41), // Warm white
                    minTemp = 2000,
                    maxTemp = 5000,
                    supportsBrightness = true,
                    supportsColor = true,
                    supportsTemp = true,
                ),
            )

            list.add(
                DeviceDetail.Sensor(
                    id = "sensor_camera_1",
                    entityId = "sensor.bedroom_climate",
                    type = DeviceType.SENSOR,
                    name = "Sensore Camera",
                    isOnline = true,
                    room = "Camera da Letto",
                    group = null,
                    activeAutomations = 0,
                    sensorList =
                        listOf(
                            SensorAttribute("Temperatura", "19.8", "°C", SensorIconType.TEMPERATURE),
                            SensorAttribute("Umidità", "62", "%", SensorIconType.HUMIDITY),
                            SensorAttribute("Batteria", "95", "%", SensorIconType.BATTERY),
                        ),
                ),
            )

            // ========== BAGNO (2 dispositivi) ==========
            list.add(
                DeviceDetail.Light(
                    id = "light_bagno_1",
                    entityId = "light.mirror_light",
                    type = DeviceType.LIGHT,
                    name = "Luce Specchio",
                    isOn = true,
                    isOnline = true,
                    room = "Bagno",
                    group = "Luci Piano Notte",
                    currentPowerW = 8.0,
                    brightness = 100,
                    colorTemp = 5000,
                    rgbColor = null,
                    minTemp = 3000,
                    maxTemp = 6500,
                    supportsBrightness = true,
                    supportsColor = false,
                    supportsTemp = true,
                ),
            )

            // Lavatrice - Dispositivo energivoro per test conflitti
            list.add(
                DeviceDetail.Generic(
                    id = "switch_bagno_lavatrice",
                    entityId = "switch.washing_machine",
                    type = DeviceType.SWITCH,
                    name = "Lavatrice",
                    isOn = false,
                    isOnline = true,
                    room = "Bagno",
                    group = "Elettrodomestici",
                    currentPowerW = 0.0,
                ),
            )

            // ========== STUDIO (3 dispositivi) ==========
            list.add(
                DeviceDetail.Light(
                    id = "light_studio_1",
                    entityId = "light.desk_lamp",
                    type = DeviceType.LIGHT,
                    name = "Lampada Scrivania",
                    isOn = true,
                    isOnline = true,
                    room = "Studio",
                    group = null,
                    currentPowerW = 12.0,
                    brightness = 85,
                    colorTemp = 4500,
                    rgbColor = null,
                    minTemp = 2700,
                    maxTemp = 6500,
                    supportsBrightness = true,
                    supportsColor = false,
                    supportsTemp = true,
                ),
            )

            list.add(
                DeviceDetail.Generic(
                    id = "switch_studio_1",
                    entityId = "switch.printer",
                    type = DeviceType.SWITCH,
                    name = "Stampante",
                    isOn = false,
                    isOnline = true,
                    room = "Studio",
                    group = "Elettrodomestici",
                    currentPowerW = 0.0,
                ),
            )

            list.add(
                DeviceDetail.MediaPlayer(
                    id = "media_studio_1",
                    entityId = "media_player.speaker_studio",
                    type = DeviceType.MEDIA_PLAYER,
                    name = "Speaker Bluetooth",
                    isOn = false,
                    isOnline = true,
                    room = "Studio",
                    group = "Intrattenimento",
                    currentPowerW = 0.0,
                    volume = 50,
                    isMuted = false,
                    isPlaying = false,
                    trackTitle = null,
                    trackArtist = null,
                ),
            )

            // ========== INGRESSO (2 dispositivi) ==========
            list.add(
                DeviceDetail.Light(
                    id = "light_ingresso_1",
                    entityId = "light.entrance_ceiling",
                    type = DeviceType.LIGHT,
                    name = "Luce Ingresso",
                    isOn = true,
                    isOnline = true,
                    room = "Ingresso",
                    group = "Luci Piano Terra",
                    currentPowerW = 10.0,
                    brightness = 60,
                    colorTemp = 3500,
                    rgbColor = Color.rgb(255, 223, 186),
                    minTemp = 2700,
                    maxTemp = 6500,
                    supportsBrightness = true,
                    supportsColor = true,
                    supportsTemp = true,
                ),
            )

            list.add(
                DeviceDetail.Sensor(
                    id = "sensor_ingresso_1",
                    entityId = "sensor.door_contact",
                    type = DeviceType.SENSOR,
                    name = "Sensore Porta",
                    isOnline = true,
                    room = "Ingresso",
                    group = "Sicurezza",
                    activeAutomations = 2,
                    sensorList =
                        listOf(
                            SensorAttribute("Stato", "Chiusa", "", SensorIconType.GENERIC),
                            SensorAttribute("Batteria", "88", "%", SensorIconType.BATTERY),
                        ),
                ),
            )

            // ========== DISPOSITIVI NEI GRUPPI (senza stanza specifica) ==========
            list.add(
                DeviceDetail.Sensor(
                    id = "sensor_motion_1",
                    entityId = "sensor.motion_corridor",
                    type = DeviceType.SENSOR,
                    name = "Sensore Movimento",
                    isOnline = true,
                    room = null,
                    group = "Sicurezza",
                    activeAutomations = 1,
                    sensorList =
                        listOf(
                            SensorAttribute("Movimento", "No", "", SensorIconType.GENERIC),
                            SensorAttribute("Batteria", "72", "%", SensorIconType.BATTERY),
                        ),
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
