package com.ndumas.appdt.data.device.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ndumas.appdt.domain.device.repository.DashboardPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dashboard_settings")

@Singleton
class DataStoreDashboardPreferences
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : DashboardPreferencesRepository {
        companion object {
            private val KEY_WIDGET_ORDER = stringPreferencesKey("dashboard_widget_order")
        }

        override fun getDashboardOrder(): Flow<List<String>> =
            context.dataStore.data
                .map { preferences ->
                    val jsonString = preferences[KEY_WIDGET_ORDER] ?: return@map emptyList()
                    try {
                        val jsonArray = JSONArray(jsonString)
                        val list = mutableListOf<String>()
                        for (i in 0 until jsonArray.length()) {
                            list.add(jsonArray.getString(i))
                        }
                        list
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

        override suspend fun saveDashboardOrder(ids: List<String>) {
            val jsonArray = JSONArray(ids)
            context.dataStore.edit { preferences ->
                preferences[KEY_WIDGET_ORDER] = jsonArray.toString()
            }
        }
    }
