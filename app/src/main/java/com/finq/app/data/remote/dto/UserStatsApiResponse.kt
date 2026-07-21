package com.finq.app.data.remote.dto

/**
 * `GET /api/users/me/stats` 응답.
 *
 * activityGrid: 오늘 포함 최근 56일(8주) 처음 시도 정답 수.
 *   index 0 = 가장 과거, index 55 = 오늘.
 *   값 = 해당 날짜에 처음 시도에서 맞힌 문제 수 (0 = 활동 없음).
 *   강도 단계: 0(없음) / 1(1개) / 2(2개) / 3(3개) / 4(4개 이상)
 */
data class UserStatsApiResponse(
    val nickname: String,
    val streak: Int,
    /**
     * 오늘 데일리 퀴즈를 1문제 이상 풀었는지 (복습 재풀이는 카운트 안 됨).
     * streak 은 "오늘 또는 어제까지" 이어진 값이라(하루 유예) 이 플래그 없이는
     * 오늘 풀었는지 알 수 없다. 구버전 서버 응답 대비 기본 false.
     */
    val solvedToday: Boolean = false,
    val maxStreak: Int,
    val totalSolved: Int,
    val correctRate: Float,
    val activityGrid: List<Int>,
)
