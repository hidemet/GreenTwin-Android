package com.ndumas.appdt.domain.error

sealed interface DataError : Error {
    enum class Network : DataError {
        NO_INTERNET,
        SERVER_UNAVAILABLE,
        UNKNOWN,
    }

    enum class Auth : DataError {
        INVALID_CREDENTIALS,
        UNAUTHORIZED,
        USER_NOT_FOUND,
        EMAIL_ALREADY_EXISTS,
    }

    enum class Validation : DataError {
        EMPTY_NAME,
        MISSING_TRIGGER,
        NO_ACTIONS,
        INVALID_DRAFT,
        NOT_FOUND,
    }
}
