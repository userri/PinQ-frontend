package com.finq.app.push

import android.util.Log
import com.finq.app.data.local.SessionManager
import com.finq.app.data.remote.NetworkModule
import com.finq.app.data.remote.dto.DeviceTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * FCM 디바이스 토큰의 백엔드 등록/해제.
 *
 * - 등록: 로그인 성공 직후, onNewToken 수신 시, 알림 설정을 켤 때
 * - 해제: 로그아웃 직전 (JWT 가 필요하므로 세션 삭제 전에 호출할 것)
 *
 * 토큰 등록 실패가 로그인 등 주 흐름을 막으면 안 되므로 모든 함수는 예외를 삼킨다.
 */
object FcmTokenManager {

    private const val TAG = "FcmTokenManager"

    /** 현재 기기의 FCM 등록 토큰. google-services 미설정 등으로 실패하면 예외. */
    suspend fun currentToken(): String = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    /** 로그인 상태면 현재 FCM 토큰을 백엔드에 등록한다. */
    suspend fun registerCurrentToken() {
        if (!SessionManager.isLoggedIn) return
        runCatching {
            NetworkModule.userApi.registerDeviceToken(DeviceTokenRequest(currentToken()))
        }.onFailure { Log.w(TAG, "FCM 토큰 등록 실패", it) }
    }

    /** onNewToken 등 토큰 문자열을 이미 알고 있을 때의 등록. */
    suspend fun registerToken(token: String) {
        if (!SessionManager.isLoggedIn) return
        runCatching {
            NetworkModule.userApi.registerDeviceToken(DeviceTokenRequest(token))
        }.onFailure { Log.w(TAG, "FCM 토큰 등록 실패", it) }
    }

    /** 로그아웃 직전 현재 FCM 토큰을 백엔드에서 해제한다. */
    suspend fun unregisterCurrentToken() {
        if (!SessionManager.isLoggedIn) return
        runCatching {
            NetworkModule.userApi.unregisterDeviceToken(currentToken())
        }.onFailure { Log.w(TAG, "FCM 토큰 해제 실패", it) }
    }
}
