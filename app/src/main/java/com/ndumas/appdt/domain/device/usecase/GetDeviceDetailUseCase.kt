package com.ndumas.appdt.domain.device.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.device.model.DeviceDetail
import com.ndumas.appdt.domain.device.repository.DeviceRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDeviceDetailUseCase
    @Inject
    constructor(
        private val repository: DeviceRepository,
    ) {
        operator fun invoke(deviceId: String): Flow<Result<DeviceDetail, DataError>> = repository.getDeviceDetail(deviceId)
    }
