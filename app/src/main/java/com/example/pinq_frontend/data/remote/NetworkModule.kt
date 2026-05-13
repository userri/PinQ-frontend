package com.example.pinq_frontend.data.remote

import com.example.pinq_frontend.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Retrofit / OkHttp / Moshi 싱글톤 컨테이너.
 *
 * ─── BASE_URL 선택 가이드 ──────────────────────────────────────────────────
 *
 * 권장: adb reverse 포트포워딩 + "http://localhost:8080/"
 *   1) PC 에서 백엔드 ./gradlew bootRun
 *   2) adb reverse tcp:8080 tcp:8080
 * 대안 A) 에뮬레이터 only:  "http://10.0.2.2:8080/"
 *   10.0.2.2 = 에뮬레이터에서 호스트 PC 의 localhost 매핑.
 *   실기기에서는 이 주소가 의미 없으므로 사용 불가.
 *
 * 대안 B) 실기기 + 같은 WiFi:  "http://<PC LAN IP>:8080/"
 *   PC IP 찾기 (macOS):  ipconfig getifaddr en0
 *   PC IP 찾기 (Win):    ipconfig | findstr "IPv4"
 *   주의:  X.X.X.255 같은 broadcast 주소는 절대 사용 금지.
 *           PC 방화벽이 8080 포트를 열어줘야 함.
 *           Spring Boot 가 0.0.0.0 으로 바인딩 되어 있어야 함
 *           (기본값으로 그렇게 됨).
 * ─────────────────────────────────────────────────────────────────────────
 */
object NetworkModule {

    // adb reverse 방식. 별도 설정 없이 에뮬레이터/실기기 모두 동작.
    private const val BASE_URL = "http://192.168.200.248:8080/"

    /** OkHttp 클라이언트 — 모든 HTTP 통신의 저수준 처리 + 로깅. */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val quizApi: QuizApi by lazy {
        retrofit.create(QuizApi::class.java)
    }
}
