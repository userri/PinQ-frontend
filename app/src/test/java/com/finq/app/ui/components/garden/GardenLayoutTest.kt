package com.finq.app.ui.components.garden

import com.finq.app.data.repository.GardenItem
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GardenLayoutTest {

    private fun item(quizId: Long, stage: ReviewStage = ReviewStage.SPROUT, graduatedAt: String? = null) =
        GardenItem(
            quizId = quizId, categoryLabel = "경제", question = "q$quizId", keyword = null,
            stage = stage, dueDate = null, waterCount = 1, absorbedCount = 1,
            graduatedAtIso = graduatedAt,
        )

    private fun garden(growing: Int, graduated: Int, counter: Int = graduated) = ReviewGarden(
        growing = (1L..growing).map { item(it) },
        graduated = (100L until 100L + graduated).map { item(it, ReviewStage.ALMOST_TREE, "2026-07-19T12:00:00") },
        graduatedTrees = counter,
    )

    @Test
    fun `같은 입력이면 항상 같은 좌표`() {
        val a = computeGardenLayout(garden(growing = 5, graduated = 3), maxSlots = 24)
        val b = computeGardenLayout(garden(growing = 5, graduated = 3), maxSlots = 24)
        assertEquals(a, b)
    }

    @Test
    fun `모든 슬롯은 0~1 범위 안`() {
        val result = computeGardenLayout(garden(growing = 10, graduated = 10), maxSlots = 24)
        result.slots.forEach {
            assertTrue("x=${it.xFrac}", it.xFrac in 0f..1f)
            assertTrue("y=${it.yFrac}", it.yFrac in 0f..1f)
        }
    }

    @Test
    fun `두 슬롯이 같은 그리드 칸을 차지하지 않는다`() {
        val result = computeGardenLayout(garden(growing = 12, graduated = 12), maxSlots = 24)
        val keys = result.slots.map { it.xFrac to it.yFrac }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `카운터가 목록보다 크면 레거시 나무를 이름 없이 채운다`() {
        val result = computeGardenLayout(garden(growing = 0, graduated = 2, counter = 5), maxSlots = 24)
        val anonymous = result.slots.filter { it.graduated && it.quizId == null }
        assertEquals(3, anonymous.size)
        anonymous.forEach { assertNull(it.stage) }
    }

    @Test
    fun `슬롯 초과분은 overflow 로 보고한다`() {
        val result = computeGardenLayout(garden(growing = 20, graduated = 20), maxSlots = 12)
        assertEquals(12, result.slots.size)
        assertEquals(28, result.overflow)
    }

    @Test
    fun `초과 시 자라는 항목이 레거시 나무보다 우선 표시된다`() {
        // growing 8 + graduated 8 + legacy 4(counter 12), 슬롯 10
        val result = computeGardenLayout(garden(growing = 8, graduated = 8, counter = 12), maxSlots = 10)
        assertEquals(8, result.slots.count { !it.graduated })
    }
}
