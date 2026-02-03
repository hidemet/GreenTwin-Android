package com.ndumas.appdt.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ndumas.appdt.databinding.BottomSheetAddWidgetBinding

class AddWidgetBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddWidgetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetAddWidgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddDevice.setOnClickListener {
            setFragmentResult(REQUEST_KEY, bundleOf(RESULT_KEY to TYPE_DEVICE))

            findNavController().popBackStack()
        }

        binding.btnAddAutomation.setOnClickListener {
            setFragmentResult(REQUEST_KEY, bundleOf(RESULT_KEY to TYPE_AUTOMATION))
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "add_widget_request"
        const val RESULT_KEY = "type"
        const val TYPE_DEVICE = "device"
        const val TYPE_AUTOMATION = "automation"
    }
}
