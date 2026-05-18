package com.example.pinq_frontend.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pinq_session")

/**
 * JWT 액세스 토큰을 DataStore 에 영구 저장하는 싱글톤.
 *
 * OkHttp Interceptor 에서 동기적으로 토큰이 필요하므로
 * 인메모리 캐시(_token)를 함께 유지한다.
 *
 * context 는 저장하지 않고 각 메서드 호출 시 직접 전달받는다.
 *
 * init(context) 를 Application.onCreate 에서 반드시 호출할 것.
 */
object SessionManager {

    private val KEY_TOKEN = stringPreferencesKey("jwt_token")

    @Volatile private var _token: String? = null

    val token: String?      get() = _token
    val isLoggedIn: Boolean get() = !_token.isNullOrEmpty()

    /** Application.onCreate 에서 한 번 호출. DataStore 에서 토큰을 복원한다. */
    fun init(context: Context) {
        runBlocking {
            val prefs = context.applicationContext.dataStore.data.firstOrNull()
            _token = prefs?.get(KEY_TOKEN)
        }
    }

    /** 로그인 성공 후 토큰 저장. */
    suspend fun saveSession(context: Context, token: String) {
        _token = token
        context.applicationContext.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
        }
    }

    /** 로그아웃 / 회원탈퇴 시 토큰 삭제. */
    suspend fun clearSession(context: Context) {
        _token = null
        context.applicationContext.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
        }
    }
}
