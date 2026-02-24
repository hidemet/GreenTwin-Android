package com.ndumas.appdt.presentation.automation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.core.util.asUiText
import com.ndumas.appdt.domain.automation.model.Automation
import com.ndumas.appdt.domain.automation.model.AutomationAction
import com.ndumas.appdt.domain.automation.model.AutomationTrigger
import com.ndumas.appdt.domain.automation.model.SolarEvent
import com.ndumas.appdt.domain.automation.usecase.GetAutomationsUseCase
import com.ndumas.appdt.domain.automation.usecase.ToggleAutomationUseCase
import com.ndumas.appdt.presentation.home.model.DashboardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AutomationListViewModel
    @Inject
    constructor(
        private val getAutomationsUseCase: GetAutomationsUseCase,
        private val toggleAutomationUseCase: ToggleAutomationUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AutomationListUiState(isLoading = true))
        val uiState = _uiState.asStateFlow()

        private val _uiEvent = Channel<AutomationListUiEvent>()
        val uiEvent = _uiEvent.receiveAsFlow()

        private var automationsJob: Job? = null

        init {
            observeAutomations()
        }

        fun onRefresh() {
            // Per il refresh, ri-osserviamo le automazioni
            observeAutomations()
        }

        /**
         * Osserva le automazioni in modo continuo.
         * Cancella job precedenti per evitare duplicazioni.
         */
        private fun observeAutomations() {
            automationsJob?.cancel()
            automationsJob =
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true, error = null) }

                    // Usa first() per ottenere il valore corrente e fermarsi
                    val result = getAutomationsUseCase().first()

                    when (result) {
                        is Result.Success -> {
                            val items =
                                result.data.map { automation ->
                                    DashboardItem.AutomationWidget(
                                        id = automation.id,
                                        name = automation.name,
                                        description = automation.description.ifBlank { generateSummary(automation) },
                                        isActive = automation.isActive,
                                    )
                                }
                            _uiState.update { it.copy(isLoading = false, items = items) }
                        }

                        is Result.Error -> {
                            _uiState.update { it.copy(isLoading = false, error = result.error.asUiText()) }
                        }
                    }

                    // Dopo il caricamento iniziale, sottoscriviamo per aggiornamenti live
                    subscribeToAutomationUpdates()
                }
        }

        /**
         * Sottoscrive agli aggiornamenti live delle automazioni (per toggle, ecc.)
         */
        private fun subscribeToAutomationUpdates() {
            viewModelScope.launch {
                getAutomationsUseCase().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val items =
                                result.data.map { automation ->
                                    DashboardItem.AutomationWidget(
                                        id = automation.id,
                                        name = automation.name,
                                        description = automation.description.ifBlank { generateSummary(automation) },
                                        isActive = automation.isActive,
                                    )
                                }
                            _uiState.update { it.copy(items = items) }
                        }

                        is Result.Error -> {
                            // Non mostrare errori durante gli aggiornamenti live
                        }
                    }
                }
            }
        }

        /**
         * Toggle attivazione/disattivazione di un'automazione.
         */
        fun onAutomationToggle(
            automationId: String,
            isActive: Boolean,
        ) {
            viewModelScope.launch {
                toggleAutomationUseCase(automationId, isActive).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val message =
                                if (isActive) {
                                    UiText.DynamicString("Automazione attivata")
                                } else {
                                    UiText.DynamicString("Automazione disattivata")
                                }
                            _uiEvent.send(AutomationListUiEvent.ShowToast(message))
                        }

                        is Result.Error -> {
                            _uiEvent.send(AutomationListUiEvent.ShowToast(result.error.asUiText()))
                        }
                    }
                }
            }
        }

        private fun generateSummary(automation: Automation): String {
            val triggerText =
                automation.triggers.firstOrNull()?.let { trigger ->
                    when (trigger) {
                        is AutomationTrigger.Time -> "Alle ${trigger.time.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                        is AutomationTrigger.Solar -> if (trigger.event == SolarEvent.SUNRISE) "All'alba" else "Al tramonto"
                        is AutomationTrigger.DeviceState -> "Quando ${trigger.deviceName.ifBlank { "dispositivo" }} cambia"
                    }
                } ?: "Manuale"

            val actionText =
                automation.actions.firstOrNull()?.let { action ->
                    when (action) {
                        is AutomationAction.DeviceAction -> {
                            val verb =
                                when {
                                    action.service.contains("turn_on") -> "Accendi"
                                    action.service.contains("turn_off") -> "Spegni"
                                    action.service.contains("toggle") -> "Cambia"
                                    else -> "Attiva"
                                }

                            val targetName = action.deviceName.ifBlank { action.deviceId.takeLast(5) }.lowercase()
                            "$verb $targetName"
                        }
                    }
                } ?: "Nessuna azione"

            val extra = if (automation.actions.size > 1) " (+${automation.actions.size - 1})" else ""

            return "$triggerText → $actionText$extra"
        }
    }

sealed interface AutomationListUiEvent {
    data class ShowToast(
        val message: UiText,
    ) : AutomationListUiEvent
}
