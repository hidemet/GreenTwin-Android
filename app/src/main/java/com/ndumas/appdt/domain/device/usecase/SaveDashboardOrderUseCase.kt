package com.ndumas.appdt.domain.device.usecase

import com.ndumas.appdt.domain.device.repository.DashboardPreferencesRepository
import javax.inject.Inject

class SaveDashboardOrderUseCase
    @Inject
    constructor(
        private val repository: DashboardPreferencesRepository,
    ) {
        suspend operator fun invoke(ids: List<String>) {
            repository.saveDashboardOrder(ids)
        }
    }
