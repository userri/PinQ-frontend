package com.finq.app.ui

/**
 * 예외를 사용자에게 보여줄 한 줄로 옮긴다.
 *
 * `e.message ?: "불러오지 못했어요"` 라고 쓰면 한글 문구는 거의 실행되지 않는다 —
 * OkHttp/Retrofit 예외의 message 는 대개 non-null 이라(`timeout`, `HTTP 500 `)
 * 그 개발자 문자열이 그대로 화면에 뜬다. 그래서 message 를 쓰지 않고, 원인이
 * 분명한 경우만 문구를 가른 뒤 나머지를 [fallback] 으로 보낸다.
 *
 * 문구를 원인별로 가르는 기준은 **사용자가 할 일**이다. 5xx 는 기다리는 것,
 * 401 은 다시 로그인하는 것, 네트워크는 연결을 보는 것 — 셋이 서로 다르다.
 */
internal fun userErrorMessage(e: Throwable, fallback: String): String = when {
    e is retrofit2.HttpException && e.code() == 401 ->
        "로그인이 만료됐어요 — 다시 로그인해 주세요"
    e is retrofit2.HttpException && e.code() >= 500 ->
        "서버에 문제가 생겼어요 — 잠시 후 다시 시도해 주세요"
    e is java.io.IOException ->
        "네트워크 연결을 확인해 주세요"
    else -> fallback
}

/** 채점 제출 실패 — [userErrorMessage] 의 채점용 진입점. */
internal fun submitErrorMessage(e: Throwable): String =
    userErrorMessage(e, "채점에 실패했어요 — 다시 시도해 주세요")
