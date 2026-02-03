package com.ndumas.appdt.data.consumption.remote.source

import com.ndumas.appdt.data.consumption.remote.ConsumptionApiService
import com.ndumas.appdt.data.consumption.remote.dto.ConsumptionDto
import javax.inject.Inject

class ConsumptionRemoteDataSourceImpl
    @Inject
    constructor(
        private val api: ConsumptionApiService,
    ) : ConsumptionRemoteDataSource {
        override suspend fun getTotalConsumption(
            startDate: String,
            endDate: String,
            group: String,
        ): List<ConsumptionDto> = api.getTotalConsumption(startDate, endDate, group)

        override suspend fun getEntitiesConsumption(
            entityIds: String,
            startDate: String,
            endDate: String,
            group: String,
        ): List<ConsumptionDto> = api.getEntitiesConsumption(entityIds, startDate, endDate, group)
    }
