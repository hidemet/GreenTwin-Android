package com.ndumas.appdt.core.validation

import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.domain.error.ValidationError.User.EmailError

interface EmailPatternValidator {
    fun isValid(email: String): Result<Unit, EmailError>
}
