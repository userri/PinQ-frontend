package com.finq.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 활동/스트릭 그리드의 강도 → 색 변환. 주간(홈)·연간(마이페이지) 그리드가 함께 쓴다.
 *
 * 강도 규약: 0=활동 없음, 1=시도했으나 정답 0개, 2=1개 정답, 3=2개 정답, 4=만점(3개 이상).
 * 파랑은 쓰지 않는다 — 빈칸은 반투명, 1~3은 연한→진한 라임, 만점만 쨍한 라임.
 */
fun streakColor(intensity: Int): Color = when (intensity) {
    0 -> StreakEmpty
    1 -> StreakL1
    2 -> StreakL2
    3 -> StreakL3
    else -> StreakMax   // 4 이상 = 만점
}
