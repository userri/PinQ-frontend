package com.finq.app.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * "다음 물주기" 표기 — 가까운 날은 날짜 대신 말로.
 * `8월 5일` 을 읽고 "내일이네"를 계산하는 건 앱이 할 일이다.
 */
class DueDateTest {

    private val today = LocalDate.of(2026, 8, 4)

    @Test
    fun `오늘 내일 모레는 말로`() {
        assertEquals("오늘", dueDateLabel(LocalDate.of(2026, 8, 4), today))
        assertEquals("내일", dueDateLabel(LocalDate.of(2026, 8, 5), today))
        assertEquals("모레", dueDateLabel(LocalDate.of(2026, 8, 6), today))
    }

    @Test
    fun `사흘 뒤부터는 날짜로`() {
        assertEquals("8월 7일", dueDateLabel(LocalDate.of(2026, 8, 7), today))
        assertEquals("8월 18일", dueDateLabel(LocalDate.of(2026, 8, 18), today))
    }

    @Test
    fun `달을 넘겨도 같은 형식`() {
        assertEquals("9월 1일", dueDateLabel(LocalDate.of(2026, 9, 1), today))
    }

    @Test
    fun `지난 날짜는 오늘로 접는다 - 지금 줄 수 있다는 뜻이라 사실상 맞다`() {
        assertEquals("오늘", dueDateLabel(LocalDate.of(2026, 7, 30), today))
    }

    @Test
    fun `한 줄 문구는 앱 전체가 이 형식 하나만 쓴다`() {
        assertEquals("다음 물주기 내일", nextWateringText(LocalDate.of(2026, 8, 5), today))
        assertEquals("다음 물주기 8월 9일", nextWateringText(LocalDate.of(2026, 8, 9), today))
    }
}
