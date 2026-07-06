package com.finq.app

import android.app.Application
import com.finq.app.data.local.SessionManager
import com.finq.app.data.remote.NetworkModule
import com.google.android.gms.ads.MobileAds
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
        // AdMob 초기화 — 수십~수백 ms 걸릴 수 있어 백그라운드에서 실행 (콜드 스타트 보호)
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@FinQApplication)
        }
    }
}
