package com.finq.app.ui.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 서버 Quiz.keyword 는 "용어 — 설명" 한 필드(최대 500자)다.
 * 카드에서 용어는 아웃라인 태그, 설명은 본문으로 갈라 보여준다.
 */
class SplitKeywordTest {

    @Test
    fun `em 대시로 용어와 설명을 가른다`() {
        val (term, desc) = splitKeyword("금융통화위원회 — 한국은행의 정책금리를 결정하는 기구.")
        assertEquals("금융통화위원회", term)
        assertEquals("한국은행의 정책금리를 결정하는 기구.", desc)
    }

    @Test
    fun `en 대시도 구분자로 본다`() {
        val (term, desc) = splitKeyword("LTV – 담보 가치 대비 대출 한도 비율.")
        assertEquals("LTV", term)
        assertEquals("담보 가치 대비 대출 한도 비율.", desc)
    }

    @Test
    fun `공백 하이픈도 구분자로 본다`() {
        val (term, desc) = splitKeyword("PER - 주가를 주당순이익으로 나눈 값.")
        assertEquals("PER", term)
        assertEquals("주가를 주당순이익으로 나눈 값.", desc)
    }

    @Test
    fun `구분자 없는 짧은 말은 용어만`() {
        val (term, desc) = splitKeyword("기준금리")
        assertEquals("기준금리", term)
        assertNull(desc)
    }

    @Test
    fun `구분자 없는 문장은 태그로 두르지 않고 설명으로만`() {
        val sentence = "환율이 한국 경제 전반에 미치는 효과는 산업과 소비 양쪽에서 복합적으로 나타납니다."
        val (term, desc) = splitKeyword(sentence)
        assertNull(term)
        assertEquals(sentence, desc)
    }

    @Test
    fun `앞부분이 길면 용어로 보지 않고 통째로 설명`() {
        val raw = "환율이 오르면 수입 물가가 함께 오른다 — 그래서 소비자 부담이 커진다."
        val (term, desc) = splitKeyword(raw)
        assertNull(term)
        assertEquals(raw, desc)
    }

    @Test
    fun `빈 문자열은 둘 다 null`() {
        val (term, desc) = splitKeyword("   ")
        assertNull(term)
        assertNull(desc)
    }
}
