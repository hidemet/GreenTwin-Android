package com.ndumas.appdt.domain.error

sealed interface ResultError

enum class NetworkError : ResultError {
    NO_INTERNET, // Es. IOException
    REQUEST_TIMEOUT,
    SERVER_UNAVAILABLE, // Errore 5xx
    UNKNOWN, // Errore generico o non mappato
}

/**
 * Errori specifici del dominio di Autenticazione.
 */
enum class AuthError : ResultError {
    INVALID_CREDENTIALS, // 401
    UNAUTHORIZED, // 403
    USER_NOT_FOUND, // 404
    EMAIL_ALREADY_EXISTS,
    UNKNOWN,
}
