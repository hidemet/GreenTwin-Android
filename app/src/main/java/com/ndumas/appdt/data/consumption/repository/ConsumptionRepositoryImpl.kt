package com.ndumas.appdt.data.consumption.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.data.consumption.mapper.BackendDateNormalizer
import com.ndumas.appdt.data.consumption.mapper.toDomain
import com.ndumas.appdt.data.consumption.remote.dto.ConsumptionDto
import com.ndumas.appdt.data.consumption.remote.source.ConsumptionRemoteDataSource
import com.ndumas.appdt.data.device.remote.DeviceApiService
import com.ndumas.appdt.data.device.remote.dto.DeviceDto
import com.ndumas.appdt.domain.consumption.model.Consumption
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdown
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdownType
import com.ndumas.appdt.domain.consumption.model.ConsumptionGranularity
import com.ndumas.appdt.domain.consumption.model.PredictionData
import com.ndumas.appdt.domain.consumption.repository.ConsumptionRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class ConsumptionRepositoryImpl
    @Inject
    constructor(
        private val dataSource: ConsumptionRemoteDataSource,
        private val deviceApi: DeviceApiService,
        private val dateNormalizer: BackendDateNormalizer,
    ) : ConsumptionRepository {
        private companion object {
            const val KWH_CONVERSION_FACTOR = 1000.0
            const val PERCENTAGE_FACTOR = 100
        }

        override fun getDailyConsumption(
            startDate: LocalDate,
            endDate: LocalDate,
            granularity: ConsumptionGranularity,
        ): Flow<Result<List<Consumption>, DataError>> =
            flow<Result<List<Consumption>, DataError>> {
                val groupParam =
                    when (granularity) {
                        ConsumptionGranularity.HOUR -> "hourly"
                        ConsumptionGranularity.DAY -> "daily"
                        ConsumptionGranularity.MONTH -> "monthly"
                    }

                val dtoList =
                    dataSource.getTotalConsumption(
                        startDate = startDate.toString(),
                        endDate = endDate.toString(),
                        group = groupParam,
                    )

                val rawDomainList =
                    dtoList.map { dto ->
                        val cleanDate = dateNormalizer.normalize(dto.date, granularity)
                        dto.toDomain(cleanDate)
                    }

                val completeList = fillGaps(rawDomainList, startDate, endDate, granularity)
                emit(Result.Success(completeList))
            }.catch { e ->
                if (e is CancellationException) throw e
                e.printStackTrace()
                emit(Result.Error(mapExceptionToDataError(e)))
            }

        override fun getConsumptionBreakdown(
            startDate: LocalDate,
            endDate: LocalDate,
            type: ConsumptionBreakdownType,
        ): Flow<Result<List<ConsumptionBreakdown>, DataError>> =
            flow<Result<List<ConsumptionBreakdown>, DataError>> {
                val allDevices = deviceApi.getDevices()
                val energyDeviceMap = mapEnergyEntityToDevice(allDevices)

                if (energyDeviceMap.isEmpty()) {
                    emit(Result.Success(emptyList()))
                    return@flow
                }

                val consumptionDtos =
                    dataSource.getEntitiesConsumption(
                        entityIds = energyDeviceMap.keys.joinToString(","),
                        startDate = startDate.toString(),
                        endDate = endDate.toString(),
                        group = "entity",
                    )

                val rawBreakdown =
                    when (type) {
                        ConsumptionBreakdownType.DEVICE -> aggregateByDevice(consumptionDtos, energyDeviceMap)
                        ConsumptionBreakdownType.ROOM -> aggregateByRoom(consumptionDtos, energyDeviceMap)
                        ConsumptionBreakdownType.GROUP -> aggregateByGroup(consumptionDtos, energyDeviceMap)
                    }

                val finalBreakdown = calculateImpactAndSort(rawBreakdown)
                emit(Result.Success(finalBreakdown))
            }.catch { e ->
                if (e is CancellationException) throw e
                e.printStackTrace()

                emit(Result.Success(emptyList()))
            }

        override fun getDailyPrediction(): Flow<Result<List<PredictionData>, DataError>> =
            flow<Result<List<PredictionData>, DataError>> {
                val response = dataSource.getDailyPrediction()
                val predictionList =
                    response.data.map { dto ->
                        PredictionData(
                            date = dto.date,
                            energyConsumptionKwh = normalizeToKwh(dto.energyConsumption, dto.energyConsumptionUnit),
                        )
                    }
                emit(Result.Success(predictionList))
            }.catch { e ->
                if (e is CancellationException) throw e
                e.printStackTrace()
                emit(Result.Error(mapExceptionToDataError(e)))
            }

        override fun getHistoricalDailyAverage(days: Int): Flow<Result<Double?, DataError>> =
            flow<Result<Double?, DataError>> {
                val today = LocalDate.now()
                val startDate = today.minusDays(days.toLong())
                // Escludiamo oggi per avere solo giorni completamente passati
                val endDate = today.minusDays(1)

                if (startDate.isAfter(endDate)) {
                    emit(Result.Success(null))
                    return@flow
                }

                val dtoList =
                    dataSource.getTotalConsumption(
                        startDate = startDate.toString(),
                        endDate = endDate.toString(),
                        group = "daily",
                    )

                if (dtoList.isEmpty()) {
                    emit(Result.Success(null))
                    return@flow
                }

                // Calcola la media dei consumi giornalieri disponibili
                val dailyValues =
                    dtoList
                        .map { dto ->
                            normalizeToKwh(dto.energyConsumption, dto.energyConsumptionUnit)
                        }.filter { it > 0 } // Esclude giorni senza dati

                val average =
                    if (dailyValues.isNotEmpty()) {
                        dailyValues.sum() / dailyValues.size
                    } else {
                        null
                    }

                emit(Result.Success(average))
            }.catch { e ->
                if (e is CancellationException) throw e
                e.printStackTrace()
                emit(Result.Error(mapExceptionToDataError(e)))
            }

        private fun aggregateByDevice(
            consumptions: List<ConsumptionDto>,
            deviceMap: Map<String, DeviceDto>,
        ): List<ConsumptionBreakdown> {
            return consumptions.mapNotNull { dto ->
                val device = deviceMap[dto.entityId] ?: return@mapNotNull null
                ConsumptionBreakdown(
                    id = device.deviceId,
                    name = device.name,
                    deviceType = device.category,
                    energyKwh = normalizeToKwh(dto.energyConsumption, dto.energyConsumptionUnit),
                    impactPercentage = 0.0,
                )
            }
        }

        private fun aggregateByRoom(
            consumptions: List<ConsumptionDto>,
            deviceMap: Map<String, DeviceDto>,
        ): List<ConsumptionBreakdown> {
            val roomMap = mutableMapOf<String, Double>()

            consumptions.forEach { dto ->
                val device = deviceMap[dto.entityId]
                val roomName = device?.mapData?.room
                if (!roomName.isNullOrBlank()) {
                    val kwh = normalizeToKwh(dto.energyConsumption, dto.energyConsumptionUnit)
                    roomMap[roomName] = (roomMap[roomName] ?: 0.0) + kwh
                }
            }

            return roomMap.map { (roomName, totalKwh) ->
                ConsumptionBreakdown(
                    id = "room_$roomName",
                    name = roomName,
                    deviceType = "room",
                    energyKwh = totalKwh,
                    impactPercentage = 0.0,
                )
            }
        }

        private fun aggregateByGroup(
            consumptions: List<ConsumptionDto>,
            deviceMap: Map<String, DeviceDto>,
        ): List<ConsumptionBreakdown> {
            val groupMap = mutableMapOf<Int, Pair<String, Double>>()

            consumptions.forEach { dto ->
                val device = deviceMap[dto.entityId]
                val kwh = normalizeToKwh(dto.energyConsumption, dto.energyConsumptionUnit)

                device?.groups?.forEach { group ->
                    val current = groupMap[group.groupId] ?: (group.name to 0.0)
                    groupMap[group.groupId] = current.first to (current.second + kwh)
                }
            }

            return groupMap.map { (groupId, data) ->
                ConsumptionBreakdown(
                    id = "group_$groupId",
                    name = data.first,
                    deviceType = "group",
                    energyKwh = data.second,
                    impactPercentage = 0.0,
                )
            }
        }

        private fun fillGaps(
            existingData: List<Consumption>,
            startDate: LocalDate,
            endDate: LocalDate,
            granularity: ConsumptionGranularity,
        ): List<Consumption> {
            val filledList = mutableListOf<Consumption>()
            val dataMap = existingData.associateBy { it.date }

            when (granularity) {
                ConsumptionGranularity.HOUR -> {
                    var current = startDate.atStartOfDay()
                    val limit = endDate.plusDays(1).atStartOfDay()
                    while (current.isBefore(limit)) {
                        val key = current.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).substring(0, 16)
                        addConsumptionToList(dataMap, key, filledList)
                        current = current.plusHours(1)
                    }
                }

                ConsumptionGranularity.DAY -> {
                    var current = startDate
                    while (!current.isAfter(endDate)) {
                        val key = current.toString()
                        addConsumptionToList(dataMap, key, filledList)
                        current = current.plusDays(1)
                    }
                }

                ConsumptionGranularity.MONTH -> {
                    var current = startDate.withDayOfMonth(1)
                    val limit = endDate.withDayOfMonth(1)
                    while (!current.isAfter(limit)) {
                        val key = current.toString()
                        addConsumptionToList(dataMap, key, filledList)
                        current = current.plusMonths(1)
                    }
                }
            }
            return filledList
        }

        private fun addConsumptionToList(
            map: Map<String, Consumption>,
            key: String,
            list: MutableList<Consumption>,
        ) {
            val item = map[key]
            if (item != null) {
                list.add(item)
            } else {
                list.add(Consumption(date = key, energyKwh = 0.0, cost = 0.0))
            }
        }

        private fun mapEnergyEntityToDevice(devices: List<DeviceDto>): Map<String, DeviceDto> =
            devices
                .filter { !it.energyEntityId.isNullOrBlank() }
                .associateBy { it.energyEntityId!! }

        private suspend fun fetchBatchConsumption(
            entityIds: Set<String>,
            start: LocalDate,
            end: LocalDate,
        ): List<ConsumptionDto> =
            dataSource.getEntitiesConsumption(
                entityIds = entityIds.joinToString(","),
                startDate = start.toString(),
                endDate = end.toString(),
                group = "entity",
            )

        private fun calculateImpactAndSort(items: List<ConsumptionBreakdown>): List<ConsumptionBreakdown> {
            val totalEnergy = items.sumOf { it.energyKwh }
            return items
                .map { item ->
                    val percent =
                        if (totalEnergy > 0) {
                            (item.energyKwh / totalEnergy) * PERCENTAGE_FACTOR
                        } else {
                            0.0
                        }
                    item.copy(impactPercentage = percent)
                }.sortedByDescending { it.energyKwh }
        }

        private fun normalizeToKwh(
            value: Double,
            unit: String?,
        ): Double {
            val safeUnit = unit ?: "Wh"
            return if (safeUnit.equals("Wh", ignoreCase = true)) value / KWH_CONVERSION_FACTOR else value
        }

        private fun mapExceptionToDataError(e: Throwable): DataError =
            when (e) {
                is IOException -> {
                    DataError.Network.NO_INTERNET
                }

                is HttpException -> {
                    when (e.code()) {
                        in 500..599 -> DataError.Network.SERVER_UNAVAILABLE
                        else -> DataError.Network.UNKNOWN
                    }
                }

                else -> {
                    DataError.Network.UNKNOWN
                }
            }
    }
