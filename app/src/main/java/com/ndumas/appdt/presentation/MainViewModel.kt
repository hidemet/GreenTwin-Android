package com.ndumas.appdt.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndumas.appdt.domain.auth.usecase.CheckAuthStateUseCase
import com.ndumas.appdt.domain.auth.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MainUiEvent {
    data object NavigateToLogin : MainUiEvent
}

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val checkAuthStateUseCase: CheckAuthStateUseCase,
        private val logoutUseCase: LogoutUseCase,
    ) : ViewModel() {
        private val _isLoading = MutableStateFlow(true)
        val isLoading = _isLoading.asStateFlow()

        private val _isUserLoggedIn = MutableStateFlow(false)
        val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

        private val _uiEvent = MutableSharedFlow<MainUiEvent>()
        val uiEvent = _uiEvent.asSharedFlow()

        init {
            checkAuthStatus()
        }

        private fun checkAuthStatus() {
            viewModelScope.launch {
                val isLoggedIn = checkAuthStateUseCase()
                _isUserLoggedIn.value = isLoggedIn

                delay(100)

                _isLoading.value = false
            }
        }

        fun logout() {
            viewModelScope.launch {
                logoutUseCase()
                _isUserLoggedIn.value = false
                _uiEvent.emit(MainUiEvent.NavigateToLogin)
            }
        }
    }
