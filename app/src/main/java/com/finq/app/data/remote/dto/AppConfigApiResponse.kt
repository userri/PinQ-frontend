package com.finq.app.data.remote.dto

/**
 * `GET /api/app/config` 응답 — 인증 불필요(공개).
 *
 * @param minVersionCode    이 값 미만이면 앱을 쓸 수 없다(강제 업데이트).
 * @param latestVersionCode 최신 배포 버전. 지금은 안내용으로만 둔다.
 * @param storeUrl          업데이트 버튼이 여는 스토어 주소.
 * @param notice            공지 문구. null 이면 공지 없음.
 *
 * 필드가 늘어나도 구버전 앱이 죽지 않도록 전부 기본값을 둔다 —
 * 특히 [minVersionCode] 기본 0 은 "아무도 막지 않음"이라 fail-open 방향으로 무너진다.
 */
data class AppConfigApiResponse(
    val minVersionCode: Int = 0,
    val latestVersionCode: Int = 0,
    val storeUrl: String? = null,
    val notice: String? = null,
)
