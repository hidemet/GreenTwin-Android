package com.ndumas.appdt.core.validation

import android.util.Patterns
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.error.ValidationError.User.EmailError
import javax.inject.Inject

class AndroidEmailPatternValidator
    @Inject
    constructor() : EmailPatternValidator {
        override fun isValid(email: String): Result<Unit, EmailError> {
            if (email.isBlank()) {
                return Result.Error(EmailError.EMPTY)
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return Result.Error(EmailError.INVALID_FORMAT)
            }
            return Result.Success(Unit)
        }
    }
