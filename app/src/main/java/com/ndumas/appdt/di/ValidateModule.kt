package com.ndumas.appdt.di

import com.ndumas.appdt.core.validation.AndroidEmailPatternValidator
import com.ndumas.appdt.core.validation.EmailPatternValidator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ValidationModule {
    @Binds
    @Singleton
    abstract fun bindEmailPatternValidator(androidEmailPatternValidator: AndroidEmailPatternValidator): EmailPatternValidator
}
