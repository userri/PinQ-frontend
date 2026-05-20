package com.finq.app.data.remote

import com.finq.app.data.remote.dto.GoogleLoginRequest
import com.finq.app.data.remote.dto.KakaoLoginRequest
import com.finq.app.data.remote.dto.LogoutRequest
import com.finq.app.data.remote.dto.RefreshRequest
import com.finq.app.data.remote.dto.TokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/kakao")
    suspend fun loginWithKakao(@Body request: KakaoLoginRequest): TokenResponse

    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): TokenResponse

    /** refresh token → 새 access + refresh token */
    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): TokenResponse

    /** 서버 측 refresh token 삭제 (로그아웃) */
    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest)
}
