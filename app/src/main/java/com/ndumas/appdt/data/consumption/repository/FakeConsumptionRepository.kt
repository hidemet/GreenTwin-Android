package com.ndumas.appdt.data.consumption.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.consumption.model.Consumption
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdown
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdownType
import com.ndumas.appdt.domain.consumption.model.ConsumptionGranularity
import com.ndumas.appdt.domain.consumption.model.PredictionData
import com.ndumas.appdt.domain.consumption.repository.ConsumptionRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * Repository fake con dati mock realistici per testing locale.
 * Genera consumi con pattern giorno/notte, weekend e variazioni stagionali.
 * Le previsioni sono basate su pattern storici con variazione controllata.
 */
class FakeConsumptionRepository
    @Inject
    constructor() : ConsumptionRepository {
        private companion object {
            // Costo energia in €/kWh
            const val ENERGY_COST_PER_KWH = 0.25

            // Consumi base per fascia oraria (kWh)
            const val BASE_NIGHT_CONSUMPTION = 0.15 // 00:00 - 06:00
            const val BASE_MORNING_CONSUMPTION = 0.45 // 06:00 - 09:00
            const val BASE_DAY_CONSUMPTION = 0.35 // 09:00 - 18:00
            const val BASE_EVENING_CONSUMPTION = 0.55 // 18:00 - 23:00
            const val BASE_LATE_NIGHT_CONSUMPTION = 0.25 // 23:00 - 00:00

            // Fattori moltiplicativi
            const val WEEKEND_MULTIPLIER = 1.3
            const val WINTER_MULTIPLIER = 1.4
            const val SUMMER_MULTIPLIER = 1.25

            // Variazione casuale massima (±%)
            const val RANDOM_VARIATION_PERCENT = 0.15

            // Delay simulato per chiamate API
            const val SIMULATED_DELAY_MS = 150L
        }

        private val random = Random(System.currentTimeMillis())

        override fun getDailyConsumption(
            startDate: LocalDate,
            endDate: LocalDate,
            granularity: ConsumptionGranularity,
        ): Flow<Result<List<Consumption>, DataError>> =
            flow {
                delay(SIMULATED_DELAY_MS)
                val mockList = mutableListOf<Consumption>()
                val now = LocalDateTime.now()

                when (granularity) {
                    ConsumptionGranularity.HOUR -> {
                        var time = startDate.atStartOfDay()
                        val limit = endDate.plusDays(1).atStartOfDay()
                        while (time.isBefore(limit)) {
                            val isFuture = time.isAfter(now)
                            val kwh = if (isFuture) 0.0 else calculateHourlyConsumption(time)
                            val dateStr = time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).substring(0, 16)
                            mockList.add(Consumption(dateStr, kwh, kwh * ENERGY_COST_PER_KWH))
                            time = time.plusHours(1)
                        }
                    }

                    ConsumptionGranularity.DAY -> {
                        var current = startDate
                        val today = LocalDate.now()
                        while (!current.isAfter(endDate)) {
                            val isFuture = current.isAfter(today)
                            val kwh = if (isFuture) 0.0 else calculateDailyConsumption(current)
                            mockList.add(Consumption(current.toString(), kwh, kwh * ENERGY_COST_PER_KWH))
                            current = current.plusDays(1)
                        }
                    }

                    ConsumptionGranularity.MONTH -> {
                        var current = startDate.withDayOfMonth(1)
                        val limit = endDate.withDayOfMonth(1)
                        val today = LocalDate.now()
                        while (!current.isAfter(limit)) {
                            val isFuture = current.isAfter(today)
                            val kwh = if (isFuture) 0.0 else calculateMonthlyConsumption(current)
                            mockList.add(Consumption(current.toString(), kwh, kwh * ENERGY_COST_PER_KWH))
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
                delay(SIMULATED_DELAY_MS)

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

        override fun getDailyPrediction(): Flow<Result<List<PredictionData>, DataError>> =
            flow {
                delay(SIMULATED_DELAY_MS)
                val now =
                    LocalDateTime
                        .now()
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0)
                val predictions = mutableListOf<PredictionData>()

                // Primi 12 elementi: consumi REALI delle ultime 12 ore (dal database)
                for (i in 11 downTo 0) {
                    val pastHour = now.minusHours(i.toLong() + 1)
                    val dateStr = formatPredictionDate(pastHour)
                    val realConsumption = calculateHourlyConsumption(pastHour)
                    predictions.add(PredictionData(dateStr, realConsumption))
                }

                // Ultimi 12 elementi: previsioni ML per le prossime 12 ore
                for (i in 0 until 12) {
                    val futureHour = now.plusHours(i.toLong())
                    val dateStr = formatPredictionDate(futureHour)

                    // Calcola previsione basata su pattern + leggera variazione
                    val baseConsumption = calculateHourlyConsumption(futureHour)
                    // Aggiungi variazione ±10% per simulare incertezza previsione
                    val variation = baseConsumption * (random.nextDouble() * 0.2 - 0.1)
                    val predictedKwh = max(0.0, baseConsumption + variation)

                    predictions.add(PredictionData(dateStr, predictedKwh))
                }

                emit(Result.Success(predictions))
            }

        override fun getHistoricalDailyAverage(days: Int): Flow<Result<Double?, DataError>> =
            flow {
                delay(SIMULATED_DELAY_MS)
                val today = LocalDate.now()

                // Calcola la media dei consumi giornalieri degli ultimi N giorni
                var totalConsumption = 0.0
                var validDays = 0

                for (i in 1..days) {
                    val pastDate = today.minusDays(i.toLong())
                    val dailyConsumption = calculateDailyConsumption(pastDate)
                    if (dailyConsumption > 0) {
                        totalConsumption += dailyConsumption
                        validDays++
                    }
                }

                val average = if (validDays > 0) totalConsumption / validDays else null
                emit(Result.Success(average))
            }

        /**
         * Calcola il consumo orario basato su pattern realistici.
         * Tiene conto di:
         * - Fascia oraria (notte, mattina, giorno, sera)
         * - Weekend (consumo maggiore)
         * - Stagione (inverno/estate = più climatizzazione)
         */
        private fun calculateHourlyConsumption(dateTime: LocalDateTime): Double {
            val hour = dateTime.hour
            val dayOfWeek = dateTime.dayOfWeek
            val month = dateTime.monthValue

            // 1. Consumo base per fascia oraria
            val baseConsumption =
                when (hour) {
                    in 0..5 -> BASE_NIGHT_CONSUMPTION
                    in 6..8 -> BASE_MORNING_CONSUMPTION
                    in 9..17 -> BASE_DAY_CONSUMPTION
                    in 18..22 -> BASE_EVENING_CONSUMPTION
                    else -> BASE_LATE_NIGHT_CONSUMPTION
                }

            // 2. Fattore weekend
            val weekendFactor =
                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                    WEEKEND_MULTIPLIER
                } else {
                    1.0
                }

            // 3. Fattore stagionale (usando sinusoide per transizione graduale)
            val seasonalFactor = calculateSeasonalFactor(month)

            // 4. Variazione casuale
            val randomVariation = 1.0 + (random.nextDouble() * 2 - 1) * RANDOM_VARIATION_PERCENT

            return baseConsumption * weekendFactor * seasonalFactor * randomVariation
        }

        /**
         * Calcola il fattore stagionale usando una sinusoide.
         * Picco in inverno (gennaio) e estate (luglio) per climatizzazione.
         */
        private fun calculateSeasonalFactor(month: Int): Double {
            // Sinusoide con due picchi: gennaio (mese 1) e luglio (mese 7)
            val radians = (month - 1) * Math.PI / 6 // 0 a 2π durante l'anno
            val seasonalWave = sin(radians * 2) // Due cicli all'anno

            return when {
                month in listOf(1, 2, 12) -> WINTER_MULTIPLIER

                // Inverno
                month in listOf(6, 7, 8) -> SUMMER_MULTIPLIER

                // Estate
                else -> 1.0 + (seasonalWave.coerceIn(-0.1, 0.1)) // Mezza stagione
            }
        }

        /**
         * Calcola il consumo giornaliero sommando pattern orari.
         */
        private fun calculateDailyConsumption(date: LocalDate): Double {
            var total = 0.0
            val now = LocalDateTime.now()

            for (hour in 0..23) {
                val dateTime = date.atTime(hour, 0)
                // Per oggi, considera solo le ore passate
                if (date == LocalDate.now() && dateTime.isAfter(now)) {
                    continue
                }
                total += calculateHourlyConsumption(dateTime)
            }
            return total
        }

        /**
         * Calcola il consumo mensile con variazioni realistiche.
         */
        private fun calculateMonthlyConsumption(date: LocalDate): Double {
            val daysInMonth = date.lengthOfMonth()
            val seasonalFactor = calculateSeasonalFactor(date.monthValue)

            // Consumo medio giornaliero base * giorni * fattore stagionale
            val avgDailyConsumption = 8.5 // kWh medio giornaliero
            val baseMonthly = avgDailyConsumption * daysInMonth * seasonalFactor

            // Variazione casuale ±10%
            val variation = baseMonthly * (random.nextDouble() * 0.2 - 0.1)

            return baseMonthly + variation
        }

        /**
         * Formatta la data per le previsioni nel formato backend.
         * Formato: "DD-MM-YYYY HH-HH+1" (es. "11-02-2026 14-15")
         */
        private fun formatPredictionDate(dateTime: LocalDateTime): String {
            val day = dateTime.dayOfMonth
            val month = dateTime.monthValue
            val year = dateTime.year
            val hourStart = dateTime.hour
            val hourEnd = (hourStart + 1) % 24
            return "$day-$month-$year $hourStart-$hourEnd"
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
