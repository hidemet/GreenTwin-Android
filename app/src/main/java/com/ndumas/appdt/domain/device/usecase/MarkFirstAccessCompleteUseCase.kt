package com.ndumas.appdt.domain.device.usecase

import com.ndumas.appdt.domain.device.repository.DashboardPreferencesRepository
import javax.inject.Inject

class MarkFirstAccessCompleteUseCase
    @Inject
    constructor(
        private val repository: DashboardPreferencesRepository,
    ) {
        suspend operator fun invoke() {
            repository.markFirstAccessComplete()
        }
    }
