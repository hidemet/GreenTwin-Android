package com.ndumas.appdt.data.service.remote.source

import com.ndumas.appdt.data.service.remote.ServiceApiService
import com.ndumas.appdt.data.service.remote.dto.ServiceRequestDto
import javax.inject.Inject

class ServiceRemoteDataSourceImpl
    @Inject
    constructor(
        private val api: ServiceApiService,
    ) : ServiceRemoteDataSource {
        override suspend fun callService(request: ServiceRequestDto) {
            api.callService(request)
        }
    }
