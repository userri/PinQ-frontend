package com.finq.app.ui.home

import java.time.LocalDate

/**
 * 홈 화면 UI 상태.
 *
 * - [quizCount]    오늘 준비된 퀴즈 수
 * - [streak]       연속 학습 일수 (백엔드 currentStreak — 오늘 풀면 오늘 포함)
 * - [activityGrid] 최근 56일 처음 시도 정답 수 강도 (index 0 = 55일 전, index 55 = 오늘)
 *                  0=활동 없음, 1~4=정답 개수(4 이상은 4로 고정)
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val nickname: String = "",
    val quizCount: Int = 0,
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val activityGrid: List<Int> = emptyList(),
    /** 오늘 복습할 오답 수 ("오늘 물 줄 잔디 N개"). */
    val reviewCount: Int = 0,
    /** reviewCount == 0 일 때 안내할 다음 물 주기 날짜. */
    val nextReviewDate: LocalDate? = null,
)
