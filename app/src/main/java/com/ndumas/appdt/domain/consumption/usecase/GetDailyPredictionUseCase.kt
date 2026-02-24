package com.ndumas.appdt.domain.consumption.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.consumption.model.PredictionData
import com.ndumas.appdt.domain.consumption.repository.ConsumptionRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase per recuperare le previsioni di consumo energetico.
 *
 * Ritorna le previsioni per le prossime 12 ore, recuperate dal backend
 * (o generate dal FakeRepository per testing locale).
 *
 * Utilizzo:
 * - Nella Home: per mostrare il riepilogo energetico giornaliero
 * - Nella schermata Consumi: solo per il filtro "Oggi"
 */
class GetDailyPredictionUseCase
    @Inject
    constructor(
        private val repository: ConsumptionRepository,
    ) {
        operator fun invoke(): Flow<Result<List<PredictionData>, DataError>> = repository.getDailyPrediction()
    }
