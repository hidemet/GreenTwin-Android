package com.ndumas.appdt.domain.auth.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.auth.model.User
import com.ndumas.appdt.domain.auth.repository.AuthRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RegisterUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        operator fun invoke(
            username: String,
            email: String,
            password: String,
        ): Flow<Result<User, DataError>> = repository.register(username, email, password)
    }
