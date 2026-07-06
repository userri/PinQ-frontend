package com.finq.app.data.repository

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
     * 회원탈퇴 — JWT 인증 기반, 본인 계정 삭제.
     */
    suspend fun withdraw()
}
