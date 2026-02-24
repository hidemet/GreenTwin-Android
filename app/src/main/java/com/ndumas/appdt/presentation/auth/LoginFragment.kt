package com.ndumas.appdt.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.etEmail.addTextChangedListener { text ->
            viewModel.onEvent(LoginEvent.EmailChanged(text.toString()))
        }

        binding.etPassword.addTextChangedListener { text ->
            viewModel.onEvent(LoginEvent.PasswordChanged(text.toString()))
        }

        binding.btnLogin.setOnClickListener {
            viewModel.onEvent(LoginEvent.OnLoginClick)
        }

        binding.btnForgotPassword.setOnClickListener {
            com.ndumas.appdt.core.ui.SnackbarHelper
                .showInfo(binding.root, "WIP")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->

                        binding.loadingOverlay.isVisible = state.isLoading

                        binding.btnLogin.isEnabled = !state.isLoading
                        binding.btnForgotPassword.isEnabled = !state.isLoading
                        binding.tilEmail.isEnabled = !state.isLoading
                        binding.tilPassword.isEnabled = !state.isLoading

                        binding.tilEmail.error = state.emailError?.asString(requireContext())
                        binding.tilPassword.error = state.passwordError?.asString(requireContext())
                    }
                }

                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is LoginUiEvent.LoginSuccess -> {
                                findNavController().navigate(R.id.action_global_main_graph)
                            }

                            is LoginUiEvent.ShowError -> {
                                Snackbar.make(binding.root, event.message.asString(requireContext()), Snackbar.LENGTH_LONG).show()
                            }
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
}
