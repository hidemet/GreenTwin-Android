package com.ndumas.appdt.presentation.automation.edit

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.FragmentDeviceActionBinding
import com.ndumas.appdt.domain.automation.model.AutomationAction
import com.ndumas.appdt.domain.device.model.DeviceDetail
import com.ndumas.appdt.domain.device.model.DeviceType
import com.ndumas.appdt.presentation.automation.create.adapter.ColorGridAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditDeviceActionFragment : Fragment(R.layout.fragment_device_action) {
    private val viewModel: AutomationEditViewModel by hiltNavGraphViewModels(R.id.automation_edit_graph)
    private val args: EditDeviceActionFragmentArgs by navArgs()

    private lateinit var colorAdapter: ColorGridAdapter

    private var _binding: FragmentDeviceActionBinding? = null
    private val binding get() = _binding!!

    private var selectedColor: Int? = null

    private val initialDeviceType: DeviceType by lazy {
        try {
            DeviceType.valueOf(args.deviceType)
        } catch (e: Exception) {
            DeviceType.OTHER
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDeviceActionBinding.bind(view)

        setupToolbar()
        setupInteractions()
        setupColorGrid()

        parentFragmentManager.setFragmentResultListener("palette_result", viewLifecycleOwner) { _, bundle ->
            if (bundle.containsKey("optimistic_color")) {
                val color = bundle.getInt("optimistic_color")
                this.selectedColor = color
                colorAdapter.setSelectedColor(color)
            }
        }

        viewModel.onEvent(AutomationEditUiEvent.SelectDeviceForAction(args.deviceId))

        observeState()
    }

    private fun setupColorGrid() {
        colorAdapter =
            ColorGridAdapter(
                onColorSelected = { color ->
                    this.selectedColor = color
                },
                onCustomPickerClick = {
                    val action =
                        EditDeviceActionFragmentDirections.actionDeviceActionFragmentToPaletteBottomSheet(
                            deviceId = args.deviceId,
                            startTab = 1,
                        )
                    findNavController().navigate(action)
                },
            )

        binding.rvColors.adapter = colorAdapter
    }

    private fun setupToolbar() {
        with(binding.includeToolbar.toolbar) {
            setNavigationOnClickListener { findNavController().popBackStack() }
            title = "Configura Dispositivo"
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val isLoading = state.isDeviceLoading

                    binding.progressIndicator.isVisible = isLoading
                    binding.layoutContentContainer.isVisible = !isLoading

                    state.configuringDevice?.let { device ->
                        if (device.id == args.deviceId) {
                            setupVisibility(device)

                            if (!isLoading) {
                                binding.includeToolbar.toolbar.title = "Configura ${device.name}"
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupVisibility(device: DeviceDetail) {
        binding.cardBrightness.isVisible = false
        binding.cardColor.isVisible = false
        binding.cardTemperature.isVisible = false

        when (device) {
            is DeviceDetail.Light -> {
                binding.cardBrightness.isVisible = device.supportsBrightness
                binding.cardColor.isVisible = device.supportsColor
                binding.cardTemperature.isVisible = device.supportsTemp
            }

            is DeviceDetail.MediaPlayer -> {
                // Futuro: binding.cardVolume.isVisible = true
            }

            else -> {}
        }
    }

    private fun setupInteractions() {
        binding.toggleGroupState.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && checkedId == R.id.btn_off) {
                binding.switchBrightness.isChecked = false
                binding.switchColor.isChecked = false
                binding.switchTemperature.isChecked = false
            }
            updateSaveButton()
        }

        // Imposta visibilità iniziale (switch parte da OFF)
        binding.sliderBrightness.isVisible = binding.switchBrightness.isChecked
        binding.switchBrightness.setOnCheckedChangeListener { _, isChecked ->
            binding.sliderBrightness.isVisible = isChecked
            updateSaveButton()
        }

        binding.switchColor.setOnCheckedChangeListener { _, isChecked ->
            binding.containerColor.isVisible = isChecked
            if (isChecked) {
                binding.switchTemperature.isChecked = false
            }
            updateSaveButton()
        }

        binding.switchTemperature.setOnCheckedChangeListener { _, isChecked ->
            binding.containerTemperature.isVisible = isChecked
            if (isChecked) {
                binding.switchColor.isChecked = false
            }
            updateSaveButton()
        }

        binding.btnSave.setOnClickListener { saveAction() }
    }

    private fun updateSaveButton() {
        binding.btnSave.isEnabled = true
    }

    private fun saveAction() {
        val params = mutableMapOf<String, Any>()

        val isTurnOn = binding.toggleGroupState.checkedButtonId == R.id.btn_on
        val command = if (isTurnOn) "turn_on" else "turn_off"

        if (command == "turn_on") {
            if (binding.switchBrightness.isChecked && binding.cardBrightness.isVisible) {
                params["brightness_pct"] = binding.sliderBrightness.value.toInt()
            }

            if (binding.switchColor.isChecked && binding.cardColor.isVisible && selectedColor != null) {
                val r = Color.red(selectedColor!!)
                val g = Color.green(selectedColor!!)
                val b = Color.blue(selectedColor!!)
                params["rgb_color"] = listOf(r, g, b)
            }

            if (binding.switchTemperature.isChecked && binding.cardTemperature.isVisible) {
                val pct = binding.sliderTemperature.value
                val kelvin = 2000 + (pct / 100 * (6500 - 2000)).toInt()
                params["kelvin"] = kelvin
            }
        }

        val action =
            AutomationAction.DeviceAction(
                deviceId = args.deviceId,
                deviceName =
                    binding.includeToolbar.toolbar.title
                        .toString()
                        .removePrefix("Configura "),
                domain = getDomainFromType(initialDeviceType),
                service = command,
                parameters = params,
            )

        viewModel.onEvent(AutomationEditUiEvent.AddAction(action))

        findNavController().popBackStack(R.id.automationEditHubFragment, false)
    }

    private fun getDomainFromType(type: DeviceType): String =
        when (type) {
            DeviceType.LIGHT -> "light"
            DeviceType.SWITCH -> "switch"
            DeviceType.TV, DeviceType.MEDIA_PLAYER -> "media_player"
            DeviceType.THERMOSTAT, DeviceType.AIR_CONDITIONER -> "climate"
            DeviceType.FAN -> "fan"
            else -> "switch"
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
