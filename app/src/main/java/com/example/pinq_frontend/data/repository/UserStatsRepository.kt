package com.example.pinq_frontend.data.repository

data class UserStats(
    val streak: Int,
    val totalSolved: Int,
    val correctRate: Float,
    val activityGrid: List<Boolean>,
)

interface UserStatsRepository {
    suspend fun getStats(): UserStats
}
