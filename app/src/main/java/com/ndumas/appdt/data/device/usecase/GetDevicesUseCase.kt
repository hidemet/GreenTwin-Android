package com.ndumas.appdt.domain.device.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.domain.device.repository.DeviceRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetDevicesUseCase
    @Inject
    constructor(
        private val repository: DeviceRepository,
    ) {
        operator fun invoke(): Flow<Result<List<Device>, DataError>> =
            repository.getDevices().map { result ->
                if (result is Result.Success) {
                    // Applichiamo la logica di ordinamento:
                    // I device accesi (isOn = true)
                    // A parità di stato, ordiniamo per nome

                    val sortedList =
                        result.data.sortedWith(
                            compareByDescending<Device> { it.isOn }
                                .thenBy { it.name.lowercase() },
                        )
                    Result.Success(sortedList)
                } else {
                    result
                }
            }
    }
