package com.ndumas.appdt.data.service.remote

import com.ndumas.appdt.data.service.remote.dto.ServiceRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ServiceApiService {
    @POST("service")
    suspend fun callService(
        @Body request: ServiceRequestDto,
    ): Any
}
