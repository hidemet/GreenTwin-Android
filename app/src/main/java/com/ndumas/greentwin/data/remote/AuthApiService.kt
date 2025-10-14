package com.ndumas.greentwin.data.remote

import com.ndumas.greentwin.data.remote.dto.LoginRequest
import com.ndumas.greentwin.data.remote.dto.LoginResponse
import retrofit2.http.GET

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interfaccia Retrofit che definisce gli endpoint dell'API del Digital Twin
 * Definiamo "cosa" vogliamo chiedere al server.
 **/
interface AuthApiService {
    /**
     *  Esegue il login dell'utente.
     *  @param request il corpo della richiesta contenente le credenziali dell'utente.
     *  @return una stringa rappresentante il token di autenticazione.
     */

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

}