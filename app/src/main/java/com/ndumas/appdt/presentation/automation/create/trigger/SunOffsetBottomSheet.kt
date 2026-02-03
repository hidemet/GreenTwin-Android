package com.ndumas.appdt.presentation.automation.create.trigger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ndumas.appdt.databinding.BottomSheetSunOffsetBinding
import com.ndumas.appdt.domain.automation.model.SolarEvent
import com.ndumas.appdt.domain.automation.model.SolarPresetProvider
import com.ndumas.appdt.presentation.automation.create.adapter.SunOffsetAdapter
import com.ndumas.appdt.presentation.automation.create.adapter.SunOffsetUiModel
import com.ndumas.appdt.presentation.automation.create.mapper.SolarUiMapper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SunOffsetBottomSheet : BottomSheetDialogFragment() {
    @Inject lateinit var presetProvider: SolarPresetProvider

    @Inject lateinit var uiMapper: SolarUiMapper

    private var _binding: BottomSheetSunOffsetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetSunOffsetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Recupera offset ed evento
        val currentOffset = arguments?.getLong(ARG_CURRENT_OFFSET) ?: 0L
        // Recuperiamo l'enum (sicuro) o default SUNRISE
        val eventName = arguments?.getString(ARG_EVENT) ?: SolarEvent.SUNRISE.name
        val currentEvent = SolarEvent.valueOf(eventName)

        val adapter = SunOffsetAdapter(selectedOffset = currentOffset)
        binding.rvOffsets.adapter = adapter

        //  Carica Dati usando l'evento corrente per le label corrette
        val rawOffsets = presetProvider.getPresets()
        val uiModels =
            rawOffsets.map { offset ->
                SunOffsetUiModel(
                    offsetMinutes = offset,
                    label = uiMapper.mapToLabel(offset, currentEvent),
                )
            }
        adapter.submitList(uiModels)

        val selectedIndex = rawOffsets.indexOf(currentOffset)
        if (selectedIndex != -1) {
            binding.rvOffsets.scrollToPosition(selectedIndex)
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener {
            val selected = adapter.getSelectedOffset()
            if (selected != null) {
                setFragmentResult(REQUEST_KEY, bundleOf(RESULT_OFFSET to selected))
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SunOffsetBottomSheet"
        const val REQUEST_KEY = "sun_offset_request"
        const val RESULT_OFFSET = "offset_minutes"

        private const val ARG_CURRENT_OFFSET = "arg_current_offset"
        private const val ARG_EVENT = "arg_event"

        fun newInstance(
            currentOffset: Long,
            event: SolarEvent,
        ): SunOffsetBottomSheet =
            SunOffsetBottomSheet().apply {
                arguments =
                    bundleOf(
                        ARG_CURRENT_OFFSET to currentOffset,
                        ARG_EVENT to event.name,
                    )
            }
    }
}
