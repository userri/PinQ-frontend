package com.example.pinq_frontend.data.remote

import com.example.pinq_frontend.data.remote.dto.GoogleLoginRequest
import com.example.pinq_frontend.data.remote.dto.KakaoLoginRequest
import com.example.pinq_frontend.data.remote.dto.TokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    /** Kakao accessToken → PinQ JWT */
    @POST("api/auth/kakao")
    suspend fun loginWithKakao(@Body request: KakaoLoginRequest): TokenResponse

    /** Google ID Token → PinQ JWT */
    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): TokenResponse
}
