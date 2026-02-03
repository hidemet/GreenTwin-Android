package com.ndumas.appdt.presentation.consumption

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.tabs.TabLayout
import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.configureConsumptionStyle
import com.ndumas.appdt.core.ui.consumption.bindPrediction
import com.ndumas.appdt.databinding.FragmentConsumptionBinding
import com.ndumas.appdt.domain.consumption.model.ConsumptionBreakdownType
import com.ndumas.appdt.presentation.consumption.adapter.ConsumptionBreakdownAdapter
import com.ndumas.appdt.presentation.consumption.formatter.ConsumptionUiFormatter
import com.ndumas.appdt.presentation.consumption.mapper.ChartDateFormatter
import com.ndumas.appdt.presentation.consumption.mapper.ConsumptionChartMapper
import com.ndumas.appdt.presentation.consumption.model.PredictionState
import com.ndumas.appdt.presentation.consumption.view.ConsumptionMarkerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ConsumptionFragment : Fragment() {
    private var _binding: FragmentConsumptionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConsumptionViewModel by viewModels()

    @Inject lateinit var chartMapper: ConsumptionChartMapper

    @Inject lateinit var chartDateFormatter: ChartDateFormatter

    @Inject lateinit var uiFormatter: ConsumptionUiFormatter

    private var lastChartDataHash: Int = 0
    private lateinit var markerView: ConsumptionMarkerView
    private val breakdownAdapter = ConsumptionBreakdownAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentConsumptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupChartConfiguration()
        setupListeners()

        observeUiState()
    }

    private fun setupRecyclerView() {
        binding.rvConsumptionBreakdown.apply {
            adapter = breakdownAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
            itemAnimator = null
        }
    }

    private fun setupChartConfiguration() {
        binding.chartConsumption.configureConsumptionStyle(requireContext())

        markerView = ConsumptionMarkerView(requireContext(), uiFormatter)
        markerView.chartView = binding.chartConsumption
        binding.chartConsumption.marker = markerView

        binding.chartConsumption.apply {
            isHighlightPerTapEnabled = true
            isHighlightPerDragEnabled = true
            setScaleEnabled(false)
        }
    }

    private fun setupListeners() {
        setupTabListener()
        setupNavigationListeners()
        setupToggleGroupListener()
        setupSortListener()
        setupChartInteractionListener()
    }

    private fun setupTabListener() {
        binding.tabPeriod.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val filter =
                        when (tab?.position) {
                            0 -> ConsumptionTimeFilter.TODAY
                            1 -> ConsumptionTimeFilter.WEEK
                            2 -> ConsumptionTimeFilter.MONTH
                            3 -> ConsumptionTimeFilter.YEAR
                            else -> ConsumptionTimeFilter.WEEK
                        }
                    viewModel.onEvent(ConsumptionEvent.OnFilterChange(filter))
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            },
        )
    }

    private fun setupNavigationListeners() {
        binding.btnPrevPeriod.setOnClickListener {
            viewModel.onEvent(ConsumptionEvent.OnPrevPeriod)
        }
        binding.btnNextPeriod.setOnClickListener {
            viewModel.onEvent(ConsumptionEvent.OnNextPeriod)
        }
    }

    private fun setupToggleGroupListener() {
        binding.toggleGroupFilters.addOnButtonCheckedListener { group, checkedId, isChecked ->

            if (isChecked) {
                val type =
                    when (checkedId) {
                        R.id.btn_filter_device -> ConsumptionBreakdownType.DEVICE
                        R.id.btn_filter_room -> ConsumptionBreakdownType.ROOM
                        R.id.btn_filter_group -> ConsumptionBreakdownType.GROUP
                        else -> ConsumptionBreakdownType.DEVICE
                    }
                viewModel.onEvent(ConsumptionEvent.OnListTypeChange(type))
            }
        }
    }

    private fun setupSortListener() {
        binding.btnSortConsumption.setOnClickListener {
            viewModel.onEvent(ConsumptionEvent.OnSortToggle)
        }
    }

    private fun setupChartInteractionListener() {
        binding.chartConsumption.setOnChartValueSelectedListener(
            object : OnChartValueSelectedListener {
                override fun onValueSelected(
                    e: Entry?,
                    h: Highlight?,
                ) {
                    binding.chartConsumption.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    e?.let {
                        viewModel.onEvent(ConsumptionEvent.OnChartSelect(it.x.toInt()))
                    }
                }

                override fun onNothingSelected() {
                    viewModel.onEvent(ConsumptionEvent.OnChartDeselect)
                }
            },
        )
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { updateUi(it) }
            }
        }
    }

    private fun updateUi(state: ConsumptionUiState) {
        val context = requireContext()

        binding.progressBar.isVisible = state.isLoading
        binding.chartConsumption.isVisible = !(state.isLoading && state.consumptionData.isEmpty())
        if (state.isLoading) return

        val targetTab =
            when (state.selectedFilter) {
                ConsumptionTimeFilter.TODAY -> 0
                ConsumptionTimeFilter.WEEK -> 1
                ConsumptionTimeFilter.MONTH -> 2
                ConsumptionTimeFilter.YEAR -> 3
            }
        if (binding.tabPeriod.selectedTabPosition != targetTab) {
            binding.tabPeriod.getTabAt(targetTab)?.select()
        }

        binding.tvCurrentDateRange.text = state.formattedDateRange.asString(context)
        binding.tvTotalValue.text = state.formattedTotalEnergy.asString(context)

        binding.tvPercentageBadge.bindPrediction(state.predictionModel)

        binding.tvEstimateLabel.text = state.formattedPredictionLabel.asString(context)
        binding.tvEstimateLabel.isVisible = state.predictionModel.isVisible

        binding.btnPrevPeriod.isEnabled = true
        binding.btnPrevPeriod.alpha = 1.0f
        binding.btnNextPeriod.isEnabled = state.isNextEnabled
        binding.btnNextPeriod.alpha = if (state.isNextEnabled) 1.0f else 0.3f

        val currentDataHash = state.consumptionData.hashCode()
        if (currentDataHash != lastChartDataHash) {
            updateChartFull(state)
            lastChartDataHash = currentDataHash
        } else {
            updateChartHighlightOnly(state)
        }

        val targetButtonId =
            when (state.breakdownType) {
                ConsumptionBreakdownType.DEVICE -> R.id.btn_filter_device
                ConsumptionBreakdownType.ROOM -> R.id.btn_filter_room
                ConsumptionBreakdownType.GROUP -> R.id.btn_filter_group
            }

        if (binding.toggleGroupFilters.checkedButtonId != targetButtonId) {
            binding.toggleGroupFilters.check(targetButtonId)
        }

        val isListEmpty = state.breakdownList.isEmpty()

        binding.rvConsumptionBreakdown.isVisible = !isListEmpty
        binding.layoutEmptyList.isVisible = isListEmpty

        breakdownAdapter.submitList(state.breakdownList)

        binding.tvBreakdownCount.text = state.formattedListHeader.asString(context)
        val sortIcon = if (state.isSortAscending) R.drawable.ic_arrow_upward else R.drawable.ic_arrow_downward
        binding.btnSortConsumption.setIconResource(sortIcon)

        binding.tvError.isVisible = state.error != null
        state.error?.let { binding.tvError.text = it.asString(context) }
    }

    private fun resolveBadgeStyle(type: PredictionState) {
        val (bgRes, textRes) =
            when (type) {
                PredictionState.POSITIVE -> Pair(R.drawable.bg_badge_green, R.color.badge_success_text)
                PredictionState.NEGATIVE -> Pair(R.drawable.bg_badge_error, R.color.badge_error_text)
                PredictionState.NEUTRAL -> Pair(R.drawable.bg_badge_neutral, R.color.badge_neutral_text)
            }

        binding.tvPercentageBadge.setBackgroundResource(bgRes)
        binding.tvPercentageBadge.setTextColor(ContextCompat.getColor(requireContext(), textRes))
    }

    /**
     * Aggiornamento PESANTE: Rigenera dati, assi e animazioni.
     * Chiamato solo quando i dati cambiano (es. cambio periodo).
     */
    private fun updateChartFull(state: ConsumptionUiState) {
        binding.chartConsumption.xAxis.apply {
            valueFormatter = chartDateFormatter.getAxisFormatter(state.selectedFilter, state.consumptionData)
            setLabelCount(state.consumptionData.size.coerceAtMost(12), false)
        }

        markerView.updateData(state.consumptionData, state.selectedFilter)

        val barData =
            chartMapper.mapToBarData(
                data = state.consumptionData,
                today = state.currentDate,
            )

        (barData.getDataSetByIndex(0) as? BarDataSet)?.apply {
            highLightAlpha = 180
            highLightColor = ContextCompat.getColor(requireContext(), R.color.black)
        }

        binding.chartConsumption.data = barData

        applyHighlight(state.selectedIndex)

        binding.chartConsumption.notifyDataSetChanged()
        binding.chartConsumption.invalidate()
        binding.chartConsumption.animateY(600)
    }

    private fun updateChartHighlightOnly(state: ConsumptionUiState) {
        markerView.updateData(state.consumptionData, state.selectedFilter)

        applyHighlight(state.selectedIndex)
        binding.chartConsumption.invalidate()
    }

    private fun applyHighlight(index: Int) {
        if (index != -1) {
            binding.chartConsumption.highlightValue(index.toFloat(), 0, false)
        } else {
            binding.chartConsumption.highlightValues(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
