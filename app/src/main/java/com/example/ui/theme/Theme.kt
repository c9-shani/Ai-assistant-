package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF001E24),
    primaryContainer = Color(0xFF004E5C),
    onPrimaryContainer = Color(0xFF70F5FF),
    secondary = NeonPurple,
    onSecondary = Color(0xFF2D0040),
    secondaryContainer = Color(0xFF5D0082),
    onSecondaryContainer = Color(0xFFEDACFF),
    tertiary = NeonGreen,
    onTertiary = Color(0xFF00220A),
    tertiaryContainer = Color(0xFF004D18),
    onTertiaryContainer = Color(0xFF85FF9E),
    background = CyberBlack,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberCardBorder,
    error = NeonRed,
    onError = Color(0xFF38000A)
)

private val LightColorScheme = DarkColorScheme // Agent command center is strictly cyberpunk dark-first

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
