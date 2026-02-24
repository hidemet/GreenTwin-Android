package com.ndumas.appdt.domain.automation.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.automation.model.AutomationDraft
import com.ndumas.appdt.domain.automation.repository.AutomationRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class UpdateAutomationUseCase
    @Inject
    constructor(
        private val repository: AutomationRepository,
    ) {
        operator fun invoke(
            id: String,
            draft: AutomationDraft,
        ): Flow<Result<Unit, DataError>> {
            // Validazione
            if (draft.name.isBlank()) {
                return flowOf(Result.Error(DataError.Validation.EMPTY_NAME))
            }
            if (draft.trigger == null) {
                return flowOf(Result.Error(DataError.Validation.MISSING_TRIGGER))
            }
            if (draft.actions.isEmpty()) {
                return flowOf(Result.Error(DataError.Validation.NO_ACTIONS))
            }

            return repository.updateAutomation(id, draft)
        }
    }
