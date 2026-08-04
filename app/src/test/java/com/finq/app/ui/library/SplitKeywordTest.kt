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

/**
 * 행 제목 추출 — 실서버 30건에서 관측된 세 형태를 모두 덮는지.
 * (콜론 28 / 쉼표 나열 2 / 대시는 과거 데이터 대비)
 */
class KeywordTitleTest {

    @Test
    fun `콜론형은 용어만 남긴다`() {
        assertEquals("종합부동산세", keywordTitle("종합부동산세: 주택 보유 시 부과되는 세금으로, 초고가 주택에…"))
        assertEquals("주택담보대출비율(LTV)", keywordTitle("주택담보대출비율(LTV): 담보 가치 대비 대출 한도 비율"))
    }

    @Test
    fun `쉼표 나열형은 첫 용어를 쓴다`() {
        // 형식 오류가 아니라 구버전 산출물 — 질문으로 되돌아가면 멀쩡한 용어를 버리게 된다.
        assertEquals("국제유가", keywordTitle("국제유가, 원·달러 환율, 수입물가, 원화 약세, 원자재 가격"))
        assertEquals("방어주", keywordTitle("방어주, 변동성 장세, 주식 순환매, 투자 포트폴리오 전환"))
    }

    @Test
    fun `대시형도 받는다`() {
        assertEquals("금융통화위원회", keywordTitle("금융통화위원회 — 한국은행의 정책금리를 결정하는 기구"))
    }

    @Test
    fun `구분자가 없어도 짧으면 그대로 제목이 된다`() {
        assertEquals("환율", keywordTitle("환율"))
    }

    @Test
    fun `null 이거나 장문이면 null - 호출부가 질문으로 되돌아간다`() {
        assertNull(keywordTitle(null))
        assertNull(keywordTitle("   "))
        assertNull(keywordTitle("구분자 없이 열여섯 자를 훌쩍 넘겨 제목 자리에 세울 수 없는 긴 문장"))
    }
}
