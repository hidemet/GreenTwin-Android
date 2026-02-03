package com.ndumas.appdt.presentation.consumption.formatter

import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.presentation.consumption.ConsumptionTimeFilter
import com.ndumas.appdt.presentation.consumption.model.PredictionState
import com.ndumas.appdt.presentation.consumption.model.PredictionUiModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

class ConsumptionUiFormatter
    @Inject
    constructor() {
        private companion object {
            const val PERCENTAGE_MULTIPLIER = 100.0
        }

        fun formatEnergy(kwh: Double): UiText {
            val format = if (kwh < 10.0 && kwh > 0.0) "%.1f" else "%.0f"

            return UiText.DynamicString(String.format(Locale.US, "$format kWh", kwh))
        }

        fun formatCost(cost: Double): UiText = UiText.StringResource(R.string.format_cost_euro, cost)

        fun formatDateRange(
            date: LocalDate,
            filter: ConsumptionTimeFilter,
        ): UiText {
            val locale = Locale.ITALY

            return when (filter) {
                ConsumptionTimeFilter.TODAY -> {
                    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
                    val formatted = date.format(formatter)

                    UiText.DynamicString(formatted.replaceFirstChar { it.titlecase(locale) })
                }

                ConsumptionTimeFilter.WEEK -> {
                    val formatter = DateTimeFormatter.ofPattern("d MMM", locale)
                    val formattedDate = date.format(formatter)

                    val cleanDate = formattedDate.replaceFirstChar { it.titlecase(locale) }

                    UiText.DynamicString("Settimana del $cleanDate")
                }

                ConsumptionTimeFilter.MONTH -> {
                    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
                    val formatted = date.format(formatter)
                    UiText.DynamicString(formatted.replaceFirstChar { it.titlecase(locale) })
                }

                ConsumptionTimeFilter.YEAR -> {
                    val formatter = DateTimeFormatter.ofPattern("yyyy", locale)
                    UiText.DynamicString(date.format(formatter))
                }
            }
        }

        fun formatSingleDate(
            dateString: String,
            filter: ConsumptionTimeFilter,
        ): UiText {
            val locale = Locale.ITALY
            return try {
                when (filter) {
                    ConsumptionTimeFilter.TODAY -> {
                        val cleanString = dateString.replace(" ", "T")
                        val dateTime = java.time.LocalDateTime.parse(cleanString)
                        val fmt = DateTimeFormatter.ofPattern("HH:00", locale)
                        UiText.DynamicString(dateTime.format(fmt))
                    }

                    ConsumptionTimeFilter.WEEK, ConsumptionTimeFilter.MONTH -> {
                        val date = LocalDate.parse(dateString)
                        val fmt = DateTimeFormatter.ofPattern("EEE d MMM", locale)
                        UiText.DynamicString(date.format(fmt).capitalizeFirstLetter())
                    }

                    ConsumptionTimeFilter.YEAR -> {
                        val date = LocalDate.parse(dateString)
                        val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
                        UiText.DynamicString(date.format(fmt).capitalizeFirstLetter())
                    }
                }
            } catch (e: Exception) {
                UiText.DynamicString(dateString)
            }
        }

        fun formatPredictionBadge(
            actual: Double,
            predicted: Double,
        ): Pair<UiText, PredictionState> {
            if (predicted <= 0 || actual <= 0) {
                return Pair(UiText.DynamicString("--%"), PredictionState.NEUTRAL)
            }

            val diff = actual - predicted
            val diffPercent = (diff / predicted) * PERCENTAGE_MULTIPLIER
            val absPercent = abs(diffPercent)

            val type =
                when {
                    diffPercent < -1.0 -> PredictionState.POSITIVE
                    diffPercent > 1.0 -> PredictionState.NEGATIVE
                    else -> PredictionState.NEUTRAL
                }

            val arrow =
                when (type) {
                    PredictionState.POSITIVE -> "↓"
                    PredictionState.NEGATIVE -> "↑"
                    PredictionState.NEUTRAL -> ""
                }

            val text =
                UiText.StringResource(
                    R.string.format_prediction_badge,
                    arrow,
                    absPercent,
                )

            return Pair(text, type)
        }

        fun isPredictionPositive(
            actual: Double,
            predicted: Double,
        ): Boolean = actual <= predicted

        private fun String.capitalizeFirstLetter(): String =
            this.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }

        fun formatPredictionLabel(predictedKwh: Double): UiText = UiText.StringResource(R.string.format_prediction_label, predictedKwh)

        fun formatEnergyValueOnly(kwh: Double): UiText {
            val format = if (kwh < 10.0 && kwh > 0.0) "%.1f" else "%.0f"
            return UiText.DynamicString(String.format(Locale.US, format, kwh))
        }

        fun formatPredictionModel(
            actual: Double,
            predicted: Double,
        ): PredictionUiModel {
            if (predicted <= 0.01) {
                return PredictionUiModel.Hidden
            }

            if (actual <= 0.0) {
                val text = UiText.StringResource(R.string.format_prediction_badge, "↓", 100.0)
                return PredictionUiModel(text, PredictionState.POSITIVE, true)
            }

            val diff = actual - predicted
            val diffPercent = (diff / predicted) * 100.0
            val absPercent = kotlin.math.abs(diffPercent)

            val state =
                when {
                    diffPercent < -1.0 -> PredictionState.POSITIVE
                    diffPercent > 1.0 -> PredictionState.NEGATIVE
                    else -> PredictionState.NEUTRAL
                }

            val arrow =
                when (state) {
                    PredictionState.POSITIVE -> "↓"
                    PredictionState.NEGATIVE -> "↑"
                    PredictionState.NEUTRAL -> ""
                }

            val text = UiText.StringResource(R.string.format_prediction_badge, arrow, absPercent)

            return PredictionUiModel(text, state, isVisible = true)
        }

        fun formatListHeader(
            count: Int,
            type: com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdownType,
        ): UiText {
            val resId =
                when (type) {
                    com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdownType.DEVICE -> R.string.format_count_devices
                    com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdownType.ROOM -> R.string.format_count_rooms
                    com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdownType.GROUP -> R.string.format_count_groups
                }
            return UiText.StringResource(resId, count)
        }
    }
