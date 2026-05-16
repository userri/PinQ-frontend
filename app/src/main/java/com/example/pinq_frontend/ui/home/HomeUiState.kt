package com.example.pinq_frontend.ui.home

/**
 * 홈 화면 UI 상태.
 *
 * - [quizCount]    오늘 준비된 퀴즈 수
 * - [streak]       연속 학습 일수 (백엔드 currentStreak — 오늘 풀면 오늘 포함)
 * - [activityGrid] 최근 56일 활동 여부 (index 0 = 55일 전, index 55 = 오늘)
 *                  홈에서는 현재 요일 기준으로 이번 주 위치에 매핑해 표시하며,
 *                  아직 오지 않은 미래 요일 칸은 비어 있을 수 있음
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val quizCount: Int = 0,
    val streak: Int = 0,
    val activityGrid: List<Boolean> = emptyList(),
)
