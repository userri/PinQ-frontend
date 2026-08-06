package com.finq.app

import android.app.Application
import android.util.Log
import com.finq.app.data.local.SessionManager
import com.finq.app.data.remote.NetworkModule
import com.finq.app.push.FcmTokenManager
import com.finq.app.push.FinQMessagingService
import com.google.android.gms.ads.MobileAds
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.KakaoSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Phase 4 메모:
 *  - 오답노트/북마크는 서버 기반으로 이관됐다.
 *  - 더 이상 SharedPreferences 기반의 LocalModule 을 사용하지 않으므로 초기화가 제거됐다.
 */
class FinQApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
        NetworkModule.init(this)
        // Kakao SDK 초기화 — BuildConfig 에서 네이티브 앱키 주입
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        // FCM 알림 채널 — 백그라운드 notification 메시지도 이 채널로 들어온다 (멱등)
        FinQMessagingService.ensureChannel(this)
        // 로그인된 세션이면 앱을 켤 때마다 토큰을 재등록한다(멱등, 실패는 삼킨다).
        //
        // 종전 등록 시점은 셋뿐이었다 — 로그인 직후 · 알림 토글 on · onNewToken.
        // 셋 다 **일회성**이라, 세션은 살아 있는데 토큰만 갈린 기기가 서버에서 영영
        // 미아가 됐다: FCM 토큰 회전, 앱 데이터 초기화, 기기 이전 복원, 그리고 그냥
        // 등록 요청이 한 번 실패한 경우(조용히 삼킨다). onNewToken 은 앱이 떠 있지
        // 않으면 놓칠 수 있고 로그인은 다시 일어나지 않으니 복구 경로가 없었다.
        // 앱 시작은 유일하게 반복되는 지점이다.
        CoroutineScope(Dispatchers.IO).launch { FcmTokenManager.registerCurrentToken() }
        // 디버그 전용 — 로그인 없이도 FCM 토큰을 Logcat 에서 확인하기 위한 출력.
        //   adb logcat -s FinQFcmToken
        // Firebase 콘솔 '테스트 메시지 전송' 에 이 토큰을 붙여 넣어 수신 테스트할 수 있다.
        // getInstance() 는 google-services.json 미배치 시 예외를 던지므로 runCatching 으로 감싼다.
        if (BuildConfig.DEBUG) {
            runCatching {
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token -> Log.i("FinQFcmToken", "FCM token = $token") }
                    .addOnFailureListener { e -> Log.w("FinQFcmToken", "FCM 토큰 조회 실패", e) }
            }.onFailure {
                Log.w("FinQFcmToken", "Firebase 미초기화 — app/google-services.json 확인 필요", it)
            }
        }
        // AdMob 초기화 — 수십~수백 ms 걸릴 수 있어 백그라운드에서 실행 (콜드 스타트 보호)
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@FinQApplication)
        }
    }
}
