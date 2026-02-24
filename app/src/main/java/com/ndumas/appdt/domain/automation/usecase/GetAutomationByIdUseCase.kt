package com.ndumas.appdt.domain.automation.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.automation.model.Automation
import com.ndumas.appdt.domain.automation.repository.AutomationRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAutomationByIdUseCase
    @Inject
    constructor(
        private val repository: AutomationRepository,
    ) {
        operator fun invoke(id: String): Flow<Result<Automation, DataError>> = repository.getAutomationById(id)
    }
