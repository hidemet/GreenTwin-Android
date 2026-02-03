package com.ndumas.appdt.domain.auth.usecase

import com.ndumas.appdt.domain.auth.repository.SessionStorage
import javax.inject.Inject

class CheckAuthStateUseCase
    @Inject
    constructor(
        private val sessionStorage: SessionStorage,
    ) {
        suspend operator fun invoke(): Boolean {
            val token = sessionStorage.getToken()
            // Qui potremmo aggiungere logica extra (es. controllare la scadenza del JWT)
            return !token.isNullOrBlank()
        }
    }
