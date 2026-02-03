package com.ndumas.appdt.domain.automation.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.automation.model.Automation
import com.ndumas.appdt.domain.automation.model.AutomationDraft
import com.ndumas.appdt.domain.automation.model.SimulationResult
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow

interface AutomationRepository {
    fun createAutomation(draft: AutomationDraft): Flow<Result<Unit, DataError>>

    fun getAutomations(): Flow<Result<List<Automation>, DataError>>

    fun simulateAutomation(draft: AutomationDraft): Flow<Result<SimulationResult, DataError>>
}
