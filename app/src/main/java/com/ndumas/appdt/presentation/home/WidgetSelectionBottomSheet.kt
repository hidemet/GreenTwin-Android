package com.ndumas.appdt.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.BottomSheetWidgetSelectionBinding
import com.ndumas.appdt.presentation.home.adapter.GroupedSelectableAdapter
import com.ndumas.appdt.presentation.home.model.SelectionItem
import kotlinx.coroutines.launch

class WidgetSelectionBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetWidgetSelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    private val adapter =
        GroupedSelectableAdapter { deviceId ->
            viewModel.toggleSelection(deviceId)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetWidgetSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.loadAvailableDevices()
    }

    private fun setupRecyclerView() {
        binding.rvSelectableDevices.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnConfirmAdd.setOnClickListener {
            viewModel.saveSelectedWidgets()
            dismiss()
        }

        binding.btnCloseEmpty.setOnClickListener {
            dismiss()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectionItems.collect { items ->

                    if (items.isEmpty()) {
                        binding.layoutContent.visibility = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                    } else {
                        binding.layoutContent.visibility = View.VISIBLE
                        binding.layoutEmpty.visibility = View.GONE

                        adapter.submitList(items)

                        val selectedCount =
                            items
                                .filterIsInstance<SelectionItem.SelectableDevice>()
                                .count { it.isSelected }

                        val hasSelection = selectedCount > 0
                        binding.btnConfirmAdd.isEnabled = hasSelection
                        binding.btnConfirmAdd.text =
                            if (hasSelection) {
                                getString(R.string.action_add_count, selectedCount)
                            } else {
                                getString(R.string.action_add)
                            }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.let { sheet ->
            val behavior = BottomSheetBehavior.from(sheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "WidgetSelectionBottomSheet"
    }
}
