package com.ndumas.appdt.presentation.automation.create

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.device.getUiStyle
import com.ndumas.appdt.databinding.FragmentAutomationHubBinding
import com.ndumas.appdt.domain.automation.model.AutomationAction
import com.ndumas.appdt.domain.automation.model.AutomationTrigger
import com.ndumas.appdt.presentation.automation.create.mapper.DeviceUiMapper
import com.ndumas.appdt.presentation.automation.create.mapper.SolarUiMapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AutomationHubFragment : Fragment(R.layout.fragment_automation_hub) {
    // ViewModel scoped al grafo di navigazione (condiviso tra i fragment del wizard)
    private val viewModel: AutomationCreateViewModel by hiltNavGraphViewModels(R.id.automation_graph)

    @Inject
    lateinit var solarUiMapper: SolarUiMapper

    @Inject
    lateinit var deviceUiMapper: DeviceUiMapper

    private var _binding: FragmentAutomationHubBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAutomationHubBinding.bind(view)

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.cardAddTrigger.setOnClickListener { navigateToTrigger() }
        binding.cardTriggerSelected.setOnClickListener { navigateToTrigger() }

        binding.cardAddAction.setOnClickListener { navigateToAction() }
        binding.cardActionSelected.setOnClickListener { navigateToAction() }

        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_hub_to_name)
        }
    }

    private fun navigateToTrigger() {
        findNavController().navigate(R.id.action_hub_to_triggerSelector)
    }

    private fun navigateToAction() {
        findNavController().navigate(R.id.action_hub_to_actionSelector)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: AutomationCreateUiState) {
        val draft = state.draft

        val hasTrigger = draft.trigger != null
        binding.cardAddTrigger.isVisible = !hasTrigger
        binding.cardTriggerSelected.isVisible = hasTrigger

        if (hasTrigger) {
            val trigger = draft.trigger
            binding.tvTriggerType.text = resolveTriggerTitle(trigger)
            binding.tvTriggerSummary.text = resolveTriggerSubtitle(trigger)
            binding.ivTriggerIcon.setImageResource(R.drawable.ic_check_circle)
        }

        val hasAction = draft.actions.isNotEmpty()
        binding.cardAddAction.isVisible = !hasAction
        binding.cardActionSelected.isVisible = hasAction

        if (hasAction) {
            val action = draft.actions.first()
            binding.tvActionType.text = "Azione"
            if (action is AutomationAction.DeviceAction) {
                binding.tvActionSummary.text = "${action.service} ${action.deviceName}"

                val deviceType = deviceUiMapper.mapDomainToDeviceType(action.domain)
                val uiStyle = deviceType.getUiStyle()

                binding.ivActionIcon.setImageResource(uiStyle.iconRes)
                binding.ivActionIcon.setColorFilter(
                    requireContext().getColor(uiStyle.activeColorRes),
                    android.graphics.PorterDuff.Mode.SRC_IN,
                )
            } else {
                binding.ivActionIcon.setImageResource(R.drawable.ic_check_circle)
                binding.ivActionIcon.clearColorFilter()
            }
        }

        binding.btnNext.isEnabled = state.isNextEnabled
    }

    private fun resolveTriggerTitle(trigger: AutomationTrigger): String =
        when (trigger) {
            is AutomationTrigger.Time -> "Orario"
            is AutomationTrigger.Solar -> "Evento Solare"
            is AutomationTrigger.DeviceState -> "Stato Dispositivo"
        }

    private fun resolveTriggerSubtitle(trigger: AutomationTrigger): String =
        when (trigger) {
            is AutomationTrigger.Time -> {
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val timeStr = trigger.time.format(timeFormatter)

                if (trigger.days.isEmpty()) {
                    "$timeStr (Una volta)"
                } else if (trigger.days.size == 7) {
                    "$timeStr (Ogni giorno)"
                } else {
                    val daysStr =
                        trigger.days
                            .sorted()
                            .joinToString(", ") { day ->
                                day
                                    .getDisplayName(TextStyle.SHORT, Locale.ITALIAN)
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ITALIAN) else it.toString() }
                            }
                    "$timeStr, $daysStr"
                }
            }

            is AutomationTrigger.Solar -> {
                val offsetText = solarUiMapper.mapToLabel(trigger.offsetMinutes, trigger.event).asString(requireContext())

                if (trigger.days.isNotEmpty()) {
                    "$offsetText (${trigger.days.size} giorni)"
                } else {
                    offsetText
                }
            }

            is AutomationTrigger.DeviceState -> {
                "${trigger.deviceName} ${trigger.operator} ${trigger.value}"
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
