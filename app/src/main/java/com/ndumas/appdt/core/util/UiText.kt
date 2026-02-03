package com.ndumas.appdt.core.ui

import android.content.Context
import androidx.annotation.StringRes

sealed class UiText {
    data class DynamicString(
        val value: String,
    ) : UiText()

    class StringResource(
        @StringRes val id: Int,
        vararg val args: Any,
    ) : UiText()

    fun asString(context: Context): String =
        when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(id, *args)
        }
}
