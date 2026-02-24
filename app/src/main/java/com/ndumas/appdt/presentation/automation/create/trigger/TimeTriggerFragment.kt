package com.ndumas.appdt.presentation.automation.create.trigger

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.FragmentTriggerTimeBinding
import com.ndumas.appdt.domain.automation.model.AutomationTrigger
import com.ndumas.appdt.presentation.automation.create.AutomationCreateUiEvent
import com.ndumas.appdt.presentation.automation.create.AutomationCreateViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalTime

@AndroidEntryPoint
class TimeTriggerFragment : Fragment(R.layout.fragment_trigger_time) {
    private val viewModel: AutomationCreateViewModel by hiltNavGraphViewModels(R.id.automation_graph)

    private var _binding: FragmentTriggerTimeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTriggerTimeBinding.bind(view)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.timePicker.setIs24HourView(true)

        binding.timePicker.hour = 7
        binding.timePicker.minute = 30
    }

    private fun setupToolbar() {
        with(binding.includeToolbar.toolbar) {
            title = "Se"
            setNavigationOnClickListener { findNavController().popBackStack() }
        }
    }

    private fun getDayChip(id: Int) = binding.root.findViewById<com.google.android.material.chip.Chip>(id)

    private fun setupListeners() {
        setupToolbar()
        binding.btnSave.setOnClickListener {
            saveTrigger()
        }

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
            chip?.setOnCheckedChangeListener { _, _ ->
                updateSaveButtonState()
            }
        }
    }

    private fun updateSaveButtonState() {
        // Always enable save button when time is selected (days are optional)
        binding.btnSave.isEnabled = true
    }

    private fun saveTrigger() {
        val hour = binding.timePicker.hour
        val minute = binding.timePicker.minute

        val selectedDays = mutableListOf<DayOfWeek>()
        if (getDayChip(R.id.chip_mon)?.isChecked == true) selectedDays.add(DayOfWeek.MONDAY)
        if (getDayChip(R.id.chip_tue)?.isChecked == true) selectedDays.add(DayOfWeek.TUESDAY)
        if (getDayChip(R.id.chip_wed)?.isChecked == true) selectedDays.add(DayOfWeek.WEDNESDAY)
        if (getDayChip(R.id.chip_thu)?.isChecked == true) selectedDays.add(DayOfWeek.THURSDAY)
        if (getDayChip(R.id.chip_fri)?.isChecked == true) selectedDays.add(DayOfWeek.FRIDAY)
        if (getDayChip(R.id.chip_sat)?.isChecked == true) selectedDays.add(DayOfWeek.SATURDAY)
        if (getDayChip(R.id.chip_sun)?.isChecked == true) selectedDays.add(DayOfWeek.SUNDAY)

        val trigger =
            AutomationTrigger.Time(
                time = LocalTime.of(hour, minute),
                days = selectedDays,
            )

        viewModel.onEvent(AutomationCreateUiEvent.SetTrigger(trigger))

        findNavController().popBackStack(R.id.automationHubFragment, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
