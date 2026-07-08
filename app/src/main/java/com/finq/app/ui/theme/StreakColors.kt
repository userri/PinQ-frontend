package com.finq.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 잔디 그리드의 강도 → 색 변환. 주간(홈)·연간(마이페이지) 그리드가 함께 쓴다.
 *
 * 강도 규약: 0=활동 없음, 1=시도했으나 정답 0개, 2=1개 정답, 3=2개 정답, 4=만점(3개 이상).
 * 이 5단계 외의 초록·파랑은 그리드에서 일절 쓰지 않는다.
 */
fun streakColor(intensity: Int): Color = when (intensity) {
    0 -> GrassEmpty
    1 -> Grass1
    2 -> Grass2
    3 -> Grass3
    else -> GrassMax   // 4 이상 = 만점
}
