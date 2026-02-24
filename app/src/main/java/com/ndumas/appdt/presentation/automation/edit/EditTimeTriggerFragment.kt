package com.ndumas.appdt.presentation.automation.edit

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.FragmentTriggerTimeBinding
import com.ndumas.appdt.domain.automation.model.AutomationTrigger
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalTime

@AndroidEntryPoint
class EditTimeTriggerFragment : Fragment(R.layout.fragment_trigger_time) {
    private val viewModel: AutomationEditViewModel by hiltNavGraphViewModels(R.id.automation_edit_graph)

    private var _binding: FragmentTriggerTimeBinding? = null
    private val binding get() = _binding!!

    private fun getDayChip(id: Int) = binding.root.findViewById<com.google.android.material.chip.Chip>(id)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTriggerTimeBinding.bind(view)

        setupToolbar()
        setupListeners()

        // Pre-fill con il trigger esistente se presente
        viewModel.uiState.value.draft.trigger?.let { trigger ->
            if (trigger is AutomationTrigger.Time) {
                binding.timePicker.hour = trigger.time.hour
                binding.timePicker.minute = trigger.time.minute
                // Imposta i giorni selezionati
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

        // Abilita il pulsante Salva (in modalità modifica, il salvataggio è sempre permesso)
        binding.btnSave.isEnabled = true
    }

    private fun setupToolbar() {
        binding.includeToolbar.toolbar.title = getString(R.string.automation_trigger_time)
        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupListeners() {
        binding.timePicker.setIs24HourView(true)

        binding.btnSave.setOnClickListener {
            val selectedTime = LocalTime.of(binding.timePicker.hour, binding.timePicker.minute)
            val selectedDays = getSelectedDays()

            val trigger = AutomationTrigger.Time(selectedTime, selectedDays)
            viewModel.onEvent(AutomationEditUiEvent.SetTrigger(trigger))

            findNavController().popBackStack(R.id.automationEditHubFragment, false)
        }
    }

    private fun getSelectedDays(): List<DayOfWeek> {
        val days = mutableListOf<DayOfWeek>()
        if (getDayChip(R.id.chip_mon)?.isChecked == true) days.add(DayOfWeek.MONDAY)
        if (getDayChip(R.id.chip_tue)?.isChecked == true) days.add(DayOfWeek.TUESDAY)
        if (getDayChip(R.id.chip_wed)?.isChecked == true) days.add(DayOfWeek.WEDNESDAY)
        if (getDayChip(R.id.chip_thu)?.isChecked == true) days.add(DayOfWeek.THURSDAY)
        if (getDayChip(R.id.chip_fri)?.isChecked == true) days.add(DayOfWeek.FRIDAY)
        if (getDayChip(R.id.chip_sat)?.isChecked == true) days.add(DayOfWeek.SATURDAY)
        if (getDayChip(R.id.chip_sun)?.isChecked == true) days.add(DayOfWeek.SUNDAY)
        return days
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
