// filename: Automation.kt
package com.ndumas.appdt.domain.automation.model

data class Automation(
    val id: String,
    val name: String,
    val description: String,
    val isActive: Boolean,
    val triggers: List<AutomationTrigger>,
    val actions: List<AutomationAction>,
)
