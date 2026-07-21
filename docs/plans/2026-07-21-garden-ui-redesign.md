# 정원 UI 재편 (잔디+나무 통합) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 정원을 "잔디 위에 나무가 자란 그림 한 장"(순수 시각 보상)으로 재편하고, 목록 기능은 오답노트 복습 필터칩으로 이관한다. 스펙: `docs/specs/2026-07-21-garden-ui-redesign-design.md`.

**Architecture:** 공유 `GardenCanvas` 컴포저블(Compose Canvas, compact/full 모드)을 신설하고 배치 계산은 순수 Kotlin(`GardenLayout`)으로 분리해 단위 테스트한다. 마이페이지의 잔디 카드를 통합 "정원 카드"로 교체, GardenScreen 은 풀스크린 시각 정원으로 재작성, 정원 나무 탭 → 보관함(오답노트) `focusQuizId` 딥링크. 백엔드 변경 0.

**Tech Stack:** Kotlin, Jetpack Compose (Material3, Canvas), Navigation-Compose, JUnit4 (기존 `testImplementation(libs.junit)` 만 사용).

## Global Constraints

- 레포: `/Users/iyr/SSAFY/PinQ-frontend`, 브랜치 `main`. **백엔드/서버 호출 변경 금지.**
- 색: `ui/theme/Color.kt` 의 역할 토큰만 사용. 유채색은 ① Lime ② Grass 램프(GrassEmpty/Grass1/Grass2/Grass3/GrassMax, `streakColor()`) ③ Error 뿐. **새 초록/파랑 raw Color 생성 금지.** 순백 텍스트 금지(TextPrimary 사용).
- Lime 은 넓은 면적 배경 금지(포인트만). Lime 위 텍스트는 OnLime.
- 주석·UI 문구는 한국어, 기존 파일들의 톤을 따른다.
- 커밋 메시지는 기존 컨벤션(`feat:`, `docs:` … 한국어 요약) + `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`.
- 검증 명령: 컴파일 `./gradlew :app:compileDebugKotlin`, 단위 테스트 `./gradlew :app:testDebugUnitTest`.
- `Date.now()` 류 비결정 요소를 배치 계산에 넣지 않는다 — 배치는 입력만의 함수.

---

### Task 1: GardenLayout — 결정적 배치 계산 (순수 Kotlin + 단위 테스트)

**Files:**
- Create: `app/src/main/java/com/finq/app/ui/components/garden/GardenLayout.kt`
- Test: `app/src/test/java/com/finq/app/ui/components/garden/GardenLayoutTest.kt`

**Interfaces:**
- Produces (Task 2 가 소비):
  - `data class GardenSlot(val xFrac: Float, val yFrac: Float, val quizId: Long?, val stage: ReviewStage?, val graduated: Boolean, val scale: Float)`
  - `data class GardenLayoutResult(val slots: List<GardenSlot>, val overflow: Int)`
  - `fun computeGardenLayout(garden: ReviewGarden, maxSlots: Int): GardenLayoutResult`
- Consumes: `ReviewGarden`, `GardenItem`, `ReviewStage` (`data/repository/ReviewRepository.kt`, 기존).

- [ ] **Step 1: 실패하는 테스트 작성**

`app/src/test/java/com/finq/app/ui/components/garden/GardenLayoutTest.kt`:

```kotlin
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
```

- [ ] **Step 2: 실패 확인**

Run: `cd /Users/iyr/SSAFY/PinQ-frontend && ./gradlew :app:testDebugUnitTest --tests "com.finq.app.ui.components.garden.GardenLayoutTest" 2>&1 | tail -20`
Expected: 컴파일 에러(FAIL) — `computeGardenLayout` 미정의.

- [ ] **Step 3: 구현**

`app/src/main/java/com/finq/app/ui/components/garden/GardenLayout.kt`:

```kotlin
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
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew :app:testDebugUnitTest --tests "com.finq.app.ui.components.garden.GardenLayoutTest" 2>&1 | tail -5`
Expected: `BUILD SUCCESSFUL`, 6 tests pass. 겹침 테스트가 실패하면 place() 의 선형 탐사/행 재배정 로직을 점검(재배정 후 occupied 마킹이 최종 cell 인지).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/finq/app/ui/components/garden/GardenLayout.kt app/src/test/java/com/finq/app/ui/components/garden/GardenLayoutTest.kt
git commit -m "feat: 정원 배치 계산(GardenLayout) — 결정적 슬롯 배치 + 테스트"
```

---

### Task 2: GardenCanvas — 잔디+나무 Canvas 드로잉 (compact/full)

**Files:**
- Create: `app/src/main/java/com/finq/app/ui/components/garden/GardenCanvas.kt`
- Modify: `app/src/debug/java/com/finq/app/debug/ShowcaseActivity.kt` (정원 캔버스 케이스 추가 — 기존 케이스 나열 방식 그대로 3케이스: 빈/성장/만원)

**Interfaces:**
- Consumes: `computeGardenLayout`, `GardenSlot`, `GardenLayoutResult` (Task 1), `ReviewGarden`, `ReviewStage`, 테마 토큰(`BgBase, BgSurface, BgElevated, Grass1, Grass2, Grass3, GrassMax, Lime, TextPrimary, TextMuted`).
- Produces (Task 4·5 가 소비):
  - `@Composable fun GardenCanvas(garden: ReviewGarden, modifier: Modifier = Modifier, compact: Boolean = false, onItemTap: ((Long) -> Unit)? = null)`
  - compact=true: 히트테스트 없음(부모가 카드 전체 clickable 처리), maxSlots=12.
  - compact=false: maxSlots=24, `onItemTap(quizId)` — quizId 있는 슬롯만.

- [ ] **Step 1: 구현** (드로잉은 TDD 대상이 아님 — 컴파일 + Preview 로 검증)

`app/src/main/java/com/finq/app/ui/components/garden/GardenCanvas.kt`:

```kotlin
package com.finq.app.ui.components.garden

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finq.app.data.repository.GardenItem
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewStage
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Grass1
import com.finq.app.ui.theme.Grass2
import com.finq.app.ui.theme.Grass3
import com.finq.app.ui.theme.GrassMax
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextMuted

