package com.ndumas.appdt.domain.automation.model

data class SimulationResult(
    val automation: Automation,
    val conflicts: List<AutomationConflict>,
    val suggestions: List<AutomationSuggestion>,
) {
    val hasConflicts: Boolean
        get() = conflicts.isNotEmpty()
}

data class AutomationConflict(
    val type: String,
    val description: String,
    val threshold: Double?,
)

data class AutomationSuggestion(
    val type: String,
    val newTime: String?,
    val savings: Double?,
)
