package com.finq.app.data.remote

import com.finq.app.data.remote.dto.ConceptStatsApiResponse
import com.finq.app.data.remote.dto.DeviceTokenRequest
import com.finq.app.data.remote.dto.GrassApiResponse
import com.finq.app.data.remote.dto.NotificationSettingsResponse
import com.finq.app.data.remote.dto.RegisterApiResponse
import com.finq.app.data.remote.dto.UpdateNicknameRequest
import com.finq.app.data.remote.dto.UpdateNotificationSettingsRequest
import com.finq.app.data.remote.dto.UserStatsApiResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface UserApi {

    @GET("api/users/me/stats")
    suspend fun getUserStats(): UserStatsApiResponse

    /** 연간 잔디밭. days 는 활동일만 오는 sparse 배열. */
    @GET("api/users/me/grass")
    suspend fun getGrass(): GrassApiResponse

    /** 카테고리별 정답률 + 취약 개념. 표본이 부족하면 weakest 가 null. */
    @GET("api/users/me/concept-stats")
    suspend fun getConceptStats(): ConceptStatsApiResponse

    /**
     * 닉네임 수정. 성공 시 200 OK + { userId, nickname }.
     */
    @PATCH("api/users/me/nickname")
    suspend fun updateNickname(@Body request: UpdateNicknameRequest): RegisterApiResponse

    /**
     * 회원탈퇴 — JWT 의 userId 로 본인 계정을 삭제한다.
     * 성공 시 204 No Content.
     */
    @DELETE("api/users/me")
    suspend fun withdraw()

    // ── FCM 푸시알림 ─────────────────────────────────────────────────────────

    /** FCM 디바이스 토큰 등록 — 로그인 직후 + onNewToken 시 호출. */
    @POST("api/users/me/device-tokens")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequest)

    /** FCM 디바이스 토큰 해제 — 로그아웃 시 호출. */
    @DELETE("api/users/me/device-tokens")
    suspend fun unregisterDeviceToken(@Query("token") token: String)

    @GET("api/users/me/notification-settings")
    suspend fun getNotificationSettings(): NotificationSettingsResponse

    /** time 은 30분 단위(HH:00/HH:30)만 허용, null 이면 기존 시각 유지. */
    @PUT("api/users/me/notification-settings")
    suspend fun updateNotificationSettings(
        @Body request: UpdateNotificationSettingsRequest,
    ): NotificationSettingsResponse
}
