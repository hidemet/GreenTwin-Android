package com.ndumas.appdt.domain.consumption.model

data class Consumption(
    val date: String,
    val energyKwh: Double,
    val cost: Double,
)
