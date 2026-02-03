package com.ndumas.appdt.domain.device.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.domain.device.model.DeviceDetail
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getDevices(): Flow<Result<List<Device>, DataError>>

    fun getDeviceDetail(deviceId: String): Flow<Result<DeviceDetail, DataError>>
}
