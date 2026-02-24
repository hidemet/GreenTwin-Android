package com.ndumas.appdt.data.device.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ndumas.appdt.domain.device.repository.DashboardPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
            private val KEY_FIRST_ACCESS = booleanPreferencesKey("first_access_completed")
            private val KEY_HIDDEN_SECTIONS = stringPreferencesKey("hidden_sections")
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

        override suspend fun isFirstAccess(): Boolean = context.dataStore.data.first()[KEY_FIRST_ACCESS] ?: true

        override suspend fun markFirstAccessComplete() {
            context.dataStore.edit { preferences ->
                preferences[KEY_FIRST_ACCESS] = false
            }
        }

        override fun getHiddenSections(): Flow<Set<String>> =
            context.dataStore.data
                .map { preferences ->
                    val jsonString = preferences[KEY_HIDDEN_SECTIONS] ?: return@map emptySet()
                    try {
                        val jsonArray = JSONArray(jsonString)
                        val set = mutableSetOf<String>()
                        for (i in 0 until jsonArray.length()) {
                            set.add(jsonArray.getString(i))
                        }
                        set
                    } catch (e: Exception) {
                        emptySet()
                    }
                }

        override suspend fun saveHiddenSections(sections: Set<String>) {
            val jsonArray = JSONArray(sections.toList())
            context.dataStore.edit { preferences ->
                preferences[KEY_HIDDEN_SECTIONS] = jsonArray.toString()
            }
        }
    }
