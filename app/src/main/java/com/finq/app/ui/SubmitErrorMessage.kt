package com.finq.app.ui

/**
 * 채점 실패를 사용자 말로 옮긴다.
 *
 * `e.message` 를 그대로 쓰면 `HTTP 500 ` 같은 개발자 문자열이 노출되고, 무엇을
 * 하면 되는지도 안 알려준다. 원인별로 **다음 행동**이 다르므로 문구를 가른다.
 */
internal fun submitErrorMessage(e: Throwable): String = when {
    e is retrofit2.HttpException && e.code() >= 500 ->
        "서버에 문제가 생겼어요 — 잠시 후 다시 시도해 주세요"
    e is retrofit2.HttpException && e.code() == 401 ->
        "로그인이 만료됐어요 — 다시 로그인해 주세요"
    e is java.io.IOException ->
        "네트워크 연결을 확인해 주세요"
    else -> "채점에 실패했어요 — 다시 시도해 주세요"
}
