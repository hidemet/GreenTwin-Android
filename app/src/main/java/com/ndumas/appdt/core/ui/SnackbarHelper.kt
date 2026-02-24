package com.ndumas.appdt.core.ui

import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.ndumas.appdt.R

/**
 * Helper centralizzato per la gestione delle Snackbar nell'app.
 * Garantisce coerenza visiva e comportamentale seguendo i principi di Nielsen
 * (Visibilità dello stato, Controllo utente, Coerenza).
 *
 * Durate:
 * - SUCCESS: LENGTH_SHORT (conferme rapide)
 * - ERROR: LENGTH_LONG (tempo per leggere l'errore)
 * - INFO: LENGTH_SHORT (informazioni generiche)
 * - UNDO: LENGTH_LONG (tempo per annullare, con action)
 */
object SnackbarHelper {
    /**
     * Mostra una Snackbar di successo (es. "Salvato", "Eliminato").
     * Durata: SHORT
     */
    fun showSuccess(
        view: View,
        message: String,
    ): Snackbar = createSnackbar(view, message, Snackbar.LENGTH_SHORT, SnackbarType.SUCCESS).also { it.show() }

    fun showSuccess(
        view: View,
        @StringRes messageRes: Int,
    ): Snackbar = showSuccess(view, view.context.getString(messageRes))

    fun showSuccess(
        view: View,
        message: UiText,
    ): Snackbar = showSuccess(view, message.asString(view.context))

    /**
     * Mostra una Snackbar di errore.
     * Durata: LONG
     */
    fun showError(
        view: View,
        message: String,
    ): Snackbar = createSnackbar(view, message, Snackbar.LENGTH_LONG, SnackbarType.ERROR).also { it.show() }

    fun showError(
        view: View,
        @StringRes messageRes: Int,
    ): Snackbar = showError(view, view.context.getString(messageRes))

    fun showError(
        view: View,
        message: UiText,
    ): Snackbar = showError(view, message.asString(view.context))

    /**
     * Mostra una Snackbar informativa.
     * Durata: SHORT
     */
    fun showInfo(
        view: View,
        message: String,
    ): Snackbar = createSnackbar(view, message, Snackbar.LENGTH_SHORT, SnackbarType.INFO).also { it.show() }

    fun showInfo(
        view: View,
        @StringRes messageRes: Int,
    ): Snackbar = showInfo(view, view.context.getString(messageRes))

    fun showInfo(
        view: View,
        message: UiText,
    ): Snackbar = showInfo(view, message.asString(view.context))

    /**
     * Mostra una Snackbar con azione di undo.
     * Durata: LONG (5 secondi per dare tempo all'utente di annullare)
     * Supporta il principio di Nielsen "Controllo e libertà dell'utente".
     */
    fun showWithUndo(
        view: View,
        message: String,
        onUndo: () -> Unit,
    ): Snackbar {
        val snackbar = createSnackbar(view, message, Snackbar.LENGTH_LONG, SnackbarType.INFO)
        snackbar.setAction(R.string.snackbar_undo) { onUndo() }
        snackbar.show()
        return snackbar
    }

    fun showWithUndo(
        view: View,
        @StringRes messageRes: Int,
        onUndo: () -> Unit,
    ): Snackbar = showWithUndo(view, view.context.getString(messageRes), onUndo)

    fun showWithUndo(
        view: View,
        message: UiText,
        onUndo: () -> Unit,
    ): Snackbar = showWithUndo(view, message.asString(view.context), onUndo)

    /**
     * Mostra una Snackbar con azione personalizzata e durata indefinita.
     * Utile per errori che richiedono un'azione (es. "Riprova").
     */
    fun showWithAction(
        view: View,
        message: String,
        actionText: String,
        onAction: () -> Unit,
    ): Snackbar {
        val snackbar = createSnackbar(view, message, Snackbar.LENGTH_INDEFINITE, SnackbarType.INFO)
        snackbar.setAction(actionText) { onAction() }
        snackbar.show()
        return snackbar
    }

    fun showWithAction(
        view: View,
        message: UiText,
        @StringRes actionTextRes: Int,
        onAction: () -> Unit,
    ): Snackbar = showWithAction(view, message.asString(view.context), view.context.getString(actionTextRes), onAction)

    private fun createSnackbar(
        view: View,
        message: String,
        duration: Int,
        type: SnackbarType,
    ): Snackbar {
        val context = view.context
        val snackbar = Snackbar.make(view, message, duration)

        when (type) {
            SnackbarType.SUCCESS -> {
                snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.snackbar_background))
                snackbar.setTextColor(ContextCompat.getColor(context, R.color.snackbar_text))
                snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.snackbar_action))
            }

            SnackbarType.ERROR -> {
                snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.snackbar_background_error))
                snackbar.setTextColor(ContextCompat.getColor(context, R.color.snackbar_text_error))
                snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.snackbar_action))
            }

            SnackbarType.INFO -> {
                snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.snackbar_background))
                snackbar.setTextColor(ContextCompat.getColor(context, R.color.snackbar_text))
                snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.snackbar_action))
            }
        }

        return snackbar
    }

    private enum class SnackbarType {
        SUCCESS,
        ERROR,
        INFO,
    }
}
