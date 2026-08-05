package com.finq.app.ui.components.garden

import com.finq.app.data.repository.GardenItem
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GardenLayoutTest {

    private fun item(
        quizId: Long,
        stage: ReviewStage = ReviewStage.SPROUT,
        graduatedAt: String? = null,
        inTodayQueue: Boolean = false,
    ) =
        GardenItem(
            quizId = quizId, categoryLabel = "경제", question = "q$quizId", keyword = null,
            stage = stage, dueDate = null, waterCount = 1, absorbedCount = 1,
            graduatedAtIso = graduatedAt, inTodayQueue = inTodayQueue,
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

    /**
     * 앞줄은 [FRONT_MAX] 개만 개별 식물로 그리므로, 오늘 물 줄 항목이 여기 못 들어오면
     * 배지("오늘 물 줄 잔디 N개")와 후광 개수가 어긋난다 — 실제로 배지 5 vs 후광 1 이 났다.
     */
    @Test
    fun `오늘 물 줄 항목은 성장 순위와 무관하게 앞줄에 들어온다`() {
        // 전시 순위가 가장 낮은(새싹, 물 1번) 항목만 due 로 두고, 앞자리를 나무 직전으로 채운다.
        val due = item(999, ReviewStage.SPROUT, inTodayQueue = true)
        val others = (1L..30L).map { item(it, ReviewStage.ALMOST_TREE) }
        val scene = computeNightScene(
            ReviewGarden(growing = others + due, graduated = emptyList(), graduatedTrees = 0),
            LocalDate.of(2026, 8, 5),
        )
        assertTrue(scene.front.any { it.item.quizId == 999L })
        assertTrue(scene.front.first().item.quizId == 999L)
    }

    @Test
    fun `due 가 없으면 종전 전시 순서를 그대로 따른다`() {
        val items = listOf(
            item(1, ReviewStage.SPROUT),
            item(2, ReviewStage.ALMOST_TREE),
            item(3, ReviewStage.GRASS),
        )
        val scene = computeNightScene(
            ReviewGarden(growing = items, graduated = emptyList(), graduatedTrees = 0),
            LocalDate.of(2026, 8, 5),
        )
        assertEquals(listOf(2L, 3L, 1L), scene.front.map { it.item.quizId })
    }
}
