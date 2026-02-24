package com.ndumas.appdt.presentation.automation.edit

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
import com.ndumas.appdt.databinding.FragmentActionSelectorBinding
import com.ndumas.appdt.presentation.automation.create.adapter.ActionGroupAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditActionSelectorFragment : Fragment(R.layout.fragment_action_selector) {
    private val viewModel: AutomationEditViewModel by hiltNavGraphViewModels(R.id.automation_edit_graph)
    private var _binding: FragmentActionSelectorBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentActionSelectorBinding.bind(view)

        setupUI()
        observeState()

        viewModel.onEvent(AutomationEditUiEvent.LoadActionDevices)
    }

    private fun setupToolBar() {
        binding.includeToolbar.toolbar.title = "Allora"
        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupUI() {
        setupToolBar()

        val adapter =
            ActionGroupAdapter { device ->
                val action =
                    EditActionSelectorFragmentDirections.actionSelectorToDeviceAction(
                        deviceId = device.id,
                        deviceType = device.type.name,
                    )
                findNavController().navigate(action)
            }

        binding.rvDevices.adapter = adapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.isVisible = state.isLoading
                    binding.rvDevices.isVisible = !state.isLoading

                    (binding.rvDevices.adapter as? ActionGroupAdapter)
                        ?.submitList(state.availableDeviceGroups)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
