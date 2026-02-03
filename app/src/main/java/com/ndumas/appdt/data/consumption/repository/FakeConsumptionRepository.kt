package com.ndumas.appdt.data.consumption.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.consumption.model.Consumption
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdown
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdownType
import com.ndumas.appdt.domain.consumption.model.ConsumptionGranularity
import com.ndumas.appdt.domain.consumption.repository.ConsumptionRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class FakeConsumptionRepository
    @Inject
    constructor() : ConsumptionRepository {
        override fun getDailyConsumption(
            startDate: LocalDate,
            endDate: LocalDate,
            granularity: ConsumptionGranularity,
        ): Flow<Result<List<Consumption>, DataError>> =
            flow {
                delay(300)
                val mockList = mutableListOf<Consumption>()
                val today = LocalDate.now()
                val now = LocalDateTime.now()

                when (granularity) {
                    ConsumptionGranularity.HOUR -> {
                        var time = startDate.atStartOfDay()
                        val limit = startDate.plusDays(1).atStartOfDay()
                        while (time.isBefore(limit)) {
                            val isFuture = time.isAfter(now)
                            val kwh = if (isFuture) 0.0 else (0.1 + Math.random() * 0.5)
                            mockList.add(Consumption(time.toString(), kwh, kwh * 0.22))
                            time = time.plusHours(1)
                        }
                    }

                    ConsumptionGranularity.DAY -> {
                        var current = startDate
                        while (!current.isAfter(endDate)) {
                            val isFuture = current.isAfter(today)
                            val kwh = if (isFuture) 0.0 else (2.0 + Math.random() * 8.0)
                            mockList.add(Consumption(current.toString(), kwh, kwh * 0.25))
                            current = current.plusDays(1)
                        }
                    }

                    ConsumptionGranularity.MONTH -> {
                        var current = startDate.withDayOfMonth(1)
                        val limit = endDate.withDayOfMonth(1)
                        while (!current.isAfter(limit)) {
                            val isFuture =
                                current.year > today.year || (current.year == today.year && current.monthValue > today.monthValue)
                            val kwh = if (isFuture) 0.0 else (120.0 + Math.random() * 60.0)
                            mockList.add(Consumption(current.toString(), kwh, kwh * 0.28))
                            current = current.plusMonths(1)
                        }
                    }
                }
                emit(Result.Success(mockList))
            }

        override fun getConsumptionBreakdown(
            startDate: LocalDate,
            endDate: LocalDate,
            type: ConsumptionBreakdownType,
        ): Flow<Result<List<ConsumptionBreakdown>, DataError>> =
            flow {
                delay(500)

                val mockData =
                    when (type) {
                        ConsumptionBreakdownType.DEVICE -> getFakeDevices()
                        ConsumptionBreakdownType.ROOM -> getFakeRooms()
                        ConsumptionBreakdownType.GROUP -> getFakeGroups()
                    }

                val total = mockData.sumOf { it.energyKwh }
                val finalData =
                    mockData
                        .map { item ->
                            val percent = if (total > 0) (item.energyKwh / total) * 100 else 0.0
                            item.copy(impactPercentage = percent)
                        }.sortedByDescending { it.energyKwh }

                emit(Result.Success(finalData))
            }

        private fun getFakeDevices(): List<ConsumptionBreakdown> =
            listOf(
                ConsumptionBreakdown("1", "Frigorifero", "refrigerator", 45.0, 0.0),
                ConsumptionBreakdown("2", "Aria Condizionata", "air_conditioner", 32.5, 0.0),
                ConsumptionBreakdown("3", "Lavatrice", "washing_machine", 18.2, 0.0),
                ConsumptionBreakdown("4", "Luci Sala", "light", 5.0, 0.0),
                ConsumptionBreakdown("5", "Luci Cucina", "light", 7.0, 0.0),
                ConsumptionBreakdown("6", "PC Studio", "desktop", 105.0, 0.0),
                ConsumptionBreakdown("7", "Forno", "oven", 12.0, 0.0),
            )

        private fun getFakeRooms(): List<ConsumptionBreakdown> =
            listOf(
                ConsumptionBreakdown("r1", "Studio", "room", 105.0, 0.0),
                ConsumptionBreakdown("r2", "Cucina", "room", 64.0, 0.0),
                ConsumptionBreakdown("r3", "Sala", "room", 37.5, 0.0),
                ConsumptionBreakdown("r4", "Bagno", "room", 18.2, 0.0),
            )

        private fun getFakeGroups(): List<ConsumptionBreakdown> =
            listOf(
                ConsumptionBreakdown("g1", "Intrattenimento", "group", 105.0, 0.0),
                ConsumptionBreakdown("g2", "Elettrodomestici", "group", 75.2, 0.0),
                ConsumptionBreakdown("g3", "Climatizzazione", "group", 32.5, 0.0),
                ConsumptionBreakdown("g4", "Illuminazione", "group", 12.0, 0.0),
            )
    }
