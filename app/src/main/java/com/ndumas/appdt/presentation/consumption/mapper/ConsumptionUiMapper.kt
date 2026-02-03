package com.ndumas.appdt.presentation.consumption.mapper

import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdown
import com.ndumas.appdt.domain.device.model.DeviceType
import com.ndumas.appdt.presentation.consumption.model.ConsumptionBreakdownUiModel
import javax.inject.Inject
import kotlin.math.roundToInt

class ConsumptionUiMapper
    @Inject
    constructor() {
        private companion object {
            const val MAX_PROGRESS = 100
            const val MIN_PROGRESS = 0
        }

        fun mapToUiModel(items: List<ConsumptionBreakdown>): List<ConsumptionBreakdownUiModel> =
            items.map { breakdown ->
                ConsumptionBreakdownUiModel(
                    id = breakdown.id,
                    name = breakdown.name,
                    valueText = UiText.StringResource(R.string.format_energy_kwh, breakdown.energyKwh),
                    progress =
                        breakdown.impactPercentage
                            .roundToInt()
                            .coerceIn(MIN_PROGRESS, MAX_PROGRESS),
                    type = resolveSourceType(breakdown.deviceType),
                )
            }

        private fun resolveSourceType(backendTypeString: String): DeviceType =
            when (backendTypeString.lowercase()) {
                "room" -> {
                    DeviceType.ROOM
                }

                "group" -> {
                    DeviceType.GROUP
                }

                else -> {
                    runCatching {
                        DeviceType.valueOf(backendTypeString.uppercase())
                    }.getOrDefault(DeviceType.OTHER)
                }
            }
    }
