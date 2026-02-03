package com.ndumas.appdt.di

import com.ndumas.appdt.core.util.Constants.BASE_URL
import com.ndumas.appdt.data.auth.remote.AuthApiService
import com.ndumas.appdt.data.auth.remote.AuthInterceptor
import com.ndumas.appdt.data.automation.remote.AutomationApiService
import com.ndumas.appdt.data.consumption.remote.ConsumptionApiService
import com.ndumas.appdt.data.device.remote.DeviceApiService
import com.ndumas.appdt.data.entity.remote.EntityApiService
import com.ndumas.appdt.data.service.remote.ServiceApiService
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
object NetworkModule {
    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(authInterceptor)
            // AUMENTA A 60 SECONDI (O ANCHE 120 SE SERVE)
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService = retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideDeviceApiService(retrofit: Retrofit): DeviceApiService = retrofit.create(DeviceApiService::class.java)

    @Provides
    @Singleton
    fun provideConsumptionApiService(retrofit: Retrofit): ConsumptionApiService = retrofit.create(ConsumptionApiService::class.java)

    @Provides
    @Singleton
    fun provideEntityApiService(retrofit: Retrofit): EntityApiService = retrofit.create(EntityApiService::class.java)

    @Provides
    @Singleton
    fun provideServiceApiService(retrofit: Retrofit): ServiceApiService = retrofit.create(ServiceApiService::class.java)

    @Provides
    @Singleton
    fun provideAutomationApiService(retrofit: Retrofit): AutomationApiService = retrofit.create(AutomationApiService::class.java)
}
