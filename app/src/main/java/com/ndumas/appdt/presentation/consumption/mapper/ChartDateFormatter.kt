package com.ndumas.appdt.presentation.consumption.mapper

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.ndumas.appdt.domain.consumption.model.Consumption
import com.ndumas.appdt.presentation.consumption.ConsumptionTimeFilter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class ChartDateFormatter
    @Inject
    constructor() {
        fun getAxisFormatter(
            filter: ConsumptionTimeFilter,
            data: List<Consumption>,
        ): IAxisValueFormatter {
            return object : IAxisValueFormatter {
                override fun getFormattedValue(
                    value: Float,
                    axis: AxisBase?,
                ): String {
                    val index = value.toInt()

                    if (index < 0 || index >= data.size) return ""

                    return try {
                        formatLabel(data[index].date, filter, index)
                    } catch (e: Exception) {
                        ""
                    }
                }
            }
        }

        private fun formatLabel(
            dateString: String,
            filter: ConsumptionTimeFilter,
            index: Int,
        ): String {
            val locale = Locale.ITALY

            return when (filter) {
                ConsumptionTimeFilter.TODAY -> {
                    val cleanString = dateString.replace(" ", "T")
                    val time = LocalDateTime.parse(cleanString)
                    val hour = time.hour
                    if (hour % 2 == 0) {
                        hour.toString()
                    } else {
                        ""
                    }
                }

                ConsumptionTimeFilter.WEEK -> {
                    val date = LocalDate.parse(dateString)
                    date.format(DateTimeFormatter.ofPattern("EEE", locale))
                }

                ConsumptionTimeFilter.MONTH -> {
                    val date = LocalDate.parse(dateString)
                    val day = date.dayOfMonth

                    if (day in listOf(1, 8, 15, 22)) {
                        day.toString()
                    } else {
                        ""
                    }
                }

                ConsumptionTimeFilter.YEAR -> {
                    val date = LocalDate.parse(dateString)
                    val monthName = date.format(DateTimeFormatter.ofPattern("MMMM", locale))

                    if (monthName.isNotEmpty()) {
                        monthName.first().uppercase()
                    } else {
                        ""
                    }
                }
            }
        }
    }
