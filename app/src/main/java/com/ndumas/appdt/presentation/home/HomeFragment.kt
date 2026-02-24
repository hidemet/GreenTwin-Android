package com.ndumas.appdt.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.ndumas.appdt.core.ui.SnackbarHelper
import com.ndumas.appdt.databinding.FragmentHomeBinding
import com.ndumas.appdt.presentation.MainViewModel
import com.ndumas.appdt.presentation.home.model.DashboardSectionType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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

        setupRecyclerView()
        setupToolbar()
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
                onAutomationDetails = { widget ->
                    // Naviga alla schermata dettagli automazione
                    val bundle =
                        Bundle().apply {
                            putString("automationId", widget.id)
                        }
                    findNavController().navigate(
                        R.id.action_homeFragment_to_automationDetailFragment,
                        bundle,
                    )
                },
                // STANZE/GRUPPI
                onRoomGroupClick = { widget ->
                    viewModel.onRoomGroupClicked(widget)
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
                        SnackbarHelper.showError(binding.root, "Errore: Drag helper non pronto")
                    }
                },
                // WIDGET ENERGIA
                onEnergyWidgetClick = {
                    // Naviga alla pagina consumi (tab Giorno selezionata di default)
                    findNavController().navigate(R.id.consumptionFragment)
                },
                onAddClick = { type ->
                    viewModel.onAddWidgetClick(type)
                },
                onSectionVisibilityToggle = { type ->
                    viewModel.toggleSectionVisibility(type)
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
                        DashboardAdapter.TYPE_EMPTY_STATE,
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
        binding.includeToolbar.btnEdit.setOnClickListener { viewModel.toggleEditMode() }
        binding.includeToolbar.btnDone.setOnClickListener { viewModel.saveEditModeChanges() }
        binding.includeToolbar.btnCloseEdit.setOnClickListener { viewModel.toggleEditMode() }
        binding.includeToolbar.ivProfile.setOnClickListener { view ->
            showUserMenu(view)
        }
    }

    private fun showUserMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor, 0, 0, R.style.Widget_App_PopupMenu_Surface)
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

                        // IMPORTANTE: Impostare prima lo stato dell'adapter
                        touchHelperCallback.isEditMode = state.isEditMode
                        dashboardAdapter.setHiddenSections(state.hiddenSections)
                        dashboardAdapter.setEditMode(state.isEditMode)

                        if (!state.isLoading) {
                            // Aggiorna la lista, poi applica i cambiamenti di stato pendenti
                            dashboardAdapter.submitList(state.dashboardItems) {
                                dashboardAdapter.applyPendingStateChanges()
                            }
                        }
                        updateEditModeUi(state.isEditMode)
                    }
                }

                launch {
                    viewModel.uiEvent.collectLatest { event ->
                        when (event) {
                            is HomeUiEvent.NavigateToDeviceControl -> {
                                val action = HomeFragmentDirections.actionHomeFragmentToDeviceControlFragment(event.entityId)
                                findNavController().navigate(action)
                            }

                            is HomeUiEvent.ShowToast -> {
                                SnackbarHelper.showInfo(binding.root, event.message)
                            }

                            is HomeUiEvent.NavigateToDevicesWithScroll -> {
                                val bundle =
                                    Bundle().apply {
                                        putString("scrollTarget", event.targetName)
                                        putBoolean("isRoom", event.isRoom)
                                    }
                                findNavController().navigate(R.id.deviceFragment, bundle)
                            }

                            is HomeUiEvent.OpenAddWidgetSheet -> {
                                openWidgetSelectionSheet(event.type)
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun updateEditModeUi(isEditMode: Boolean) {
        if (isEditMode) {
            binding.includeToolbar.tvTitle.text = getString(R.string.title_customize_home)
            binding.includeToolbar.btnCloseEdit.visibility = View.VISIBLE
            binding.includeToolbar.btnDone.visibility = View.VISIBLE
            binding.includeToolbar.groupActions.visibility = View.GONE
        } else {
            binding.includeToolbar.tvTitle.text = getString(R.string.title_home)
            binding.includeToolbar.btnCloseEdit.visibility = View.GONE
            binding.includeToolbar.btnDone.visibility = View.GONE
            binding.includeToolbar.groupActions.visibility = View.VISIBLE
        }
    }

    private fun openWidgetSelectionSheet(type: DashboardSectionType) {
        when (type) {
            DashboardSectionType.DEVICES -> {
                val selectionSheet = WidgetSelectionBottomSheet()
                selectionSheet.show(parentFragmentManager, WidgetSelectionBottomSheet.TAG)
            }

            DashboardSectionType.AUTOMATIONS -> {
                val automationSheet = AutomationSelectionBottomSheet()
                automationSheet.show(parentFragmentManager, AutomationSelectionBottomSheet.TAG)
            }

            DashboardSectionType.ROOMS_GROUPS -> {
                val roomGroupSheet = RoomGroupSelectionBottomSheet()
                roomGroupSheet.show(parentFragmentManager, RoomGroupSelectionBottomSheet.TAG)
            }

            DashboardSectionType.INFO -> {
                // Info non ha un add widget sheet per ora, o se vuoi gestire qualcosa puoi farlo qui
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
