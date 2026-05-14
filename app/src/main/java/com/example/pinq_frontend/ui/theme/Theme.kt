package com.example.pinq_frontend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PinQBlue,
    onPrimary = White,
    primaryContainer = PinQLightBlue,
    onPrimaryContainer = PinQDarkNavy,
    background = PinQBackground,
    surface = White,
    onSurface = Color(0xFF1A1D2E),
    onSurfaceVariant = Color(0xFF6B7280),
    surfaceVariant = Color(0xFFEDF0F7),
    error = PinQRed,
    errorContainer = Color(0xFFFFECED),
    onErrorContainer = Color(0xFF7F1D1D),
    secondary = PinQDarkNavy,
    onSecondary = White,
    secondaryContainer = PinQLightBlue,
    onSecondaryContainer = PinQDarkNavy,
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),
)

@Composable
fun PinQ_frontendTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
