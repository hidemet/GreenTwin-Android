package com.ndumas.appdt.data.consumption.mapper

import com.ndumas.appdt.data.consumption.remote.dto.ConsumptionDto
import com.ndumas.appdt.domain.consumption.model.Consumption

// Richiede la data già normalizzata
fun ConsumptionDto.toDomain(normalizedDate: String): Consumption {
    val normalizedEnergy =
        if (this.energyConsumptionUnit.equals("Wh", ignoreCase = true)) {
            this.energyConsumption / 1000.0
        } else {
            this.energyConsumption
        }

    return Consumption(
        date = normalizedDate,
        energyKwh = normalizedEnergy,
        cost = this.cost ?: 0.0,
    )
}