private const val COMPACT_MAX_SLOTS = 12
private const val FULL_MAX_SLOTS = 24

/** 하늘(위 40%)과 잔디밭(아래 60%)의 경계 분율. */
private const val HORIZON = 0.40f

/**
 * 경제잔디 정원 — 잔디밭 위에 새싹/풀/나무가 자란 그림 한 장.
 *
 * 배치는 [computeGardenLayout] (결정적) — 같은 정원이면 항상 같은 그림.
 * 색은 잔디 램프(Grass1~GrassMax)와 Lime 포인트만 쓴다.
 *
 * @param compact 마이페이지 위젯 모드 — 슬롯 12개, 히트테스트 없음(부모가 clickable).
 * @param onItemTap full 모드에서 나무/새싹 탭 콜백(quizId). 레거시 나무는 콜백 없음.
 */
@Composable
fun GardenCanvas(
    garden: ReviewGarden,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onItemTap: ((Long) -> Unit)? = null,
) {
    val layout = remember(garden, compact) {
        computeGardenLayout(garden, if (compact) COMPACT_MAX_SLOTS else FULL_MAX_SLOTS)
    }
    val empty = layout.slots.isEmpty()

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!compact && onItemTap != null) {
                        Modifier.pointerInput(layout) {
                            awaitEachGestureTap { offset ->
                                hitTest(layout.slots, offset, size.width.toFloat(), size.height.toFloat())
                                    ?.let(onItemTap)
                            }
                        }
                    } else Modifier
                ),
        ) {
            drawSkyAndField()
            drawGrassTufts()
            // 뒤(작은 y)부터 그려 앞 항목이 자연스럽게 겹치게 한다.
            layout.slots.sortedBy { it.yFrac }.forEach { drawSlot(it) }
        }

        if (empty) {
            Text(
                text = "오답을 복습하면 나무가 자라요",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        if (layout.overflow > 0) {
            Text(
                text = "+${layout.overflow}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
            )
        }
    }
}

/** 탭 제스처 1회 감지 — detectTapGestures 의 얇은 래퍼(테스트 없는 UI 유틸). */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.awaitEachGestureTap(
    onTap: (Offset) -> Unit,
) = androidx.compose.foundation.gestures.detectTapGestures(onTap = onTap)

/** 슬롯 중심 기준 히트 반경 안의 가장 가까운 quizId 슬롯. 없으면 null. */
internal fun hitTest(
    slots: List<GardenSlot>,
    tap: Offset,
    width: Float,
    height: Float,
): Long? {
    val radius = width / 10f
    return slots
        .filter { it.quizId != null }
        .map { it to (Offset(it.xFrac * width, fieldY(it.yFrac, height)) - tap) }
        .minByOrNull { (_, d) -> d.getDistanceSquared() }
        ?.takeIf { (_, d) -> d.getDistance() <= radius }
        ?.first?.quizId
}

/** 배치 yFrac(0~1)을 잔디밭 밴드(HORIZON~0.95) 픽셀 y 로 사상. */
private fun fieldY(yFrac: Float, height: Float): Float =
    height * (HORIZON + yFrac * (0.95f - HORIZON))

private fun DrawScope.drawSkyAndField() {
    // 하늘 — 네이비 그라데이션(테마 배경 계열만).
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(BgElevated, BgBase),
            endY = size.height * HORIZON,
        ),
        size = Size(size.width, size.height * HORIZON),
    )
    // 잔디밭 — 짙은→밝은 초록 수직 그라데이션.
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Grass1, Grass2),
            startY = size.height * HORIZON,
            endY = size.height,
        ),
        topLeft = Offset(0f, size.height * HORIZON),
        size = Size(size.width, size.height * (1f - HORIZON)),
    )
}

/** 풀결 텍스처 — 고정 패턴(결정적)의 작은 세로선들. */
private fun DrawScope.drawGrassTufts() {
    val n = 40
    repeat(n) { i ->
        val x = size.width * ((i * 37 % n) + 0.5f) / n
        val yf = ((i * 17 % n).toFloat() / n)
        val y = fieldY(yf, size.height)
        val h = size.height * 0.018f * (0.7f + 0.3f * yf)
        drawLine(
            color = Grass3.copy(alpha = 0.35f),
            start = Offset(x, y),
            end = Offset(x, y - h),
            strokeWidth = size.width / 300f,
        )
    }
}

private fun DrawScope.drawSlot(slot: GardenSlot) {
    val cx = slot.xFrac * size.width
    val cy = fieldY(slot.yFrac, size.height)
    val unit = size.height * 0.055f * slot.scale  // 기본 치수 단위

    when {
        slot.graduated -> drawTree(cx, cy, unit)
        slot.stage == ReviewStage.SPROUT -> drawSprout(cx, cy, unit)
        slot.stage == ReviewStage.GRASS -> drawBush(cx, cy, unit)
        else -> drawSapling(cx, cy, unit)  // ALMOST_TREE
    }
}

