package com.ndumas.appdt.data.auth.mapper

import com.ndumas.appdt.data.auth.remote.dto.TokenResponse
import com.ndumas.appdt.data.auth.remote.dto.UserDto
import com.ndumas.appdt.domain.auth.model.Token
import com.ndumas.appdt.domain.auth.model.User

// Funzione di estensone per convertire il DTO di rete nel modello di dominio.
fun TokenResponse.toDomain(): Token =
    Token(
        accessToken = this.accessToken,
        tokenType = this.tokenType,
    )

fun UserDto.toDomain(): User =
    User(
        userId = this.userId,
        email = this.email,
        username = this.username,
    )
