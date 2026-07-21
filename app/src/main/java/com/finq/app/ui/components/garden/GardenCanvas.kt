package com.finq.app.ui.components.garden

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
                            detectTapGestures { offset ->
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
        quadraticBezierTo(cx - unit * 0.6f, cy - unit * 0.5f, cx - unit * 0.35f, cy - unit * 0.9f)
        quadraticBezierTo(cx - unit * 0.05f, cy - unit * 0.5f, cx, cy)
        moveTo(cx, cy)
        quadraticBezierTo(cx, cy - unit * 0.7f, cx, cy - unit * 1.05f)
        quadraticBezierTo(cx + unit * 0.08f, cy - unit * 0.6f, cx, cy)
        moveTo(cx, cy)
        quadraticBezierTo(cx + unit * 0.6f, cy - unit * 0.5f, cx + unit * 0.35f, cy - unit * 0.9f)
        quadraticBezierTo(cx + unit * 0.05f, cy - unit * 0.5f, cx, cy)
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
