package com.ndumas.appdt.presentation.consumption.model

import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.device.model.DeviceType

data class ConsumptionBreakdownUiModel(
    val id: String,
    val name: String,
    val valueText: UiText,
    val progress: Int,
    val type: DeviceType,
)
