package com.ndumas.appdt.domain.automation.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.automation.model.Automation
import com.ndumas.appdt.domain.automation.repository.AutomationRepository
import com.ndumas.appdt.domain.device.repository.DashboardPreferencesRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetAvailableAutomationsUseCase
    @Inject
    constructor(
        private val automationRepository: AutomationRepository,
        private val preferencesRepository: DashboardPreferencesRepository,
    ) {
        operator fun invoke(): Flow<Result<List<Automation>, DataError>> =
            combine(
                automationRepository.getAutomations(),
                preferencesRepository.getDashboardOrder(),
            ) { automationsResult, savedIds ->
                when (automationsResult) {
                    is Result.Success -> {
                        val allAutomations = automationsResult.data
                        // Filtra: tieni solo le automazioni il cui ID NON è nella lista salvata
                        val availableAutomations =
                            allAutomations.filter { automation ->
                                automation.id !in savedIds
                            }
                        Result.Success(availableAutomations)
                    }

                    is Result.Error -> {
                        Result.Error(automationsResult.error)
                    }
                }
            }
    }
