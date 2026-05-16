package com.example.pinq_frontend.data.repository

data class UserStats(
    val streak: Int,
    val totalSolved: Int,
    val correctRate: Float,
    val activityGrid: List<Boolean>,
)

interface UserStatsRepository {
    suspend fun getStats(): UserStats

    /**
     * 회원탈퇴. Phase 2 에서는 demo 닉네임으로 호출.
     */
    suspend fun withdraw(nickname: String)
}
