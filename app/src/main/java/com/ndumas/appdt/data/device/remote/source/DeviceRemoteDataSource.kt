package com.ndumas.appdt.data.device.remote.source

import com.ndumas.appdt.data.device.remote.dto.DeviceDto

interface DeviceRemoteDataSource {
    suspend fun getDevices(): List<DeviceDto>
}
