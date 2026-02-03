package com.ndumas.appdt.domain.automation.model

import java.time.DayOfWeek
import java.time.LocalTime

sealed interface AutomationTrigger {
    data class Time(
        val time: LocalTime,
        val days: List<DayOfWeek> = emptyList(),
    ) : AutomationTrigger

    data class Solar(
        val event: SolarEvent,
        val offsetMinutes: Long = 0,
        val days: List<DayOfWeek> = emptyList(),
    ) : AutomationTrigger

    data class DeviceState(
        val deviceId: String,
        val deviceName: String,
        val attribute: String,
        val operator: String,
        val value: String,
    ) : AutomationTrigger
}

enum class SolarEvent { SUNRISE, SUNSET }

sealed interface AutomationAction {
    data class DeviceAction(
        val deviceId: String,
        val deviceName: String,
        val service: String, // "turn_on"
        val domain: String, // "light"
        val parameters: Map<String, Any> = emptyMap(),
    ) : AutomationAction
}

data class AutomationDraft(
    val name: String = "",
    val description: String = "",
    val trigger: AutomationTrigger? = null,
    val actions: List<AutomationAction> = emptyList(),
    val isActive: Boolean = true,
) {
    val hasTriggerAndAction: Boolean
        get() = trigger != null && actions.isNotEmpty()

    val isValid: Boolean
        get() = name.isNotBlank() && hasTriggerAndAction
}
