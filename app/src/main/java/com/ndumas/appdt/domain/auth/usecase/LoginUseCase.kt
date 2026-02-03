package com.ndumas.appdt.domain.auth.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.auth.model.Token
import com.ndumas.appdt.domain.auth.repository.AuthRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        operator fun invoke(
            email: String,
            password: String,
        ): Flow<Result<Token, DataError>> {
            // La logica di business qui è semplicissima: delega la chiamata al repository.
            // In un caso più complesso, potremmo aggiungere qui validazioni, logica di retry, etc.
            return repository.login(email, password)
        }
    }
