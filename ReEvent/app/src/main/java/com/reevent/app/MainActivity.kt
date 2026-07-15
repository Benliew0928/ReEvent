package com.reevent.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.reevent.app.core.data.AuthRepository
import com.reevent.app.ui.ReEventApp
import com.reevent.app.ui.theme.ReEventTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleAuthenticationIntent(intent)
        setContent {
            ReEventTheme {
                ReEventApp()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthenticationIntent(intent)
    }

    private fun handleAuthenticationIntent(intent: android.content.Intent) {
        if (intent.data?.scheme == "reevent") {
            lifecycleScope.launch { authRepository.handleOAuthCallback(intent) }
        }
    }
}
