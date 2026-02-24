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
                delay(100)

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
                delay(100)

                val dummyAut = Automation("temp_id", draft.name, "", true, emptyList(), emptyList())
                val threshold = 2000.0 // 2 kW - soglia per test usabilità

                // SCENARIO 1: Automazione Non Fattibile - troppi dispositivi energivori insieme
                val totalPower = calculateTotalPower(draft.actions)

                if (totalPower >= threshold) {
                    // Automazione singola che supera la soglia
                    val deviceNames = getDeviceNames(draft.actions)
                    val devicesText =
                        if (deviceNames.size > 1) {
                            deviceNames.joinToString(" e ")
                        } else {
                            deviceNames.firstOrNull() ?: "i dispositivi selezionati"
                        }

                    val conflict =
                        AutomationConflict(
                            type = "NOT_FEASIBLE_AUTOMATION",
                            description =
                                "La potenza richiesta dall'automazione (${totalPower.toInt()} W) supera " +
                                    "la potenza massima disponibile (${threshold.toInt()} W).",
                            threshold = threshold,
                        )
                    emit(Result.Success(SimulationResult(dummyAut, listOf(conflict), emptyList())))
                    return@flow
                }

                // SCENARIO 2: Conflitto con Automazioni Esistenti
                // Simula conflitto se l'automazione si attiva in orari critici
                val trigger = draft.trigger
                if (trigger is AutomationTrigger.Time) {
                    val conflictingTime = isConflictingTime(trigger.time)
                    if (conflictingTime != null) {
                        val deviceName = getFirstDeviceName(draft.actions)
                        val conflict =
                            AutomationConflict(
                                type = "EXCESSIVE_ENERGY",
                                description =
                                    "La potenza richiesta dal sistema supererà il valore massimo di ${threshold.toInt()} W " +
                                        "dalle ${conflictingTime.first} alle ${(trigger.time.hour + 1).toString().padStart(2, '0')}:00. " +
                                        "L'automazione '${conflictingTime.second}' (${conflictingTime.third}) è già attiva in questo orario.",
                                threshold = threshold,
                            )
                        emit(Result.Success(SimulationResult(dummyAut, listOf(conflict), emptyList())))
                        return@flow
                    }
                }

                // Nessun conflitto
                emit(Result.Success(SimulationResult(dummyAut, emptyList(), emptyList())))
            }

        /**
         * Calcola la potenza totale stimata delle azioni.
         * Valori realistici basati su elettrodomestici comuni in Italia.
         */
        private fun calculateTotalPower(actions: List<AutomationAction>): Double {
            var total = 0.0
            actions.forEach { action ->
                if (action is AutomationAction.DeviceAction) {
                    val name = action.deviceName.lowercase()
                    total +=
                        when {
                            name.contains("forno") -> 2200.0
                            name.contains("lavatrice") -> 2000.0
                            name.contains("lavastoviglie") -> 1800.0
                            name.contains("phon") || name.contains("asciugacapelli") -> 1500.0
                            name.contains("ferro") && name.contains("stiro") -> 1400.0
                            name.contains("microonde") -> 1200.0
                            name.contains("caffè") || name.contains("macchina") -> 1000.0
                            name.contains("tv") -> 100.0
                            name.contains("luce") || name.contains("lampada") -> 10.0
                            else -> 50.0 // Default per dispositivi sconosciuti
                        }
                }
            }
            return total
        }

        /**
         * Estrae i nomi dei dispositivi dalle azioni
         */
        private fun getDeviceNames(actions: List<AutomationAction>): List<String> =
            actions.mapNotNull { action ->
                if (action is AutomationAction.DeviceAction) {
                    action.deviceName.ifBlank { null }
                } else {
                    null
                }
            }

        /**
         * Ottiene il nome del primo dispositivo
         */
        private fun getFirstDeviceName(actions: List<AutomationAction>): String {
            val names = getDeviceNames(actions)
            return names.firstOrNull() ?: "il dispositivo"
        }

        /**
         * Verifica se l'orario è in conflitto con automazioni esistenti mock.
         * Simula automazioni pre-esistenti in orari critici della giornata.
         *
         * @return Triple(orario, nome_automazione, dispositivi_attivi) o null se nessun conflitto
         */
        private fun isConflictingTime(time: LocalTime): Triple<String, String, String>? {
            // Simula automazioni pre-esistenti in orari critici
            val hour = time.hour
            return when {
                hour == 19 -> Triple("19:00", "Bucato", "Lavatrice")
                hour == 7 -> Triple("07:00", "Buongiorno", "Macchina Caffè")
                hour == 21 -> Triple("21:00", "Serata", "TV e Luce Soggiorno")
                else -> null
            }
        }

        override fun getAutomationById(id: String): Flow<Result<Automation, DataError>> =
            flow {
                delay(50)
                val automation = _automations.value.find { it.id == id }
                if (automation != null) {
                    emit(Result.Success(automation))
                } else {
                    emit(Result.Error(DataError.Validation.NOT_FOUND))
                }
            }

        override fun updateAutomation(
            id: String,
            draft: AutomationDraft,
        ): Flow<Result<Unit, DataError>> =
            flow {
                delay(100)
                val index = _automations.value.indexOfFirst { it.id == id }
                if (index == -1) {
                    emit(Result.Error(DataError.Validation.NOT_FOUND))
                    return@flow
                }

                val updatedAutomation =
                    Automation(
                        id = id,
                        name = draft.name,
                        description = draft.description,
                        isActive = draft.isActive,
                        triggers = listOfNotNull(draft.trigger),
                        actions = draft.actions,
                    )

                _automations.update { currentList ->
                    currentList.toMutableList().apply {
                        this[index] = updatedAutomation
                    }
                }

                emit(Result.Success(Unit))
            }

        override fun deleteAutomation(id: String): Flow<Result<Unit, DataError>> =
            flow {
                delay(100)
                val exists = _automations.value.any { it.id == id }
                if (!exists) {
                    emit(Result.Error(DataError.Validation.NOT_FOUND))
                    return@flow
                }

                _automations.update { currentList ->
                    currentList.filter { it.id != id }
                }

                emit(Result.Success(Unit))
            }

        override fun toggleAutomationActive(
            id: String,
            isActive: Boolean,
        ): Flow<Result<Unit, DataError>> =
            flow {
                delay(50)
                val index = _automations.value.indexOfFirst { it.id == id }
                if (index == -1) {
                    emit(Result.Error(DataError.Validation.NOT_FOUND))
                    return@flow
                }

                _automations.update { currentList ->
                    currentList.toMutableList().apply {
                        this[index] = this[index].copy(isActive = isActive)
                    }
                }

                emit(Result.Success(Unit))
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
                    actions =
                        listOf(
                            AutomationAction.DeviceAction("light_2", "Luce Salotto", "turn_off", "light"),
                        ),
                ),
                // Automazione per test Compito 7 - Conflitto EXCESSIVE_ENERGY
                Automation(
                    id = "mock_bucato",
                    name = "Bucato",
                    description = "Avvia la lavatrice alle 19:00",
                    isActive = true,
                    triggers =
                        listOf(
                            AutomationTrigger.Time(LocalTime.of(19, 0), emptyList()),
                        ),
                    actions =
                        listOf(
                            AutomationAction.DeviceAction("switch_bagno_lavatrice", "Lavatrice", "turn_on", "switch"),
                        ),
                ),
            )
    }
