package com.finq.app.data.remote.dto

/** POST /api/users/register 및 PATCH /api/users/me/nickname 응답. */
data class RegisterApiResponse(
    val userId: Long,
    val nickname: String,
)
