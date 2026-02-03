package com.ndumas.appdt.domain.device.repository

import kotlinx.coroutines.flow.Flow

interface DashboardPreferencesRepository {
    suspend fun saveDashboardOrder(ids: List<String>)

    fun getDashboardOrder(): Flow<List<String>>
}
