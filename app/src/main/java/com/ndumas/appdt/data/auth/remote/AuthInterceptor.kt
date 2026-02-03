package com.ndumas.appdt.data.auth.remote

import com.ndumas.appdt.domain.auth.repository.SessionStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor
    @Inject
    constructor(
        private val sessionStorage: SessionStorage,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val token =
                runBlocking {
                    sessionStorage.getToken()
                }

            val requestBuilder = chain.request().newBuilder()

            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            return chain.proceed(requestBuilder.build())
        }
    }
