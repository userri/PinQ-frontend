package com.finq.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    // 주요 버튼·토글 ON·활성 인디케이터 = 쨍한 라임, 그 위 텍스트 = 네이비
    primary = AccentFill,
    onPrimary = OnAccent,
    primaryContainer = AccentSoft,
    onPrimaryContainer = BrandNavy,

    // 헤더·본문 강조 = 네이비
    secondary = BrandNavy,
    onSecondary = OnDark,
    secondaryContainer = AccentSoft,
    onSecondaryContainer = BrandNavy,

    tertiary = AccentFill,
    onTertiary = OnAccent,
    tertiaryContainer = AccentSoft,
    onTertiaryContainer = BrandNavy,

    background = ScreenBackground,
    onBackground = TextStrong,
    surface = SurfaceWhite,
    onSurface = TextStrong,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextMuted,

    error = IncorrectFill,
    onError = OnDark,
    errorContainer = IncorrectSoft,
    onErrorContainer = IncorrectFill,

    outline = OutlineColor,
    outlineVariant = DividerColor,
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
