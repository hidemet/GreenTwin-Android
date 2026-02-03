package com.ndumas.appdt.presentation.automation.create.mapper

import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.automation.model.SolarEvent
import javax.inject.Inject
import kotlin.math.abs

class SolarUiMapper
    @Inject
    constructor() {
        fun mapToLabel(
            offsetMinutes: Long,
            event: SolarEvent,
        ): UiText {
            // Caso Base: 0
            if (offsetMinutes == 0L) {
                return if (event == SolarEvent.SUNRISE) {
                    UiText.StringResource(R.string.sun_at_sunrise)
                } else {
                    UiText.StringResource(R.string.sun_at_sunset)
                }
            }

            val absMinutes = abs(offsetMinutes)
            val hours = absMinutes / 60
            val minutes = absMinutes % 60
            val isNegative = offsetMinutes < 0

            return if (event == SolarEvent.SUNRISE) {
                getSunriseLabel(isNegative, hours, minutes)
            } else {
                getSunsetLabel(isNegative, hours, minutes)
            }
        }

        private fun getSunriseLabel(
            isNegative: Boolean,
            hours: Long,
            minutes: Long,
        ): UiText =
            if (isNegative) {
                when {
                    hours > 0 && minutes > 0 -> UiText.StringResource(R.string.sun_before_sunrise_hm, hours, minutes)
                    hours > 0 -> UiText.StringResource(R.string.sun_before_sunrise_h, hours)
                    else -> UiText.StringResource(R.string.sun_before_sunrise_m, minutes)
                }
            } else {
                when {
                    hours > 0 && minutes > 0 -> UiText.StringResource(R.string.sun_after_sunrise_hm, hours, minutes)
                    hours > 0 -> UiText.StringResource(R.string.sun_after_sunrise_h, hours)
                    else -> UiText.StringResource(R.string.sun_after_sunrise_m, minutes)
                }
            }

        private fun getSunsetLabel(
            isNegative: Boolean,
            hours: Long,
            minutes: Long,
        ): UiText =
            if (isNegative) {
                when {
                    hours > 0 && minutes > 0 -> UiText.StringResource(R.string.sun_before_sunset_hm, hours, minutes)
                    hours > 0 -> UiText.StringResource(R.string.sun_before_sunset_h, hours)
                    else -> UiText.StringResource(R.string.sun_before_sunset_m, minutes)
                }
            } else {
                when {
                    hours > 0 && minutes > 0 -> UiText.StringResource(R.string.sun_after_sunset_hm, hours, minutes)
                    hours > 0 -> UiText.StringResource(R.string.sun_after_sunset_h, hours)
                    else -> UiText.StringResource(R.string.sun_after_sunset_m, minutes)
                }
            }
    }
