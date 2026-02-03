package com.ndumas.appdt.presentation.automation.create

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.FragmentTriggerSelectorBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TriggerSelectorFragment : Fragment(R.layout.fragment_trigger_selector) {
    private var _binding: FragmentTriggerSelectorBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTriggerSelectorBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.cardOptionTime.setOnClickListener {
            findNavController().navigate(R.id.action_selector_to_timeTrigger)
        }

        binding.cardOptionSolar.setOnClickListener {
            findNavController().navigate(R.id.action_selector_to_sunTrigger)
        }

        binding.cardOptionDevice.setOnClickListener {
            Toast.makeText(context, "WIP", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
