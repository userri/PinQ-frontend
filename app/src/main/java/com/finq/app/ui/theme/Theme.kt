package com.finq.app.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

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
    ) {
        // LocalContentColor 를 제공하는 건 Surface 의 역할이라, MaterialTheme 만 감싸면
        // 색을 지정하지 않은 Text 가 Compose 기본값(검정)을 상속해 다크 배경에서 사라진다.
        // (마이페이지 '버전' 값이 실제로 안 보였다.) 여기서 한 번 깔아두면
        // 색 지정을 빠뜨려도 안 보이는 대신 읽히는 쪽으로 무너진다.
        // Button·Surface 등 자체 contentColor 를 주는 컴포넌트는 지역적으로 덮어쓰므로 영향 없음.
        CompositionLocalProvider(
            LocalContentColor provides TextPrimary,
            content = content,
        )
    }
}
