package com.ndumas.appdt.domain.consumption.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.consumption.model.Consumption
import com.ndumas.appdt.domain.consumption.model.ConsumptionGranularity
import com.ndumas.appdt.domain.consumption.repository.ConsumptionRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetConsumptionHistoryUseCase
    @Inject
    constructor(
        private val repository: ConsumptionRepository,
    ) {
        operator fun invoke(
            start: LocalDate,
            end: LocalDate,
            granularity: ConsumptionGranularity,
        ): Flow<Result<List<Consumption>, DataError>> = repository.getDailyConsumption(start, end, granularity)
    }
