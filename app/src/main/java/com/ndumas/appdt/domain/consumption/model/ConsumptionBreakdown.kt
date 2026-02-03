package com.ndumas.appdt.domain.consumption.model

data class ConsumptionBreakdown(
    val id: String,
    val name: String,
    val deviceType: String,
    val energyKwh: Double,
    val impactPercentage: Double,
)
