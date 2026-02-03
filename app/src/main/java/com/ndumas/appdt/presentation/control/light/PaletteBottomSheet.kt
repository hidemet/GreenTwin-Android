package com.ndumas.appdt.presentation.control.light

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.BottomSheetPaletteBinding
import com.ndumas.appdt.presentation.control.DeviceControlEvent
import com.ndumas.appdt.presentation.control.DeviceControlUiState
import com.ndumas.appdt.presentation.control.DeviceControlViewModel
import com.ndumas.appdt.presentation.control.LightMode
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class PaletteBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetPaletteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DeviceControlViewModel by viewModels()

    private val args: PaletteBottomSheetArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetPaletteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
        setupTemperatureControls()
        setupColorControls()

        val initialMode = if (args.startTab == 1) LightMode.COLOR_MODE else LightMode.WHITE_MODE
        setInitialState(initialMode)

        observeState()
    }

    private fun setInitialState(mode: LightMode) {
        val tabIndex = if (mode == LightMode.COLOR_MODE) 1 else 0

        binding.tabLayoutPalette.post {
            binding.tabLayoutPalette.getTabAt(tabIndex)?.select()
        }

        updateViewVisibility(mode)
    }

    private fun setupTabs() {
        binding.tabLayoutPalette.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val mode =
                        when (tab?.position) {
                            1 -> LightMode.COLOR_MODE
                            else -> LightMode.WHITE_MODE
                        }
                    updateViewVisibility(mode)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            },
        )
    }

    private fun updateViewVisibility(mode: LightMode) {
        when (mode) {
            LightMode.WHITE_MODE -> {
                binding.includeTemperature.root.isVisible = true
                binding.includeColor.root.isVisible = false
            }

            LightMode.COLOR_MODE -> {
                binding.includeTemperature.root.isVisible = false
                binding.includeColor.root.isVisible = true
            }
        }
    }

    private fun setupTemperatureControls() {
        val include = binding.includeTemperature

        include.sliderTemperature.setLabelFormatter { value -> "${value.toInt()}K" }

        include.sliderTemperature.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.onEvent(DeviceControlEvent.OnTemperatureChanged(value))
            }
        }

        include.btnDone.setOnClickListener {
            notifyRefreshAndDismiss()
        }
    }

    private fun setupColorControls() {
        val include = binding.includeColor
        val context = requireContext()

        ContextCompat.getDrawable(context, R.drawable.thumb_ring_white)?.let { drawable ->
            include.colorPickerView.setSelectorDrawable(drawable)
            include.brightnessSlideBar.setSelectorDrawable(drawable)
        }

        include.colorPickerView.attachBrightnessSlider(include.brightnessSlideBar)

        include.colorPickerView.setColorListener(
            ColorEnvelopeListener { envelope, fromUser ->
                if (fromUser) {
                    viewModel.onEvent(DeviceControlEvent.OnColorSelected(envelope.color))
                }
            },
        )

        include.btnDone.setOnClickListener {
            notifyRefreshAndDismiss()
        }
    }

    private fun notifyRefreshAndDismiss() {
        val bundle = bundleOf("refresh" to true)

        val currentState = viewModel.uiState.value
        if (currentState is DeviceControlUiState.LightControl) {
            if (currentState.activeMode == LightMode.COLOR_MODE) {
                currentState.rgbColor?.let { bundle.putInt("optimistic_color", it) }
            } else {
                bundle.putFloat("optimistic_temp", currentState.colorTemp.toFloat())
            }
        }

        setFragmentResult("palette_result", bundle)
        dismiss()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    updateUiFromState(state)
                }
            }
        }
    }

    private fun updateUiFromState(state: DeviceControlUiState) {
        // Loading & Error
        val isLoading = state is DeviceControlUiState.Loading
        binding.progressBar.isVisible = isLoading

        if (isLoading || state is DeviceControlUiState.Error) {
            // Se c'è errore o caricamento, nascondi i controlli per sicurezza
            binding.includeTemperature.root.isVisible = false
            binding.includeColor.root.isVisible = false
            return
        }

        // Sync Controlli (Solo se è una luce)
        if (state is DeviceControlUiState.LightControl) {
            val isColorMode = state.activeMode == LightMode.COLOR_MODE

            // 1. Sync Slider Temperatura
            with(binding.includeTemperature.sliderTemperature) {
                // Aggiorna range se cambiato dal backend
                if (valueFrom != state.minTemp.toFloat()) valueFrom = state.minTemp.toFloat()
                if (valueTo != state.maxTemp.toFloat()) valueTo = state.maxTemp.toFloat()

                // Smart Update: evita loop se l'utente sta trascinando
                if (!isPressed && !isHovered) {
                    val safeVal = state.colorTemp.toFloat().coerceIn(valueFrom, valueTo)
                    if (abs(value - safeVal) > 1f) {
                        value = safeVal
                    }
                }
            }

            // 2. Sync Color Picker
            val picker = binding.includeColor.colorPickerView
            // Selettore colore: aggiorna solo se siamo in modalità colore
            if (isColorMode) {
                // Posticipa se la view non è misurata (fix crash layout)
                if (picker.width > 0) {
                    if (!picker.isPressed) picker.selectByHsvColor(state.rgbColor ?: -1)
                } else {
                    picker.post {
                        if (!picker.isPressed) picker.selectByHsvColor(state.rgbColor ?: -1)
                    }
                }
            }
        } else {
            // Se il dispositivo non è una luce (es. errore navigazione), chiudi
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
