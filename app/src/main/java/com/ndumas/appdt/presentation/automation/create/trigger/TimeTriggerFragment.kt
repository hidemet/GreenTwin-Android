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

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            saveTrigger()
        }

        val dayChips =
            listOf(
                binding.chipMon,
                binding.chipTue,
                binding.chipWed,
                binding.chipThu,
                binding.chipFri,
                binding.chipSat,
                binding.chipSun,
            )

        dayChips.forEach { chip ->
            chip.setOnCheckedChangeListener { _, _ ->
                updateSaveButtonState()
            }
        }
    }

    private fun updateSaveButtonState() {
        val hasAtLeastOneDaySelected =
            binding.chipMon.isChecked ||
                binding.chipTue.isChecked ||
                binding.chipWed.isChecked ||
                binding.chipThu.isChecked ||
                binding.chipFri.isChecked ||
                binding.chipSat.isChecked ||
                binding.chipSun.isChecked

        binding.btnSave.isEnabled = hasAtLeastOneDaySelected
    }

    private fun saveTrigger() {
        val hour = binding.timePicker.hour
        val minute = binding.timePicker.minute

        val selectedDays = mutableListOf<DayOfWeek>()
        if (binding.chipMon.isChecked) selectedDays.add(DayOfWeek.MONDAY)
        if (binding.chipTue.isChecked) selectedDays.add(DayOfWeek.TUESDAY)
        if (binding.chipWed.isChecked) selectedDays.add(DayOfWeek.WEDNESDAY)
        if (binding.chipThu.isChecked) selectedDays.add(DayOfWeek.THURSDAY)
        if (binding.chipFri.isChecked) selectedDays.add(DayOfWeek.FRIDAY)
        if (binding.chipSat.isChecked) selectedDays.add(DayOfWeek.SATURDAY)
        if (binding.chipSun.isChecked) selectedDays.add(DayOfWeek.SUNDAY)

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
