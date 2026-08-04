package com.finq.app.ui.components

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val ABSOLUTE = DateTimeFormatter.ofPattern("M월 d일")

/**
 * "다음 물주기" 뒤에 붙는 값.
 *
 * 가까운 날은 **날짜 대신 말로** 준다. `8월 5일` 을 읽고 "아 내일이네"를 계산하는 건
 * 앱이 할 일이지 사용자가 할 일이 아니다. 오답노트 목록이 이미 `오늘` 을 쓰고 있어
 * 어휘도 이미 있다.
 *
 * 지난 날짜(이론상 안 나오지만 시계 어긋남·캐시)는 "오늘"로 접는다 — 지금 줄 수 있다는 뜻이라
 * 사실상 맞고, 지난 날짜를 그대로 보여주면 버그로 읽힌다.
 */
fun dueDateLabel(date: LocalDate, today: LocalDate = LocalDate.now()): String =
    when (ChronoUnit.DAYS.between(today, date)) {
        in Long.MIN_VALUE..0L -> "오늘"
        1L -> "내일"
        2L -> "모레"
        else -> date.format(ABSOLUTE)
    }

/**
 * 다음 물주기 한 줄. **표기는 앱 전체에서 이 함수 하나로 고정한다** —
 * 종전엔 홈이 `다음 물 주기 8/5`, 완료 화면이 `다음 물 주기 8/5`, 채점 화면이
 * `다음 물주기 8월 5일` 로 셋이 달랐다. 같은 개념을 화면마다 다르게 쓰면
 * 같은 값인지 확인하는 인지 비용이 든다.
 */
fun nextWateringText(date: LocalDate, today: LocalDate = LocalDate.now()): String =
    "다음 물주기 ${dueDateLabel(date, today)}"
