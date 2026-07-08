package com.finq.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// 경제잔디 Design Tokens — 단일 다크 네이비 테마 (라이트 모드 없음)
//
// ⚠️ 이 파일이 앱에서 유일하게 raw 색 값(Color(0x…))을 갖는 곳이다.
//    화면 코드에서는 반드시 아래 "역할 토큰"만 참조할 것.
//    XML 드로어블은 res/values/colors.xml 의 같은 역할 토큰을 참조한다.
//
// 원칙:
//   · 순백(#FFFFFF) 텍스트 금지 → TextPrimary(#F4F7FB) 사용.
//   · Lime 은 넓은 면적 배경으로 쓰지 않는다 → 버튼·잔디 칸·뱃지·활성 인디케이터만.
//   · Lime 위 텍스트/아이콘은 언제나 OnLime(네이비).
// ─────────────────────────────────────────────────────────────────────────────

// ── 배경 계층 (어두운 순) ─────────────────────────────────────────────────────
/** 화면 최하단 배경 · 시스템 바. */
val BgBase = Color(0xFF081A2E)
/** 기본 카드. */
val BgSurface = Color(0xFF0E2540)
/** 떠 있는 카드 · 바텀시트 · 다이얼로그 · 잔디 빈칸. */
val BgElevated = Color(0xFF16385C)
/** pressed/hover · 선택된 항목 배경. */
val BgSubtle = Color(0xFF1E4470)

// ── 텍스트 (순백 금지) ────────────────────────────────────────────────────────
/** 제목 · 본문. */
val TextPrimary = Color(0xFFF4F7FB)
/** 보조 설명. */
val TextSecondary = Color(0xFFB8C7DA)
/** 비활성 · 힌트 · 타임스탬프 · 비활성 탭. */
val TextMuted = Color(0xFF7E93AC)

// ── 브랜드 ────────────────────────────────────────────────────────────────────
/** 주요 버튼 배경 · 활성 토글 · 만점 잔디 · 활성 탭 아이콘. */
val Lime = Color(0xFFAEF23C)
/** 진한 잔디 단계. */
val LimeDeep = Color(0xFF7FB528)
/** 중간 잔디 단계. */
val LimeMid = Color(0xFF5A8A1E)
/** 연한 잔디 단계 (다크 배경용 저채도) · 정답 공개 배경. */
val LimeFaint = Color(0xFF35551A)
/** Lime 배경 위 텍스트/아이콘 — 항상 네이비. */
val OnLime = Color(0xFF0E2540)

// ── 기타 ──────────────────────────────────────────────────────────────────────
/** 테두리 · 디바이더. */
val Outline = Color(0xFF2A4A6E)
/** 오답 · 삭제 (다크 배경용으로 밝게). */
val Error = Color(0xFFFF6B6B)
/** 오답 배경. */
val ErrorFaint = Color(0xFF4A2530)
/** 성공 = Lime 재사용 (별도 초록을 만들지 않는다). */
val Success = Lime

// ── 외부 파트너 브랜드색 (규정색 — 임의 변경 금지) ────────────────────────────
val KakaoYellow = Color(0xFFFEE500)
val KakaoLabel = Color(0xFF191919)
val GoogleWhite = Color(0xFFFFFFFF)
val GoogleBorder = Color(0xFFDADADA)
val GoogleLabel = Color(0xFF3C4043)
