package com.ndumas.appdt.presentation.automation.util

import android.content.Context
import com.ndumas.appdt.R

/**
 * Utility per tradurre i nomi dei servizi delle automazioni dall'inglese all'italiano.
 */
object AutomationServiceTranslator {
    fun translateService(
        context: Context,
        service: String,
    ): String =
        when (service) {
            "turn_on" -> context.getString(R.string.action_turn_on)
            "turn_off" -> context.getString(R.string.action_turn_off)
            "toggle" -> context.getString(R.string.action_toggle)
            else -> service // Fallback: ritorna il servizio originale
        }
}
