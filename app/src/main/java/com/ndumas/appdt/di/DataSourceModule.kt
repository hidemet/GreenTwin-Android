package com.ndumas.appdt.di

import com.ndumas.appdt.data.consumption.remote.source.ConsumptionRemoteDataSource
import com.ndumas.appdt.data.consumption.remote.source.ConsumptionRemoteDataSourceImpl
import com.ndumas.appdt.data.device.remote.source.DeviceRemoteDataSource
import com.ndumas.appdt.data.device.remote.source.DeviceRemoteDataSourceImpl
import com.ndumas.appdt.data.entity.remote.source.EntityRemoteDataSource
import com.ndumas.appdt.data.entity.remote.source.EntityRemoteDataSourceImpl
import com.ndumas.appdt.data.service.remote.source.ServiceRemoteDataSource
import com.ndumas.appdt.data.service.remote.source.ServiceRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun bindDeviceRemoteDataSource(impl: DeviceRemoteDataSourceImpl): DeviceRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindConsumptionRemoteDataSource(impl: ConsumptionRemoteDataSourceImpl): ConsumptionRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindServiceRemoteDataSource(impl: ServiceRemoteDataSourceImpl): ServiceRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindEntityRemoteDataSource(impl: EntityRemoteDataSourceImpl): EntityRemoteDataSource
}
