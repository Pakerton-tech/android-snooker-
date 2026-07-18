package com.pakertong.snooker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF4500),
    secondary = Color(0xFFFFD700),
    surface = Color(0xFF1a1a2e),
    surfaceVariant = Color(0xFF16213e),
    background = Color(0xFF0f0f23),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onSurface = Color.White,
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    onBackground = Color.White
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFD4380D),
    secondary = Color(0xFFB8860B),
    surface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFFF0F0F0),
    background = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF49454F),
    onBackground = Color(0xFF1C1B1F)
)

enum class AppTheme { SYSTEM, DARK, LIGHT }

object ThemeManager {
    var currentTheme by mutableStateOf(AppTheme.SYSTEM)
    var matchCount by mutableStateOf(0)
}

@Composable
fun SnookerTheme(content: @Composable () -> Unit) {
    val useDarkTheme = when (ThemeManager.currentTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
