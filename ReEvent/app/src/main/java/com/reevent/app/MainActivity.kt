package com.reevent.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.reevent.app.core.data.AuthRepository
import com.reevent.app.core.data.AppResult
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
            lifecycleScope.launch {
                when (val result = authRepository.handleOAuthCallback(intent)) {
                    is AppResult.Success -> if (result.value == null) {
                        Toast.makeText(
                            this@MainActivity,
                            "Google sign-in could not be completed. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is AppResult.Failure -> Toast.makeText(
                        this@MainActivity,
                        "Google sign-in could not be completed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
