package com.finq.app.data.repository

import com.finq.app.data.remote.UserApi
import com.finq.app.data.remote.dto.NotificationSettingsResponse
import com.finq.app.data.remote.dto.UpdateNotificationSettingsRequest

/** 푸시알림 설정 데이터. time 은 "HH:mm" (30분 단위). */
data class NotificationSettings(
    val enabled: Boolean,
    val time: String,
)

/**
 * 푸시알림 설정 유스케이스 — 백엔드의 notification-settings API 를 감싼다.
 * 디바이스 토큰 등록/해제는 [com.finq.app.push.FcmTokenManager] 가 담당한다.
 */
class NotificationRepository(private val userApi: UserApi) {

    suspend fun getSettings(): NotificationSettings =
        userApi.getNotificationSettings().toModel()

    /** time 이 null 이면 서버가 기존 시각을 유지한다. */
    suspend fun updateSettings(enabled: Boolean, time: String? = null): NotificationSettings =
        userApi.updateNotificationSettings(
            UpdateNotificationSettingsRequest(enabled = enabled, time = time)
        ).toModel()

    private fun NotificationSettingsResponse.toModel() =
        NotificationSettings(enabled = enabled, time = time)
}
