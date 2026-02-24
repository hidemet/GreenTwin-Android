package com.ndumas.appdt.data.auth.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.auth.model.Token
import com.ndumas.appdt.domain.auth.model.User
import com.ndumas.appdt.domain.auth.repository.AuthRepository
import com.ndumas.appdt.domain.auth.repository.SessionStorage
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeAuthRepository
    @Inject
    constructor(
        private val sessionStorage: SessionStorage,
    ) : AuthRepository {
        override fun login(
            email: String,
            password: String,
        ): Flow<Result<Token, DataError>> =
            flow {
                // Simula una piccola attesa
                kotlinx.coroutines.delay(1000)

                if (email.isNotBlank() && password.isNotBlank()) {
                    val fakeToken =
                        Token(
                            accessToken = "fake_access_token",
                            tokenType = "Bearer",
                        )
                    sessionStorage.saveToken(fakeToken.accessToken)
                    emit(Result.Success(fakeToken))
                } else {
                    emit(Result.Error(DataError.Auth.INVALID_CREDENTIALS))
                }
            }

        override fun register(
            username: String,
            email: String,
            password: String,
        ): Flow<Result<User, DataError>> =
            flow {
                kotlinx.coroutines.delay(1000)

                if (email.isNotBlank() && password.isNotBlank()) {
                    val fakeUser =
                        User(
                            userId = 1,
                            email = email,
                            username = username,
                        )
                    emit(Result.Success(fakeUser))
                } else {
                    emit(Result.Error(DataError.Auth.EMAIL_ALREADY_EXISTS))
                }
            }

        override suspend fun logout(): Result<Unit, DataError> {
            kotlinx.coroutines.delay(500)
            sessionStorage.clearToken()
            return Result.Success(Unit)
        }
    }
