package com.ndumas.appdt.data.consumption.remote.source

import com.ndumas.appdt.data.consumption.remote.dto.ConsumptionDto
import com.ndumas.appdt.data.consumption.remote.dto.PredictionResponseDto

interface ConsumptionRemoteDataSource {
    suspend fun getTotalConsumption(
        startDate: String,
        endDate: String,
        group: String,
    ): List<ConsumptionDto>

    suspend fun getEntitiesConsumption(
        entityIds: String,
        startDate: String,
        endDate: String,
        group: String,
    ): List<ConsumptionDto>

    suspend fun getDailyPrediction(): PredictionResponseDto
}
