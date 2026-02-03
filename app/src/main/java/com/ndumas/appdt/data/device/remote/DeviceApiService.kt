package com.ndumas.appdt.data.device.remote

import com.ndumas.appdt.data.device.remote.dto.DeviceDto
import retrofit2.http.GET

interface DeviceApiService {
    // L'endpoint base è definito in Constants.BASE_URL
    @GET("device")
    suspend fun getDevices(): List<DeviceDto>
}
