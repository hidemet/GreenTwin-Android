package com.ndumas.appdt.domain.consumption.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.consumption.model.ConsumptionGranularity
import com.ndumas.appdt.domain.consumption.model.EnergySummary
import com.ndumas.appdt.domain.consumption.repository.ConsumptionRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetEnergySummaryUseCase
    @Inject
    constructor(
        private val repository: ConsumptionRepository,
    ) {
        operator fun invoke(): Flow<Result<EnergySummary, DataError>> {
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            return repository
                .getDailyConsumption(
                    startDate = yesterday,
                    endDate = today,
                    granularity = ConsumptionGranularity.DAY,
                ).map { result ->
                    when (result) {
                        is Result.Success -> {
                            val list = result.data

                            val todayData = list.find { it.date == today.toString() }
                            val yesterdayData = list.find { it.date == yesterday.toString() }

                            val todayKwh = todayData?.energyKwh ?: 0.0
                            val yesterdayKwh = yesterdayData?.energyKwh ?: 0.0

                            val trend =
                                if (yesterdayKwh > 0) {
                                    ((todayKwh - yesterdayKwh) / yesterdayKwh) * 100
                                } else {
                                    0.0
                                }

                            Result.Success(
                                EnergySummary(
                                    todayConsumptionKwh = todayKwh,
                                    yesterdayConsumptionKwh = yesterdayKwh,
                                    trendPercentage = trend,
                                ),
                            )
                        }

                        is Result.Error -> {
                            Result.Error(result.error)
                        }
                    }
                }
        }
    }
