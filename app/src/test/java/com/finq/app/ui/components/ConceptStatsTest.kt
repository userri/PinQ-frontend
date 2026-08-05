package com.finq.app.ui.components

import com.finq.app.data.repository.ConceptStat
import com.finq.app.data.repository.ConceptStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConceptStatsTest {

    private fun stat(name: String, total: Int, correct: Int) =
        ConceptStat(name, name, total, correct, correct.toFloat() / total)

    @Test
    fun `표시 정답률이 같으면 전부 지목한다`() {
        // 25/43 = 58.14%, 7/12 = 58.33% — 화면엔 둘 다 58% 로 보인다.
        val categories = listOf(stat("금리", 20, 17), stat("환율", 43, 25), stat("부동산", 12, 7))
        val group = weakConceptGroup(ConceptStats(categories, weakest = categories[1]))
        assertEquals(listOf("환율", "부동산"), group.map { it.displayName })
    }

    @Test
    fun `표본이 3 미만인 카테고리는 지목하지 않는다`() {
        // 2문제 틀렸다고 0% 로 지목하면 시작하자마자 질책이 된다.
        val categories = listOf(stat("금리", 20, 17), stat("부동산", 10, 5), stat("증시", 2, 0))
        val group = weakConceptGroup(ConceptStats(categories, weakest = categories[2]))
        assertEquals(listOf("부동산"), group.map { it.displayName })
    }

    @Test
    fun `전부 미달이면 지목하지 않는다`() {
        // 화면이 통째로 붉어지면 빨강이 아무것도 구별해주지 못한다 — 문구 한 줄로 받는다.
        val categories = listOf(stat("금리", 27, 16), stat("증시", 22, 10), stat("환율", 17, 7))
        val stats = ConceptStats(categories, weakest = categories[2])
        assertTrue(isAllBelowBar(stats))
        assertTrue(weakConceptGroup(stats).isEmpty())
    }

    @Test
    fun `하나라도 기준 이상이면 전부 미달이 아니다`() {
        // 빨강이 아직 "저 줄은 미달"을 구별해주므로 그대로 둔다.
        val categories = listOf(stat("금리", 20, 17), stat("증시", 22, 10), stat("환율", 17, 7))
        val stats = ConceptStats(categories, weakest = categories[2])
        assertFalse(isAllBelowBar(stats))
        assertEquals(listOf("환율"), weakConceptGroup(stats).map { it.displayName })
    }

    @Test
    fun `표본이 3 미만인 것만 있으면 전부 미달로 보지 않는다`() {
        val categories = listOf(stat("증시", 2, 0))
        assertFalse(isAllBelowBar(ConceptStats(categories, weakest = null)))
    }

    @Test
    fun `기준 60퍼센트 이상이면 최저여도 지목하지 않는다`() {
        // 상대 기준이었다면 72% 가 "흔들려요"로 지목됐다 — 거짓이다.
        val categories = listOf(stat("금리", 20, 17), stat("증시", 25, 18)) // 85%, 72%
        assertTrue(weakConceptGroup(ConceptStats(categories, weakest = categories[1])).isEmpty())
    }

    @Test
    fun `정확히 60퍼센트는 기준 미달이 아니다`() {
        val categories = listOf(stat("금리", 10, 6), stat("증시", 100, 59)) // 60%, 59%
        val group = weakConceptGroup(ConceptStats(categories, weakest = categories[1]))
        assertEquals(listOf("증시"), group.map { it.displayName })
    }

    @Test
    fun `지목 대상이 셋을 넘으면 배너를 숨긴다`() {
        // 전반이 낮은 것이지 특정 개념이 약한 게 아니다 — 개념 진단으로 답할 문제가 아니다.
        // (기준 이상인 개념 하나를 섞어 '전부 미달' 경로와 구분한다.)
        val categories = listOf(stat("금리", 20, 17)) + (1..4).map { stat("개념$it", 10, 5) }
        assertTrue(weakConceptGroup(ConceptStats(categories, weakest = categories[1])).isEmpty())
    }

    @Test
    fun `서버가 표본 부족이라고 하면 지목하지 않는다`() {
        val categories = listOf(stat("금리", 20, 17))
        assertTrue(weakConceptGroup(ConceptStats(categories, weakest = null)).isEmpty())
    }
}
