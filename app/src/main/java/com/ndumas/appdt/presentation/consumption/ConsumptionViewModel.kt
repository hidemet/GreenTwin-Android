package com.ndumas.appdt.presentation.consumption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.core.util.asUiText
import com.ndumas.appdt.domain.consumption.model.ConsumptionGranularity
import com.ndumas.appdt.domain.consumption.usecase.GetConsumptionBreakdownUseCase
import com.ndumas.appdt.domain.consumption.usecase.GetConsumptionHistoryUseCase
import com.ndumas.appdt.presentation.consumption.formatter.ConsumptionUiFormatter
import com.ndumas.appdt.presentation.consumption.mapper.ConsumptionUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * ViewModel per la schermata Consumi.
 *
 * Gestisce:
 * - Visualizzazione consumi per giorno/settimana/mese/anno
 * - Breakdown per dispositivo/stanza/gruppo
 */
@HiltViewModel
class ConsumptionViewModel
    @Inject
    constructor(
        private val getHistoryUseCase: GetConsumptionHistoryUseCase,
        private val getBreakdownUseCase: GetConsumptionBreakdownUseCase,
        private val uiMapper: ConsumptionUiMapper,
        private val formatter: ConsumptionUiFormatter,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ConsumptionUiState())
        val uiState = _uiState.asStateFlow()

        private var loadJob: Job? = null

        companion object {
            private const val DAYS_IN_WEEK = 7L
            private const val YEARS_IN_PERIOD = 1L
        }

        init {
            loadData(_uiState.value.selectedFilter, LocalDate.now())
        }

        fun onEvent(event: ConsumptionEvent) {
            when (event) {
                is ConsumptionEvent.OnFilterChange -> {
                    loadData(event.filter, LocalDate.now())
                }

                ConsumptionEvent.OnRetry -> {
                    loadData(_uiState.value.selectedFilter, _uiState.value.currentDate)
                }

                ConsumptionEvent.OnSortToggle -> {
                    toggleSort()
                }

                ConsumptionEvent.OnPrevPeriod -> {
                    changePeriod(-1)
                }

                ConsumptionEvent.OnNextPeriod -> {
                    changePeriod(1)
                }

                is ConsumptionEvent.OnChartSelect -> {
                    updateSelection(event.index)
                }

                ConsumptionEvent.OnChartDeselect -> {
                    updateSelection(-1)
                }

                is ConsumptionEvent.OnListTypeChange -> {
                    if (_uiState.value.breakdownType == event.type) return

                    _uiState.update { it.copy(breakdownType = event.type) }

                    val currentDate = _uiState.value.currentDate
                    val (start, end) =
                        calculateDateRange(
                            _uiState.value.selectedFilter,
                            currentDate,
                        )

                    viewModelScope.launch {
                        fetchBreakdownData(start, end)
                    }
                }
            }
        }

        private fun updateSelection(index: Int) {
            _uiState.update { state ->

                state.copy(
                    selectedIndex = index,
                )
            }
        }

        private fun toggleSort() {
            val currentList = _uiState.value.breakdownList
            val newIsAscending = !_uiState.value.isSortAscending

            val sortedList =
                if (newIsAscending) {
                    currentList.sortedBy { it.progress }
                } else {
                    currentList.sortedByDescending { it.progress }
                }

            _uiState.update {
                it.copy(breakdownList = sortedList, isSortAscending = newIsAscending)
            }
        }

        private fun changePeriod(amount: Long) {
            val current = _uiState.value.currentDate
            val filter = _uiState.value.selectedFilter

            val newDate =
                when (filter) {
                    ConsumptionTimeFilter.TODAY -> current.plusDays(amount)
                    ConsumptionTimeFilter.WEEK -> current.plusWeeks(amount)
                    ConsumptionTimeFilter.MONTH -> current.plusMonths(amount)
                    ConsumptionTimeFilter.YEAR -> current.plusYears(amount)
                }

            loadData(filter, newDate)
        }

        private fun loadData(
            filter: ConsumptionTimeFilter,
            referenceDate: LocalDate,
        ) {
            loadJob?.cancel()

            loadJob =
                viewModelScope.launch {
                    val (startDate, endDate) = calculateDateRange(filter, referenceDate)
                    val today = LocalDate.now()
                    val isNextEnabled = endDate.isBefore(today)

                    _uiState.update {
                        it.copy(
                            isLoading = true,
                            selectedFilter = filter,
                            error = null,
                            currentDate = referenceDate,
                            formattedDateRange = formatter.formatDateRange(referenceDate, filter),
                            selectedIndex = -1,
                            isNextEnabled = isNextEnabled,
                        )
                    }

                    launch { fetchHistoryData(startDate, endDate, filter) }

                    launch { fetchBreakdownData(startDate, endDate) }
                }
        }

        private suspend fun fetchHistoryData(
            startDate: LocalDate,
            endDate: LocalDate,
            filter: ConsumptionTimeFilter,
        ) {
            val granularity =
                when (filter) {
                    ConsumptionTimeFilter.TODAY -> ConsumptionGranularity.HOUR
                    ConsumptionTimeFilter.WEEK -> ConsumptionGranularity.DAY
                    ConsumptionTimeFilter.MONTH -> ConsumptionGranularity.DAY
                    ConsumptionTimeFilter.YEAR -> ConsumptionGranularity.MONTH
                }

            getHistoryUseCase(startDate, endDate, granularity).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val data = result.data
                        val totalEnergy = data.sumOf { it.energyKwh }
                        val totalCost = data.sumOf { it.cost }

                        _uiState.update {
                            it.copy(
                                consumptionData = data,
                                totalEnergy = totalEnergy,
                                totalCost = totalCost,
                                formattedTotalEnergy = formatter.formatEnergyValueOnly(totalEnergy),
                                formattedTotalCost = formatter.formatCost(totalCost),
                                selectedIndex = -1,
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update { it.copy(error = result.error.asUiText()) }
                    }
                }
            }
        }

        private suspend fun fetchBreakdownData(
            startDate: LocalDate,
            endDate: LocalDate,
        ) {
            val breakdownType = _uiState.value.breakdownType
            getBreakdownUseCase(startDate, endDate, breakdownType).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val uiList = uiMapper.mapToUiModel(result.data)
                        val headerText = formatter.formatListHeader(uiList.size, breakdownType)

                        _uiState.update {
                            it.copy(
                                breakdownList = uiList,
                                formattedListHeader = headerText,
                                isLoading = false,
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = result.error.asUiText())
                        }
                    }
                }
            }
        }

        private fun calculateDateRange(
            filter: ConsumptionTimeFilter,
            refDate: LocalDate,
        ): Pair<LocalDate, LocalDate> =
            when (filter) {
                ConsumptionTimeFilter.TODAY -> {
                    refDate to refDate
                }

                ConsumptionTimeFilter.WEEK -> {
                    val start = refDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    start to start.plusDays(6)
                }

                ConsumptionTimeFilter.MONTH -> {
                    refDate.withDayOfMonth(1) to refDate.with(TemporalAdjusters.lastDayOfMonth())
                }

                ConsumptionTimeFilter.YEAR -> {
                    refDate.withDayOfYear(1) to refDate.with(TemporalAdjusters.lastDayOfYear())
                }
            }
    }
