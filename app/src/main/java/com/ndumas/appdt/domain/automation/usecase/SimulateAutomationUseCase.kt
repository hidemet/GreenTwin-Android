package com.ndumas.appdt.domain.automation.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.automation.model.AutomationDraft
import com.ndumas.appdt.domain.automation.model.SimulationResult
import com.ndumas.appdt.domain.automation.repository.AutomationRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SimulateAutomationUseCase
    @Inject
    constructor(
        private val repository: AutomationRepository,
    ) {
        operator fun invoke(draft: AutomationDraft): Flow<Result<SimulationResult, DataError>> {
            if (!draft.hasTriggerAndAction) {
                return flowOf(Result.Error(DataError.Validation.INVALID_DRAFT))
            }
            return repository.simulateAutomation(draft)
        }
    }
