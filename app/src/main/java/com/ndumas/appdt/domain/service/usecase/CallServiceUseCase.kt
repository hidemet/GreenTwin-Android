package com.ndumas.appdt.domain.service.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.error.DataError
import com.ndumas.appdt.domain.service.model.ServiceRequest
import com.ndumas.appdt.domain.service.repository.ServiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CallServiceUseCase
    @Inject
    constructor(
        private val repository: ServiceRepository,
    ) {
        operator fun invoke(
            entityId: String,
            service: String,
            params: Map<String, Any>? = null,
        ): Flow<Result<Unit, DataError>> {
            val request =
                ServiceRequest(
                    entityId = entityId,
                    service = service,
                    data = params,
                )

            return repository.callService(request)
        }
    }
