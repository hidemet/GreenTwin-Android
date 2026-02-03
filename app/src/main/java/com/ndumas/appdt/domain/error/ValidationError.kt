package com.ndumas.appdt.domain.error

sealed interface Error

sealed interface ValidationError : Error {
    sealed interface User : ValidationError {
        enum class EmailError : Error {
            EMPTY,
            INVALID_FORMAT,
        }

        enum class PasswordError : Error {
            EMPTY,
            TOO_SHORT,
        }
    }
}
