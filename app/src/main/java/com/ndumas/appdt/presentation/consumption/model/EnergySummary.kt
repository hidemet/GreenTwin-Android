package com.ndumas.appdt.domain.consumption.model

data class EnergySummary(
    val todayConsumptionKwh: Double,
    val yesterdayConsumptionKwh: Double,
    val trendPercentage: Double,
)
