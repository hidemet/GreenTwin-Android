package com.ndumas.appdt.domain.device.usecase

import com.ndumas.appdt.domain.device.repository.DashboardPreferencesRepository
import com.ndumas.appdt.presentation.home.model.DashboardSectionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetHiddenSectionsUseCase
    @Inject
    constructor(
        private val repository: DashboardPreferencesRepository,
    ) {
        operator fun invoke(): Flow<Set<DashboardSectionType>> =
            repository.getHiddenSections().map { stringSet ->
                stringSet
                    .mapNotNull { name ->
                        try {
                            DashboardSectionType.valueOf(name)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }.toSet()
            }
    }
