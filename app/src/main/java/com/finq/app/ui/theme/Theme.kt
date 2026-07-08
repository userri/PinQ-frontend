package com.finq.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// 단일 다크 네이비 테마 — 라이트 모드는 제공하지 않는다(isSystemInDarkTheme 분기 없음).
private val DarkColorScheme = darkColorScheme(
    // 주요 버튼·토글 ON·활성 인디케이터 = 라임, 그 위 텍스트/아이콘 = 네이비
    primary = Lime,
    onPrimary = OnLime,
    primaryContainer = BgSubtle,
    onPrimaryContainer = TextPrimary,

    // 보조 = 본문 톤(다크 배경 위 읽히는 밝은 청회색)
    secondary = TextSecondary,
    onSecondary = OnLime,
    secondaryContainer = BgSubtle,
    onSecondaryContainer = TextPrimary,

    tertiary = Lime,
    onTertiary = OnLime,
    tertiaryContainer = BgSubtle,
    onTertiaryContainer = TextPrimary,

    background = BgBase,
    onBackground = TextPrimary,
    surface = BgSurface,
    onSurface = TextPrimary,
    surfaceVariant = BgElevated,
    onSurfaceVariant = TextSecondary,

    error = Error,
    onError = OnLime,
    errorContainer = ErrorFaint,
    onErrorContainer = Error,

    outline = Outline,
    outlineVariant = Outline,

    scrim = BgBase,
)

@Composable
fun FinQTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
