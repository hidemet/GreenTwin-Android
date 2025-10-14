package com.ndumas.greentwin.data.remote

import com.ndumas.greentwin.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * L'Interceptor di OkHTTP si occupa di aggiungere l'header di autorizzazione a tutte le richieste di rete in uscita.
 */
class AuthInterceptor @Inject constructor(
    private val sessionManager : SessionManager): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Non aggiunge il token alle richieste pubbliche come il login,
        // altrimenti si rischia di inviare un token non valido e la richiesta fallisce.
        if (originalRequest.url().encodedPath().contains("auth/login")) {
            return chain.proceed(originalRequest)
        }
        val token = sessionManager.getToken()

        // Costruisce una nuova richiesta aggiungendo l'header solo se il token esiste.
        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)

    }
}