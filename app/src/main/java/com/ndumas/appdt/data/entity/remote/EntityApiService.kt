package com.ndumas.appdt.data.entity.remote

import com.ndumas.appdt.data.entity.remote.dto.EntityStateDto
import retrofit2.http.GET
import retrofit2.http.Path

interface EntityApiService {
    // Chiama l'endpoint del backend: @entity_router.get("/{entity_id}")
    @GET("entity/{entity_id}")
    suspend fun getEntityDetails(
        @Path("entity_id") entityId: String,
    ): EntityStateDto
}
