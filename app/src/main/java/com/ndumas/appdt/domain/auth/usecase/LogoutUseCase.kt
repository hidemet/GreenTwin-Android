package com.ndumas.appdt.domain.auth.usecase

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.auth.repository.AuthRepository
import com.ndumas.appdt.domain.error.DataError
import javax.inject.Inject

class LogoutUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke(): Result<Unit, DataError> = authRepository.logout()
    }
