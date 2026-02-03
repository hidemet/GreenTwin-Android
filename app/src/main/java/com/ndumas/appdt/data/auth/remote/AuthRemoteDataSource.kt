package com.ndumas.appdt.data.auth.remote

import com.ndumas.appdt.data.auth.remote.dto.LoginRequest
import com.ndumas.appdt.data.auth.remote.dto.RegisterRequest
import com.ndumas.appdt.data.auth.remote.dto.TokenResponse
import com.ndumas.appdt.data.auth.remote.dto.UserDto
import javax.inject.Inject

class AuthRemoteDataSource
    @Inject
    constructor(
        private val authApi: AuthApiService,
    ) {
        suspend fun login(request: LoginRequest): TokenResponse = authApi.login(request)

        suspend fun register(request: RegisterRequest): UserDto = authApi.register(request)

        suspend fun logout(): LogoutResponse = authApi.logout()
    }
