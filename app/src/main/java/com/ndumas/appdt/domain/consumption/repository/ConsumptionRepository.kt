package com.ndumas.appdt.domain.consumption.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.consumption.model.Consumption
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdown
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdownType
import com.ndumas.appdt.domain.consumption.model.ConsumptionGranularity
import com.ndumas.appdt.domain.consumption.model.PredictionData
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

    fun getDailyPrediction(): Flow<Result<List<PredictionData>, DataError>>

    /**
     * Calcola la media giornaliera storica degli ultimi N giorni.
     * Se sono disponibili meno giorni, usa quelli disponibili.
     * Include weekend nel calcolo.
     *
     * @param days Numero di giorni da considerare (default 7)
     * @return Media giornaliera in kWh, o null se non ci sono dati
     */
    fun getHistoricalDailyAverage(days: Int = 7): Flow<Result<Double?, DataError>>
}
