package com.ndumas.appdt.presentation.automation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.SnackbarHelper
import com.ndumas.appdt.core.ui.device.getUiStyle
import com.ndumas.appdt.databinding.FragmentAutomationDetailBinding
import com.ndumas.appdt.domain.automation.model.Automation
import com.ndumas.appdt.domain.automation.model.AutomationAction
import com.ndumas.appdt.domain.automation.model.AutomationTrigger
import com.ndumas.appdt.domain.automation.model.SolarEvent
import com.ndumas.appdt.presentation.automation.create.mapper.DeviceUiMapper
import com.ndumas.appdt.presentation.automation.util.AutomationServiceTranslator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AutomationDetailFragment : Fragment() {
    private var _binding: FragmentAutomationDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AutomationDetailViewModel by viewModels()

    @Inject
    lateinit var deviceUiMapper: DeviceUiMapper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAutomationDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupListeners()
        observeState()
        observeEvents()
    }

    private fun setupToolbar() {
        binding.includeToolbar.toolbar.apply {
            title = getString(R.string.automation_detail_title)
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            inflateMenu(R.menu.menu_automation_detail)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        viewModel.onEditClick()
                        true
                    }

                    R.id.action_delete -> {
                        viewModel.onDeleteClick()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
            val currentState =
                viewModel.uiState.value.automation
                    ?.isActive
            if (currentState != null && currentState != isChecked) {
                viewModel.onToggleActive(isChecked)
            }
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadAutomation()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    when (event) {
                        is AutomationDetailUiEvent.NavigateBack -> {
                            findNavController().navigateUp()
                        }

                        is AutomationDetailUiEvent.NavigateToEdit -> {
                            navigateToEdit()
                        }

                        is AutomationDetailUiEvent.ShowSnackbar -> {
                            SnackbarHelper.showInfo(binding.root, event.message)
                        }

                        is AutomationDetailUiEvent.AutomationDeleted -> {
                            findNavController().navigateUp()
                        }

                        is AutomationDetailUiEvent.ShowUndoSnackbar -> {
                            SnackbarHelper.showWithUndo(
                                binding.root,
                                event.message,
                            ) {
                                viewModel.cancelDelete()
                            }
                        }

                        is AutomationDetailUiEvent.DeleteCancelled -> {
                            SnackbarHelper.showInfo(binding.root, R.string.automation_delete_cancelled)
                        }
                    }
                }
            }
        }
    }

    private fun updateUi(state: AutomationDetailUiState) {
        binding.loadingOverlay.isVisible = state.isLoading
        binding.scrollView.isVisible = !state.isLoading && state.automation != null && state.error == null
        binding.errorState.isVisible = state.error != null && !state.isLoading

        state.error?.let {
            binding.tvErrorMessage.text = it.asString(requireContext())
        }

        state.automation?.let { automation ->
            bindAutomation(automation)
        }

        if (state.showDeleteConfirmDialog) {
            showDeleteConfirmationDialog()
        }

        binding.switchActive.isEnabled = !state.isToggling
    }

    private fun bindAutomation(automation: Automation) {
        // Nome automazione
        binding.tvAutomationName.text = automation.name
        // Stato attivo
        binding.switchActive.setOnCheckedChangeListener(null)
        binding.switchActive.isChecked = automation.isActive
        binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
            val currentState =
                viewModel.uiState.value.automation
                    ?.isActive
            if (currentState != null && currentState != isChecked) {
                viewModel.onToggleActive(isChecked)
            }
        }

        // Descrizione
        if (automation.description.isNotBlank()) {
            binding.rowDescription.isVisible = true
            binding.tvDescription.text = automation.description
        } else {
            binding.rowDescription.isVisible = false
        }

        // Trigger
        bindTrigger(automation.triggers.firstOrNull())

        // Azioni
        bindActions(automation.actions)
    }

    private fun bindTrigger(trigger: AutomationTrigger?) {
        if (trigger == null) {
            binding.tvTriggerTitle.text = getString(R.string.automation_no_trigger)
            binding.tvTriggerDays.isVisible = false
            binding.ivTriggerIcon.setImageResource(R.drawable.ic_schedule)
            return
        }

        when (trigger) {
            is AutomationTrigger.Time -> {
                binding.ivTriggerIcon.setImageResource(R.drawable.ic_schedule)
                binding.tvTriggerTitle.text =
                    getString(
                        R.string.automation_at_time,
                        trigger.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    )

                if (trigger.days.isNotEmpty()) {
                    binding.tvTriggerDays.text = formatDays(trigger.days)
                    binding.tvTriggerDays.isVisible = true
                } else {
                    binding.tvTriggerDays.isVisible = false
                }
            }

            is AutomationTrigger.Solar -> {
                binding.ivTriggerIcon.setImageResource(R.drawable.ic_sunny)
                binding.tvTriggerTitle.text =
                    when (trigger.event) {
                        SolarEvent.SUNRISE -> getString(R.string.automation_at_sunrise)
                        SolarEvent.SUNSET -> getString(R.string.automation_at_sunset)
                    }

                if (trigger.days.isNotEmpty()) {
                    binding.tvTriggerDays.text = formatDays(trigger.days)
                    binding.tvTriggerDays.isVisible = true
                } else {
                    binding.tvTriggerDays.isVisible = false
                }
            }

            is AutomationTrigger.DeviceState -> {
                binding.ivTriggerIcon.setImageResource(R.drawable.ic_devices)
                binding.tvTriggerTitle.text =
                    getString(
                        R.string.automation_when_device_changes,
                        trigger.deviceName.ifBlank { "dispositivo" },
                    )
                binding.tvTriggerDays.isVisible = false
            }
        }
    }

    private fun bindActions(actions: List<AutomationAction>) {
        binding.containerActions.removeAllViews()

        if (actions.isEmpty()) {
            val textView =
                TextView(requireContext()).apply {
                    text = getString(R.string.automation_no_actions)
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
                    setTextColor(resources.getColor(R.color.tw_gray_800, null))
                }
            binding.containerActions.addView(textView)
            return
        }

        for ((index, action) in actions.withIndex()) {
            when (action) {
                is AutomationAction.DeviceAction -> {
                    val textView =
                        TextView(requireContext()).apply {
                            text = formatActionText(action)
                            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
                            setTextColor(resources.getColor(R.color.tw_gray_800, null))
                            if (index > 0) {
                                setPadding(0, 8, 0, 0)
                            }
                        }
                    binding.containerActions.addView(textView)
                }
            }
        }
    }

    /**
     * Formatta il testo dell'azione nel formato "Verbo nome dispositivo (parametri)"
     * Es: "Accendi luce cucina" o "Accendi luce cucina (Luminosità: 80%)"
     */
    private fun formatActionText(action: AutomationAction.DeviceAction): String {
        val serviceText = AutomationServiceTranslator.translateService(requireContext(), action.service)
        val deviceName = action.deviceName.ifBlank { "dispositivo" }.lowercase()

        val params = formatActionParams(action)

        return if (params.isNotBlank()) {
            "$serviceText $deviceName ($params)"
        } else {
            "$serviceText $deviceName"
        }
    }

    private fun formatActionParams(action: AutomationAction.DeviceAction): String =
        action.parameters.entries.joinToString(", ") { (key, value) ->
            when (key) {
                "brightness" -> "Luminosità: ${(value as? Number)?.toInt() ?: value}%"
                "color_temp" -> "Temperatura: $value"
                "rgb_color" -> "Colore RGB"
                else -> "$key: $value"
            }
        }

    private fun formatDays(days: List<DayOfWeek>): String {
        if (days.size == 7) return "Ogni giorno"
        if (days.size == 5 &&
            days.containsAll(
                listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                ),
            )
        ) {
            return "Giorni feriali"
        }
        if (days.size == 2 && days.containsAll(listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))) {
            return "Fine settimana"
        }

        return days.joinToString(", ") {
            it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }
    }

    private fun showDeleteConfirmationDialog() {
        val dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_automation_title)
                .setMessage(R.string.delete_automation_message)
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    viewModel.onDismissDeleteDialog()
                    dialog.dismiss()
                }.setPositiveButton(R.string.delete) { dialog, _ ->
                    viewModel.onConfirmDelete()
                    dialog.dismiss()
                }.setOnDismissListener {
                    viewModel.onDismissDeleteDialog()
                }.create()

        dialog.show()

        // Apply colorSurface background and colorOnSurface text after showing
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val surfaceColor =
            com.google.android.material.color.MaterialColors.getColor(
                requireContext(),
                com.google.android.material.R.attr.colorSurface,
                "colorSurface not found",
            )

        val onSurfaceColor =
            com.google.android.material.color.MaterialColors.getColor(
                requireContext(),
                com.google.android.material.R.attr.colorOnSurface,
                "colorOnSurface not found",
            )

        dialog.findViewById<android.view.View>(com.google.android.material.R.id.parentPanel)?.apply {
            setBackgroundColor(surfaceColor)
        }

        dialog.findViewById<android.widget.TextView>(androidx.appcompat.R.id.alertTitle)?.apply {
            setTextColor(onSurfaceColor)
        }

        dialog.findViewById<android.widget.TextView>(android.R.id.message)?.apply {
            setTextColor(onSurfaceColor)
        }
    }

    private fun navigateToEdit() {
        val automationId = viewModel.getAutomationId()
        val bundle =
            Bundle().apply {
                putString("automationId", automationId)
            }
        findNavController().navigate(R.id.action_automationDetailFragment_to_edit_flow, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
