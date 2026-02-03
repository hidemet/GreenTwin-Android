package com.ndumas.appdt.data.entity.remote.source

import com.ndumas.appdt.data.entity.remote.dto.EntityStateDto

interface EntityRemoteDataSource {
    suspend fun getEntityDetails(entityId: String): EntityStateDto
}
