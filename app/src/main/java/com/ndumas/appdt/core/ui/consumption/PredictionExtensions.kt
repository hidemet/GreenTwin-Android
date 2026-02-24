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
    val (bgRes, textColorRes, iconRes) =
        when (model.state) {
            PredictionState.POSITIVE -> Triple(R.drawable.bg_badge_green, R.color.badge_success_text, R.drawable.ic_trending_down)
            PredictionState.NEGATIVE -> Triple(R.drawable.bg_badge_error, R.color.badge_error_text, R.drawable.ic_trending_up)
            PredictionState.NEUTRAL -> Triple(R.drawable.bg_badge_neutral, R.color.badge_neutral_text, R.drawable.ic_trending_flat)
        }

    this.setBackgroundResource(bgRes)
    this.setTextColor(ContextCompat.getColor(this.context, textColorRes))

    // Imposta icona trending con tint appropriato
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(iconRes, 0, 0, 0)
    this.compoundDrawableTintList = ContextCompat.getColorStateList(this.context, textColorRes)
}
