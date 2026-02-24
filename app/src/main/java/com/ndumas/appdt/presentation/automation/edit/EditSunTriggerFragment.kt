package com.ndumas.appdt.presentation.automation.edit

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.FragmentTriggerSunBinding
import com.ndumas.appdt.domain.automation.model.AutomationTrigger
import com.ndumas.appdt.domain.automation.model.SolarEvent
import com.ndumas.appdt.presentation.automation.create.mapper.SolarUiMapper
import com.ndumas.appdt.presentation.automation.create.trigger.SunOffsetBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import javax.inject.Inject

@AndroidEntryPoint
class EditSunTriggerFragment : Fragment(R.layout.fragment_trigger_sun) {
    private val viewModel: AutomationEditViewModel by hiltNavGraphViewModels(R.id.automation_edit_graph)

    @Inject lateinit var uiMapper: SolarUiMapper

    private var _binding: FragmentTriggerSunBinding? = null
    private val binding get() = _binding!!

    private var selectedEvent: SolarEvent = SolarEvent.SUNRISE
    private var selectedOffset: Long = 0L

    private fun getDayChip(id: Int) = binding.root.findViewById<com.google.android.material.chip.Chip>(id)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTriggerSunBinding.bind(view)

        // Pre-fill con trigger esistente
        viewModel.uiState.value.draft.trigger?.let { trigger ->
            if (trigger is AutomationTrigger.Solar) {
                selectedEvent = trigger.event
                selectedOffset = trigger.offsetMinutes
                trigger.days.forEach { day ->
                    when (day) {
                        DayOfWeek.MONDAY -> getDayChip(R.id.chip_mon)?.isChecked = true
                        DayOfWeek.TUESDAY -> getDayChip(R.id.chip_tue)?.isChecked = true
                        DayOfWeek.WEDNESDAY -> getDayChip(R.id.chip_wed)?.isChecked = true
                        DayOfWeek.THURSDAY -> getDayChip(R.id.chip_thu)?.isChecked = true
                        DayOfWeek.FRIDAY -> getDayChip(R.id.chip_fri)?.isChecked = true
                        DayOfWeek.SATURDAY -> getDayChip(R.id.chip_sat)?.isChecked = true
                        DayOfWeek.SUNDAY -> getDayChip(R.id.chip_sun)?.isChecked = true
                    }
                }
            }
        }

        setupToolbar()
        setupEventToggle()
        setupOffsetSelector()
        setupDaySelection()
        setupSave()

        // Imposta lo stato iniziale del toggle group basato su selectedEvent
        when (selectedEvent) {
            SolarEvent.SUNRISE -> binding.btnSunrise.isChecked = true
            SolarEvent.SUNSET -> binding.btnSunset.isChecked = true
        }

        updateOffsetUi()
        updateSaveButtonState()
    }

    private fun setupToolbar() {
        with(binding.includeToolbar.toolbar) {
            title = "Se"
            setNavigationOnClickListener { findNavController().popBackStack() }
        }
    }

    private fun setupEventToggle() {
        binding.toggleGroupEvent.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedEvent =
                    when (checkedId) {
                        R.id.btn_sunrise -> SolarEvent.SUNRISE
                        R.id.btn_sunset -> SolarEvent.SUNSET
                        else -> SolarEvent.SUNRISE
                    }
                updateOffsetUi()
            }
        }
    }

    private fun setupOffsetSelector() {
        binding.btnOffsetSelector.setOnClickListener {
            val dialog = SunOffsetBottomSheet.newInstance(selectedOffset, selectedEvent)
            dialog.show(childFragmentManager, SunOffsetBottomSheet.TAG)
        }

        childFragmentManager.setFragmentResultListener(
            SunOffsetBottomSheet.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, bundle ->
            selectedOffset = bundle.getLong(SunOffsetBottomSheet.RESULT_OFFSET)
            updateOffsetUi()
        }
    }

    private fun updateOffsetUi() {
        val label = uiMapper.mapToLabel(selectedOffset, selectedEvent)
        binding.btnOffsetSelector.text = label.asString(requireContext())
    }

    private fun setupDaySelection() {
        val dayChips =
            listOf(
                getDayChip(R.id.chip_mon),
                getDayChip(R.id.chip_tue),
                getDayChip(R.id.chip_wed),
                getDayChip(R.id.chip_thu),
                getDayChip(R.id.chip_fri),
                getDayChip(R.id.chip_sat),
                getDayChip(R.id.chip_sun),
            )
        dayChips.forEach { chip ->
            chip?.setOnCheckedChangeListener { _, _ -> updateSaveButtonState() }
        }
    }

    private fun updateSaveButtonState() {
        binding.btnSave.isEnabled = true
    }

    private fun setupSave() {
        binding.btnSave.setOnClickListener {
            val selectedDays = mutableListOf<DayOfWeek>()
            if (getDayChip(R.id.chip_mon)?.isChecked == true) selectedDays.add(DayOfWeek.MONDAY)
            if (getDayChip(R.id.chip_tue)?.isChecked == true) selectedDays.add(DayOfWeek.TUESDAY)
            if (getDayChip(R.id.chip_wed)?.isChecked == true) selectedDays.add(DayOfWeek.WEDNESDAY)
            if (getDayChip(R.id.chip_thu)?.isChecked == true) selectedDays.add(DayOfWeek.THURSDAY)
            if (getDayChip(R.id.chip_fri)?.isChecked == true) selectedDays.add(DayOfWeek.FRIDAY)
            if (getDayChip(R.id.chip_sat)?.isChecked == true) selectedDays.add(DayOfWeek.SATURDAY)
            if (getDayChip(R.id.chip_sun)?.isChecked == true) selectedDays.add(DayOfWeek.SUNDAY)

            val trigger =
                AutomationTrigger.Solar(
                    event = selectedEvent,
                    offsetMinutes = selectedOffset,
                    days = selectedDays,
                )

            viewModel.onEvent(AutomationEditUiEvent.SetTrigger(trigger))
            findNavController().popBackStack(R.id.automationEditHubFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
