package com.ndumas.appdt.data.consumption.mapper

import com.ndumas.appdt.domain.consumption.model.ConsumptionGranularity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Componente responsabile della normalizzazione delle date "raw" provenienti dal Backend Python
 * in formato standard ISO-8601 utilizzabile dal Domain Layer.
 *
 */
class BackendDateNormalizer
    @Inject
    constructor() {
        // Definizione formati del backend
        private val hourInputFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        private val dayInputFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        private val monthInputFmt = DateTimeFormatter.ofPattern("MM-yyyy")

        fun normalize(
            rawDate: String,
            granularity: ConsumptionGranularity,
        ): String =
            try {
                when (granularity) {
                    ConsumptionGranularity.HOUR -> {
                        val dt = LocalDateTime.parse(rawDate, hourInputFmt)

                        dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    }

                    ConsumptionGranularity.DAY -> {
                        val d = LocalDate.parse(rawDate, dayInputFmt)

                        d.toString()
                    }

                    ConsumptionGranularity.MONTH -> {
                        val ym = YearMonth.parse(rawDate, monthInputFmt)

                        ym.atDay(1).toString()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
    }
