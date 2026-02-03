package com.ndumas.appdt.domain.auth.repository

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.auth.model.Token
import com.ndumas.appdt.domain.auth.model.User
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(
        email: String,
        password: String,
    ): Flow<Result<Token, DataError>>

    fun register(
        username: String,
        email: String,
        password: String,
    ): Flow<Result<User, DataError>>

    suspend fun logout(): Result<Unit, DataError>
}
