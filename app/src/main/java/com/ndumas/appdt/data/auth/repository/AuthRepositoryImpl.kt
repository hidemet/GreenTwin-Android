package com.ndumas.appdt.data.auth.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.data.auth.mapper.toDomain
import com.ndumas.appdt.data.auth.remote.AuthRemoteDataSource
import com.ndumas.appdt.data.auth.remote.dto.LoginRequest
import com.ndumas.appdt.data.auth.remote.dto.RegisterRequest
import com.ndumas.appdt.domain.auth.model.Token
import com.ndumas.appdt.domain.auth.model.User
import com.ndumas.appdt.domain.auth.repository.AuthRepository
import com.ndumas.appdt.domain.auth.repository.SessionStorage
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl
    @Inject
    constructor(
        private val remoteDataSource: AuthRemoteDataSource,
        private val sessionStorage: SessionStorage,
    ) : AuthRepository {
        override fun login(
            email: String,
            password: String,
        ): Flow<Result<Token, DataError>> =
            flow {
                try {
                    val tokenDto = remoteDataSource.login(LoginRequest(email, password))
                    sessionStorage.saveToken(tokenDto.accessToken)
                    emit(Result.Success(tokenDto.toDomain()))
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    emit(Result.Error(mapExceptionToDataError(e)))
                }
            }

        override fun register(
            username: String,
            email: String,
            password: String,
        ): Flow<Result<User, DataError>> =
            flow {
                try {
                    val request = RegisterRequest(username, email, password)
                    val userDto = remoteDataSource.register(request)
                    emit(Result.Success(userDto.toDomain()))
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    emit(Result.Error(mapExceptionToDataError(e)))
                }
            }

        private fun mapExceptionToDataError(e: Exception): DataError =
            when (e) {
                is IOException -> {
                    DataError.Network.NO_INTERNET
                }

                is HttpException -> {
                    when (e.code()) {
                        400 -> DataError.Auth.EMAIL_ALREADY_EXISTS
                        401 -> DataError.Auth.INVALID_CREDENTIALS
                        403 -> DataError.Auth.UNAUTHORIZED
                        404 -> DataError.Auth.USER_NOT_FOUND
                        in 500..599 -> DataError.Network.SERVER_UNAVAILABLE
                        else -> DataError.Network.UNKNOWN
                    }
                }

                else -> {
                    DataError.Network.UNKNOWN
                }
            }

        override suspend fun logout(): Result<Unit, DataError> =
            try {
                remoteDataSource.logout()
                sessionStorage.clearToken()
                Result.Success(Unit)
            } catch (e: IOException) {
                // Anche se fallisce la rete, puliamo il token localmente
                sessionStorage.clearToken()
                Result.Success(Unit)
            } catch (e: HttpException) {
                // Anche se fallisce la chiamata API, puliamo il token localmente
                sessionStorage.clearToken()
                Result.Success(Unit)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                // Fallback: puliamo comunque il token locale
                sessionStorage.clearToken()
                Result.Success(Unit)
            }
    }
