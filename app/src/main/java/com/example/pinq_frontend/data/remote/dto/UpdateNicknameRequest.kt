package com.example.pinq_frontend.data.remote.dto

/** PATCH /api/users/me/nickname 요청 바디. */
data class UpdateNicknameRequest(
    val nickname: String,
)
