package com.priveat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.priveat.app.ui.navigation.PrivEatApp
import com.priveat.app.ui.screens.AuthScreen
import com.priveat.app.ui.theme.PrivEatTheme
import com.priveat.app.viewmodel.PrivEatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrivEatTheme {
                val viewModel: PrivEatViewModel = viewModel(factory = PrivEatViewModel.factory(application))
                val session by viewModel.session.collectAsStateWithLifecycle()
                if (session.isLoggedIn) {
                    PrivEatApp(viewModel)
                } else {
                    AuthScreen(viewModel)
                }
            }
        }
    }
}
