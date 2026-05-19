package com.example.pinq_frontend.data.repository

import android.content.Context
import com.example.pinq_frontend.BuildConfig
import com.example.pinq_frontend.data.local.SessionManager
import com.example.pinq_frontend.data.remote.AuthApi
import com.example.pinq_frontend.data.remote.dto.GoogleLoginRequest
import com.example.pinq_frontend.data.remote.dto.KakaoLoginRequest
import com.example.pinq_frontend.data.remote.dto.LogoutRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 소셜 로그인 유스케이스.
 *
 * 카카오: Kakao SDK → accessToken → 백엔드 → JWT 저장
 * 구글:  Credential Manager → idToken → 백엔드 → JWT 저장
 */
class AuthRepository(
    private val authApi: AuthApi,
) {
    // ── 카카오 로그인 ─────────────────────────────────────────────────────────

    /**
     * 카카오 로그인을 수행하고 PinQ JWT 를 SessionManager 에 저장한다.
     *
     * 카카오톡 앱이 설치돼 있으면 앱 로그인, 없으면 카카오계정 웹 로그인으로 폴백.
     */
    suspend fun loginWithKakao(context: Context): Result<String> = runCatching {
        val token = getKakaoToken(context)
        val response = authApi.loginWithKakao(KakaoLoginRequest(token.accessToken))
        SessionManager.saveSession(context, response.accessToken, response.refreshToken, response.userId)
        response.nickname
    }

    /** Kakao SDK 콜백을 코루틴으로 변환 */
    private suspend fun getKakaoToken(context: Context): OAuthToken =
        suspendCoroutine { cont ->
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) cont.resumeWithException(error)
                else if (token != null) cont.resume(token)
                else cont.resumeWithException(IllegalStateException("카카오 토큰이 null 입니다"))
            }

            // 카카오톡 설치 여부에 따라 로그인 방식 분기
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                    if (error != null) {
                        // 카카오톡 설치됐지만 로그인 취소 → 웹 로그인으로 재시도 안 함
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            cont.resumeWithException(error)
                            return@loginWithKakaoTalk
                        }
                        // 카카오톡 오류 → 카카오계정 웹으로 폴백
                        UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                    } else if (token != null) {
                        cont.resume(token)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
            }
        }

    // ── 구글 로그인 ───────────────────────────────────────────────────────────

    /**
     * 구글 로그인을 수행하고 PinQ JWT 를 SessionManager 에 저장한다.
     *
     * Credential Manager(One Tap / Bottom Sheet) 를 사용한다.
     * filterByAuthorizedAccounts = false 로 설정해 새 계정도 허용한다.
     */
    suspend fun loginWithGoogle(context: Context): Result<String> = runCatching {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)   // 신규 계정 허용
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)             // 자동 선택 끔 (UX 명확성)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, request)
        val credential = GoogleIdTokenCredential.createFrom(result.credential.data)

        val response = authApi.loginWithGoogle(GoogleLoginRequest(credential.idToken))
        SessionManager.saveSession(context, response.accessToken, response.refreshToken, response.userId)
        response.nickname
    }

    // ── 로그아웃 ─────────────────────────────────────────────────────────────

    suspend fun logout(context: Context) {
        // 서버 측 refresh token 삭제
        SessionManager.refreshToken?.let { token ->
            runCatching { authApi.logout(LogoutRequest(token)) }
        }
        SessionManager.clearSession(context)
        runCatching { UserApiClient.instance.logout { } }
    }
}
