package com.example.pinq_frontend.data.repository

data class UserStats(
    val nickname: String,
    val streak: Int,
    val maxStreak: Int,
    val totalSolved: Int,
    val correctRate: Float,
    val activityGrid: List<Int>,
)

interface UserStatsRepository {
    suspend fun getStats(): UserStats

    /**
     * 닉네임 수정. 성공 시 변경된 닉네임 반환.
     */
    suspend fun updateNickname(nickname: String): String

    /**
     * 회원탈퇴. Phase 2 에서는 demo 닉네임으로 호출.
     */
    suspend fun withdraw(nickname: String)
}
