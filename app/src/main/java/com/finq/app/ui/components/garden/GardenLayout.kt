package com.finq.app.ui.components.garden

import com.finq.app.data.repository.GardenItem
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewStage

/**
 * 정원 그림 위 항목 한 개의 배치.
 *
 * 좌표는 캔버스 크기 무관 0~1 분율 — 실제 픽셀 변환은 GardenCanvas 가 한다.
 *  - [quizId] null 이면 레거시 졸업분(이름 없는 나무) — 탭 불가.
 *  - [stage] null 이면 완성 나무(졸업).
 *  - [scale] 원근감 — 뒤(위)일수록 작게.
 */
data class GardenSlot(
    val xFrac: Float,
    val yFrac: Float,
    val quizId: Long?,
    val stage: ReviewStage?,
    val graduated: Boolean,
    val scale: Float,
)

/** [overflow] 는 슬롯이 모자라 그리지 못한 항목 수 — "+N" 표기에 쓴다. */
data class GardenLayoutResult(
    val slots: List<GardenSlot>,
    val overflow: Int,
)

private const val COLS = 6

/**
 * 정원 배치 — 입력만의 함수(결정적). 같은 정원이면 항상 같은 그림.
 *
 * 규칙:
 *  - 그리드 COLS 열 × 필요한 만큼의 행. 행이 뒤(작은 index)일수록 위·작게(원근).
 *  - 각 항목의 칸은 quizId 해시로 고르되 점유된 칸이면 다음 빈 칸으로(선형 탐사).
 *  - 레거시 졸업분(counter − graduated 목록)은 이름 없는 나무로 남은 칸을 채운다.
 *  - 슬롯 초과 시 우선순위: 자라는 중 > 졸업 목록 > 레거시. 초과분은 overflow.
 */
fun computeGardenLayout(garden: ReviewGarden, maxSlots: Int): GardenLayoutResult {
    val legacyCount = (garden.graduatedTrees - garden.graduated.size).coerceAtLeast(0)
    val total = garden.growing.size + garden.graduated.size + legacyCount
    val overflow = (total - maxSlots).coerceAtLeast(0)

    // 우선순위 순으로 자르고, 그리는 순서는 "뒤(나무)부터" — 앞의 새싹이 나무를 가리지 않게.
    var budget = maxSlots
    val growing = garden.growing.take(budget).also { budget -= it.size }
    val graduated = garden.graduated.take(budget).also { budget -= it.size }
    val legacy = legacyCount.coerceAtMost(budget)

    val rows = ((growing.size + graduated.size + legacy + COLS - 1) / COLS).coerceAtLeast(1)
    val occupied = BooleanArray(rows * COLS)

    fun place(seed: Long, preferBack: Boolean): Int {
        val start = ((seed % (rows * COLS)).toInt() + rows * COLS) % (rows * COLS)
        var cell = start
        while (occupied[cell]) cell = (cell + 1) % (rows * COLS)
        // 나무(preferBack)는 뒤 행 쪽, 새싹은 앞 행 쪽을 선호 — 행만 재배정하고 열은 유지.
        val col = cell % COLS
        val rowOrder = if (preferBack) (0 until rows) else (rows - 1 downTo 0)
        for (row in rowOrder) {
            val candidate = row * COLS + col
            if (!occupied[candidate]) { cell = candidate; break }
        }
        occupied[cell] = true
        return cell
    }

    fun slot(cell: Int, quizId: Long?, stage: ReviewStage?, graduated: Boolean, seed: Long): GardenSlot {
        val row = cell / COLS
        val col = cell % COLS
        // 같은 seed 면 같은 지터 — 격자 티를 없애되 결정성 유지.
        val jx = ((seed * 1103515245 + 12345) ushr 16 and 0xFF).toFloat() / 255f - 0.5f
        val jy = ((seed * 6364136223846793005 + 1442695040888963407) ushr 16 and 0xFF).toFloat() / 255f - 0.5f
        val backFrac = if (rows == 1) 1f else row.toFloat() / (rows - 1)  // 0=맨뒤, 1=맨앞
        return GardenSlot(
            xFrac = ((col + 0.5f + jx * 0.5f) / COLS).coerceIn(0f, 1f),
            yFrac = ((row + 0.5f + jy * 0.4f) / rows).coerceIn(0f, 1f),
            quizId = quizId,
            stage = stage,
            graduated = graduated,
            scale = 0.7f + 0.3f * backFrac,
        )
    }

    val slots = buildList {
        graduated.forEach { add(slot(place(it.quizId, preferBack = true), it.quizId, null, true, it.quizId)) }
        repeat(legacy) { i ->
            val seed = -(i + 1L)  // 레거시는 음수 시드 — quizId 와 충돌 없음.
            add(slot(place(seed, preferBack = true), null, null, true, seed))
        }
        growing.forEach { add(slot(place(it.quizId, preferBack = false), it.quizId, it.stage, false, it.quizId)) }
    }
    return GardenLayoutResult(slots = slots, overflow = overflow)
}
