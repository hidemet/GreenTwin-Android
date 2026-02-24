package com.ndumas.appdt.presentation.control.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.FragmentDeviceDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeviceDetailFragment : Fragment() {
    private var _binding: FragmentDeviceDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DeviceDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDeviceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        observeState()
    }

    private fun setupToolbar() {
        binding.includeToolbar.toolbar.apply {
            title = ""
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
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

    private fun updateUi(state: DeviceDetailUiState) {
        binding.loadingSpinner.isVisible = state.isLoading
        binding.loadingOverlay.isVisible = state.isLoading
        binding.cardDetails.isVisible = !state.isLoading

        if (!state.isLoading) {
            binding.includeToolbar.toolbar.title = getString(R.string.device_detail_title)
            binding.tvName.text = state.name
            binding.tvDeviceType.text = state.deviceType
            binding.ivDeviceIcon.setImageResource(state.deviceIconRes)
            binding.tvRoom.text = state.room ?: getString(R.string.no_room)
            binding.tvGroup.text = state.group ?: getString(R.string.no_group)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
