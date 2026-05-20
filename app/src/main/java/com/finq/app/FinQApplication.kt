package com.finq.app

import android.app.Application
import com.finq.app.data.local.SessionManager
import com.finq.app.data.remote.NetworkModule
import com.kakao.sdk.common.KakaoSdk

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
    }
}
