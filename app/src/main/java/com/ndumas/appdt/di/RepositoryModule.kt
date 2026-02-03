package com.ndumas.appdt.di

import com.ndumas.appdt.data.auth.repository.AuthRepositoryImpl
import com.ndumas.appdt.data.automation.repository.AutomationRepositoryImpl
import com.ndumas.appdt.domain.automation.repository.AutomationRepository
// import com.ndumas.greentwin.data.consumption.repository.ConsumptionRepositoryImpl
// import com.ndumas.greentwin.data.consumption.repository.ConsumptionRepositoryImpl
import com.ndumas.appdt.data.consumption.repository.FakeConsumptionRepository
import com.ndumas.appdt.data.device.local.DataStoreDashboardPreferences
import com.ndumas.appdt.data.device.repository.DeviceRepositoryImpl
import com.ndumas.appdt.data.service.repository.ServiceRepositoryImpl
import com.ndumas.appdt.domain.auth.repository.AuthRepository
import com.ndumas.appdt.domain.consumption.repository.ConsumptionRepository
import com.ndumas.appdt.domain.device.repository.DashboardPreferencesRepository
import com.ndumas.appdt.domain.device.repository.DeviceRepository
import com.ndumas.appdt.domain.service.repository.ServiceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    //    @Binds
//    @Singleton
//    abstract fun bindConsumptionRepository(impl: ConsumptionRepositoryImpl): ConsumptionRepository

    @Binds
    @Singleton
    //    abstract fun bindConsumptionRepository(impl: ConsumptionRepositoryImpl): ConsumptionRepository
    abstract fun bindFakeConsumptionRepository(impl: FakeConsumptionRepository): ConsumptionRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
//    abstract fun bindDeviceRepository(impl: FakeDeviceRepository): DeviceRepository
    abstract fun bindDeviceRepository(deviceRepositoryImpl: DeviceRepositoryImpl): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindDashboardPreferencesRepository(impl: DataStoreDashboardPreferences): DashboardPreferencesRepository

    @Binds
    @Singleton
//    abstract fun bindServiceRepository(impl: FakeServiceRepository): ServiceRepository
    abstract fun bindServiceRepository(impl: ServiceRepositoryImpl): ServiceRepository

    @Binds
    @Singleton
    abstract fun bindAutomationRepository(impl: AutomationRepositoryImpl): AutomationRepository
//    abstract fun bindFakeAutomationRepository(impl: FakeAutomationRepository): AutomationRepository
}
