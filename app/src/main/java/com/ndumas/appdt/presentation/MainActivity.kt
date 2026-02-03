package com.ndumas.appdt.presentation

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeAuthState()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        val topLevelDestinations =
            setOf(
                R.id.homeFragment,
                R.id.deviceFragment,
                R.id.automationFragment,
                R.id.consumptionFragment,
            )

        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (destination.id in topLevelDestinations) {
                binding.bottomNavigation.visibility = View.VISIBLE
            } else {
                binding.bottomNavigation.visibility = View.GONE
            }
        }
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isUserLoggedIn.collect { isLoggedIn ->
                        if (isLoggedIn) {
                            val navHostFragment =
                                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                            val navController = navHostFragment?.navController

                            if (navController?.currentDestination?.id == R.id.loginFragment) {
                                navController.navigate(R.id.action_global_main_graph)
                            }
                        }
                    }
                }

                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is MainUiEvent.NavigateToLogin -> {
                                val navHostFragment =
                                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                                val navController = navHostFragment?.navController
                                navController?.navigate(R.id.action_global_auth_graph)
                            }
                        }
                    }
                }
            }
        }
    }
}
