package com.ndumas.appdt.di

import com.ndumas.appdt.data.consumption.repository.FakeConsumptionRepository
import com.ndumas.appdt.domain.consumption.repository.ConsumptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module che inietta il FakeConsumptionRepository.
 * Usare questo modulo per testing locale con dati mock realistici.
 *
 * Per switchare alla versione reale:
 * 1. Commentare questo modulo
 * 2. Decommentare RealConsumptionModule
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FakeConsumptionModule {
    @Binds
    @Singleton
    abstract fun bindConsumptionRepository(impl: FakeConsumptionRepository): ConsumptionRepository
}
