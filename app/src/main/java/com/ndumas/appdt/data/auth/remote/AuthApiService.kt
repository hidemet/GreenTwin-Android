package com.ndumas.appdt.data.auth.remote

import com.ndumas.appdt.data.auth.remote.dto.LoginRequest
import com.ndumas.appdt.data.auth.remote.dto.RegisterRequest
import com.ndumas.appdt.data.auth.remote.dto.TokenResponse
import com.ndumas.appdt.data.auth.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): TokenResponse

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): UserDto

    @GET("auth/me")
    suspend fun getMe(): UserDto

    @POST("auth/refresh")
    suspend fun refresh(): TokenResponse

    @GET("api/logout")
    suspend fun logout(): LogoutResponse
}

data class LogoutResponse(
    val message: String,
)