/** 완성 나무 — 줄기 + 3단 캐노피. 캐노피 하이라이트만 Lime 포인트. */
private fun DrawScope.drawTree(cx: Float, cy: Float, unit: Float) {
    drawRoundRect(
        color = Grass1,
        topLeft = Offset(cx - unit * 0.14f, cy - unit * 1.2f),
        size = Size(unit * 0.28f, unit * 1.2f),
        cornerRadius = CornerRadius(unit * 0.1f),
    )
    drawCircle(color = Grass2, radius = unit * 0.75f, center = Offset(cx, cy - unit * 1.5f))
    drawCircle(color = Grass3, radius = unit * 0.55f, center = Offset(cx - unit * 0.45f, cy - unit * 1.2f))
    drawCircle(color = Grass3, radius = unit * 0.55f, center = Offset(cx + unit * 0.45f, cy - unit * 1.2f))
    drawCircle(color = GrassMax, radius = unit * 0.30f, center = Offset(cx + unit * 0.25f, cy - unit * 1.75f))
    drawCircle(color = Lime.copy(alpha = 0.9f), radius = unit * 0.10f, center = Offset(cx - unit * 0.2f, cy - unit * 1.85f))
}

/** 새싹(stage 0) — 짧은 줄기 + 떡잎 두 장. */
private fun DrawScope.drawSprout(cx: Float, cy: Float, unit: Float) {
    drawLine(color = Grass2, start = Offset(cx, cy), end = Offset(cx, cy - unit * 0.5f), strokeWidth = unit * 0.1f)
    drawCircle(color = Grass3, radius = unit * 0.22f, center = Offset(cx - unit * 0.2f, cy - unit * 0.55f))
    drawCircle(color = Grass3, radius = unit * 0.22f, center = Offset(cx + unit * 0.2f, cy - unit * 0.55f))
}

/** 풀(stage 1) — 잎 3갈래 부채꼴. */
private fun DrawScope.drawBush(cx: Float, cy: Float, unit: Float) {
    val path = Path().apply {
        moveTo(cx, cy)
        quadraticTo(cx - unit * 0.6f, cy - unit * 0.5f, cx - unit * 0.35f, cy - unit * 0.9f)
        quadraticTo(cx - unit * 0.05f, cy - unit * 0.5f, cx, cy)
        moveTo(cx, cy)
        quadraticTo(cx, cy - unit * 0.7f, cx, cy - unit * 1.05f)
        quadraticTo(cx + unit * 0.08f, cy - unit * 0.6f, cx, cy)
        moveTo(cx, cy)
        quadraticTo(cx + unit * 0.6f, cy - unit * 0.5f, cx + unit * 0.35f, cy - unit * 0.9f)
        quadraticTo(cx + unit * 0.05f, cy - unit * 0.5f, cx, cy)
    }
    drawPath(path, color = Grass3)
}

/** 나무 직전(stage 2) — 가는 줄기 + 작은 캐노피. */
private fun DrawScope.drawSapling(cx: Float, cy: Float, unit: Float) {
    drawLine(color = Grass1, start = Offset(cx, cy), end = Offset(cx, cy - unit * 0.9f), strokeWidth = unit * 0.14f)
    drawCircle(color = Grass2, radius = unit * 0.45f, center = Offset(cx, cy - unit * 1.1f))
    drawCircle(color = Grass3, radius = unit * 0.28f, center = Offset(cx + unit * 0.15f, cy - unit * 1.25f))
}

