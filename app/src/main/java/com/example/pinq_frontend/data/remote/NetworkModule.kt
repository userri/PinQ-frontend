package com.example.pinq_frontend.data.remote

import android.content.Context
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
    private const val MAX_AUTH_RESPONSE_COUNT = 2

    private val refreshLock = Any()

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
     * - 같은 요청은 priorResponse 체인 기준 1회만 재시도한다.
     * - 동시 401 응답은 refreshLock으로 직렬화해 refresh token rotation 충돌을 막는다.
     * - refresh token이 없거나 재발급에 실패하면 세션을 삭제하고 null을 반환한다.
     */
    private fun tokenAuthenticator(context: Context): Authenticator {
        val appContext = context.applicationContext
        return object : Authenticator {
            override fun authenticate(route: Route?, response: Response): Request? {
                if (response.responseCount() >= MAX_AUTH_RESPONSE_COUNT) return null

                val requestToken = response.request.bearerToken()
                val currentToken = SessionManager.accessToken
                if (!currentToken.isNullOrEmpty() && currentToken != requestToken) {
                    return response.request.withBearerToken(currentToken)
                }

                return synchronized(refreshLock) {
                    val latestToken = SessionManager.accessToken
                    if (!latestToken.isNullOrEmpty() && latestToken != requestToken) {
                        return@synchronized response.request.withBearerToken(latestToken)
                    }

                    val refreshToken = SessionManager.refreshToken
                    if (refreshToken.isNullOrEmpty()) {
                        SessionManager.clearSessionSync(appContext)
                        return@synchronized null
                    }

                    // 동기 호출 — Authenticator는 백그라운드 스레드에서 실행된다.
                    val newTokens = runCatching {
                        kotlinx.coroutines.runBlocking {
                            authApiForRefresh.refresh(RefreshRequest(refreshToken))
                        }
                    }.getOrNull()

                    if (newTokens == null) {
                        SessionManager.clearSessionSync(appContext)
                        return@synchronized null
                    }

                    SessionManager.updateTokensSync(appContext, newTokens.accessToken, newTokens.refreshToken)
                    response.request.withBearerToken(newTokens.accessToken)
                }
            }
        }
    }

    /** OkHttp 클라이언트 팩토리 — Application context 만 캡처하도록 init 내부에서만 호출한다. */
    private fun buildOkHttpClient(context: Context): OkHttpClient =
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
    fun init(context: Context) {
        val appContext = context.applicationContext
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(buildOkHttpClient(appContext))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val quizApi:    QuizApi    by lazy { retrofit.create(QuizApi::class.java) }
    val userApi:    UserApi    by lazy { retrofit.create(UserApi::class.java) }
    val authApi:    AuthApi    by lazy { retrofit.create(AuthApi::class.java) }
    val libraryApi: LibraryApi by lazy { retrofit.create(LibraryApi::class.java) }

    private fun Response.responseCount(): Int {
        var count = 1
        var prior = priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private fun Request.bearerToken(): String? =
        header("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.removePrefix("Bearer ")

    private fun Request.withBearerToken(token: String): Request =
        newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
}
