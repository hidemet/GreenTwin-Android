package com.ndumas.appdt.domain.service.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.error.DataError
import com.ndumas.appdt.domain.service.model.ServiceRequest
import kotlinx.coroutines.flow.Flow

interface ServiceRepository {
    fun callService(request: ServiceRequest): Flow<Result<Unit, DataError>>
}
