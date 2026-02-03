package com.ndumas.appdt.domain.device.usecase

import com.ndumas.appdt.domain.device.repository.DashboardPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedOrderUseCase
    @Inject
    constructor(
        private val repository: DashboardPreferencesRepository,
    ) {
        operator fun invoke(): Flow<List<String>> = repository.getDashboardOrder()
    }
