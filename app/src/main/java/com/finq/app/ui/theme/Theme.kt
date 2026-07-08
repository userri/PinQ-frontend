package com.finq.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    // 주요 버튼·포인트 = 라임, 그 위 텍스트 = 네이비 (대비 확보)
    primary = FinQLime,
    onPrimary = FinQNavy,
    primaryContainer = FinQLimeSoft,
    onPrimaryContainer = FinQNavy,

    // 헤더·본문 강조 = 네이비
    secondary = FinQNavy,
    onSecondary = White,
    secondaryContainer = FinQLimeSoft,
    onSecondaryContainer = FinQNavy,

    tertiary = FinQLime,
    onTertiary = FinQNavy,
    tertiaryContainer = FinQLimeSoft,
    onTertiaryContainer = FinQNavy,

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
fun FinQTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
