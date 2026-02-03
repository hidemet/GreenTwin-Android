package com.ndumas.appdt.domain.consumption.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.consumption.model.Consumption
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdown
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdownType
import com.ndumas.appdt.domain.consumption.model.ConsumptionGranularity
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ConsumptionRepository {
    fun getDailyConsumption(
        startDate: LocalDate,
        endDate: LocalDate,
        granularity: ConsumptionGranularity,
    ): Flow<Result<List<Consumption>, DataError>>

    fun getConsumptionBreakdown(
        startDate: LocalDate,
        endDate: LocalDate,
        type: ConsumptionBreakdownType,
    ): Flow<Result<List<ConsumptionBreakdown>, DataError>>
}
