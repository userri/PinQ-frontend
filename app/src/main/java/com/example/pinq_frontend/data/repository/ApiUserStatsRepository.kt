package com.example.pinq_frontend.data.repository

import com.example.pinq_frontend.data.remote.UserApi

class ApiUserStatsRepository(private val api: UserApi) : UserStatsRepository {

    override suspend fun getStats(): UserStats {
        val dto = api.getUserStats()
        return UserStats(
            streak = dto.streak,
            totalSolved = dto.totalSolved,
            correctRate = dto.correctRate,
            activityGrid = dto.activityGrid,
        )
    }

    override suspend fun withdraw(nickname: String) {
        api.withdraw(nickname)
    }
}
