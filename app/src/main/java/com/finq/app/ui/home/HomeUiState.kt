package com.finq.app.ui.home

import java.time.LocalDate

/**
 * 홈 화면 UI 상태.
 *
 * - [quizCount]   오늘 준비된 퀴즈 수
 * - [streak]      연속 학습 일수 (백엔드 currentStreak — 오늘 풀면 오늘 포함)
 * - [weekLevels]  이번 주(월~일) 잔디 level. GET /api/users/me/grass 의 days[].level 을
 *                 그대로 슬라이스한 값. 미래 날짜는 -1(아직 오지 않음).
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val nickname: String = "",
    val quizCount: Int = 0,
    val streak: Int = 0,
    /** 오늘 데일리 퀴즈를 풀었는가 — 스트릭 문구 분기용. activityGrid/weekLevels 로 유추 금지. */
    val solvedToday: Boolean = false,
    val maxStreak: Int = 0,
    val weekLevels: List<Int> = emptyList(),
    /** 오늘 복습할 오답 수 ("오늘 물 줄 잔디 N개"). */
    val reviewCount: Int = 0,
    /** reviewCount == 0 일 때 안내할 다음 물 주기 날짜. */
    val nextReviewDate: LocalDate? = null,
    /** 오늘 세트 전체 문제 수 — 완료 상태 "N/M 정답" 표기용. */
    val todayTotal: Int = 0,
    /** 오늘 세트에서 첫 시도에 맞힌 수 (quiz.correct == true). */
    val todayCorrect: Int = 0,
    /** 정원 히어로 미니 프리뷰 데이터. 로드 실패 시 null — 빈 정원으로 그린다. */
    val garden: com.finq.app.data.repository.ReviewGarden? = null,
)
