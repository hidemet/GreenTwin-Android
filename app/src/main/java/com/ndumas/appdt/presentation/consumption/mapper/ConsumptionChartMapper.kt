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

            // 1. Creiamo solo le entries, senza logica colori
            data.forEachIndexed { index, item ->
                entries.add(BarEntry(index.toFloat(), item.energyKwh.toFloat()))
            }

            // 2. Configuriamo il DataSet
            val dataSet =
                BarDataSet(entries, "Consumi").apply {
                    // COLORE UNICO: Lime standard per tutte le barre
                    color = context.getColor(R.color.tw_lime_400)

                    // Nessun valore scritto sopra le barre (pulizia)
                    setDrawValues(false)

                    // CONFIGURAZIONE SELEZIONE (Feedback al tocco)
                    // Quando l'utente tocca, la barra si scurisce
                    highLightAlpha = 150
                    highLightColor = context.getColor(R.color.tw_gray_900)
                }

            // 3. Ritorniamo i dati con uno stile barre più elegante (più strette)
            return BarData(dataSet).apply {
                barWidth = 0.5f
            }
        }
    }
