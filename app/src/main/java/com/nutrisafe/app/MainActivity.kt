package com.nutrisafe.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutrisafe.app.ui.navigation.NutriSafeApp
import com.nutrisafe.app.ui.screens.AuthScreen
import com.nutrisafe.app.ui.theme.NutriSafeTheme
import com.nutrisafe.app.viewmodel.NutriSafeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutriSafeTheme {
                val viewModel: NutriSafeViewModel = viewModel(factory = NutriSafeViewModel.factory(application))
                val session by viewModel.session.collectAsStateWithLifecycle()
                if (session.isLoggedIn) {
                    NutriSafeApp(viewModel)
                } else {
                    AuthScreen(viewModel)
                }
            }
        }
    }
}
