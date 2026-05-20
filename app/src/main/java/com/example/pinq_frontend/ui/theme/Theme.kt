package com.example.pinq_frontend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = FinQNavy,
    onPrimary = White,
    primaryContainer = FinQBlueSoft,
    onPrimaryContainer = FinQNavy,

    secondary = FinQBlue,
    onSecondary = White,
    secondaryContainer = FinQBlueSoft,
    onSecondaryContainer = FinQNavy,

    tertiary = FinQYellow,
    onTertiary = FinQTextStrong,
    tertiaryContainer = FinQYellowSoft,
    onTertiaryContainer = FinQTextStrong,

    background = FinQBackground,
    onBackground = FinQTextStrong,
    surface = FinQSurface,
    onSurface = FinQTextStrong,
    surfaceVariant = FinQSurfaceMuted,
    onSurfaceVariant = FinQTextMuted,

    error = FinQRed,
    onError = White,
    errorContainer = FinQRedSoft,
    onErrorContainer = FinQRed,

    outline = FinQOutline,
    outlineVariant = FinQDivider,
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
