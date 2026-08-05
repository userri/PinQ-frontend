package com.finq.app.ui.components

import com.finq.app.data.repository.ConceptStat
import com.finq.app.data.repository.ConceptStats
import org.junit.Assert.assertEquals
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
        val categories = listOf(stat("금리", 20, 17), stat("증시", 2, 0))
        val group = weakConceptGroup(ConceptStats(categories, weakest = categories[0]))
        assertEquals(listOf("금리"), group.map { it.displayName })
    }

    @Test
    fun `지목 대상이 셋을 넘으면 배너를 숨긴다`() {
        // 전반이 낮은 것이지 특정 개념이 약한 게 아니다 — 개념 진단으로 답할 문제가 아니다.
        val categories = (1..4).map { stat("개념$it", 10, 6) }
        assertTrue(weakConceptGroup(ConceptStats(categories, weakest = categories[0])).isEmpty())
    }

    @Test
    fun `서버가 표본 부족이라고 하면 지목하지 않는다`() {
        val categories = listOf(stat("금리", 20, 17))
        assertTrue(weakConceptGroup(ConceptStats(categories, weakest = null)).isEmpty())
    }
}
