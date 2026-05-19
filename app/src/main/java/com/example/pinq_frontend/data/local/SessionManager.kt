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
 * Access/Refresh 토큰을 DataStore에 영구 저장하는 싱글톤.
 *
 * OkHttp Authenticator에서 동기적으로 토큰이 필요하므로
 * 인메모리 캐시(_accessToken, _refreshToken, _userId)를 함께 유지한다.
 *
 * init(context)를 Application.onCreate에서 반드시 호출할 것.
 */
object SessionManager {

    private val KEY_ACCESS_TOKEN   = stringPreferencesKey("jwt_token")
    private val KEY_REFRESH_TOKEN  = stringPreferencesKey("refresh_token")
    private val KEY_USER_ID        = stringPreferencesKey("user_id")

    @Volatile private var _accessToken:  String? = null
    @Volatile private var _refreshToken: String? = null
    @Volatile private var _userId:       Long?   = null

    val accessToken:  String?      get() = _accessToken
    val refreshToken: String?      get() = _refreshToken
    val userId:       Long?        get() = _userId
    val isLoggedIn:   Boolean      get() = !_accessToken.isNullOrEmpty()

    /** Application.onCreate에서 한 번 호출. DataStore에서 세션을 복원한다. */
    fun init(context: Context) {
        runBlocking {
            val prefs = context.applicationContext.dataStore.data.firstOrNull()
            _accessToken  = prefs?.get(KEY_ACCESS_TOKEN)
            _refreshToken = prefs?.get(KEY_REFRESH_TOKEN)
            _userId       = prefs?.get(KEY_USER_ID)?.toLongOrNull()
        }
    }

    /** 로그인 성공 후 토큰 저장. */
    suspend fun saveSession(context: Context, accessToken: String, refreshToken: String, userId: Long) {
        _accessToken  = accessToken
        _refreshToken = refreshToken
        _userId       = userId
        context.applicationContext.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN]  = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID]       = userId.toString()
        }
    }

    /** 재발급 후 access + refresh token만 갱신 (동기 버전 — Authenticator용). */
    fun updateTokensSync(context: Context, accessToken: String, refreshToken: String) {
        _accessToken  = accessToken
        _refreshToken = refreshToken
        runBlocking {
            context.applicationContext.dataStore.edit { prefs ->
                prefs[KEY_ACCESS_TOKEN]  = accessToken
                prefs[KEY_REFRESH_TOKEN] = refreshToken
            }
        }
    }

    /** 로그아웃 / 회원탈퇴 시 세션 전체 삭제. */
    suspend fun clearSession(context: Context) {
        _accessToken  = null
        _refreshToken = null
        _userId       = null
        context.applicationContext.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_USER_ID)
        }
    }

    /** 세션 전체 삭제 (동기 버전 — Authenticator용). */
    fun clearSessionSync(context: Context) {
        _accessToken  = null
        _refreshToken = null
        _userId       = null
        runBlocking {
            context.applicationContext.dataStore.edit { prefs ->
                prefs.remove(KEY_ACCESS_TOKEN)
                prefs.remove(KEY_REFRESH_TOKEN)
                prefs.remove(KEY_USER_ID)
            }
        }
    }
}
