package com.ndumas.appdt.domain.consumption.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdown
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdownType
import com.ndumas.appdt.domain.consumption.repository.ConsumptionRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetConsumptionBreakdownUseCase
    @Inject
    constructor(
        private val repository: ConsumptionRepository,
    ) {
        operator fun invoke(
            start: LocalDate,
            end: LocalDate,
            type: ConsumptionBreakdownType,
        ): Flow<Result<List<ConsumptionBreakdown>, DataError>> =
            repository.getConsumptionBreakdown(start, end, type).map { result ->
                when (result) {
                    is Result.Success -> {
                        val rawList = result.data

                        Result.Success(rawList)
                    }

                    is Result.Error -> {
                        result
                    }
                }
            }
    }
