package com.ndumas.appdt.data.entity.remote.source

import com.ndumas.appdt.data.entity.remote.EntityApiService
import com.ndumas.appdt.data.entity.remote.dto.EntityStateDto
import javax.inject.Inject

class EntityRemoteDataSourceImpl
    @Inject
    constructor(
        private val api: EntityApiService,
    ) : EntityRemoteDataSource {
        override suspend fun getEntityDetails(entityId: String): EntityStateDto = api.getEntityDetails(entityId)
    }
