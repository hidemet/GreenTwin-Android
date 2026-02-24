package com.ndumas.appdt.domain.consumption.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.consumption.model.ConsumptionGranularity
import com.ndumas.appdt.domain.consumption.model.EnergySummary
import com.ndumas.appdt.domain.consumption.repository.ConsumptionRepository
import com.ndumas.appdt.domain.error.DataError
import com.ndumas.appdt.presentation.consumption.model.PredictionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

/**
 * UseCase per calcolare il riepilogo energetico giornaliero.
 *
 * La logica di calcolo della percentuale:
 * 1. Prende il consumo reale di oggi (dalle 00:00 fino all'ora corrente)
 * 2. Aggiunge la previsione per le ore restanti (elementi 12-23 dell'endpoint prediction)
 * 3. Confronta la stima totale giornaliera con la media storica degli ultimi 7 giorni
 *
 * Struttura dati prediction (24 elementi):
 * - Elementi 0-11: consumi REALI delle ultime 12 ore (dal database)
 * - Elementi 12-23: previsioni ML per le prossime 12 ore
 */
class GetEnergySummaryUseCase
    @Inject
    constructor(
        private val repository: ConsumptionRepository,
    ) {
        private companion object {
            const val PERCENTAGE_MULTIPLIER = 100.0
            const val TREND_THRESHOLD = 1.0
            const val FUTURE_PREDICTIONS_START_INDEX = 12
            const val HISTORICAL_DAYS = 7
        }

        operator fun invoke(): Flow<Result<EnergySummary, DataError>> {
            val today = LocalDate.now()

            return combine(
                repository.getDailyConsumption(
                    startDate = today,
                    endDate = today,
                    granularity = ConsumptionGranularity.HOUR,
                ),
                repository.getDailyPrediction(),
                repository.getHistoricalDailyAverage(HISTORICAL_DAYS),
            ) { consumptionResult, predictionResult, averageResult ->
                when {
                    consumptionResult is Result.Error -> {
                        Result.Error(consumptionResult.error)
                    }

                    predictionResult is Result.Error -> {
                        Result.Error(predictionResult.error)
                    }

                    averageResult is Result.Error -> {
                        Result.Error(averageResult.error)
                    }

                    consumptionResult is Result.Success &&
                        predictionResult is Result.Success &&
                        averageResult is Result.Success -> {
                        val consumptionList = consumptionResult.data
                        val predictionList = predictionResult.data
                        val historicalAverage = averageResult.data

                        // Consumo reale di oggi fino all'ora corrente
                        val todayRealKwh = consumptionList.sumOf { it.energyKwh }

                        // Previsione per le ore restanti (elementi 12-23)
                        val futurePredictonKwh =
                            predictionList
                                .drop(FUTURE_PREDICTIONS_START_INDEX)
                                .sumOf { it.energyConsumptionKwh }

                        // Stima totale giornaliera = reale + previsione ore restanti
                        val estimatedTotalToday = todayRealKwh + futurePredictonKwh

                        // Calcola trend rispetto alla media storica
                        val (trendPercentage, trendState) =
                            if (historicalAverage != null && historicalAverage > 0) {
                                calculateTrend(estimatedTotalToday, historicalAverage)
                            } else {
                                Pair(0.0, PredictionState.NEUTRAL)
                            }

                        Result.Success(
                            EnergySummary(
                                todayConsumptionKwh = todayRealKwh,
                                predictedConsumptionKwh = estimatedTotalToday,
                                trendPercentage = trendPercentage,
                                trendState = trendState,
                            ),
                        )
                    }

                    else -> {
                        Result.Error(DataError.Network.UNKNOWN)
                    }
                }
            }
        }

        /**
         * Calcola il trend percentuale rispetto alla media storica.
         *
         * @param estimated Stima consumo giornaliero (reale + previsione)
         * @param historicalAverage Media storica giornaliera
         * @return Pair di (percentuale assoluta, stato del trend)
         *
         * - POSITIVE (verde): consumo stimato < media (stai risparmiando)
         * - NEGATIVE (rosso): consumo stimato > media (stai consumando di più)
         * - NEUTRAL (grigio): differenza trascurabile (±1%)
         */
        private fun calculateTrend(
            estimated: Double,
            historicalAverage: Double,
        ): Pair<Double, PredictionState> {
            if (historicalAverage <= 0 || estimated <= 0) {
                return Pair(0.0, PredictionState.NEUTRAL)
            }

            val diff = estimated - historicalAverage
            val diffPercent = (diff / historicalAverage) * PERCENTAGE_MULTIPLIER

            val state =
                when {
                    diffPercent < -TREND_THRESHOLD -> PredictionState.POSITIVE

                    // Consumo inferiore = buono
                    diffPercent > TREND_THRESHOLD -> PredictionState.NEGATIVE

                    // Consumo superiore = male
                    else -> PredictionState.NEUTRAL
                }

            return Pair(abs(diffPercent), state)
        }
    }
