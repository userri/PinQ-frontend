package com.finq.app.data.repository

import com.finq.app.data.remote.UserApi
import com.finq.app.data.remote.dto.UpdateNicknameRequest

class ApiUserStatsRepository(private val api: UserApi) : UserStatsRepository {

    override suspend fun getStats(): UserStats {
        val dto = api.getUserStats()
        return UserStats(
            nickname = dto.nickname,
            streak = dto.streak,
            maxStreak = dto.maxStreak,
            totalSolved = dto.totalSolved,
            correctRate = dto.correctRate,
            activityGrid = dto.activityGrid,
        )
    }

    override suspend fun updateNickname(nickname: String): String {
        val response = api.updateNickname(UpdateNicknameRequest(nickname))
        return response.nickname
    }

    override suspend fun withdraw(nickname: String) {
        api.withdraw(nickname)
    }
}
