package com.finq.app.data.local

import android.content.Context

/**
 * 서버가 알 필요 없는 **1회성 로컬 플래그** 모음.
 *
 * 저장소 이름은 온보딩·개념 인트로와 같은 `finq_intro` 를 쓴다 — 같은 성격(한 번 보면
 * 끝)인 플래그가 파일마다 다른 저장소로 흩어지면 초기화·디버깅이 어려워진다.
 * 저장소는 공유하되 키 이름은 여기서 한곳에 모아 둔다.
 */
private const val PREFS_NAME = "finq_intro"

private fun prefs(context: Context) =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

// ── 공지 ──────────────────────────────────────────────────────────────────────

private const val KEY_SEEN_NOTICE = "seen_notice_hash"

/**
 * 이 공지를 이미 봤는가. 문구 자체를 키로 삼지 않고 **해시**를 저장한다 —
 * 공지가 길어져도 저장 크기가 일정하고, 문구가 바뀌면 해시가 달라져 다시 뜬다.
 */
fun hasSeenNotice(context: Context, notice: String): Boolean =
    prefs(context).getInt(KEY_SEEN_NOTICE, 0) == notice.hashCode()

fun markNoticeSeen(context: Context, notice: String) {
    prefs(context).edit().putInt(KEY_SEEN_NOTICE, notice.hashCode()).apply()
}

// ── 맛보기 문제 ───────────────────────────────────────────────────────────────

private const val KEY_TASTE_DONE = "taste_quiz_done"

/** 로그인 전 맛보기 문제를 이미 풀었는가. true 면 곧장 로그인 화면으로. */
fun hasSeenTasteQuiz(context: Context): Boolean =
    prefs(context).getBoolean(KEY_TASTE_DONE, false)

fun markTasteQuizSeen(context: Context) {
    prefs(context).edit().putBoolean(KEY_TASTE_DONE, true).apply()
}

// ── 피드백 ────────────────────────────────────────────────────────────────────

private const val KEY_FEEDBACK_BANNER_DISMISSED = "feedback_banner_dismissed"
private const val KEY_FEEDBACK_HINT_SHOWN = "feedback_hint_shown"
private const val KEY_FIRST_LAUNCH_AT = "first_launch_at"

/** 피드백을 묻기 전에 기다리는 기간. 며칠은 써 봐야 할 말이 생긴다. */
private const val FEEDBACK_DELAY_DAYS = 3L

/**
 * 이 기기에서 앱을 처음 연 시각(epoch millis). 없으면 지금으로 기록하고 그 값을 돌려준다.
 *
 * **가입일 대신 쓰는 값이다** — 서버가 가입 시각을 내려주지 않는다. 신규 사용자에겐
 * 사실상 같은 값이고, 기존 사용자에겐 "이 버전을 쓰기 시작한 날"이 되는데
 * 물어볼 대상이 새 버전의 경험이므로 오히려 그쪽이 맞다.
 */
fun firstLaunchAt(context: Context): Long {
    val p = prefs(context)
    val saved = p.getLong(KEY_FIRST_LAUNCH_AT, 0L)
    if (saved > 0L) return saved
    val now = System.currentTimeMillis()
    p.edit().putLong(KEY_FIRST_LAUNCH_AT, now).apply()
    return now
}

/** 홈 피드백 배너를 지금 띄울 때가 됐는가 — 첫 실행 +3일이 지났고 아직 안 닫았으면. */
fun shouldShowFeedbackBanner(context: Context): Boolean {
    if (isFeedbackBannerDismissed(context)) return false
    val elapsed = System.currentTimeMillis() - firstLaunchAt(context)
    return elapsed >= FEEDBACK_DELAY_DAYS * 24 * 60 * 60 * 1000
}

/** 홈 피드백 배너를 닫았는가(또는 폼으로 넘어갔는가). 한 번 해제되면 영구히 안 뜬다. */
fun isFeedbackBannerDismissed(context: Context): Boolean =
    prefs(context).getBoolean(KEY_FEEDBACK_BANNER_DISMISSED, false)

fun markFeedbackBannerDismissed(context: Context) {
    prefs(context).edit().putBoolean(KEY_FEEDBACK_BANNER_DISMISSED, true).apply()
}

/**
 * "마이페이지에서 언제든 보낼 수 있어요" 안내를 이미 띄웠는가.
 * 배너가 사라지는 순간 딱 한 번만 알려 준다 — 창구가 사라진 게 아님을 알리는 게 목적이라
 * 두 번째부터는 잔소리가 된다.
 */
fun isFeedbackHintShown(context: Context): Boolean =
    prefs(context).getBoolean(KEY_FEEDBACK_HINT_SHOWN, false)

fun markFeedbackHintShown(context: Context) {
    prefs(context).edit().putBoolean(KEY_FEEDBACK_HINT_SHOWN, true).apply()
}
