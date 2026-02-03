package com.ndumas.appdt.domain.auth.model

data class Token(
    val accessToken: String,
    val tokenType: String,
)
