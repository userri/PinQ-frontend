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
 *
 * ⚠️ 삼키되 **조용히 삼키지 않는다.** 종전엔 미로그인 조기 리턴에 로그가 아예 없고
 * 실패도 `Log.w` 한 줄뿐이라, "푸시가 안 온다"를 받았을 때 ⓐ 토큰을 못 받았는지
 * ⓑ 로그인 상태가 아니라 건너뛴 건지 ⓒ 서버가 거절했는지를 기기에서 가릴 수 없었다.
 * 세 갈래가 각각 흔적을 남긴다. 토큰은 앞 12자만 남긴다 — 전체는 그 기기로 푸시를
 * 보낼 수 있는 자격증명이라 로그에 통째로 흘리지 않는다.
 */
object FcmTokenManager {

    private const val TAG = "FcmTokenManager"

    /** 로그용 토큰 축약 — 식별에는 충분하고 재사용에는 부족한 길이. */
    private fun String.short(): String = "${take(12)}…(${length})"

    /** 현재 기기의 FCM 등록 토큰. google-services 미설정 등으로 실패하면 예외. */
    suspend fun currentToken(): String = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    /** 로그인 상태면 현재 FCM 토큰을 백엔드에 등록한다. */
    suspend fun registerCurrentToken() {
        if (!SessionManager.isLoggedIn) {
            Log.w(TAG, "등록 건너뜀 — 로그인 상태가 아님")
            return
        }
        val token = runCatching { currentToken() }
            .onFailure { Log.w(TAG, "FCM 토큰 획득 실패", it) }
            .getOrNull() ?: return
        register(token)
    }

    /** onNewToken 등 토큰 문자열을 이미 알고 있을 때의 등록. */
    suspend fun registerToken(token: String) {
        if (!SessionManager.isLoggedIn) {
            Log.w(TAG, "등록 건너뜀(onNewToken) — 로그인 상태가 아님")
            return
        }
        register(token)
    }

    private suspend fun register(token: String) {
        runCatching { NetworkModule.userApi.registerDeviceToken(DeviceTokenRequest(token)) }
            .onSuccess { Log.i(TAG, "FCM 토큰 등록 성공 ${token.short()}") }
            .onFailure { Log.w(TAG, "FCM 토큰 등록 실패 ${token.short()}", it) }
    }

    /** 로그아웃 직전 현재 FCM 토큰을 백엔드에서 해제한다. */
    suspend fun unregisterCurrentToken() {
        if (!SessionManager.isLoggedIn) return
        runCatching {
            NetworkModule.userApi.unregisterDeviceToken(currentToken())
        }.onFailure { Log.w(TAG, "FCM 토큰 해제 실패", it) }
    }
}
