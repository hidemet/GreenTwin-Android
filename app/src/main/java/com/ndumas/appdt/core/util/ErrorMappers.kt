package com.ndumas.appdt.core.util

import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.auth.usecase.validation.ValidatePasswordUseCase
import com.ndumas.appdt.domain.error.DataError
import com.ndumas.appdt.domain.error.Error
import com.ndumas.appdt.domain.error.ValidationError

fun ValidationError.User.EmailError.asUiText(): UiText =
    when (this) {
        ValidationError.User.EmailError.EMPTY -> UiText.StringResource(R.string.error_email_empty)
        ValidationError.User.EmailError.INVALID_FORMAT -> UiText.StringResource(R.string.error_email_invalid_format)
    }

fun ValidationError.User.PasswordError.asUiText(): UiText =
    when (this) {
        ValidationError.User.PasswordError.EMPTY -> {
            UiText.StringResource(R.string.error_password_empty)
        }

        ValidationError.User.PasswordError.TOO_SHORT -> {
            UiText.StringResource(
                R.string.error_password_too_short,
                ValidatePasswordUseCase.MIN_PASSWORD_LENGTH,
            )
        }
    }

fun DataError.asUiText(): UiText =
    when (this) {
        is DataError.Network -> {
            when (this) {
                DataError.Network.NO_INTERNET -> UiText.StringResource(R.string.error_no_internet)
                DataError.Network.SERVER_UNAVAILABLE -> UiText.StringResource(R.string.error_server_unavailable)
                DataError.Network.UNKNOWN -> UiText.StringResource(R.string.error_unknown)
            }
        }

        is DataError.Auth -> {
            when (this) {
                DataError.Auth.INVALID_CREDENTIALS -> UiText.StringResource(R.string.error_invalid_credentials)
                DataError.Auth.UNAUTHORIZED -> UiText.StringResource(R.string.error_unauthorized)
                DataError.Auth.USER_NOT_FOUND -> UiText.StringResource(R.string.error_user_not_found)
                DataError.Auth.EMAIL_ALREADY_EXISTS -> UiText.StringResource(R.string.error_email_exists)
            }
        }

        is DataError.Validation -> {
            when (this) {
                DataError.Validation.EMPTY_NAME -> UiText.DynamicString("Il nome non può essere vuoto")
                DataError.Validation.MISSING_TRIGGER -> UiText.DynamicString("Manca il trigger di attivazione")
                DataError.Validation.NO_ACTIONS -> UiText.DynamicString("L'automazione deve avere almeno un'azione")
                DataError.Validation.INVALID_DRAFT -> UiText.DynamicString("Il draf non è valido")
                DataError.Validation.NOT_FOUND -> UiText.StringResource(R.string.error_not_found)
            }
        }
    }

fun Error.asUiText(): UiText =
    when (this) {
        is DataError -> this.asUiText()
        is ValidationError.User.EmailError -> this.asUiText()
        is ValidationError.User.PasswordError -> this.asUiText()
    }
