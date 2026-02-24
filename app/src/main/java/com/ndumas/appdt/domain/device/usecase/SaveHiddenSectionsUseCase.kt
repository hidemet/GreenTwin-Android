package com.ndumas.appdt.domain.device.usecase

import com.ndumas.appdt.domain.device.repository.DashboardPreferencesRepository
import com.ndumas.appdt.presentation.home.model.DashboardSectionType
import javax.inject.Inject

class SaveHiddenSectionsUseCase
    @Inject
    constructor(
        private val repository: DashboardPreferencesRepository,
    ) {
        suspend operator fun invoke(sections: Set<DashboardSectionType>) {
            val stringSet = sections.map { it.name }.toSet()
            repository.saveHiddenSections(stringSet)
        }
    }
