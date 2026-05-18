package com.example.pinq_frontend.data.remote

import com.example.pinq_frontend.BuildConfig
import com.example.pinq_frontend.data.local.SessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// Retrofit / OkHttp / Moshi 싱글톤 컨테이너.
//
// BASE_URL 선택 가이드:
//   권장: adb reverse 포트포워딩 + "http://localhost:8080/"
//     1) PC 에서 백엔드 ./gradlew bootRun
//     2) adb reverse tcp:8080 tcp:8080
//   대안 A) 에뮬레이터 only:  "http://10.0.2.2:8080/"
//   대안 B) 실기기 + 같은 WiFi:  "http://<PC LAN IP>:8080/"
//     PC IP 찾기 (macOS): ipconfig getifaddr en0
object NetworkModule {

    // base.url=http://192.168.x.x:8080/ 를 local.properties 에 설정하면 자동 반영.
    // 미설정 시 adb reverse tcp:8080 tcp:8080 방식으로 localhost:8080 사용.
    private val BASE_URL get() = BuildConfig.BASE_URL

    /**
     * JWT 인터셉터 — SessionManager 에 토큰이 있으면 모든 요청에 Bearer 헤더를 추가한다.
     * /api/auth/ 하위 엔드포인트는 토큰 없이도 동작하므로 특별 처리 불필요.
     */
    private val authInterceptor = Interceptor { chain ->
        val original: Request = chain.request()
        val token = SessionManager.token
        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        chain.proceed(request)
    }

    /** OkHttp 클라이언트 — JWT 인터셉터 + 로깅 포함. */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
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

    val quizApi: QuizApi by lazy { retrofit.create(QuizApi::class.java) }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val libraryApi: LibraryApi by lazy { retrofit.create(LibraryApi::class.java) }
}
