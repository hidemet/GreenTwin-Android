package com.ndumas.appdt.domain.auth.usecase.validation

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.error.ValidationError.User.PasswordError
import javax.inject.Inject

class ValidatePasswordUseCase
    @Inject
    constructor() {
        companion object {
            const val MIN_PASSWORD_LENGTH = 4 // 8
        }

        operator fun invoke(password: String): Result<Unit, PasswordError> {
            if (password.isBlank()) {
                return Result.Error(PasswordError.EMPTY)
            }
            if (password.length < MIN_PASSWORD_LENGTH) {
                return Result.Error(PasswordError.TOO_SHORT)
            }
            return Result.Success(Unit)
        }
    }
