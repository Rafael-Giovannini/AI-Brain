package app.ghostfit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val GhostPurple = Color(0xFF7C3AED)
private val GhostPurpleLight = Color(0xFFA78BFA)
private val GhostPurpleDark = Color(0xFF4C1D95)

private val LightColorScheme = lightColorScheme(
    primary = GhostPurple,
    onPrimary = Color.White,
    primaryContainer = GhostPurpleLight,
    secondary = Color(0xFF625B71),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
)

private val DarkColorScheme = darkColorScheme(
    primary = GhostPurpleLight,
    onPrimary = GhostPurpleDark,
    primaryContainer = GhostPurple,
    secondary = Color(0xFFCCC2DC),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
)

@Composable
fun GhostFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
