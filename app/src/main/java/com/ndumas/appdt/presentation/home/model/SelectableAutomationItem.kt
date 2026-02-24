package com.ndumas.appdt.presentation.home.model

import com.ndumas.appdt.domain.automation.model.Automation

data class SelectableAutomationItem(
    val automation: Automation,
    val isSelected: Boolean = false,
)
