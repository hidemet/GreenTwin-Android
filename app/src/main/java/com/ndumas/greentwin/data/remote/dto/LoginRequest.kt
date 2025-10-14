package com.ndumas.greentwin.data.remote.dto

import com.squareup.moshi.Json


/**
 * Data Transfer Object (DTO) per la richiesta di login.
 * Contiene le credenziali dell'utente da inviare al backend.
 * @property username Il nome utente dell'utente.
 * @property password La password dell'utente.
 */
data class LoginRequest(
    @field:Json(name = "email") val email: String,
    @field:Json(name = "password") val password: String
)