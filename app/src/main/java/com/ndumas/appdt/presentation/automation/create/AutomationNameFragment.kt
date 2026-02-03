package com.ndumas.appdt.presentation.automation.create

import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import com.ndumas.appdt.databinding.FragmentAutomationNameBinding
import com.ndumas.appdt.domain.automation.model.SimulationResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AutomationNameFragment : Fragment(R.layout.fragment_automation_name) {
    private val viewModel: AutomationCreateViewModel by hiltNavGraphViewModels(R.id.automation_graph)
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
    }

    override fun onResume() {
        super.onResume()

        binding.etName.postDelayed({
            if (_binding != null) {
                binding.etName.requestFocus()
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
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.btnSave.isEnabled = false

        binding.etName.addTextChangedListener { text ->
            viewModel.onEvent(AutomationCreateUiEvent.UpdateName(text.toString()))
            updateSaveButtonState(text.toString())
        }

        binding.btnSave.setOnClickListener {
            val imm =
                requireContext().getSystemService(
                    android.content.Context.INPUT_METHOD_SERVICE,
                ) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)

            android.util.Log.d("DEBUG_FRAG", "Bottone premuto!")
            val name = binding.etName.text.toString()
            if (name.isBlank()) {
                binding.tilName.error = "Inserisci un nome"
                return@setOnClickListener
            }

            viewModel.onEvent(AutomationCreateUiEvent.SaveAutomation)
        }
    }

    private fun updateSaveButtonState(text: String? = null) {
        val currentText = text ?: binding.etName.text.toString()
        val isLoading = viewModel.uiState.value.isLoading
        binding.btnSave.isEnabled = currentText.isNotBlank() && !isLoading
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    updateSaveButtonState()
                    binding.btnSave.text = if (state.isLoading) "" else getString(R.string.confirm)
                    binding.progressSave.isVisible = state.isLoading

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
                        is AutomationCreateUiEvent.AutomationSaved -> {
                            Toast.makeText(context, "Automazione salvata!", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack(R.id.automation_graph, true)
                        }

                        is AutomationCreateUiEvent.ShowError -> {
                            Toast.makeText(context, event.message.asString(requireContext()), Toast.LENGTH_SHORT).show()
                        }

                        else -> { /* Altri eventi gestiti altrove o non pertinenti qui */ }
                    }
                }
            }
        }
    }

    private fun showConflictDialog(result: SimulationResult) {
        val context = requireContext()

        val sb = StringBuilder()

        if (result.conflicts.isNotEmpty()) {
            sb.append("Rilevati i seguenti problemi:\n")
            result.conflicts.forEach { conflict ->
                sb.append("• ${conflict.description}\n")
            }
        }

        val suggestion = result.suggestions.firstOrNull()
        if (suggestion != null) {
            sb.append("\n💡 Suggerimento: ")
            if (suggestion.newTime != null) {
                sb.append("Sposta alle ${suggestion.newTime}")
            }
            if (suggestion.savings != null && suggestion.savings > 0) {
                sb.append(" (Risparmi €${String.format("%.2f", suggestion.savings)})")
            }
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_conflict_title)
            .setMessage(sb.toString().trim())
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton(R.string.dialog_conflict_edit) { dialog, _ ->

                viewModel.onEvent(AutomationCreateUiEvent.DismissConflictDialog)
                dialog.dismiss()
                findNavController().popBackStack(R.id.automationHubFragment, false)
            }.setNegativeButton(R.string.dialog_conflict_ignore) { dialog, _ ->

                viewModel.onEvent(AutomationCreateUiEvent.ForceSaveAutomation)
                dialog.dismiss()
            }.setOnCancelListener {
                viewModel.onEvent(AutomationCreateUiEvent.DismissConflictDialog)
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
