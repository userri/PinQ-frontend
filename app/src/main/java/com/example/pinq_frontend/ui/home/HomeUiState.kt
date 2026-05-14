package com.example.pinq_frontend.ui.home

/**
 * 홈 화면 UI 상태.
 *
 * - [quizCount] 오늘 준비된 퀴즈 수 (API 로드 후 채워짐)
 * - [streak]    연속 학습 일수 (Phase 2 스트릭 API 연동 전까지 0)
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val quizCount: Int = 0,
    val streak: Int = 0,
)
