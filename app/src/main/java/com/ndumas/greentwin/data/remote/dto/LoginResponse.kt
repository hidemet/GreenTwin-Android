package com.ndumas.greentwin.data.remote.dto

import com.squareup.moshi.Json


/**
 * Data Transfer Object (DTO) per la richiesta di login.
 * Contiene il token di autenticazione restituito dal backend in caso di successo.
 * @property token di autenticazione restituito dal backend in caso di successo
 */
data class LoginResponse(
    @field:Json(name = "token") val token: String
)