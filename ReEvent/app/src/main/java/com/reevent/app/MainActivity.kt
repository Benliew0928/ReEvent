package com.reevent.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.reevent.app.core.data.AuthRepository
import com.reevent.app.core.data.AppResult
import com.reevent.app.feature.passports.PassportAppLink
import com.reevent.app.feature.passports.PassportQrPayload
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
        handleIncomingIntent(intent)
        setContent {
            ReEventTheme {
                ReEventApp()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: android.content.Intent) {
        if (intent.data?.scheme == "reevent") {
            lifecycleScope.launch {
                when (val result = authRepository.handleOAuthCallback(intent)) {
                    is AppResult.Success -> if (result.value == null) {
                        Toast.makeText(
                            this@MainActivity,
                            "This sign-in or recovery link could not be completed. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is AppResult.Failure -> Toast.makeText(
                        this@MainActivity,
                        "This sign-in or recovery link is invalid or expired. Request a new link and try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            return
        }

        val passportUrl = intent.data?.toString() ?: return
        if (PassportQrPayload.validate(passportUrl, BuildConfig.PUBLIC_BASE_URL) is PassportQrPayload.Validation.Canonical) {
            PassportAppLink.submit(passportUrl)
        }
    }
}
