package com.finq.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// 경제잔디 Design Tokens — 단일 다크 네이비 테마 (라이트 모드 없음)
//
// ⚠️ 이 파일이 앱에서 유일하게 raw 색 값(Color(0x…))을 갖는 곳이다.
//    화면 코드에서는 반드시 아래 "역할 토큰"만 참조할 것.
//    XML 드로어블은 res/values/colors.xml 의 같은 역할 토큰을 참조한다.
//
// ── 색 사용 규칙 (통일성 원칙) ────────────────────────────────────────────────
// 앱 전체에서 유채색은 딱 3그룹만 존재한다:
//   ① Lime      — 포인트 (로고 "잔디", 활성 탭, 토글 ON, 주요 버튼, 뱃지)
//   ② Grass 램프 — 잔디 그리드 전용 (주간·연간 공통, 아래 5단계만)
//   ③ Error     — 오답 전용
// 그 외 모든 것은 네이비 중립 단계(BgBase/BgSurface/BgElevated/BgSubtle/Outline)
// 와 텍스트 3종(TextPrimary/TextSecondary/TextMuted)으로 표현한다.
// 초록이 필요한 모든 곳은 Grass 램프 또는 Lime 중에서만 고른다. 다른 초록·파랑 금지.
//
// 그 외 원칙:
//   · 순백(#FFFFFF) 텍스트 금지 → TextPrimary(#F4F7FB) 사용.
//   · Lime 은 넓은 면적 배경으로 쓰지 않는다 → 버튼·잔디 칸·뱃지·활성 인디케이터만.
//   · Lime 위 텍스트/아이콘은 언제나 OnLime(네이비).
// ─────────────────────────────────────────────────────────────────────────────

// ── 배경 계층 (어두운 순) ─────────────────────────────────────────────────────
/** 화면 최하단 배경 · 시스템 바. */
val BgBase = Color(0xFF081A2E)
/** 기본 카드. */
val BgSurface = Color(0xFF0E2540)
/** 떠 있는 카드 · 바텀시트 · 다이얼로그. */
val BgElevated = Color(0xFF16385C)
/** pressed/hover · 선택된 항목 배경 · 중립 칩/인디케이터. */
val BgSubtle = Color(0xFF1E4470)

// ── 텍스트 (순백 금지) ────────────────────────────────────────────────────────
/** 제목 · 본문. */
val TextPrimary = Color(0xFFF4F7FB)
/** 보조 설명. */
val TextSecondary = Color(0xFFB8C7DA)
/** 비활성 · 힌트 · 타임스탬프 · 비활성 탭. */
val TextMuted = Color(0xFF7E93AC)

// ── ① 브랜드 포인트 ───────────────────────────────────────────────────────────
/** 로고 "잔디" · 활성 탭 · 토글 ON · 주요 버튼 · 뱃지. 앱의 유일한 포인트색. */
val Lime = Color(0xFFAEF23C)
/** Lime 배경 위 텍스트/아이콘 — 항상 네이비. */
val OnLime = Color(0xFF0E2540)

// ── ② 잔디 램프 (그리드 전용 — 주간·연간 공통) ────────────────────────────────
// 낮은 단계에 라임을 섞으면 올리브색으로 죽어서, 짙은 초록 → 밝은 초록으로 올린다.
/** 활동 없음 (BgElevated 와 동일값 재사용). */
val GrassEmpty = Color(0xFF16385C)
/** 1단계 — 짙은 초록 (0~1개 정답). */
val Grass1 = Color(0xFF124A2E)
/** 2단계 — 초록 (2개 정답). */
val Grass2 = Color(0xFF1E7A42)
/** 3단계 — 밝은 초록 (3개 정답). */
val Grass3 = Color(0xFF3ECF63)
/** 4단계(최고) — 브랜드 라임, 4개 이상 정답 (Lime 재사용). */
val GrassMax = Lime

// ── ③ 오답 ────────────────────────────────────────────────────────────────────
/** 오답 · 삭제 (다크 배경용으로 밝게). */
val Error = Color(0xFFFF6B6B)
/** 오답 배경. */
val ErrorFaint = Color(0xFF4A2530)

// ── 중립 기타 ─────────────────────────────────────────────────────────────────
/** 테두리 · 디바이더. */
val Outline = Color(0xFF2A4A6E)
/** 성공 = Lime 재사용 (별도 초록을 만들지 않는다). */
val Success = Lime

// ── 외부 파트너 브랜드색 (규정색 — 임의 변경 금지) ────────────────────────────
val KakaoYellow = Color(0xFFFEE500)
val KakaoLabel = Color(0xFF191919)
val GoogleWhite = Color(0xFFFFFFFF)
val GoogleBorder = Color(0xFFDADADA)
val GoogleLabel = Color(0xFF3C4043)
