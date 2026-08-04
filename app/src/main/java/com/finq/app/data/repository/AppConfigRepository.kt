package com.finq.app.data.repository

import com.finq.app.data.remote.NetworkModule

/**
 * 앱 전역 설정(버전 게이트·공지).
 *
 * @param minVersionCode 이 값 미만이면 앱을 못 쓴다.
 * @param storeUrl       업데이트 버튼이 열 주소. null 이면 버튼을 숨긴다.
 * @param notice         공지 문구. null 이면 공지 없음.
 */
data class AppConfig(
    val minVersionCode: Int,
    val storeUrl: String?,
    val notice: String?,
)

interface AppConfigRepository {
    /** 실패하면 null — 호출자는 조용히 통과시킨다(fail-open). */
    suspend fun getAppConfig(): AppConfig?
}

/**
 * 조회 실패를 **예외로 올리지 않는다.**
 *
 * 게이트의 목적은 "너무 낡은 앱이 서버와 어긋나는 것"을 막는 건데,
 * 조회 자체가 실패했을 땐 그 판단 근거가 없다. 그때 앱을 막으면
 * 서버 장애·비행기 모드가 곧바로 "앱이 안 켜짐"이 된다 —
 * 강제 업데이트를 못 거는 것보다 나쁜 사고다. 그래서 fail-open.
 */
class ApiAppConfigRepository : AppConfigRepository {
    override suspend fun getAppConfig(): AppConfig? =
        runCatching {
            val dto = NetworkModule.appConfigApi.getAppConfig()
            AppConfig(
                minVersionCode = dto.minVersionCode,
                storeUrl = dto.storeUrl,
                notice = dto.notice?.takeIf { it.isNotBlank() },
            )
        }.getOrNull()
}
