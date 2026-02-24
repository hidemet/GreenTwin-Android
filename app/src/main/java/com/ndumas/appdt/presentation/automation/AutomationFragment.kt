package com.ndumas.appdt.presentation.automation

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.SnackbarHelper
import com.ndumas.appdt.databinding.FragmentAutomationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AutomationFragment : Fragment() {
    private var _binding: FragmentAutomationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AutomationListViewModel by viewModels()

    private lateinit var adapter: AutomationListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAutomationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
        setupRecyclerView()
        setupListeners()
        observeState()
    }

    private fun setupUi() {
        binding.includeToolbar.toolbar.title = getString(R.string.nav_automation)
        // Nasconde il pulsante di navigazione indietro dalla toolbar
        binding.includeToolbar.toolbar.navigationIcon = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.onRefresh()
    }

    private fun setupRecyclerView() {
        adapter =
            AutomationListAdapter(
                onToggle = { widget, isActive ->
                    viewModel.onAutomationToggle(widget.id, isActive)
                },
                onItemClick = { widget ->
                    // Naviga alla schermata dettagli
                    val bundle =
                        Bundle().apply {
                            putString("automationId", widget.id)
                        }
                    findNavController().navigate(
                        R.id.action_automationFragment_to_automationDetailFragment,
                        bundle,
                    )
                },
            )

        binding.rvAutomations.apply {
            adapter = this@AutomationFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.onRefresh()
        }
        binding.swipeRefresh.setColorSchemeResources(R.color.tw_lime_500)

        binding.fabCreateAutomation.setOnClickListener {
            findNavController().navigate(R.id.action_automationFragment_to_create_flow)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { state ->
                        val isSwipeRefreshing = binding.swipeRefresh.isRefreshing

                        binding.progressBar.isVisible = state.isLoading && state.items.isEmpty() && !isSwipeRefreshing

                        if (!state.isLoading) binding.swipeRefresh.isRefreshing = false

                        val showEmptyState = !state.isLoading && state.items.isEmpty() && state.error == null
                        binding.emptyState.isVisible = showEmptyState
                        binding.rvAutomations.isVisible = !showEmptyState

                        adapter.submitList(state.items)

                        state.error?.let {
                            SnackbarHelper.showError(binding.root, it)
                        }
                    }
                }

                launch {
                    viewModel.uiEvent.collectLatest { event ->
                        when (event) {
                            is AutomationListUiEvent.ShowToast -> {
                                SnackbarHelper.showInfo(binding.root, event.message)
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
