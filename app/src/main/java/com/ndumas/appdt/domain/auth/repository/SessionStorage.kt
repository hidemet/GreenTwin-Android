package com.ndumas.appdt.domain.auth.repository

interface SessionStorage {
    suspend fun saveToken(token: String)

    suspend fun getToken(): String?

    suspend fun clearToken()
}
