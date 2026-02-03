package com.ndumas.appdt.domain.consumption.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.consumption.model.Consumption
import com.ndumas.appdt.domain.consumption.model.ConsumptionGranularity // <--- IMPORTANTE
import com.ndumas.appdt.domain.consumption.repository.ConsumptionRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetTodayConsumptionUseCase
    @Inject
    constructor(
        private val repository: ConsumptionRepository,
    ) {
        operator fun invoke(): Flow<Result<Consumption?, DataError>> {
            val today = LocalDate.now()

            return repository
                .getDailyConsumption(
                    startDate = today,
                    endDate = today,
                    granularity = ConsumptionGranularity.DAY,
                ).map { result ->
                    when (result) {
                        is Result.Success -> {
                            val todayData = result.data.firstOrNull()
                            Result.Success(todayData)
                        }

                        is Result.Error -> {
                            Result.Error(result.error)
                        }
                    }
                }
        }
    }
