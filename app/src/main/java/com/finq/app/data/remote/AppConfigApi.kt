package com.finq.app.data.remote

import com.finq.app.data.remote.dto.AppConfigApiResponse
import retrofit2.http.GET

/** 앱 전역 설정 — 버전 게이트·공지. 인증 불필요(로그인 전에도 조회한다). */
interface AppConfigApi {
    @GET("api/app/config")
    suspend fun getAppConfig(): AppConfigApiResponse
}
