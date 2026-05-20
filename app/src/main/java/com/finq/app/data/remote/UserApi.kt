package com.finq.app.data.remote

import com.finq.app.data.remote.dto.RegisterApiResponse
import com.finq.app.data.remote.dto.UpdateNicknameRequest
import com.finq.app.data.remote.dto.UserStatsApiResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Query

interface UserApi {

    @GET("api/users/me/stats")
    suspend fun getUserStats(): UserStatsApiResponse

    /**
     * 닉네임 수정. 성공 시 200 OK + { userId, nickname }.
     */
    @PATCH("api/users/me/nickname")
    suspend fun updateNickname(@Body request: UpdateNicknameRequest): RegisterApiResponse

    /**
     * 회원탈퇴 — Phase 2 에서는 인증이 없으므로 nickname 으로 식별한다.
     * 성공 시 204 No Content.
     */
    @DELETE("api/users/me")
    suspend fun withdraw(@Query("nickname") nickname: String)
}
