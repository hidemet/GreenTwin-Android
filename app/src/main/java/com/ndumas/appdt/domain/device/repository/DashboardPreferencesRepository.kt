package com.ndumas.appdt.domain.device.repository

import kotlinx.coroutines.flow.Flow

interface DashboardPreferencesRepository {
    suspend fun saveDashboardOrder(ids: List<String>)

    fun getDashboardOrder(): Flow<List<String>>

    suspend fun isFirstAccess(): Boolean

    suspend fun markFirstAccessComplete()

    fun getHiddenSections(): Flow<Set<String>>

    suspend fun saveHiddenSections(sections: Set<String>)
}
