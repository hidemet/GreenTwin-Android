package com.ndumas.appdt.data.service.remote.source

import com.ndumas.appdt.data.service.remote.dto.ServiceRequestDto

interface ServiceRemoteDataSource {
    suspend fun callService(request: ServiceRequestDto)
}
