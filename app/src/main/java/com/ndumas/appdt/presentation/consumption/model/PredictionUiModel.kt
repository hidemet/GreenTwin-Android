package com.ndumas.appdt.presentation.consumption.model

import com.ndumas.appdt.core.ui.UiText

data class PredictionUiModel(
    val text: UiText,
    val state: PredictionState,
    val isVisible: Boolean,
) {
    companion object {
        val Hidden =
            PredictionUiModel(
                text = UiText.DynamicString(""),
                state = PredictionState.NEUTRAL,
                isVisible = false,
            )
    }
}
