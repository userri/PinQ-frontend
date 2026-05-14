package com.example.pinq_frontend.data.local

import android.content.Context

/**
 * 로컬 저장소 싱글톤 컨테이너.
 *
 * [NetworkModule]과 동일한 패턴으로, Application 수명과 같은 단일 인스턴스를 보장한다.
 * SharedPreferences는 Android 내부에서도 캐싱되지만,
 * 앱 전체에서 같은 [WrongNoteStore] 인스턴스를 공유해 DI 일관성을 유지한다.
 *
 * 사용: 최초 호출 시 반드시 [init]으로 초기화 (Application.onCreate 권장).
 * 이후에는 어디서든 [wrongNoteStore]로 접근.
 */
object LocalModule {

    @Volatile private var _wrongNoteStore: WrongNoteStore? = null

    val wrongNoteStore: WrongNoteStore
        get() = _wrongNoteStore
            ?: error("LocalModule not initialized. Call LocalModule.init(context) first.")

    fun init(context: Context) {
        if (_wrongNoteStore == null) {
            synchronized(this) {
                if (_wrongNoteStore == null) {
                    _wrongNoteStore = WrongNoteStore(context.applicationContext)
                }
            }
        }
    }
}
