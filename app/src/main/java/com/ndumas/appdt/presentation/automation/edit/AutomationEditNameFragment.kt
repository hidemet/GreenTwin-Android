package com.ndumas.appdt.presentation.automation.edit

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.SnackbarHelper
import com.ndumas.appdt.databinding.FragmentAutomationNameBinding
import com.ndumas.appdt.domain.automation.model.SimulationResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AutomationEditNameFragment : Fragment(R.layout.fragment_automation_name) {
    private val viewModel: AutomationEditViewModel by hiltNavGraphViewModels(R.id.automation_edit_graph)
    private var _binding: FragmentAutomationNameBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAutomationNameBinding.bind(view)

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val imeInsets =
                insets.getInsets(
                    androidx.core.view.WindowInsetsCompat.Type
                        .ime(),
                )
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, imeInsets.bottom)
            insets
        }

        setupListeners()
        observeState()
        observeEvents()

        // Pre-fill with existing name
        binding.etName.setText(viewModel.uiState.value.draft.name)
    }

    override fun onResume() {
        super.onResume()

        binding.etName.postDelayed({
            if (_binding != null) {
                binding.etName.requestFocus()
                binding.etName.setSelection(binding.etName.text?.length ?: 0)
                val window = requireActivity().window
                androidx.core.view.WindowCompat
                    .getInsetsController(window, binding.etName)
                    .show(
                        androidx.core.view.WindowInsetsCompat.Type
                            .ime(),
                    )
            }
        }, 300)
    }

    private fun setupListeners() {
        with(binding.includeToolbar.toolbar) {
            title = getString(R.string.edit_automation_name_title)
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener { findNavController().popBackStack() }
        }

        binding.btnSave.text = getString(R.string.save)

        binding.etName.addTextChangedListener { text ->
            // Cancella l'errore quando l'utente inizia a digitare
            binding.tilName.error = null
            viewModel.onEvent(AutomationEditUiEvent.UpdateName(text.toString()))
        }

        binding.btnSave.setOnClickListener {
            val imm =
                requireContext().getSystemService(
                    android.content.Context.INPUT_METHOD_SERVICE,
                ) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)

            val name = binding.etName.text.toString()
            if (name.isBlank()) {
                binding.tilName.error = getString(R.string.error_empty_name)
                binding.etName.requestFocus()
                return@setOnClickListener
            }

            viewModel.onEvent(AutomationEditUiEvent.SaveAutomation)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Il pulsante è disabilitato solo durante il salvataggio
                    binding.btnSave.isEnabled = !state.isSaving
                    binding.btnSave.text = if (state.isSaving) "" else getString(R.string.save)
                    binding.progressSave.isVisible = state.isSaving

                    if (state.showConflictDialog && state.simulationResult != null) {
                        showConflictDialog(state.simulationResult)
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    when (event) {
                        is AutomationEditUiEvent.AutomationSaved -> {
                            SnackbarHelper.showSuccess(binding.root, R.string.automation_updated)
                            findNavController().popBackStack(R.id.automation_edit_graph, true)
                            findNavController().navigate(R.id.automationFragment)
                        }

                        is AutomationEditUiEvent.ShowError -> {
                            SnackbarHelper.showError(binding.root, event.message)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun showConflictDialog(result: SimulationResult) {
        val sb = StringBuilder()
        if (result.conflicts.isNotEmpty()) {
            sb.append("Rilevati i seguenti problemi:\n")
            result.conflicts.forEach { conflict ->
                sb.append("• ${conflict.description}\n")
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_conflict_title)
            .setMessage(sb.toString())
            .setIcon(R.drawable.ic_warning_amber)
            .setNegativeButton(R.string.dialog_conflict_edit) { dialog, _ ->
                viewModel.onEvent(AutomationEditUiEvent.DismissConflictDialog)
                dialog.dismiss()
            }.setPositiveButton(R.string.dialog_conflict_ignore) { dialog, _ ->
                viewModel.onEvent(AutomationEditUiEvent.ForceSaveAutomation)
                dialog.dismiss()
            }.setOnDismissListener {
                viewModel.onEvent(AutomationEditUiEvent.DismissConflictDialog)
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
