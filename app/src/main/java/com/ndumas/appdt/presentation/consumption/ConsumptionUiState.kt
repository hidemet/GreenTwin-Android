package com.ndumas.appdt.presentation.consumption

import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.consumption.model.Consumption
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdownType
import com.ndumas.appdt.presentation.consumption.model.ConsumptionBreakdownUiModel
import com.ndumas.appdt.presentation.consumption.model.PredictionUiModel
import java.time.LocalDate

data class ConsumptionUiState(
    val isLoading: Boolean = false,
    val isNextEnabled: Boolean = false,
    val selectedFilter: ConsumptionTimeFilter = ConsumptionTimeFilter.WEEK,
    val currentDate: LocalDate = LocalDate.now(),
    val consumptionData: List<Consumption> = emptyList(),
    val totalEnergy: Double = 0.0,
    val totalCost: Double = 0.0,
    val predictedEnergy: Double = 0.0,
    val selectedIndex: Int = -1,
    val formattedTotalEnergy: UiText = UiText.DynamicString(""),
    val formattedTotalCost: UiText = UiText.DynamicString(""),
    val formattedDateRange: UiText = UiText.DynamicString(""),
    val formattedPredictionLabel: UiText = UiText.DynamicString(""),
    val predictionModel: PredictionUiModel = PredictionUiModel.Hidden,
    val breakdownType: ConsumptionBreakdownType = ConsumptionBreakdownType.DEVICE,
    val formattedListHeader: UiText = UiText.DynamicString(""),
    val breakdownList: List<ConsumptionBreakdownUiModel> = emptyList(),
    val isSortAscending: Boolean = false,
    val error: UiText? = null,
)
