package com.ndumas.appdt.domain.device.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.domain.device.repository.DashboardPreferencesRepository
import com.ndumas.appdt.domain.device.repository.DeviceRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetAvailableDevicesUseCase
    @Inject
    constructor(
        private val deviceRepository: DeviceRepository,
        private val preferencesRepository: DashboardPreferencesRepository,
    ) {
        operator fun invoke(): Flow<Result<List<Device>, DataError>> =
            combine(
                deviceRepository.getDevices(),
                preferencesRepository.getDashboardOrder(),
            ) { devicesResult, savedIds ->

                when (devicesResult) {
                    is Result.Success -> {
                        val allDevices = devicesResult.data
                        // FILTRO: Tengo solo quelli il cui ID NON è nella lista salvata
                        val availableDevices =
                            allDevices.filter { device ->
                                device.id !in savedIds
                            }
                        Result.Success(availableDevices)
                    }

                    is Result.Error -> {
                        Result.Error(devicesResult.error)
                    }
                }
            }
    }