@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun GardenCanvasCompactPreview() {
    FinQTheme {
        GardenCanvas(
            garden = previewGarden(),
            compact = true,
            modifier = Modifier.fillMaxWidth().height(160.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF081A2E, heightDp = 640)
@Composable
private fun GardenCanvasFullPreview() {
    FinQTheme {
        GardenCanvas(
            garden = previewGarden(),
            modifier = Modifier.fillMaxSize(),
            onItemTap = {},
        )
    }
}

private fun previewGarden(): ReviewGarden {
    fun g(id: Long, stage: ReviewStage) = GardenItem(
        quizId = id, categoryLabel = "경제", question = "q$id", keyword = null,
        stage = stage, dueDate = null, waterCount = 2, absorbedCount = 1, graduatedAtIso = null,
    )
    return ReviewGarden(
        growing = listOf(
            g(1, ReviewStage.SPROUT), g(2, ReviewStage.GRASS), g(3, ReviewStage.ALMOST_TREE),
            g(4, ReviewStage.SPROUT), g(5, ReviewStage.GRASS),
        ),
        graduated = listOf(
            g(101, ReviewStage.ALMOST_TREE).copy(graduatedAtIso = "2026-07-19T12:00:00"),
            g(102, ReviewStage.ALMOST_TREE).copy(graduatedAtIso = "2026-07-20T12:00:00"),
        ),
        graduatedTrees = 4,
    )
}
```

주의(구현자): `detectTapGestures` 래퍼(`awaitEachGestureTap`)가 어색하면 Canvas 의 `pointerInput` 안에서 `detectTapGestures(onTap = …)` 를 직접 호출하는 형태로 바꿔도 된다 — 시그니처(GardenCanvas 파라미터)만 유지할 것. `quadraticTo` 가 없는 Compose 버전이면 `quadraticBezierTo` 사용.

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew :app:compileDebugKotlin 2>&1 | tail -5`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Showcase 케이스 추가**

`ShowcaseActivity.kt` 의 기존 케이스 나열부에(파일을 열어 기존 항목 추가 패턴 그대로) 3케이스 추가 — 각각 `GardenCanvas(garden = …, compact = true, modifier = Modifier.fillMaxWidth().height(160.dp))`:
1. "정원 · 빈": `ReviewGarden.EMPTY`
2. "정원 · 성장": Step 1 의 `previewGarden()` 과 동일 구성 데이터를 인라인으로
3. "정원 · 만원(+N)": growing 15개 + graduatedTrees 20 (overflow 표기 확인)

- [ ] **Step 4: 디버그 빌드 확인 후 Commit**

Run: `./gradlew :app:compileDebugKotlin 2>&1 | tail -3` → `BUILD SUCCESSFUL`

```bash
git add app/src/main/java/com/finq/app/ui/components/garden/GardenCanvas.kt app/src/debug/java/com/finq/app/debug/ShowcaseActivity.kt
git commit -m "feat: GardenCanvas — 잔디밭 위 새싹/풀/나무 Canvas 드로잉 (compact/full)"
```

---

### Task 3: 오답노트 복습 필터칩 (전체/오답만/복습중/졸업)

**Files:**
- Create: `app/src/main/java/com/finq/app/ui/library/ReviewFilter.kt`
- Modify: `app/src/main/java/com/finq/app/ui/library/LibraryScreens.kt` (LibraryListScreen 에 필터 슬롯 추가)
- Modify: `app/src/main/java/com/finq/app/ui/library/WrongNoteTabRoute.kt` (칩 상태 + 적용)
- Test: `app/src/test/java/com/finq/app/ui/library/ReviewFilterTest.kt`

**Interfaces:**
- Consumes: `AttemptItem`, `ReviewStatus` (`data/model/AttemptItem.kt`, 기존).
- Produces:
  - `enum class ReviewFilter(val label: String) { ALL("전체"), NOT_STARTED("오답만"), GROWING("복습중"), GRADUATED("졸업🌳") }`
  - `fun List<AttemptItem>.applyReviewFilter(filter: ReviewFilter): List<AttemptItem>`
  - `LibraryListScreen` 신규 파라미터: `extraFilterRow: (@Composable () -> Unit)? = null` (카테고리칩 아래 렌더).

- [ ] **Step 1: 실패하는 테스트 작성**

`app/src/test/java/com/finq/app/ui/library/ReviewFilterTest.kt`:

```kotlin
package com.finq.app.ui.library

import com.finq.app.data.model.AttemptItem
import com.finq.app.data.model.Category
import com.finq.app.data.model.ReviewStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewFilterTest {

    private fun item(quizId: Long, review: ReviewStatus?) = AttemptItem(
        quizId = quizId, category = Category.selectable.first(), question = "q",
        choices = emptyList(), selectedChoiceId = null, correctChoiceId = 1L,
        correct = false, explanation = "", keyword = null, article = null,
        bookmarked = false, solvedAtIso = null, review = review,
    )

    private val notStarted = item(1, review = null)
    private val growing = item(2, ReviewStatus(stage = 1, waterCount = 2, absorbedCount = 1, graduated = false, dueDateIso = null))
    private val graduated = item(3, ReviewStatus(stage = 2, waterCount = 5, absorbedCount = 3, graduated = true, dueDateIso = null))
    private val all = listOf(notStarted, growing, graduated)

    @Test
    fun `전체는 그대로`() = assertEquals(all, all.applyReviewFilter(ReviewFilter.ALL))

    @Test
    fun `오답만 - 복습 큐 미진입`() =
        assertEquals(listOf(notStarted), all.applyReviewFilter(ReviewFilter.NOT_STARTED))

    @Test
    fun `복습중 - 진입했고 미졸업`() =
        assertEquals(listOf(growing), all.applyReviewFilter(ReviewFilter.GROWING))

    @Test
    fun `졸업 - graduated true`() =
        assertEquals(listOf(graduated), all.applyReviewFilter(ReviewFilter.GRADUATED))
}
```

주의: `Category.selectable.first()` 가 없으면 `Category` enum 을 열어 임의 값(예: `Category.INTEREST_RATE` 등 실제 첫 항목)으로 대체.

- [ ] **Step 2: 실패 확인**

Run: `./gradlew :app:testDebugUnitTest --tests "com.finq.app.ui.library.ReviewFilterTest" 2>&1 | tail -10`
Expected: 컴파일 FAIL — `ReviewFilter` 미정의.

- [ ] **Step 3: ReviewFilter 구현**

`app/src/main/java/com/finq/app/ui/library/ReviewFilter.kt`:

```kotlin
package com.finq.app.ui.library

import com.finq.app.data.model.AttemptItem

/**
 * 오답노트 복습 상태 필터 — 옛 정원 목록 기능의 이관처.
 *
 *  - 오답만: 아직 복습 큐에 오르지 않은 오답 (review == null)
 *  - 복습중: 물 주는 중 (자라는 새싹/풀/나무직전)
 *  - 졸업: 다 키운 나무 — 복습 큐에 다시 나오지 않는다
 */
enum class ReviewFilter(val label: String) {
    ALL("전체"),
    NOT_STARTED("오답만"),
    GROWING("복습중"),
    GRADUATED("졸업🌳"),
}

fun List<AttemptItem>.applyReviewFilter(filter: ReviewFilter): List<AttemptItem> = when (filter) {
    ReviewFilter.ALL -> this
    ReviewFilter.NOT_STARTED -> filter { it.review == null }
    ReviewFilter.GROWING -> filter { it.review != null && !it.review.graduated }
    ReviewFilter.GRADUATED -> filter { it.review?.graduated == true }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew :app:testDebugUnitTest --tests "com.finq.app.ui.library.ReviewFilterTest" 2>&1 | tail -5`
Expected: `BUILD SUCCESSFUL`, 4 pass.

- [ ] **Step 5: LibraryListScreen 슬롯 + WrongNoteTabRoute 칩 UI**

`LibraryScreens.kt` — `LibraryListScreen` 파라미터에 추가(카테고리칩 Row 렌더 직후 호출):

```kotlin
    /** 카테고리칩 아래 추가 필터 Row (오답노트의 복습 필터칩). null 이면 없음. */
    extraFilterRow: (@Composable () -> Unit)? = null,
```

```kotlin
        // 카테고리 필터칩
        CategoryFilterRow(
            selected = selectedCategory,
            onSelect = { selectedCategory = it },
        )
        extraFilterRow?.invoke()
```

`WrongNoteTabRoute.kt` — 상태와 칩 Row 를 추가하고 items 에 필터 적용:

```kotlin
@Composable
fun WrongNoteTabRoute(
    viewModel: LibraryViewModel,
    snackbarHostState: SnackbarHostState? = null,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var reviewFilter by remember { mutableStateOf(ReviewFilter.ALL) }

    LaunchedEffect(Unit) { viewModel.loadWrongNotes() }
    // (기존 toggleError LaunchedEffect 유지)

    val filtered = remember(state.wrongNotes, reviewFilter) {
        state.wrongNotes.applyReviewFilter(reviewFilter)
    }

    LibraryListScreen(
        title = "오답노트",
        subtitle = if (filtered.isEmpty()) "" else "${filtered.size}문제",
        items = filtered,
        isLoading = state.isLoadingWrong,
        error = state.wrongError,
        emptyMessage = when (reviewFilter) {
            ReviewFilter.ALL -> "오답이 없어요"
            ReviewFilter.NOT_STARTED -> "복습을 기다리는 오답이 없어요"
            ReviewFilter.GROWING -> "자라는 중인 복습이 없어요"
            ReviewFilter.GRADUATED -> "아직 완성한 나무가 없어요"
        },
        emptyIconRes = R.drawable.ic_trophy,
        onRetry = viewModel::loadWrongNotes,
        onToggleBookmark = { item -> viewModel.toggleBookmark(item.quizId, item.bookmarked) },
        extraFilterRow = {
            ReviewFilterRow(selected = reviewFilter, onSelect = { reviewFilter = it })
        },
        modifier = modifier,
    )
}

/** 복습 상태 칩 Row — CategoryFilterRow 와 같은 시각 언어(라임 선택칩). */
@Composable
private fun ReviewFilterRow(selected: ReviewFilter, onSelect: (ReviewFilter) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 10.dp),
    ) {
        items(ReviewFilter.entries) { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Lime else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    text = filter.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) OnLime else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}
```

(필요 import: `androidx.compose.foundation.background/clickable/layout.*/lazy.LazyRow/lazy.items/shape.RoundedCornerShape`, `androidx.compose.runtime.*`, `androidx.compose.ui.draw.clip`, `androidx.compose.ui.text.font.FontWeight`, `androidx.compose.ui.unit.dp`, `com.finq.app.ui.theme.Lime/OnLime`.)

- [ ] **Step 6: 컴파일 + 전체 테스트**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest 2>&1 | tail -5`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/finq/app/ui/library/ app/src/test/java/com/finq/app/ui/library/
git commit -m "feat: 오답노트 복습 필터칩(전체/오답만/복습중/졸업) — 정원 목록 기능 이관"
```

---

### Task 4: 마이페이지 통합 정원 카드

**Files:**
- Create: `app/src/main/java/com/finq/app/ui/components/garden/GardenSection.kt`
- Modify: `app/src/main/java/com/finq/app/ui/mypage/MyPageViewModel.kt` (garden 로드 추가)
- Modify: `app/src/main/java/com/finq/app/ui/screen/MyPageScreen.kt` (잔디 블록 교체)
- Modify: `app/src/main/java/com/finq/app/ui/navigation/FinQNavigation.kt` (MyPageViewModel factory 에 reviewRepository 전달)
- Modify: `app/src/main/java/com/finq/app/ui/components/GrassCalendarCard.kt` (카드 프레임 없는 본문 컴포저블 분리)

**Interfaces:**
- Consumes: `GardenCanvas` (Task 2), `GrassCalendar`, `ReviewGarden`, `ReviewRepository.getGarden()` (기존).
- Produces:
  - `@Composable fun GardenSection(grass: GrassCalendar?, grassFailed: Boolean, garden: ReviewGarden?, onRetryGrass: () -> Unit, onOpenGarden: () -> Unit, modifier: Modifier = Modifier)` — 마이페이지용 통합 카드.
  - `GrassCalendarCard.kt` 에 `@Composable fun GrassCalendarBody(grass: GrassCalendar, onDaySelected…)` 형태로 카드 프레임(Card/헤더) 없는 본문(요약칩+격자+범례+상세) 분리. 기존 `GrassCalendarCard` 는 본문을 감싸는 형태로 유지(다른 호출처 보호).
  - `MyPageUiState` 에 `val garden: ReviewGarden? = null` 추가, `MyPageViewModel.factory(statsRepository, notificationRepository, reviewRepository)` 시그니처 변경.

- [ ] **Step 1: MyPageViewModel 에 garden 로드 추가**

`MyPageUiState` 에 필드 추가:

```kotlin
    /** 정원(자라는 새싹/나무). null 이면 로딩 전 — 캔버스 자리는 스켈레톤. 실패해도 조용히(부가 정보). */
    val garden: ReviewGarden? = null,
```

`MyPageViewModel`: 생성자에 `private val reviewRepository: ReviewRepository` 추가, `refresh()` 에 `loadGarden()` 호출 추가, 메서드 추가:

```kotlin
    /** 정원 — 부가 정보. 실패 시 이전 값 유지(조용한 실패), 첫 실패는 캔버스 없이 잔디만 그린다. */
    fun loadGarden() {
        viewModelScope.launch {
            runCatching { reviewRepository.getGarden() }
                .onSuccess { garden -> _uiState.update { it.copy(garden = garden) } }
                .onFailure { }
        }
    }
```

companion factory 를 3-인자로 변경하고, `FinQNavigation.kt` 의 `MyPageViewModel.factory(statsRepository, notificationRepository)` 호출부를 `MyPageViewModel.factory(statsRepository, notificationRepository, reviewRepository)` 로 수정 (해당 composable 스코프에 이미 `reviewRepository` 가 있음 — GARDEN 라우트에서 쓰는 것과 동일 인스턴스. 없으면 NavHost 상위에서 내려오는 파라미터를 확인해 같은 것을 사용).

- [ ] **Step 2: GrassCalendarBody 분리**

`GrassCalendarCard.kt` 에서 기존 `GrassCalendarCard` 의 Card 내부(헤더 Row 제외: GrassSummaryRow + 그리드 + Legend + DayDetail)를 `GrassCalendarBody(grass: GrassCalendar)` 로 추출. `GrassCalendarCard` 는 기존 시그니처 그대로 Card+헤더+`GrassCalendarBody` 를 조합해 동작 불변.

- [ ] **Step 3: GardenSection 작성**

`app/src/main/java/com/finq/app/ui/components/garden/GardenSection.kt`:

```kotlin
package com.finq.app.ui.components.garden

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finq.app.data.repository.GrassCalendar
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.ui.components.GrassCalendarBody
import com.finq.app.ui.components.GrassCalendarError
import com.finq.app.ui.components.GrassCalendarSkeleton
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary

/**
 * 마이페이지 통합 "정원" 카드 — 위에는 정원 그림(잔디+나무), 아래에는 연간 잔디 그리드.
 * "경제잔디" 성장 메타포가 물리적으로 한 화면에 모인다.
 *
 * 정원 그림 탭 → 풀스크린 정원. 잔디 로드 실패 시 기존 재시도 카드로 폴백.
 */
@Composable
fun GardenSection(
    grass: GrassCalendar?,
    grassFailed: Boolean,
    garden: ReviewGarden?,
    onRetryGrass: () -> Unit,
    onOpenGarden: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (grass == null) {
        if (grassFailed) GrassCalendarError(onRetry = onRetryGrass, modifier = modifier)
        else GrassCalendarSkeleton(modifier = modifier)
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Outline),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "정원",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(
                    text = "🌳 키운 나무 ${grass.graduatedTrees}그루 →",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.clickable(onClick = onOpenGarden),
                )
            }
            Spacer(Modifier.height(12.dp))

            // 정원 그림 — garden 로딩 전엔 은은한 자리 표시(레이아웃 점프 방지).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgElevated)
                    .clickable(onClick = onOpenGarden),
            ) {
                if (garden != null) {
                    GardenCanvas(
                        garden = garden,
                        compact = true,
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            GrassCalendarBody(grass = grass)
        }
    }
}
```

- [ ] **Step 4: MyPageScreen 교체**

`MyPageScreen.kt` 의 잔디 블록(현재 298~300행 부근):

```kotlin
            grass != null -> GrassCalendarCard(grass = grass, onTreesClick = onOpenGarden)
            grassFailed -> GrassCalendarError(onRetry = onRetryGrass)
            else -> GrassCalendarSkeleton()
```

을 다음으로 교체(when 없이 단일 호출 — 분기는 GardenSection 내부가 담당):

```kotlin
        GardenSection(
            grass = grass,
            grassFailed = grassFailed,
            garden = garden,
            onRetryGrass = onRetryGrass,
            onOpenGarden = onOpenGarden,
        )
```

`MyPageScreen`/`MyPageContent` 시그니처에 `garden: ReviewGarden? = null` 파라미터를 추가하고 체인으로 전달, `FinQNavigation.kt` 의 `MyPageScreen(...)` 호출에 `garden = state.garden,` 추가. 기존 `GrassCalendarCard` import 는 미사용이 되면 제거.

- [ ] **Step 5: 컴파일 + 테스트 + Commit**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest 2>&1 | tail -5` → `BUILD SUCCESSFUL`

```bash
git add app/src/main/java/com/finq/app/ui/components/ app/src/main/java/com/finq/app/ui/mypage/ app/src/main/java/com/finq/app/ui/screen/MyPageScreen.kt app/src/main/java/com/finq/app/ui/navigation/FinQNavigation.kt
git commit -m "feat: 마이페이지 통합 정원 카드 — 정원 그림 + 연간 잔디 한 카드"
```

---

### Task 5: GardenScreen 재탄생 — 풀스크린 시각 정원

**Files:**
- Modify: `app/src/main/java/com/finq/app/ui/screen/GardenScreen.kt` (전면 재작성)

**Interfaces:**
- Consumes: `GardenCanvas` full 모드 (Task 2), `GardenViewModel`(기존 그대로).
- Produces (Task 6 이 소비): `GardenScreen` 신규 시그니처 —

```kotlin
@Composable
fun GardenScreen(
    garden: ReviewGarden?,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    /** 나무/새싹 탭 → 오답노트 해당 문제로. */
    onOpenQuiz: (Long) -> Unit,
    modifier: Modifier = Modifier,
)
```

- [ ] **Step 1: 재작성**

기존 파일의 목록 관련 코드(GardenContent 의 LazyColumn, GardenItemCard, SectionTitle, formatGraduatedDate, GARDEN_DATE_FORMAT)를 삭제하고 다음 구조로 교체. 상단 바·로딩·에러 분기는 기존 코드 그대로 유지:

```kotlin
/**
 * 정원 — 잔디 위에 나무가 자란 그림 한 장 (순수 시각 보상).
 *
 * 목록 기능은 오답노트 복습 필터칩으로 이관됐다. 나무/새싹 탭 → 오답노트 해당 문제.
 * "총 몇 그루"는 항상 [ReviewGarden.graduatedTrees] 카운터가 진실.
 */
@Composable
fun GardenScreen(
    garden: ReviewGarden?,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onOpenQuiz: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase),
    ) {
        // ── 상단 바 ── (기존 Row 그대로: ← + "정원")
        …

        when {
            isLoading -> …  // 기존 그대로 (CircularProgressIndicator color = Lime)
            error != null -> …  // 기존 그대로 (재시도 버튼)
            garden != null -> Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "자라는 중 ${garden.growing.size} · 🌳 키운 나무 ${garden.graduatedTrees}그루",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
                Text(
                    text = "나무를 누르면 그 문제의 오답노트로 가요",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(8.dp))
                GardenCanvas(
                    garden = garden,
                    onItemTap = onOpenQuiz,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
            }
        }
    }
}
```

Preview 는 GardenCanvas 의 previewGarden 과 같은 방식의 인라인 데이터로 갱신(기존 Preview 의 GardenItem 재사용 가능). `onOpenQuiz = {}` 추가.

- [ ] **Step 2: 호출부 임시 컴파일 보정**

`FinQNavigation.kt` GARDEN 라우트의 `GardenScreen(...)` 호출에 `onOpenQuiz = {}` 를 추가해 컴파일 유지 (실제 딥링크 연결은 Task 6).

- [ ] **Step 3: 컴파일 + Commit**

Run: `./gradlew :app:compileDebugKotlin 2>&1 | tail -3` → `BUILD SUCCESSFUL`

```bash
git add app/src/main/java/com/finq/app/ui/screen/GardenScreen.kt app/src/main/java/com/finq/app/ui/navigation/FinQNavigation.kt
git commit -m "feat: GardenScreen 재탄생 — 목록 제거, 풀스크린 시각 정원"
```

---

### Task 6: 정원 → 오답노트 focusQuizId 딥링크

**Files:**
- Modify: `app/src/main/java/com/finq/app/ui/navigation/FinQNavigation.kt`
- Modify: `app/src/main/java/com/finq/app/ui/library/LibraryTabScreen.kt`
- Modify: `app/src/main/java/com/finq/app/ui/library/WrongNoteTabRoute.kt`
- Modify: `app/src/main/java/com/finq/app/ui/library/LibraryScreens.kt`
- Modify: `app/src/main/java/com/finq/app/ui/library/AttemptItemCard.kt`

**Interfaces:**
- Consumes: Task 5 의 `onOpenQuiz`, Task 3 의 `extraFilterRow`/`ReviewFilter`.
- Produces:
  - 라우트 패턴 `"library_tab?focusQuizId={focusQuizId}"` (`FinQRoutes.LIBRARY_TAB` 상수는 `"library_tab"` 유지 — 탭 이동·bottomNav 는 기존 문자열로 navigate 하면 optional 인자 defaultValue 로 매칭된다).
  - `LibraryTabScreen(focusQuizId: Long? = null, …)` → `WrongNoteTabRoute(focusQuizId: Long? = null, …)` → `LibraryListScreen(focusQuizId: Long? = null, …)` → `AttemptItemCard(initialExpanded: Boolean = false, …)`.

- [ ] **Step 1: 라우트 패턴 + GARDEN 딥링크 연결**

`FinQRoutes` 에 추가:

```kotlin
    /** 보관함 탭 optional 인자 — 정원 나무 탭 딥링크용. */
    const val LIBRARY_TAB_PATTERN = "library_tab?focusQuizId={focusQuizId}"
```

`composable(FinQRoutes.LIBRARY_TAB)` 를 다음으로 변경:

```kotlin
            composable(
                route = FinQRoutes.LIBRARY_TAB_PATTERN,
                arguments = listOf(navArgument("focusQuizId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }),
            ) { entry ->
                val focusQuizId = entry.arguments?.getLong("focusQuizId")
                    ?.takeIf { it > 0 }
                val libraryVm = libraryViewModel(libraryRepository)
                LibraryTabScreen(
                    wrongNoteViewModel = libraryVm,
                    bookmarkViewModel  = libraryVm,
                    historyViewModel   = libraryVm,
                    snackbarHostState  = snackbarHostState,
                    onStartQuiz = { navController.resumeOrStartSession() },
                    focusQuizId = focusQuizId,
                )
            }
```

(import: `androidx.navigation.NavType`, `androidx.navigation.navArgument`.)

**중요 — 라우트 매칭 3곳 확인:**
1. `bottomNavRoutes` set: `FinQRoutes.LIBRARY_TAB` 항목을 `FinQRoutes.LIBRARY_TAB_PATTERN` 으로 교체 (currentRoute 는 패턴 문자열로 온다).
2. bottomNavItems 등 `navigate(FinQRoutes.LIBRARY_TAB)` 호출은 그대로 둔다 — optional 인자라 매칭된다.
3. `pauseSessionToLibrary()` 의 `navigate(FinQRoutes.LIBRARY_TAB)` 도 그대로.

GARDEN 라우트의 `onOpenQuiz` 를 실제 연결:

```kotlin
                    onOpenQuiz = { quizId ->
                        navController.navigate("library_tab?focusQuizId=$quizId") {
                            launchSingleTop = true
                        }
                    },
```

- [ ] **Step 2: LibraryTabScreen → WrongNoteTabRoute 전달**

`LibraryTabScreen` 에 `focusQuizId: Long? = null` 추가. focus 진입이면 오답노트 페이지(0)가 초기 페이지이므로 pager 는 그대로. `WrongNoteTabRoute(…, focusQuizId = focusQuizId)` 로 전달.

`WrongNoteTabRoute`: `focusQuizId: Long? = null` 파라미터 추가. focus 가 있으면 필터를 "전체"로 시작(졸업 항목도 보이도록 — 이미 초기값이 ALL 이므로 추가 코드 없음). `LibraryListScreen(…, focusQuizId = focusQuizId)` 전달.

- [ ] **Step 3: LibraryListScreen 스크롤 + 카드 펼침**

`LibraryListScreen` 에 `focusQuizId: Long? = null` 추가. LazyColumn 에 state 를 달고 진입 시 1회 스크롤:

```kotlin
    val listState = rememberLazyListState()
    // 정원 딥링크 — 목록이 준비되면 해당 문제로 1회 스크롤. 목록에 없으면 조용히 무시.
    LaunchedEffect(focusQuizId, items) {
        val index = focusQuizId?.let { id -> filtered.indexOfFirst { it.quizId == id } } ?: -1
        if (index >= 0) listState.animateScrollToItem(index)
    }
```

LazyColumn 에 `state = listState` 추가, item 렌더에:

```kotlin
                items(filtered, key = { it.quizId }) { item ->
                    AttemptItemCard(
                        item = item,
                        onToggleBookmark = { onToggleBookmark(item) },
                        onStartQuiz = onStartQuiz?.let { cb -> { cb(item) } },
                        initialExpanded = item.quizId == focusQuizId,
                    )
                }
```

(import `androidx.compose.foundation.lazy.rememberLazyListState`, `androidx.compose.runtime.LaunchedEffect`.)

- [ ] **Step 4: AttemptItemCard 초기 펼침**

```kotlin
fun AttemptItemCard(
    item: AttemptItem,
    onToggleBookmark: () -> Unit,
    onStartQuiz: (() -> Unit)? = null,
    /** 정원 딥링크로 진입한 카드 — 처음부터 펼쳐 보여준다. */
    initialExpanded: Boolean = false,
) {
    var expanded by remember(item.quizId) { mutableStateOf(initialExpanded) }
```

- [ ] **Step 5: 컴파일 + 전체 테스트 + Commit**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest 2>&1 | tail -5` → `BUILD SUCCESSFUL`

```bash
git add app/src/main/java/com/finq/app/ui/navigation/FinQNavigation.kt app/src/main/java/com/finq/app/ui/library/
git commit -m "feat: 정원 나무 탭 → 오답노트 focusQuizId 딥링크 (스크롤+카드 펼침)"
```

---

### Task 7: 최종 검증 + 마감

**Files:** 없음 (검증만; 발견된 결함은 해당 파일 수정)

- [ ] **Step 1: 전체 빌드·테스트**

Run: `./gradlew :app:assembleDebug :app:testDebugUnitTest 2>&1 | tail -5`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: 수동 체인 검증** (에뮬레이터/기기; 불가하면 @Preview 4종 — GardenCanvas compact/full, GardenSection, GardenScreen — 렌더 확인으로 대체하고 그 사실을 보고)

1. 마이페이지 → 통합 정원 카드에 정원 그림+잔디 그리드가 한 카드로 보이는가
2. 정원 그림/"키운 나무 →" 탭 → 풀스크린 정원
3. 나무 탭 → 보관함 오답노트 탭, 해당 문제로 스크롤·펼침
4. 오답노트 필터칩 4종 동작, 빈 상태 문구 확인
5. 하단 네비 "보관함" 탭이 여전히 활성 표시되는가 (라우트 패턴 변경 회귀 확인 — 가장 위험한 지점)

- [ ] **Step 3: Push**

```bash
git push
```

(프론트 온리 — 서버 배포 절차 불필요.)
