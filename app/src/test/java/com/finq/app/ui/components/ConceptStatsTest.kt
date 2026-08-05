package com.finq.app.ui.components

import com.finq.app.data.repository.ConceptStat
import com.finq.app.data.repository.ConceptStats
import org.junit.Assert.assertEquals
import org.junit.Test

class ConceptStatsTest {

    private fun stat(name: String, total: Int, correct: Int) =
        ConceptStat(name, name, total, correct, correct.toFloat() / total)

    private fun diagnose(vararg categories: ConceptStat) =
        conceptDiagnosis(ConceptStats(categories.toList(), weakest = categories.lastOrNull()))

    private fun weakNames(d: ConceptDiagnosis) =
        (d as ConceptDiagnosis.Weak).concepts.map { it.displayName }

    @Test
    fun `기준 미달을 전부 지목한다 — 약한 순으로`() {
        // 25/43 = 58.14%, 7/12 = 58.33% — 화면엔 둘 다 58% 로 보인다.
        val d = diagnose(stat("금리", 20, 17), stat("부동산", 12, 7), stat("환율", 43, 25))
        assertEquals(listOf("환율", "부동산"), weakNames(d))
    }

    @Test
    fun `표본이 3 미만인 카테고리는 지목하지 않는다`() {
        // 2문제 틀렸다고 0% 로 지목하면 시작하자마자 질책이 된다.
        val d = diagnose(stat("금리", 20, 17), stat("부동산", 10, 5), stat("증시", 2, 0))
        assertEquals(listOf("부동산"), weakNames(d))
    }

    @Test
    fun `정확히 60퍼센트는 기준 미달이 아니다`() {
        val d = diagnose(stat("금리", 10, 6), stat("증시", 100, 59)) // 60%, 59%
        assertEquals(listOf("증시"), weakNames(d))
    }

    @Test
    fun `기준 이상이 하나라도 있으면 넷 이상도 그대로 지목한다`() {
        // 종전엔 넷을 넘으면 배너가 통째로 사라져 빨강 넷에 아무 말이 없었다.
        val d = diagnose(stat("금리", 20, 17), *(1..4).map { stat("개념$it", 10, 5) }.toTypedArray())
        assertEquals(4, (d as ConceptDiagnosis.Weak).concepts.size)
    }

    @Test
    fun `표본이 있는 개념이 전부 미달이면 AllBelow`() {
        // 화면이 통째로 붉어지면 빨강이 아무것도 구별해주지 못한다 — 경고색을 끄고 문구로 받는다.
        val d = diagnose(stat("금리", 27, 16), stat("증시", 22, 10), stat("환율", 17, 7))
        assertEquals(ConceptDiagnosis.AllBelow, d)
    }

    @Test
    fun `전부 기준 이상이면 AllGood`() {
        assertEquals(ConceptDiagnosis.AllGood, diagnose(stat("금리", 20, 17), stat("증시", 10, 6)))
    }

    @Test
    fun `표본 3 이상인 개념이 없으면 NotEnough`() {
        assertEquals(ConceptDiagnosis.NotEnough, diagnose(stat("증시", 2, 0)))
    }

    @Test
    fun `카테고리가 비어도 NotEnough`() {
        assertEquals(ConceptDiagnosis.NotEnough, conceptDiagnosis(ConceptStats(emptyList(), null)))
    }
}
