package com.finq.app.ui.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PracticeGrowthTest {

    private val today = LocalDate.of(2026, 7, 22)

    @Test
    fun `졸업이면 스트립 없음`() {
        assertNull(growthStrip(stage = 2, graduated = true, dueDateIso = "2026-07-25", today = today))
    }

    @Test
    fun `stage 0 은 1 of 3 단계 · 새싹`() {
        val s = growthStrip(stage = 0, graduated = false, dueDateIso = null, today = today)!!
        assertEquals("1/3단계", s.stageText)
        assertEquals(com.finq.app.R.drawable.ic_stage_sprout, s.stageIconRes)
        assertFalse(s.finalStage)
    }

    @Test
    fun `stage 2 는 마지막 단계 플래그`() {
        val s = growthStrip(stage = 2, graduated = false, dueDateIso = "2026-07-24", today = today)!!
        assertEquals("3/3단계", s.stageText)
        assertEquals(com.finq.app.R.drawable.ic_stage_almost_tree, s.stageIconRes)
        assertTrue(s.finalStage)
    }

    @Test
    fun `due 가 오늘이면 오늘 물 줄 수 있어요 · dueToday`() {
        val s = growthStrip(stage = 1, graduated = false, dueDateIso = "2026-07-22", today = today)!!
        assertEquals("오늘 물 줄 수 있어요", s.dueText)
        assertTrue(s.dueToday)
    }

    @Test
    fun `due 가 과거여도 오늘 물 줄 수 있어요로 취급`() {
        val s = growthStrip(stage = 1, graduated = false, dueDateIso = "2026-07-20", today = today)!!
        assertEquals("오늘 물 줄 수 있어요", s.dueText)
        assertTrue(s.dueToday)
    }

    @Test
    fun `due 가 미래면 D-n 물 주기`() {
        val s = growthStrip(stage = 1, graduated = false, dueDateIso = "2026-07-25", today = today)!!
        assertEquals("D-3 · 7/25 물 주기", s.dueText)
        assertFalse(s.dueToday)
    }

    @Test
    fun `due 파싱 실패면 시점 생략`() {
        val s = growthStrip(stage = 1, graduated = false, dueDateIso = "not-a-date", today = today)!!
        assertNull(s.dueText)
        assertFalse(s.dueToday)
    }

    @Test
    fun `due null 이면 시점 생략`() {
        val s = growthStrip(stage = 0, graduated = false, dueDateIso = null, today = today)!!
        assertNull(s.dueText)
    }

    @Test
    fun `채점 - 정답`() = assertTrue(isPracticeCorrect(selectedChoiceId = 5L, correctChoiceId = 5L))

    @Test
    fun `채점 - 오답`() = assertFalse(isPracticeCorrect(selectedChoiceId = 5L, correctChoiceId = 7L))

    @Test
    fun `채점 - 마스킹(null)이면 오답 취급`() =
        assertFalse(isPracticeCorrect(selectedChoiceId = 5L, correctChoiceId = null))
}
