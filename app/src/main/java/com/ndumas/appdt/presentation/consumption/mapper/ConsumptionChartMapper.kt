package com.ndumas.appdt.presentation.consumption.mapper

import android.content.Context
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.ndumas.appdt.R
import com.ndumas.appdt.domain.consumption.model.Consumption
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject

class ConsumptionChartMapper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun mapToBarData(
            data: List<Consumption>,
            today: LocalDate,
        ): BarData {
            val entries = ArrayList<BarEntry>()
            val colors = ArrayList<Int>()

            val colorDefault = context.getColor(R.color.tw_lime_400)
            val colorHighlight = context.getColor(R.color.greentwin_secondary)

            val todayString = today.toString()

            data.forEachIndexed { index, item ->

                entries.add(BarEntry(index.toFloat(), item.energyKwh.toFloat()))

                val isToday = item.date == todayString
                colors.add(if (isToday) colorHighlight else colorDefault)
            }

            val dataSet =
                BarDataSet(entries, "Consumi").apply {
                    setColors(colors)
                    setDrawValues(false)
                }

            return BarData(dataSet).apply {
                barWidth = 0.6f
            }
        }
    }
