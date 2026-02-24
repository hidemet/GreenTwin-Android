package com.ndumas.appdt.data.auth.repository

import com.ndumas.appdt.domain.auth.repository.SessionStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSessionStorage @Inject constructor() : SessionStorage {
    private var token: String? = null

    override suspend fun saveToken(token: String) {
        this.token = token
    }

    override suspend fun getToken(): String? {
        return token
    }

    override suspend fun clearToken() {
        token = null
    }
}
