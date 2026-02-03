package com.ndumas.appdt.di

import com.ndumas.appdt.data.auth.local.EncryptedSessionStorage
import com.ndumas.appdt.domain.auth.repository.SessionStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {
    @Binds
    @Singleton
    abstract fun bindSessionStorage(encryptedSessionStorage: EncryptedSessionStorage): SessionStorage
}
