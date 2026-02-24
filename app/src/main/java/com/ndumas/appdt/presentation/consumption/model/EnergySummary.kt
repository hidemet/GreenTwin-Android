package com.ndumas.appdt.domain.consumption.model

import com.ndumas.appdt.presentation.consumption.model.PredictionState

data class EnergySummary(
    val todayConsumptionKwh: Double,
    val predictedConsumptionKwh: Double,
    val trendPercentage: Double,
    val trendState: PredictionState,
)
