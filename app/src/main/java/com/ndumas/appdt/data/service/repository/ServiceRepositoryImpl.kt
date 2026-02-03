package com.ndumas.appdt.data.service.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.data.service.remote.dto.ServiceRequestDto
import com.ndumas.appdt.data.service.remote.source.ServiceRemoteDataSource
import com.ndumas.appdt.domain.error.DataError
import com.ndumas.appdt.domain.service.model.ServiceRequest
import com.ndumas.appdt.domain.service.repository.ServiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class ServiceRepositoryImpl
    @Inject
    constructor(
        private val dataSource: ServiceRemoteDataSource,
    ) : ServiceRepository {
        override fun callService(request: ServiceRequest): Flow<Result<Unit, DataError>> =
            flow<Result<Unit, DataError>> {
                try {
                    val dto =
                        ServiceRequestDto(
                            entityId = request.entityId,
                            service = request.service,
                            data = request.data ?: emptyMap(),
                            user = request.userId ?: "android_client",
                        )

                    // Log di debug
                    android.util.Log.d("SERVICE_DEBUG", "Sending DTO: $dto")
                    dataSource.callService(dto)

                    emit(Result.Success(Unit))
                } catch (e: HttpException) {
                    if (e is CancellationException) throw e
                    e.printStackTrace()

                    val errorBody = e.response()?.errorBody()?.string()
                    android.util.Log.e("SERVICE_ERROR", "HTTP ${e.code()} Error Body: $errorBody")
                    emit(Result.Error(mapExceptionToDataError(e)))
                }
            }

        private fun mapExceptionToDataError(e: Throwable): DataError =
            when (e) {
                is IOException -> {
                    DataError.Network.NO_INTERNET
                }

                is HttpException -> {
                    when (e.code()) {
                        in 500..599 -> DataError.Network.SERVER_UNAVAILABLE
                        401 -> DataError.Auth.UNAUTHORIZED
                        else -> DataError.Network.UNKNOWN
                    }
                }

                else -> {
                    DataError.Network.UNKNOWN
                }
            }
    }
