package com.ndumas.appdt.core.ui.consumption

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ndumas.appdt.R
import com.ndumas.appdt.presentation.consumption.model.PredictionState
import com.ndumas.appdt.presentation.consumption.model.PredictionUiModel

fun TextView.bindPrediction(model: PredictionUiModel) {
    if (!model.isVisible) {
        this.visibility = View.GONE
        return
    }
    this.visibility = View.VISIBLE

    this.text = model.text.asString(this.context)

    // Risoluzione stile
    val (bgRes, textColorRes) =
        when (model.state) {
            PredictionState.POSITIVE -> R.drawable.bg_badge_green to R.color.badge_success_text
            PredictionState.NEGATIVE -> R.drawable.bg_badge_error to R.color.badge_error_text
            PredictionState.NEUTRAL -> R.drawable.bg_badge_neutral to R.color.badge_neutral_text
        }

    this.setBackgroundResource(bgRes)
    this.setTextColor(ContextCompat.getColor(this.context, textColorRes))
}
