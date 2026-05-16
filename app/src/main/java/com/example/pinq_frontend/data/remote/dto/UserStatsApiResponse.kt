package com.example.pinq_frontend.data.remote.dto

/**
 * `GET /api/users/me/stats` 응답.
 *
 * activityGrid: 오늘 포함 최근 56일(8주) 활동 여부.
 *   index 0 = 가장 과거, index 55 = 오늘.
 */
data class UserStatsApiResponse(
    val streak: Int,
    val totalSolved: Int,
    val correctRate: Float,
    val activityGrid: List<Boolean>,
)
