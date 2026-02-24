package com.ndumas.appdt.presentation.device

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
import androidx.recyclerview.widget.GridLayoutManager
import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.SnackbarHelper
import com.ndumas.appdt.databinding.FragmentDeviceBinding
import com.ndumas.appdt.presentation.device.adapter.DeviceListAdapter
import com.ndumas.appdt.presentation.home.model.DashboardItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeviceFragment : Fragment() {
    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DeviceListViewModel by viewModels()
    private lateinit var adapter: DeviceListAdapter

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
        setupHeader()
        setupRecyclerView()
        setupListeners()
        observeUiState()
    }

    private fun setupRecyclerView() {
        adapter =
            DeviceListAdapter(
                onDeviceToggle = { widget ->
                    viewModel.onDeviceClicked(widget.device)
                },
                onDeviceDetails = { widget ->
                    viewModel.onDeviceLongClicked(widget.device)
                },
                onSectionToggle = { headerId ->
                    viewModel.onSectionToggle(headerId)
                },
            )

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)

        gridLayoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val type = adapter.getItemViewType(position)
                    return when (type) {
                        DeviceListAdapter.TYPE_HEADER,
                        DeviceListAdapter.TYPE_EMPTY_STATE,
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

    private fun setupHeader() {
        binding.includeToolbar.toolbar.title = getString(R.string.nav_devices)

        // Configurazione tab
        val tabLayout = binding.includeToolbar.tabLayout

        if (tabLayout.tabCount == 0) {
            tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_rooms))
            tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_groups))
            tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_unassigned))
        }

        // Seleziona tab in base ai parametri di navigazione
        val scrollTarget = arguments?.getString("scrollTarget")
        val isRoom = arguments?.getBoolean("isRoom", true) ?: true

        if (scrollTarget != null) {
            val tabIndex = if (isRoom) 0 else 1
            tabLayout.getTabAt(tabIndex)?.select()
        }
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.onRefresh()
        }
        binding.swipeRefresh.setColorSchemeResources(R.color.tw_lime_500)

        binding.includeToolbar.tabLayout.addOnTabSelectedListener(
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

                        adapter.submitList(state.items) {
                            // Dopo submit, controlla se c'è un target di scroll pendente
                            // Solo se non stiamo caricando e abbiamo elementi
                            if (!state.isLoading && state.items.isNotEmpty()) {
                                handlePendingScrollTarget(state.items)
                            }
                        }
                    }
                }

                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is DeviceListUiEvent.NavigateToControl -> {
                                val action =
                                    DeviceFragmentDirections.actionDeviceFragmentToDeviceControlFragment(
                                        deviceId = event.deviceId,
                                    )
                                findNavController().navigate(action)
                            }

                            is DeviceListUiEvent.ShowToast -> {
                                SnackbarHelper.showInfo(binding.root, event.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handlePendingScrollTarget(items: List<DashboardItem>) {
        val target = viewModel.consumePendingScrollTarget() ?: return
        val headerId = "header_$target"

        val position =
            items.indexOfFirst {
                it is DashboardItem.SectionHeader && it.id == headerId
            }

        if (position != -1) {
            binding.rvDevices.post {
                binding.rvDevices.smoothScrollToPosition(position)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
