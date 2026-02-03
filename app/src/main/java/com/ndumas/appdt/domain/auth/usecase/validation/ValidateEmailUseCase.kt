package com.ndumas.appdt.domain.auth.usecase.validation

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.core.validation.EmailPatternValidator
import com.ndumas.appdt.domain.error.ValidationError.User.EmailError
import javax.inject.Inject

class ValidateEmailUseCase
    @Inject
    constructor(
        private val validator: EmailPatternValidator,
    ) {
        operator fun invoke(email: String): Result<Unit, EmailError> = validator.isValid(email)
    }
