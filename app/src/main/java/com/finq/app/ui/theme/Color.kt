package com.finq.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// 경제잔디 Design Tokens
//
// ⚠️ 이 파일이 앱에서 유일하게 raw 색 값(Color(0x…))을 갖는 곳이다.
//    화면 코드에서는 반드시 아래 "역할 토큰"만 참조할 것.
//    XML 드로어블은 res/values/colors.xml 의 같은 역할 토큰을 참조한다.
//
// 대비 원칙:
//   · 쨍한 라임(BrandLime)은 흰 배경 위 작은 글씨로 쓰면 대비가 부족하다(1.4:1).
//     → 큰 면적·채움·토글·인디케이터에만 사용(AccentFill), 그 위 글씨는 네이비(OnAccent).
//   · 밝은 배경 위 작은 강조 텍스트/아이콘은 진한 라임(AccentText) 또는 네이비.
// ─────────────────────────────────────────────────────────────────────────────

// ── 브랜드 원색 ───────────────────────────────────────────────────────────────
val BrandNavy = Color(0xFF0E2540)      // 베이스: 다크 카드·헤더·본문
val BrandNavyMid = Color(0xFF16385C)
val BrandNavyDeep = Color(0xFF081A2E)
val BrandLime = Color(0xFFAEF23C)      // 브랜드 강조
val BrandLimeDeep = Color(0xFF7FB528)
val BrandLimeSoft = Color(0xFFEAF8CE)

// ── 강조(Accent) 역할 ─────────────────────────────────────────────────────────
/** 큰 면적·채움: 주요 버튼, 활성 탭 인디케이터, 알림 토글 ON, 스트릭 채운 칸, 아바타. */
val AccentFill = BrandLime
/** AccentFill 위에 얹는 텍스트·아이콘. */
val OnAccent = BrandNavy
/** 밝은 배경 위 작은 강조 텍스트·아이콘 (오답노트 강조 글씨 등). */
val AccentText = BrandLimeDeep
/** 연한 강조 칩/행 배경. */
val AccentSoft = BrandLimeSoft

// ── 퀴즈 의미색 ───────────────────────────────────────────────────────────────
/** 정답 채움 및 정답 강조 텍스트. 흰 글씨 대비 4.6:1 확보를 위해 라임보다 진하다. */
val CorrectFill = Color(0xFF3E7A15)
val CorrectSoft = BrandLimeSoft
val IncorrectFill = Color(0xFFEF4444)
val IncorrectSoft = Color(0xFFFFE4E6)
val SuccessGreen = Color(0xFF22C55E)

// ── 표면(Surface) ─────────────────────────────────────────────────────────────
val ScreenBackground = Color(0xFFF4F7FB)  // 오프화이트 밝은 배경
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceMuted = Color(0xFFF1F5FA)
val SurfaceSubtle = Color(0xFFEDF0F7)     // 히트맵 빈 칸 / 범례 스와치
val OutlineStrong = Color(0xFFE5E7EB)     // 비활성 원형 배경
val DividerColor = Color(0xFFE1E7EF)
val OutlineColor = Color(0xFFD2DAE5)

// ── 텍스트 ────────────────────────────────────────────────────────────────────
val TextStrong = BrandNavy
val TextBody = Color(0xFF1B2A3F)
val TextMuted = Color(0xFF61708A)
val TextSubtle = Color(0xFF93A3B8)
/** 네이비 등 다크 표면 위 텍스트. */
val OnDark = Color(0xFFFFFFFF)

// ── 활동/스트릭 그리드 램프 (주간·연간 공용) ─────────────────────────────────
// 파랑은 그리드에서 쓰지 않는다. 빈칸=반투명, 1~3단계=연한→진한 라임,
// 최고 단계(만점)만 쨍한 라임. 강도 → 색 변환은 streakColor(intensity) 를 쓸 것.
/** 미참여 칸 — 반투명(카드 배경이 비쳐 보인다). */
val StreakEmpty = Color(0x0F0E2540)
val StreakL1 = Color(0xFFDDF2B4)   // 1단계 — 연한 라임
val StreakL2 = Color(0xFFBBE377)   // 2단계 — 중간 라임
val StreakL3 = BrandLimeDeep       // 3단계 — 진한 라임 (#7FB528)
/** 최고 단계(만점) — 쨍한 라임. */
val StreakMax = BrandLime

// ── 외부 파트너 브랜드색 (규정색 — 임의 변경 금지) ────────────────────────────
val KakaoYellow = Color(0xFFFEE500)
val KakaoLabel = Color(0xFF191919)
val GoogleWhite = Color(0xFFFFFFFF)
val GoogleBorder = Color(0xFFDADADA)
val GoogleLabel = Color(0xFF3C4043)
