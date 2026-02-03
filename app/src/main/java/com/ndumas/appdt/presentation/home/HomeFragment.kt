package com.ndumas.appdt.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.FragmentHomeBinding
import com.ndumas.appdt.presentation.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest // Importa collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var dashboardAdapter: DashboardAdapter
    private lateinit var touchHelperCallback: DashboardTouchHelper
    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            AddWidgetBottomSheet.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, bundle ->
            val type = bundle.getString(AddWidgetBottomSheet.RESULT_KEY)

            if (type == AddWidgetBottomSheet.TYPE_DEVICE) {
                val selectionSheet = WidgetSelectionBottomSheet()
                selectionSheet.show(parentFragmentManager, WidgetSelectionBottomSheet.TAG)
            } else if (type == AddWidgetBottomSheet.TYPE_AUTOMATION) {
                // TODO: Gestire automazioni in futuro
                Toast.makeText(requireContext(), "Automazioni in arrivo!", Toast.LENGTH_SHORT).show()
            }
        }
        setupRecyclerView()
        setupToolbar()
        setupFab()
        setupSwipeRefresh()
        observeUiState()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.onRefresh()
        }
        binding.swipeRefresh.setColorSchemeResources(R.color.tw_lime_500)
    }

    private fun setupRecyclerView() {
        dashboardAdapter =
            DashboardAdapter(
                // DISPOSITIVI
                onDeviceToggle = { widget ->
                    viewModel.onDeviceToggleClick(widget.device)
                },
                onDeviceDetails = { widget ->
                    viewModel.onDeviceLongClicked(widget.device)
                },
                // AUTOMAZIONI
                onAutomationToggle = { widget ->
                    // TODO: futuro viewModel.onAutomationToggle(widget)
                    Toast.makeText(context, "Automazione: ${widget.name}", Toast.LENGTH_SHORT).show()
                },
                onAutomationDetails = { widget ->
                    Toast.makeText(context, "Dettagli Automazione: ${widget.name}", Toast.LENGTH_SHORT).show()
                },
                // EDIT MODE
                onDeleteClick = { item ->
                    viewModel.removeWidget(item.id)
                },
                dragStartListener = { holder ->
                    android.util.Log.d("DragDebug", "dragStartListener invoked for holder at position ${holder.bindingAdapterPosition}")
                    if (::itemTouchHelper.isInitialized) {
                        android.util.Log.d("DragDebug", "Calling itemTouchHelper.startDrag()")
                        itemTouchHelper.startDrag(holder)
                    } else {
                        Toast.makeText(context, "Errore: Drag helper non pronto", Toast.LENGTH_SHORT).show()
                    }
                },
            )

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val viewType = dashboardAdapter.getItemViewType(position)
                    return when (viewType) {
                        DashboardAdapter.TYPE_ENERGY,
                        DashboardAdapter.TYPE_HEADER,
                        -> 2

                        else -> 1
                    }
                }
            }

        touchHelperCallback =
            DashboardTouchHelper(dashboardAdapter) { _ ->
                val currentList = dashboardAdapter.currentList
                viewModel.updateLocalOrder(currentList)
            }
        itemTouchHelper = ItemTouchHelper(touchHelperCallback)

        binding.rvDevices.apply {
            adapter = dashboardAdapter
            layoutManager = gridLayoutManager
        }
        itemTouchHelper.attachToRecyclerView(binding.rvDevices)
    }

    private fun setupToolbar() {
        binding.btnEditPencil.setOnClickListener { viewModel.toggleEditMode() }
        binding.btnDone.setOnClickListener { viewModel.saveEditModeChanges() }
        binding.btnCloseEdit.setOnClickListener { viewModel.toggleEditMode() }
        binding.ivProfile.setOnClickListener { view ->
            showUserMenu(view)
        }
    }

    private fun showUserMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_user_profile, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    mainViewModel.logout()
                    true
                }

                else -> {
                    false
                }
            }
        }
        popup.show()
    }

    private fun setupFab() {
        binding.fabAddWidget.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addWidgetBottomSheet)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { state ->

                        if (state.isLoading && !binding.swipeRefresh.isRefreshing) {
                            binding.progressBar.isVisible = true
                        } else {
                            binding.progressBar.isVisible = false
                            binding.swipeRefresh.isRefreshing = false
                        }
                        binding.tvError.isVisible = state.error != null
                        state.error?.let { binding.tvError.text = it.asString(requireContext()) }

                        if (!state.isLoading) {
                            dashboardAdapter.submitList(state.dashboardItems)
                        }
                        updateEditModeUi(state.isEditMode)

                        touchHelperCallback.isEditMode = state.isEditMode
                        dashboardAdapter.setEditMode(state.isEditMode)
                    }
                }

                launch {
                    viewModel.uiEvent.collectLatest { event ->

                        when (event) {
                            is HomeUiEvent.NavigateToLightControl -> {
                                val action = HomeFragmentDirections.actionHomeFragmentToLightControlFragment(event.entityId)
                                findNavController().navigate(action)
                            }

                            is HomeUiEvent.ShowToast -> {
                                Toast.makeText(requireContext(), event.message.asString(requireContext()), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateEditModeUi(isEditMode: Boolean) {
        if (isEditMode) {
            binding.tvToolbarTitle.text = "Personalizza Home"
            binding.btnCloseEdit.visibility = View.VISIBLE
            binding.btnDone.visibility = View.VISIBLE
            binding.fabAddWidget.show()
            binding.groupNormalMode.visibility = View.GONE
        } else {
            binding.tvToolbarTitle.text = "Home"
            binding.btnCloseEdit.visibility = View.GONE
            binding.btnDone.visibility = View.GONE
            binding.fabAddWidget.hide()
            binding.groupNormalMode.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
