package com.ndumas.greentwin.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ndumas.greentwin.R
import dagger.hilt.android.AndroidEntryPoint

// Questa Activity ospiterà i nostri Fragment
// che a loro volta useranno Hilt per l'iniezione dei ViewModel.
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // logica di navigazione da implementare
    }
}