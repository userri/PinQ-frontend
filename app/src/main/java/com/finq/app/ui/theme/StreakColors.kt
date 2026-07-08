package com.finq.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 잔디 그리드의 강도 → 색 변환. 주간(홈)·연간(마이페이지) 그리드가 함께 쓴다.
 *
 * 강도 규약: 0=활동 없음, 1=시도했으나 정답 0개, 2=1개 정답, 3=2개 정답, 4=만점(3개 이상).
 * 파란 계열은 쓰지 않는다 — 빈칸 BgElevated → LimeFaint → LimeMid → LimeDeep → 만점 Lime.
 */
fun streakColor(intensity: Int): Color = when (intensity) {
    0 -> BgElevated
    1 -> LimeFaint
    2 -> LimeMid
    3 -> LimeDeep
    else -> Lime   // 4 이상 = 만점
}
