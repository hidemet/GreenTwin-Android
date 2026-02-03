package com.ndumas.appdt.data.service.repository

import android.graphics.Color
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.data.device.repository.FakeDeviceRepository // CAMBIATO
import com.ndumas.appdt.domain.device.model.DeviceDetail
import com.ndumas.appdt.domain.error.DataError
import com.ndumas.appdt.domain.service.model.ServiceRequest
import com.ndumas.appdt.domain.service.repository.ServiceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeServiceRepository
    @Inject
    constructor(
        private val fakeDeviceRepository: FakeDeviceRepository,
    ) : ServiceRepository {
        override fun callService(request: ServiceRequest): Flow<Result<Unit, DataError>> =
            flow {
                delay(300)

                fakeDeviceRepository.updateDeviceState(request.entityId) { currentDevice ->
                    when (request.service) {
                        "turn_on" -> {
                            if (currentDevice is DeviceDetail.Light) {
                                var updated = currentDevice.copy(isOn = true)

                                // Aggiorna luminosità se presente
                                val bright = request.data?.get("brightness_pct") as? Int
                                if (bright != null) updated = updated.copy(brightness = bright)

                                // Aggiorna colore se presente
                                val rgb = (request.data?.get("rgb_color") as? List<*>)?.filterIsInstance<Int>()
                                if (rgb != null && rgb.size == 3) {
                                    val colorInt = Color.rgb(rgb[0], rgb[1], rgb[2])
                                    updated = updated.copy(rgbColor = colorInt)
                                }

                                // Aggiorna temperatura se presente (es. "kelvin")
                                val kelvin = request.data?.get("kelvin") as? Int
                                if (kelvin != null) updated = updated.copy(colorTemp = kelvin)

                                updated
                            } else {
                                // Generico
                                (currentDevice as? DeviceDetail.Generic)?.copy(isOn = true) ?: currentDevice
                            }
                        }

                        "turn_off" -> {
                            if (currentDevice is DeviceDetail.Light) {
                                currentDevice.copy(isOn = false)
                            } else {
                                (currentDevice as? DeviceDetail.Generic)?.copy(isOn = false) ?: currentDevice
                            }
                        }

                        else -> {
                            currentDevice
                        } // Ignora servizi sconosciuti
                    }
                }

                emit(Result.Success(Unit))
            }
    }
