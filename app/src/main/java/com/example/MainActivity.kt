package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.SalonTheme
import com.example.ui.viewmodel.SalonViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SalonViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            SalonTheme(appTheme = settings.appTheme) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
