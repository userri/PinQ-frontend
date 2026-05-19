package com.example.pinq_frontend.data.remote

import com.example.pinq_frontend.BuildConfig
import com.example.pinq_frontend.data.local.SessionManager
import com.example.pinq_frontend.data.remote.dto.RefreshRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
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

    private val BASE_URL get() = BuildConfig.BASE_URL

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * access token 재발급 전용 Retrofit — Authenticator 내부에서 사용.
     * okHttpClient와 분리해서 순환 참조를 방지한다.
     */
    private val authRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
    private val authApiForRefresh: AuthApi by lazy { authRetrofit.create(AuthApi::class.java) }

    /**
     * JWT 인터셉터 — SessionManager에 토큰이 있으면 모든 요청에 Bearer 헤더를 추가한다.
     */
    private val authInterceptor = Interceptor { chain ->
        val token = SessionManager.accessToken
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    /**
     * 401 응답 시 refresh token으로 access token을 재발급하고 요청을 재시도한다.
     *
     * - refresh token도 없거나 재발급 실패 시 null을 반환 → 로그인 화면으로 유도.
     * - 같은 요청에 대해 재시도를 반복하지 않도록 응답 횟수를 체크한다.
     */
    private fun tokenAuthenticator(context: android.content.Context) = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            // 이미 재시도한 요청이면 포기 (무한 루프 방지)
            if (response.request.header("X-Retry-Auth") != null) return null

            val refreshToken = SessionManager.refreshToken ?: return null

            // 동기 호출 — Authenticator는 백그라운드 스레드에서 실행된다
            val newTokens = runCatching {
                kotlinx.coroutines.runBlocking {
                    authApiForRefresh.refresh(RefreshRequest(refreshToken))
                }
            }.getOrNull() ?: return null

            SessionManager.updateTokensSync(context, newTokens.accessToken, newTokens.refreshToken)

            return response.request.newBuilder()
                .header("Authorization", "Bearer ${newTokens.accessToken}")
                .header("X-Retry-Auth", "true")
                .build()
        }
    }

    /** OkHttp 클라이언트 팩토리 — context가 필요해서 Application.onCreate에서 초기화한다. */
    fun buildOkHttpClient(context: android.content.Context): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator(context))
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

    private lateinit var retrofit: Retrofit

    /** Application.onCreate에서 반드시 호출 */
    fun init(context: android.content.Context) {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(buildOkHttpClient(context))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val quizApi:    QuizApi    by lazy { retrofit.create(QuizApi::class.java) }
    val userApi:    UserApi    by lazy { retrofit.create(UserApi::class.java) }
    val authApi:    AuthApi    by lazy { retrofit.create(AuthApi::class.java) }
    val libraryApi: LibraryApi by lazy { retrofit.create(LibraryApi::class.java) }
}
