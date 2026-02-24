package com.ndumas.appdt.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ndumas.appdt.databinding.BottomSheetRoomGroupSelectionBinding
import com.ndumas.appdt.presentation.home.adapter.RoomGroupSelectionAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RoomGroupSelectionBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetRoomGroupSelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var adapter: RoomGroupSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetRoomGroupSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Carica le stanze/gruppi disponibili
        viewModel.loadAvailableRoomsGroups()
    }

    private fun setupRecyclerView() {
        adapter =
            RoomGroupSelectionAdapter { id ->
                viewModel.toggleRoomGroupSelection(id)
            }

        binding.rvRoomGroups.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnConfirmAdd.setOnClickListener {
            viewModel.saveSelectedRoomsGroups()
            dismiss()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.roomGroupSelectionItems.collectLatest { items ->

                    if (items.isEmpty()) {
                        binding.layoutContent.isVisible = false
                        binding.layoutEmpty.isVisible = true
                    } else {
                        binding.layoutContent.isVisible = true
                        binding.layoutEmpty.isVisible = false
                        adapter.submitList(items.toList())

                        // Abilita il pulsante solo se c'è almeno una selezione
                        val hasSelection = items.any { it.isSelected }
                        binding.btnConfirmAdd.isEnabled = hasSelection

                        // Update button text with count
                        binding.btnConfirmAdd.text =
                            if (hasSelection) {
                                val selectedCount = items.count { it.isSelected }
                                getString(com.ndumas.appdt.R.string.action_add_count, selectedCount)
                            } else {
                                getString(com.ndumas.appdt.R.string.action_add)
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

    companion object {
        const val TAG = "RoomGroupSelectionBottomSheet"
    }
}
