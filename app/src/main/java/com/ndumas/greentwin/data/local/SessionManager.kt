package com.ndumas.greentwin.data.local

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestisce la sessione utente, in particolare il token di autenticazione.
 * NOTA: Attualmente usa una variabile in memoria. Nelle prossime fasi, verrà potenziata per utilizzare EncryptedSharedPreferences per la persistenza sicura
 */
@Singleton
class SessionManager @Inject constructor(){
    private var authToken: String? = null

    fun saveToken(token: String) {
        authToken = token
    }

    fun getToken(): String? {
        return authToken
    }

    fun clearToken() {
        authToken = null
    }
}