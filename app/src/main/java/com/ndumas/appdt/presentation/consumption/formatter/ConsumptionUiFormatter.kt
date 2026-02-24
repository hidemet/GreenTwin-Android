package com.ndumas.appdt.presentation.consumption.formatter

import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.presentation.consumption.ConsumptionTimeFilter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class ConsumptionUiFormatter
    @Inject
    constructor() {
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

        private fun String.capitalizeFirstLetter(): String =
            this.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }

        fun formatEnergyValueOnly(kwh: Double): UiText {
            val format = if (kwh < 10.0 && kwh > 0.0) "%.1f" else "%.0f"
            return UiText.DynamicString(String.format(Locale.US, format, kwh))
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
