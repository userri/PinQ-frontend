package com.finq.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── 경제잔디 Design System ───────────────────────────────────────────────────
// Primary brand: deep navy + bright lime. (내부 식별자는 FinQ* 유지 — applicationId
// 및 코드 호환을 위해 이름은 그대로 두고 값만 리브랜딩한다.)

val FinQNavy = Color(0xFF0E2540)            // 배경·헤더·본문 텍스트
val FinQNavyDeep = Color(0xFF081A2E)
val FinQNavyMid = Color(0xFF16385C)

// Accent — 쨍한 라임 (포인트·정답·주요 버튼·스트릭). 라임 위 텍스트는 네이비.
val FinQLime = Color(0xFFAEF23C)            // 메인 라임
val FinQLimeDeep = Color(0xFF7FB528)        // 스트릭 농도용 보조 라임
val FinQLimeSoft = Color(0xFFEAF8CE)        // 라임 칩 배경 (연한 톤)

// 하위 호환용 별칭 — 기존 코드가 참조하는 FinQBlue* 이름 유지.
// 예전의 파랑 액센트 자리를 라임(짙은 톤)으로 전면 교체 → 브랜드 색이 확실히 달라 보이게.
val FinQBlue = FinQLimeDeep                 // 선택 / 강조 텍스트·아이콘 (라임 그린, 밝은 배경서 읽힘)
val FinQBlueSoft = FinQLimeSoft             // 정답 칩 배경 (연한 라임)

// Surfaces
val FinQBackground = Color(0xFFF4F7FB)      // 오프화이트 화면 배경
val FinQSurface = Color(0xFFFFFFFF)
val FinQSurfaceMuted = Color(0xFFF1F5FA)
val FinQDivider = Color(0xFFE1E7EF)
val FinQOutline = Color(0xFFD2DAE5)

// Text
val FinQTextStrong = Color(0xFF0E2540)
val FinQTextBody = Color(0xFF1B2A3F)
val FinQTextMuted = Color(0xFF61708A)
val FinQTextSubtle = Color(0xFF93A3B8)

// Semantic — 강조/팁은 라임으로 통일 (기존 Yellow* 이름 유지, 값만 라임).
val FinQYellow = FinQLime                   // 팁 / 포인트 강조
val FinQYellowSoft = FinQLimeSoft           // "알아두면 좋아요" 배경
val FinQYellowStroke = FinQLimeDeep
val FinQGreen = Color(0xFF22C55E)
val FinQRed = Color(0xFFEF4444)
val FinQRedSoft = Color(0xFFFFE4E6)

val White = Color(0xFFFFFFFF)
