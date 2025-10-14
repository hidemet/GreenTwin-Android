package com.ndumas.greentwin.data.remote

import com.ndumas.greentwin.data.remote.dto.LoginRequest
import javax.inject.Inject

/**
 * DataSource che gestisce le operazioni di rete per l'autenticazione.
 * Incapsula la logica di chiamata all'API service, facendo da tramite
 * tra il Repository e Retrofit.
 */
class AuthRemoteDataSource @Inject constructor(
    private val authApiService: AuthApiService
) {

    /**
     * Esegue la chiamata di login tramite l'AuthApiService.
     * @param loginRequest L'oggetto con le credenziali dell'utente.
     * @return La risposta dal server contenente il token.
     */
    suspend fun login(loginRequest: LoginRequest) = authApiService.login(loginRequest)
}
