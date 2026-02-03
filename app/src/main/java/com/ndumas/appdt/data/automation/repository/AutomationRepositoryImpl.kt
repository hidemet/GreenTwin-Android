package com.ndumas.appdt.data.automation.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.data.automation.mapper.AutomationMapper
import com.ndumas.appdt.data.automation.remote.AutomationApiService
import com.ndumas.appdt.domain.automation.model.Automation // <--- QUESTO MANCAVA
import com.ndumas.appdt.domain.automation.model.AutomationDraft
import com.ndumas.appdt.domain.automation.model.SimulationResult
import com.ndumas.appdt.domain.automation.repository.AutomationRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AutomationRepositoryImpl
    @Inject
    constructor(
        private val api: AutomationApiService,
        private val mapper: AutomationMapper,
    ) : AutomationRepository {
        override fun createAutomation(draft: AutomationDraft): Flow<Result<Unit, DataError>> =
            flow<Result<Unit, DataError>> {
                val request = mapper.mapToRequest(draft)
                api.createAutomation(request)
                emit(Result.Success(Unit))
            }.catch { e ->
                emit(Result.Error(mapExceptionToDataError(e)))
            }

        override fun getAutomations(): Flow<Result<List<Automation>, DataError>> =
            flow<Result<List<Automation>, DataError>> {
                val dtos = api.getAutomations()
                val domainList = dtos.map { mapper.mapDtoToDomain(it) }
                emit(Result.Success(domainList))
            }.catch { e ->
                android.util.Log.e("AUTO_REPO", "Errore nel caricamento automazioni", e)
                emit(Result.Error(mapExceptionToDataError(e)))
            }

        override fun simulateAutomation(draft: AutomationDraft): Flow<Result<SimulationResult, DataError>> =
            flow<Result<SimulationResult, DataError>> {
                val request = mapper.mapToRequest(draft)
                val response = api.simulateAutomation(request, returnStateMatrix = false)
                val domainResult = mapper.mapSimulationDtoToDomain(response)
                emit(Result.Success(domainResult))
            }.catch { e ->
                emit(Result.Error(mapExceptionToDataError(e)))
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
