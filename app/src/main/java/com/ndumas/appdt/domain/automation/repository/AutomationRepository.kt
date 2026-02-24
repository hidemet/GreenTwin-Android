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

    fun getAutomationById(id: String): Flow<Result<Automation, DataError>>

    fun updateAutomation(
        id: String,
        draft: AutomationDraft,
    ): Flow<Result<Unit, DataError>>

    fun deleteAutomation(id: String): Flow<Result<Unit, DataError>>

    fun toggleAutomationActive(
        id: String,
        isActive: Boolean,
    ): Flow<Result<Unit, DataError>>

    fun simulateAutomation(draft: AutomationDraft): Flow<Result<SimulationResult, DataError>>
}
