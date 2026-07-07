package com.finq.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviceTokenRequest(
    @Json(name = "token") val token: String,
)

@JsonClass(generateAdapter = true)
data class NotificationSettingsResponse(
    @Json(name = "enabled") val enabled: Boolean,
    @Json(name = "time")    val time: String,   // "HH:mm" (30분 단위)
)

@JsonClass(generateAdapter = true)
data class UpdateNotificationSettingsRequest(
    @Json(name = "enabled") val enabled: Boolean,
    // null 이면 기존 시각 유지 (Moshi 기본 설정은 null 필드를 직렬화하지 않음)
    @Json(name = "time")    val time: String? = null,
)
