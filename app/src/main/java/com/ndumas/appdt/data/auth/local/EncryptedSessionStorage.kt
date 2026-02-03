package com.ndumas.appdt.data.auth.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ndumas.appdt.domain.auth.repository.SessionStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedSessionStorage
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : SessionStorage {
        // La MasterKey gestisce la chiave di cifratura
        private val masterKey =
            MasterKey
                .Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        private val sharedPreferences: SharedPreferences =
            EncryptedSharedPreferences.create(
                context,
                "secret_shared_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Cifratura delle chiavi
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM, // Cifratura dei valori
            )

        companion object {
            private const val KEY_TOKEN = "auth_token"
        }

        override suspend fun saveToken(token: String) =
            withContext(Dispatchers.IO) {
                sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
            }

        override suspend fun getToken(): String? =
            withContext(Dispatchers.IO) {
                sharedPreferences.getString(KEY_TOKEN, null)
            }

        override suspend fun clearToken() =
            withContext(Dispatchers.IO) {
                sharedPreferences.edit().remove(KEY_TOKEN).apply()
            }
    }
