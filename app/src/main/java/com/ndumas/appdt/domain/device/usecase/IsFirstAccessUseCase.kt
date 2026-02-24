package com.ndumas.appdt.domain.device.usecase

import com.ndumas.appdt.domain.device.repository.DashboardPreferencesRepository
import javax.inject.Inject

class IsFirstAccessUseCase
    @Inject
    constructor(
        private val repository: DashboardPreferencesRepository,
    ) {
        suspend operator fun invoke(): Boolean = repository.isFirstAccess()
    }
