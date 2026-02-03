package com.ndumas.appdt.presentation.automation

import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.presentation.home.model.DashboardItem

data class AutomationListUiState(
    val isLoading: Boolean = false,
    val items: List<DashboardItem.AutomationWidget> = emptyList(),
    val error: UiText? = null,
)
