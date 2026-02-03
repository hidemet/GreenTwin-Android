package com.ndumas.appdt.data.device.remote.source

import com.ndumas.appdt.data.device.remote.DeviceApiService
import com.ndumas.appdt.data.device.remote.dto.DeviceDto
import javax.inject.Inject

class DeviceRemoteDataSourceImpl
    @Inject
    constructor(
        private val apiService: DeviceApiService,
    ) : DeviceRemoteDataSource {
        override suspend fun getDevices(): List<DeviceDto> = apiService.getDevices()
    }
