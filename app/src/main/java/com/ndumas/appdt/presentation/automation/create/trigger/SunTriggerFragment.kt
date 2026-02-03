package com.ndumas.appdt.presentation.automation.create.trigger

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.FragmentTriggerSunBinding
import com.ndumas.appdt.domain.automation.model.AutomationTrigger
import com.ndumas.appdt.domain.automation.model.SolarEvent
import com.ndumas.appdt.presentation.automation.create.AutomationCreateUiEvent
import com.ndumas.appdt.presentation.automation.create.AutomationCreateViewModel
import com.ndumas.appdt.presentation.automation.create.mapper.SolarUiMapper
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import javax.inject.Inject

@AndroidEntryPoint
class SunTriggerFragment : Fragment(R.layout.fragment_trigger_sun) {
    private val viewModel: AutomationCreateViewModel by hiltNavGraphViewModels(R.id.automation_graph)

    @Inject lateinit var uiMapper: SolarUiMapper

    private var _binding: FragmentTriggerSunBinding? = null
    private val binding get() = _binding!!

    private var selectedEvent: SolarEvent = SolarEvent.SUNRISE
    private var selectedOffset: Long = 0L

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTriggerSunBinding.bind(view)

        setupToolbar()
        setupEventToggle()
        setupOffsetSelector()
        setupDaySelection()
        setupSave()

        updateEventUi()
        updateOffsetUi()
        updateSaveButtonState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupEventToggle() {
        binding.layoutEventSelector.setOnClickListener {
            selectedEvent =
                if (selectedEvent == SolarEvent.SUNRISE) {
                    SolarEvent.SUNSET
                } else {
                    SolarEvent.SUNRISE
                }
            updateEventUi()
        }
    }

    private fun updateEventUi() {
        if (selectedEvent == SolarEvent.SUNRISE) {
            binding.ivEventIcon.setImageResource(R.drawable.ic_wb_twilight)
            binding.tvEventLabel.text = getString(R.string.sun_event_sunrise)
        } else {
            binding.ivEventIcon.setImageResource(R.drawable.ic_wb_twilight)
            binding.tvEventLabel.text = getString(R.string.sun_event_sunset)
        }
        updateOffsetUi()
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
                binding.chipMon,
                binding.chipTue,
                binding.chipWed,
                binding.chipThu,
                binding.chipFri,
                binding.chipSat,
                binding.chipSun,
            )

        dayChips.forEach { chip ->
            chip.setOnCheckedChangeListener { _, _ -> updateSaveButtonState() }
        }
    }

    private fun updateSaveButtonState() {
        binding.btnSave.isEnabled = true
    }

    private fun setupSave() {
        binding.btnSave.setOnClickListener {
            val selectedDays = mutableListOf<DayOfWeek>()
            if (binding.chipMon.isChecked) selectedDays.add(DayOfWeek.MONDAY)
            if (binding.chipTue.isChecked) selectedDays.add(DayOfWeek.TUESDAY)
            if (binding.chipWed.isChecked) selectedDays.add(DayOfWeek.WEDNESDAY)
            if (binding.chipThu.isChecked) selectedDays.add(DayOfWeek.THURSDAY)
            if (binding.chipFri.isChecked) selectedDays.add(DayOfWeek.FRIDAY)
            if (binding.chipSat.isChecked) selectedDays.add(DayOfWeek.SATURDAY)
            if (binding.chipSun.isChecked) selectedDays.add(DayOfWeek.SUNDAY)

            val trigger =
                AutomationTrigger.Solar(
                    event = selectedEvent,
                    offsetMinutes = selectedOffset,
                    days = selectedDays,
                )

            viewModel.onEvent(AutomationCreateUiEvent.SetTrigger(trigger))
            findNavController().popBackStack(R.id.automationHubFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
