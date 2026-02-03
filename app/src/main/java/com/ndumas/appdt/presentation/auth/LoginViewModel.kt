package com.ndumas.appdt.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.core.util.asUiText
import com.ndumas.appdt.domain.auth.usecase.LoginUseCase
import com.ndumas.appdt.domain.auth.usecase.RegisterUseCase
import com.ndumas.appdt.domain.auth.usecase.validation.ValidateEmailUseCase
import com.ndumas.appdt.domain.auth.usecase.validation.ValidatePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val loginUseCase: LoginUseCase,
        private val registerUseCase: RegisterUseCase,
        private val validateEmailUseCase: ValidateEmailUseCase,
        private val validatePasswordUseCase: ValidatePasswordUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoginUiState())
        val uiState = _uiState.asStateFlow()

        private val _uiEvent = Channel<LoginUiEvent>()
        val uiEvent = _uiEvent.receiveAsFlow()

        fun onEvent(event: LoginEvent) {
            when (event) {
                is LoginEvent.EmailChanged -> {
                    _uiState.update { it.copy(email = event.email, emailError = null) }
                }

                is LoginEvent.PasswordChanged -> {
                    _uiState.update { it.copy(password = event.password, passwordError = null) }
                }

                LoginEvent.OnLoginClick -> {
                    submitLogin()
                }
            }
        }

        private fun submitLogin() {
            if (!validateFields()) {
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                when (val result = loginUseCase(uiState.value.email, uiState.value.password).first()) {
                    is Result.Success -> {
                        _uiEvent.send(LoginUiEvent.LoginSuccess)
                    }

                    is Result.Error -> {
                        _uiEvent.send(LoginUiEvent.ShowError(result.error.asUiText()))
                    }
                }
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        private fun validateFields(): Boolean {
            val emailResult = validateEmailUseCase(uiState.value.email)
            val passwordResult = validatePasswordUseCase(uiState.value.password)

            val emailError = if (emailResult is Result.Error) emailResult.error.asUiText() else null
            val passwordError = if (passwordResult is Result.Error) passwordResult.error.asUiText() else null

            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                )
            }

            return emailError == null && passwordError == null
        }
    }

data class LoginUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val emailError: UiText? = null,
    val password: String = "",
    val passwordError: UiText? = null,
)

sealed interface LoginUiEvent {
    data class ShowError(
        val message: UiText,
    ) : LoginUiEvent

    data object LoginSuccess : LoginUiEvent
}

sealed interface LoginEvent {
    data class EmailChanged(
        val email: String,
    ) : LoginEvent

    data class PasswordChanged(
        val password: String,
    ) : LoginEvent

    data object OnLoginClick : LoginEvent
}
