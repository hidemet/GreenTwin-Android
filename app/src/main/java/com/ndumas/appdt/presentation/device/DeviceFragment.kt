package com.ndumas.appdt.presentation.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ndumas.appdt.databinding.FragmentDeviceBinding
import com.ndumas.appdt.presentation.home.DashboardAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeviceFragment : Fragment() {
    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DeviceListViewModel by viewModels()
    private lateinit var adapter: DashboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeUiState()
    }

    private fun setupRecyclerView() {
        adapter =
            DashboardAdapter(
                onDeviceToggle = { widget ->
                    viewModel.onDeviceClicked(widget.device)
                },
                onDeviceDetails = { widget ->
                    viewModel.onDeviceLongClicked(widget.device)
                },
                onAutomationToggle = {},
                onAutomationDetails = {},
                onDeleteClick = {},
                dragStartListener = {},
            )

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)

        gridLayoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val type = adapter.getItemViewType(position)
                    return when (type) {
                        DashboardAdapter.TYPE_HEADER,
                        DashboardAdapter.TYPE_EMPTY_STATE,
                        -> 2

                        else -> 1
                    }
                }
            }

        binding.rvDevices.apply {
            this.adapter = this@DeviceFragment.adapter
            this.layoutManager = gridLayoutManager
        }
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.onRefresh()
        }

        binding.tabLayout.addOnTabSelectedListener(
            object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                    tab?.let { viewModel.onTabSelected(it.position) }
                }

                override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}

                override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            },
        )
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { state ->
                        binding.progressBar.isVisible = state.isLoading && state.items.isEmpty()
                        binding.swipeRefresh.isRefreshing = state.isLoading && state.items.isNotEmpty()

                        binding.tvError.isVisible = state.error != null
                        state.error?.let { binding.tvError.text = it.asString(requireContext()) }

                        adapter.submitList(state.items)
                    }
                }

                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is DeviceListUiEvent.NavigateToControl -> {
                                val action =
                                    DeviceFragmentDirections.actionDeviceFragmentToLightControlFragment(
                                        deviceId = event.deviceId,
                                    )
                                findNavController().navigate(action)
                            }

                            is DeviceListUiEvent.ShowToast -> {
                                Toast.makeText(requireContext(), event.message.asString(requireContext()), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
