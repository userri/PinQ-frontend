package com.example.pinq_frontend.data.remote

import com.example.pinq_frontend.data.remote.dto.UserStatsApiResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {

    @GET("api/users/me/stats")
    suspend fun getUserStats(): UserStatsApiResponse

    /**
     * 회원탈퇴 — Phase 2 에서는 인증이 없으므로 nickname 으로 식별한다.
     * 성공 시 204 No Content.
     */
    @DELETE("api/users/me")
    suspend fun withdraw(@Query("nickname") nickname: String)
}
