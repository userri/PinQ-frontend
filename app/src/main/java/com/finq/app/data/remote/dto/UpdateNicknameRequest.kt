package com.finq.app.data.remote.dto

/** PATCH /api/users/me/nickname 요청 바디. */
data class UpdateNicknameRequest(
    val nickname: String,
)
