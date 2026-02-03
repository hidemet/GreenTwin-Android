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
import com.ndumas.appdt.databinding.FragmentActionSelectorBinding
import com.ndumas.appdt.presentation.automation.create.adapter.ActionGroupAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ActionSelectorFragment : Fragment(R.layout.fragment_action_selector) {
    private val viewModel: AutomationCreateViewModel by hiltNavGraphViewModels(R.id.automation_graph)
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

        viewModel.onEvent(AutomationCreateUiEvent.LoadActionDevices)
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val adapter =
            ActionGroupAdapter { device ->

                val action =
                    ActionSelectorFragmentDirections.actionSelectorToDeviceAction(
                        deviceId = device.id,
                        deviceType = device.type.name,
                    )
                findNavController().navigate(action)
            }
        binding.rvDevices.adapter = adapter
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.isVisible = state.isLoading

                    val adapter = binding.rvDevices.adapter as? ActionGroupAdapter
                    adapter?.submitList(state.availableDeviceGroups)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
