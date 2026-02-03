package com.ndumas.appdt.presentation.control.light

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.FragmentLightControlBinding
import com.ndumas.appdt.presentation.control.DeviceControlEvent
import com.ndumas.appdt.presentation.control.DeviceControlUiEvent
import com.ndumas.appdt.presentation.control.DeviceControlUiState
import com.ndumas.appdt.presentation.control.DeviceControlViewModel
import com.ndumas.appdt.presentation.control.LightMode
import com.ndumas.appdt.presentation.control.adapter.SensorGridAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class LightControlFragment : Fragment() {
    private var _binding: FragmentLightControlBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DeviceControlViewModel by viewModels()

    private val args: LightControlFragmentArgs by navArgs()

    private val sensorAdapter = SensorGridAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("palette_result") { _, bundle ->
            var hasOptimisticUpdate = false

            if (bundle.containsKey("optimistic_color")) {
                viewModel.onEvent(DeviceControlEvent.OnColorSelected(bundle.getInt("optimistic_color")))
                hasOptimisticUpdate = true
            }
            if (bundle.containsKey("optimistic_temp")) {
                viewModel.onEvent(DeviceControlEvent.OnTemperatureChanged(bundle.getFloat("optimistic_temp")))
                hasOptimisticUpdate = true
            }

            if (bundle.getBoolean("refresh") && !hasOptimisticUpdate) {
                viewModel.onEvent(DeviceControlEvent.OnRefresh)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLightControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupListeners()
        setupSlider()
        setupFab()
        setupPresets()
        setupRecyclerView()

        observeViewModel()
    }

    private fun setupToolbar() {
        binding.btnClose.setOnClickListener { findNavController().popBackStack() }
        binding.btnInfo.setOnClickListener {
            Snackbar.make(binding.root, "ID Dispositivo: ${args.deviceId}", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        binding.rvSensors.apply {
            adapter = sensorAdapter

            layoutManager = GridLayoutManager(requireContext(), 2)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupListeners() {
        binding.btnPowerToggle.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            viewModel.onEvent(DeviceControlEvent.Toggle)
        }

        setupToggleGroupListener()
    }

    private fun setupToggleGroupListener() {
        binding.toggleGroupPower.addOnButtonCheckedListener { _, checkedId, isChecked ->

            if (isChecked) {
                val isTargetOn = (checkedId == R.id.btn_pill_on)

                val currentStateOn =
                    when (val state = viewModel.uiState.value) {
                        is DeviceControlUiState.LightControl -> state.isOn
                        is DeviceControlUiState.GenericControl -> state.isOn
                        else -> false
                    }

                if (isTargetOn != currentStateOn) {
                    binding.toggleGroupPower.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.onEvent(DeviceControlEvent.Toggle)
                }
            }
        }
    }

    private fun setupSlider() {
        binding.sliderBrightness.setLabelFormatter { value ->
            "${value.toInt()}%"
        }
        binding.sliderBrightness.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.onEvent(DeviceControlEvent.OnBrightnessChanged(value))
            }
        }
    }

    private fun setupFab() {
        binding.fabPalette.setOnClickListener {
            viewModel.onEvent(DeviceControlEvent.OnPaletteClick)
        }
    }

    private fun setupPresets() {
        val presets =
            listOf(
                binding.preset1,
                binding.preset2,
                binding.preset3,
                binding.preset4,
                binding.preset5,
                binding.preset6,
            )

        presets.forEach { card ->
            val color = card.cardBackgroundColor.defaultColor
            card.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                viewModel.onEvent(DeviceControlEvent.OnPresetSelected(color))
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { state ->
                        updateUi(state)
                    }
                }

                launch {
                    viewModel.uiEvent.collectLatest { event ->
                        when (event) {
                            is DeviceControlUiEvent.ShowError -> {
                                Snackbar
                                    .make(binding.root, event.message.asString(requireContext()), Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(requireContext().getColor(R.color.tw_red_500))
                                    .show()
                            }

                            DeviceControlUiEvent.NavigateBack -> {
                                findNavController().popBackStack()
                            }

                            DeviceControlUiEvent.OpenPalette -> {
                                val action =
                                    LightControlFragmentDirections
                                        .actionLightControlFragmentToPaletteBottomSheet(args.deviceId)
                                findNavController().navigate(action)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUi(state: DeviceControlUiState) {
        val context = requireContext()

        binding.loadingSpinner.isVisible = state is DeviceControlUiState.Loading

        if (state is DeviceControlUiState.Error) {
            binding.contentGroup.isVisible = false
            binding.rvSensors.isVisible = false
            binding.btnPowerToggle.isVisible = false

            Snackbar
                .make(binding.root, state.message.asString(context), Snackbar.LENGTH_INDEFINITE)
                .setAction("Riprova") { viewModel.onEvent(DeviceControlEvent.OnRefresh) }
                .show()
            return
        }

        binding.contentGroup.isVisible = false
        binding.rvSensors.isVisible = false
        binding.layoutMediaControls.root.isVisible = false
        binding.toggleGroupPower.isVisible = false
        binding.btnPowerToggle.isVisible = false

        when (state) {
            is DeviceControlUiState.LightControl -> {
                binding.contentGroup.isVisible = true
                binding.btnPowerToggle.isVisible = true
                renderLightControl(state)
            }

            is DeviceControlUiState.GenericControl -> {
                binding.contentGroup.isVisible = true

                renderGenericControl(state)
            }

            is DeviceControlUiState.SensorControl -> {
                binding.rvSensors.isVisible = true
                renderSensorControl(state)
            }

            is DeviceControlUiState.MediaControl -> {
                binding.layoutMediaControls.root.isVisible = true
                renderMediaControl(state)
            }

            else -> { /* Loading/Error già gestiti */ }
        }
    }

    private fun renderSensorControl(state: DeviceControlUiState.SensorControl) {
        updateHeader(state.name, state.roomName, state.groupName)

        updateCommonControls(
            isOn = true,
            isOnline = state.isOnline,
            powerW = null,
            automationsCount = state.activeAutomations,
        )

        binding.chipGroupStatus.isVisible = true

        binding.chipState.isVisible = false
        binding.chipConsumption.isVisible = false

        binding.rvSensors.isVisible = true
        sensorAdapter.submitList(state.sensors)
    }

    private fun updateHeader(
        name: String,
        room: String?,
        group: String?,
    ) {
        binding.tvTitle.text = name

        val subtitle =
            when {
                !room.isNullOrBlank() && !group.isNullOrBlank() -> "$room • $group"
                !room.isNullOrBlank() -> room
                !group.isNullOrBlank() -> group
                else -> "Dispositivo"
            }

        binding.tvSubtitle.text = subtitle
    }

    private fun updatePillControl(
        isOn: Boolean,
        isOnline: Boolean,
    ) {
        binding.toggleGroupPower.isVisible = true

        val targetId = if (isOn) R.id.btn_pill_on else R.id.btn_pill_off
        if (binding.toggleGroupPower.checkedButtonId != targetId) {
            binding.toggleGroupPower.check(targetId)
        }

        binding.toggleGroupPower.isEnabled = isOnline
        binding.btnPillOn.isEnabled = isOnline
        binding.btnPillOff.isEnabled = isOnline
        binding.toggleGroupPower.alpha = if (isOnline) 1.0f else 0.4f
    }

    private fun renderLightControl(state: DeviceControlUiState.LightControl) {
        val context = requireContext()

        updateHeader(state.name, state.roomName, state.groupName)
        updateCommonControls(
            state.isOn,
            state.isOnline,
            state.currentPowerW,
            state.activeAutomations,
        )

        val showSlider = state.supportsBrightness
        val canControl = state.isOnline

        if (showSlider) {
            binding.sliderBrightness.isVisible = true
            binding.toggleGroupPower.isVisible = false
            binding.btnPowerToggle.isVisible = true

            if (!binding.sliderBrightness.isPressed && !binding.sliderBrightness.isHovered) {
                if (abs(binding.sliderBrightness.value - state.brightness) > 1.0f) {
                    binding.sliderBrightness.value = state.brightness.toFloat()
                }
            }

            val sliderColor =
                if (state.activeMode == LightMode.COLOR_MODE && state.isOn) {
                    ColorStateList.valueOf(state.rgbColor ?: context.getColor(R.color.tw_lime_400))
                } else {
                    ColorStateList.valueOf(context.getColor(R.color.tw_lime_400))
                }
            binding.sliderBrightness.trackActiveTintList = sliderColor

            binding.sliderBrightness.isEnabled = state.isOn && canControl
            binding.sliderBrightness.alpha = if (state.isOn && canControl) 1.0f else 0.4f
        } else {
            binding.sliderBrightness.isVisible = false
            binding.btnPowerToggle.isVisible = false

            updatePillControl(state.isOn, state.isOnline)
        }

        binding.fabPalette.isVisible = state.supportsColor || state.supportsTemp
        binding.flowPresets.isVisible = state.supportsColor

        val colorsEnabled = state.isOn && canControl
        val colorsAlpha = if (colorsEnabled) 1.0f else 0.4f

        binding.fabPalette.isEnabled = colorsEnabled
        binding.fabPalette.alpha = colorsAlpha
        binding.flowPresets.alpha = colorsAlpha

        val presets = listOf(binding.preset1, binding.preset2, binding.preset3, binding.preset4, binding.preset5, binding.preset6)
        presets.forEach {
            it.isVisible = state.supportsColor
            it.isEnabled = colorsEnabled
            it.alpha = colorsAlpha
        }
    }

    private fun renderGenericControl(state: DeviceControlUiState.GenericControl) {
        updateHeader(state.name, state.roomName, state.groupName)
        updateCommonControls(
            state.isOn,
            state.isOnline,
            state.currentPowerW,
            state.activeAutomations,
        )

        binding.sliderBrightness.isVisible = false
        binding.btnPowerToggle.isVisible = false

        updatePillControl(state.isOn, state.isOnline)

        binding.fabPalette.isVisible = false
        binding.flowPresets.isVisible = false
        val presets = listOf(binding.preset1, binding.preset2, binding.preset3, binding.preset4, binding.preset5, binding.preset6)
        presets.forEach { it.isVisible = false }
    }

    private fun renderMediaControl(state: DeviceControlUiState.MediaControl) {
        updateHeader(state.name, state.roomName, state.groupName)
        updateCommonControls(state.isOn, state.isOnline, state.currentPowerW, state.activeAutomations)

        binding.layoutMediaControls.root.isVisible = true
        binding.toggleGroupPower.isVisible = false

        with(binding.layoutMediaControls) {
            val trackInfo =
                if (!state.trackTitle.isNullOrBlank()) {
                    if (!state.trackArtist.isNullOrBlank()) {
                        "${state.trackArtist} - ${state.trackTitle}"
                    } else {
                        state.trackTitle
                    }
                } else {
                    if (state.isOn) "Pronto all'uso" else "Spento"
                }

            tvMediaTrack.text = trackInfo
            tvMediaTrack.isVisible = true

            val iconRes = if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
            btnPlayPause.setIconResource(iconRes)

            if (!sliderVolume.isPressed && !sliderVolume.isHovered) {
                sliderVolume.value = state.volume.toFloat().coerceIn(0f, 100f)
            }

            val controlsEnabled = state.isOn && state.isOnline
            root.alpha = if (controlsEnabled) 1.0f else 0.4f
            btnPlayPause.isEnabled = controlsEnabled
            btnNext.isEnabled = controlsEnabled
            btnPrev.isEnabled = controlsEnabled
            sliderVolume.isEnabled = controlsEnabled
        }
    }

    private fun updateCommonControls(
        isOn: Boolean,
        isOnline: Boolean,
        powerW: Double?,
        automationsCount: Int,
    ) {
        val context = requireContext()

        binding.chipState.text = if (isOn) "ON" else "OFF"
        binding.chipConnection.isVisible = !isOnline

        binding.btnPowerToggle.isEnabled = isOnline

        if (powerW != null && powerW > 0.0) {
            binding.chipConsumption.isVisible = true

            val formattedPower = if (powerW >= 10) "${powerW.toInt()} W" else String.format("%.1f W", powerW)
            binding.chipConsumption.text = formattedPower
        } else {
            binding.chipConsumption.isVisible = false
        }

        if (automationsCount > 0) {
            binding.chipAutomation.isVisible = true
            val label = resources.getQuantityString(R.plurals.automations_count, automationsCount, automationsCount)

            binding.chipAutomation.text = "$automationsCount Automazioni"
        } else {
            binding.chipAutomation.isVisible = false
        }
        if (isOn) {
            binding.btnPowerToggle.setBackgroundColor(context.getColor(R.color.white))
            binding.btnPowerToggle.iconTint = ColorStateList.valueOf(context.getColor(R.color.tw_lime_800))
            binding.btnPowerToggle.alpha = 1.0f
        } else {
            binding.btnPowerToggle.setBackgroundColor(context.getColor(R.color.tw_gray_800))
            binding.btnPowerToggle.iconTint = ColorStateList.valueOf(context.getColor(R.color.tw_gray_400))

            binding.btnPowerToggle.alpha = if (isOnline) 1.0f else 0.4f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
