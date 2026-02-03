package com.ndumas.appdt.presentation.consumption

import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdownType

sealed interface ConsumptionEvent {
    data class OnFilterChange(
        val filter: ConsumptionTimeFilter,
    ) : ConsumptionEvent

    data object OnSortToggle : ConsumptionEvent

    data object OnPrevPeriod : ConsumptionEvent

    data object OnNextPeriod : ConsumptionEvent

    data object OnRetry : ConsumptionEvent

    data class OnChartSelect(
        val index: Int,
    ) : ConsumptionEvent

    data object OnChartDeselect : ConsumptionEvent

    data class OnListTypeChange(
        val type: ConsumptionBreakdownType,
    ) : ConsumptionEvent
}
