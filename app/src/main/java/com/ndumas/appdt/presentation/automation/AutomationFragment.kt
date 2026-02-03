package com.ndumas.appdt.presentation.automation

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.FragmentAutomationBinding
import com.ndumas.appdt.presentation.home.DashboardAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AutomationFragment : Fragment() {
    private var _binding: FragmentAutomationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AutomationListViewModel by viewModels()

    private lateinit var adapter: DashboardAdapter

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

        setupRecyclerView()
        setupListeners()
        observeState()
    }

    override fun onResume() {
        super.onResume()

        viewModel.onRefresh()
    }

    private fun setupRecyclerView() {
        adapter =
            DashboardAdapter(
                onDeviceToggle = {},
                onDeviceDetails = {},
                onAutomationToggle = { widget ->
                    // TODO: Implementare Logica Toggle
                    Toast.makeText(context, "Toggle ${widget.name} (WIP)", Toast.LENGTH_SHORT).show()
                },
                onAutomationDetails = { widget ->
                    // TODO: Implementare Logica Edit/Delete
                    Toast.makeText(context, "Dettagli ${widget.name} (WIP)", Toast.LENGTH_SHORT).show()
                },
                onDeleteClick = {},
                dragStartListener = {},
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

        binding.fabCreateAutomation.setOnClickListener {
            findNavController().navigate(R.id.action_automationFragment_to_create_flow)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->

                    val isSwipeRefreshing = binding.swipeRefresh.isRefreshing

                    binding.progressBar.isVisible = state.isLoading && state.items.isEmpty() && !isSwipeRefreshing

                    if (!state.isLoading) binding.swipeRefresh.isRefreshing = false

                    val showEmptyState = !state.isLoading && state.items.isEmpty() && state.error == null
                    binding.emptyState.isVisible = showEmptyState
                    binding.rvAutomations.isVisible = !showEmptyState

                    adapter.submitList(state.items)

                    state.error?.let {
                        Toast.makeText(context, it.asString(requireContext()), Toast.LENGTH_SHORT).show()
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
