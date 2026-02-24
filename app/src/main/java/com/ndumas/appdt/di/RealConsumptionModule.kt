package com.ndumas.appdt.di

/*
 * Hilt Module che inietta il ConsumptionRepositoryImpl (versione reale).
 * Usare questo modulo per collegarsi al backend e recuperare dati reali.
 *
 * Per attivare questo modulo:
 * 1. Decommentare il codice sotto
 * 2. Commentare FakeConsumptionModule
 *
 * NOTA: Questo modulo richiede che il backend sia raggiungibile e configurato.
 */

/*
import com.ndumas.appdt.data.consumption.repository.ConsumptionRepositoryImpl
import com.ndumas.appdt.domain.consumption.repository.ConsumptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RealConsumptionModule {

    @Binds
    @Singleton
    abstract fun bindConsumptionRepository(impl: ConsumptionRepositoryImpl): ConsumptionRepository
}
*/
