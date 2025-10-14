package com.ndumas.greentwin.di

import com.ndumas.greentwin.core.util.Constants.BASE_URL
import com.ndumas.greentwin.data.remote.AuthApiService
import com.ndumas.greentwin.data.remote.AuthInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // SessionManager e AuthInterceptor vengono iniettati automaticamente da Hilt
    // grazie all'annotazione @Inject sui loro costruttori. Non è necessario
    // un @Provides esplicito per loro.

    /**
     * Fornisce l'istanza di Moshi per la serializzazione/deserializzazione JSON.
     * È configurato con il KotlinJsonAdapterFactory per supportare le data class di Kotlin.
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * Fornisce il client OkHttpClient.
     * L'AuthInterceptor viene iniettato automaticamente da Hilt.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            // Qui in futuro potremmo aggiungere altri interceptor, es. per il logging.
            .build()
    }

    /**
     * Fornisce l'istanza di Retrofit, configurata con la BASE_URL, il client OkHttp
     * e il converter Moshi.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * Fornisce l'implementazione concreta dell'interfaccia AuthApiService.
     * Retrofit si occupa di generare il codice necessario.
     */
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
}
