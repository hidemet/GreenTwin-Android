package com.ndumas.appdt.data.automation.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.automation.model.Automation
import com.ndumas.appdt.domain.automation.model.AutomationAction
import com.ndumas.appdt.domain.automation.model.AutomationConflict
import com.ndumas.appdt.domain.automation.model.AutomationDraft
import com.ndumas.appdt.domain.automation.model.AutomationTrigger
import com.ndumas.appdt.domain.automation.model.SimulationResult
import com.ndumas.appdt.domain.automation.repository.AutomationRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAutomationRepository
    @Inject
    constructor() : AutomationRepository {
        private val _automations = MutableStateFlow<List<Automation>>(generateMockData())

        override fun getAutomations(): Flow<Result<List<Automation>, DataError>> =
            _automations.map { list ->
                Result.Success(list)
            }

        override fun createAutomation(draft: AutomationDraft): Flow<Result<Unit, DataError>> =
            flow {
                delay(1000)

                val newId = System.currentTimeMillis().toString()

                val newAutomation =
                    Automation(
                        id = newId,
                        name = draft.name,
                        description = draft.description,
                        isActive = draft.isActive,
                        triggers = listOfNotNull(draft.trigger),
                        actions = draft.actions,
                    )

                _automations.update { currentList ->
                    currentList + newAutomation
                }

                emit(Result.Success(Unit))
            }

        override fun simulateAutomation(draft: AutomationDraft): Flow<Result<SimulationResult, DataError>> =
            flow {
                delay(1000)

                val isHighPowerDevice =
                    draft.actions.any { action ->
                        if (action is AutomationAction.DeviceAction) {
                            val name = action.deviceName.lowercase()
                            name.contains("forno") || name.contains("lavatrice") || name.contains("phon")
                        } else {
                            false
                        }
                    }

                val hasConflict = draft.name.contains("conflitto", ignoreCase = true) || isHighPowerDevice

                val dummyAut = Automation("temp_id", draft.name, "", true, emptyList(), emptyList())

                if (hasConflict) {
                    val conflict =
                        AutomationConflict(
                            type = "OVERLOAD",
                            description = "Attenzione: L'attivazione simultanea supera la soglia di 3.0 kW.",
                            threshold = 3.0,
                        )
                    emit(Result.Success(SimulationResult(dummyAut, listOf(conflict), emptyList())))
                } else {
                    emit(Result.Success(SimulationResult(dummyAut, emptyList(), emptyList())))
                }
            }

        private fun generateMockData(): List<Automation> =
            listOf(
                Automation(
                    id = "mock_1",
                    name = "Buongiorno",
                    description = "Accende le luci alle 7:00",
                    isActive = true,
                    triggers =
                        listOf(
                            AutomationTrigger.Time(LocalTime.of(7, 0), emptyList()),
                        ),
                    actions =
                        listOf(
                            AutomationAction.DeviceAction("light_1", "Luce Cucina", "turn_on", "light"),
                        ),
                ),
                Automation(
                    id = "mock_2",
                    name = "Notte Fonda",
                    description = "Spegne tutto a mezzanotte",
                    isActive = false,
                    triggers =
                        listOf(
                            AutomationTrigger.Time(LocalTime.of(0, 0), emptyList()),
                        ),
                    actions = emptyList(),
                ),
            )
    }
