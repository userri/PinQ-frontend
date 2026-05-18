package com.example.pinq_frontend.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KakaoLoginRequest(
    @Json(name = "accessToken") val accessToken: String,
)

@JsonClass(generateAdapter = true)
data class GoogleLoginRequest(
    @Json(name = "idToken") val idToken: String,
)

@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = "accessToken") val accessToken: String,
    @Json(name = "tokenType")   val tokenType: String,
    @Json(name = "expiresIn")   val expiresIn: Long,
    @Json(name = "nickname")    val nickname: String,
)
