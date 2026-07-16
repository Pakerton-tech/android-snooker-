package com.pakertong.snooker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF4500),
    secondary = Color(0xFFFFD700),
    surface = Color(0xFF1a1a2e),
    background = Color(0xFF0f0f23),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onSurface = Color.White,
    onBackground = Color.White
)

@Composable
fun SnookerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
