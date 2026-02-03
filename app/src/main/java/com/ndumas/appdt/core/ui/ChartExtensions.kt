package com.ndumas.appdt.core.ui

import android.content.Context
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.ndumas.appdt.R

fun BarChart.configureConsumptionStyle(context: Context) {
    description.isEnabled = false
    legend.isEnabled = false
    setScaleEnabled(false)
    isDoubleTapToZoomEnabled = false
    setPinchZoom(false)

    setDrawGridBackground(false)
    setDrawBorders(false)

    // Placeholder
    setNoDataText("Nessun dato disponibile")
    setNoDataTextColor(ContextCompat.getColor(context, R.color.tw_gray_500))

    extraBottomOffset = 10f

    // 2. Asse X Date/Giorni
    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        setDrawGridLines(false)

        // Disegna la linea dell'asse X
        setDrawAxisLine(true)
        axisLineColor = ContextCompat.getColor(context, R.color.tw_gray_300)
        axisLineWidth = 1f
        yOffset = 10f
        textColor = ContextCompat.getColor(context, R.color.ds_text_secondary)
        textSize = 12f

        // Impedisce di saltare le etichette
        granularity = 1f
        isGranularityEnabled = true

        // Centra le etichette
        setCenterAxisLabels(false)
    }

    axisLeft.apply {
        setDrawGridLines(true)
        gridColor = ContextCompat.getColor(context, R.color.tw_gray_100)
        enableGridDashedLine(10f, 10f, 0f)
        setDrawAxisLine(false) // Nascondi la linea verticale
        setDrawLabels(true)
        textColor = ContextCompat.getColor(context, R.color.ds_text_secondary)
        textSize = 11f
        axisMinimum = 0f

        //  Unità di misura
        valueFormatter =
            object : IAxisValueFormatter {
                override fun getFormattedValue(
                    value: Float,
                    axis: com.github.mikephil.charting.components.AxisBase?,
                ): String {
                    // Se è un numero intero (es. 4.0), mostra "4 kWh"
                    // Se è decimale (es. 4.5), mostra "4.5 kWh"
                    return if (value % 1 == 0f) {
                        "${value.toInt()} kWh"
                    } else {
                        String.format("%.1f kWh", value)
                    }
                }
            }
    }

    axisLeft.apply {
        setDrawGridLines(true)
        gridColor = ContextCompat.getColor(context, R.color.tw_gray_100) // Più leggero
        enableGridDashedLine(10f, 10f, 0f)
        setDrawAxisLine(false) // Nascondiamo la linea verticale Y
        setDrawLabels(true)
        textColor = ContextCompat.getColor(context, R.color.ds_text_secondary)
        axisMinimum = 0f
    }

    axisRight.isEnabled = false

    animateY(1000)
}
