package app.ghostfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.ghostfit.ui.onboarding.WelcomeScreen
import app.ghostfit.ui.theme.GhostFitTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GhostFitTheme {
                // TODO(Phase-1): Replace with NavHost when onboarding navigation is wired
                WelcomeScreen(
                    onGetStarted = {
                        // TODO(Phase-1): Navigate to PermissionScreen
                    }
                )
            }
        }
    }
}
