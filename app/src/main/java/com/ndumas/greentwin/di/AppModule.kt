package com.ndumas.greentwin.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Qui, nelle prossime sessioni, aggiungeremo le funzioni @Provides
    // per fornire Retrofit, Moshi, i Repository, gli Use Case, etc.
    // Esempio:
    //
    // @Provides
    // @Singleton
    // fun provideMyApi(): MyApi {
    //     return Retrofit.Builder()
    //            .baseUrl("https://example.com")
    //            .build()
    //            .create(MyApi::class.java)
    // }

}