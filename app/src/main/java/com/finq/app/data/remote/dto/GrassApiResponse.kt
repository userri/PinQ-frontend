package com.finq.app.data.remote.dto

/**
 * `GET /api/users/me/grass` 응답 — 연간 잔디밭.
 *
 * [days] 는 **활동한 날만** 오는 sparse 배열이다. 빈 날짜는 클라이언트가 level 0 으로 채운다.
 * 복습만 한 날도 [days] 에 포함되며 level 1 로 온다.
 *
 * @param from           집계 시작일 (ISO-8601 `yyyy-MM-dd`)
 * @param to             집계 종료일 (ISO-8601 `yyyy-MM-dd`, 보통 오늘)
 * @param graduatedTrees 복습을 3번 맞혀 졸업한(나무가 된) 문제 수.
 */
data class GrassApiResponse(
    val from: String,
    val to: String,
    val totalActiveDays: Int,
    val perfectDays: Int,
    val currentStreak: Int,
    val maxStreak: Int,
    val graduatedTrees: Int = 0,
    val days: List<GrassDayApiResponse>,
)

/**
 * 하루치 잔디.
 *
 * @param level    0=활동 없음, 1~3=농도 단계, 4=만점.
 *                 [com.finq.app.ui.theme.streakColor] 의 강도 규약과 1:1 로 매칭된다.
 * @param reviewed 그날 복습한 문제 수. 복습만 한 날은 solved=0, reviewed>0, level=1.
 */
data class GrassDayApiResponse(
    val date: String,
    val solved: Int,
    val correct: Int,
    val level: Int,
    val reviewed: Int = 0,
)
