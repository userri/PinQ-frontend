package com.finq.app.ui.components.garden

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.data.repository.GardenItem
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewStage
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.Grass1
import com.finq.app.ui.theme.Grass2
import com.finq.app.ui.theme.Grass3
import com.finq.app.ui.theme.Lime
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.pow
import kotlin.random.Random

// ─────────────────────────────────────────────────────────────────────────────
// 파생 색 — Color.kt 토큰의 lerp 만 사용(새 브랜드색 금지). 홈 밤 풍경과 같은 언어.
// ─────────────────────────────────────────────────────────────────────────────

private val NightTop = lerp(BgBase, Color.Black, 0.62f)
private val HorizonGlow = lerp(BgBase, Grass1, 0.35f)
private val GrassDeep = lerp(Grass1, Color.Black, 0.45f)
private val HillTopColor = lerp(Grass2, Grass1, 0.55f)
private val ForestSilhouette = lerp(Grass1, Color.Black, 0.25f)

/** 하늘/잔디 경계 분율 — 홈보다 잔디가 깊다(정원은 "그 안에 들어간" 화면). */
private const val HORIZON = 0.34f

/** 앞줄 큐레이션 상한 — 데이터가 수백이어도 개별 렌더는 이 수를 넘지 않는다. */
private const val FRONT_MAX = 13

/** 뒷배경 실루엣 상한 — 나머지 개수에 비례하되 폭증 금지. */
private const val SILHOUETTE_MAX = 26

private const val STAR_COUNT = 64
private const val FIREFLY_COUNT = 6

// ─────────────────────────────────────────────────────────────────────────────
// 배치 모델 — remember(garden) 로 시드 고정. 재구성돼도 그림이 흔들리지 않는다.
// ─────────────────────────────────────────────────────────────────────────────

/** 앞줄 식물 한 그루의 배치. 좌표는 분율, depth 0=멀리(작고 흐릿)·1=가까이. */
internal data class ScenePlant(
    val item: GardenItem,
    val xFrac: Float,
    val depth: Float,
    val rotationDeg: Float,
    /** 오늘 물 줄 수 있는가 — Lime 글로우 신호. */
    val due: Boolean,
)

internal data class SceneLayout(
    val front: List<ScenePlant>,
    /** 앞줄에 못 들어간 나머지 수(레거시 졸업분 포함) — 실루엣 밀도의 근거. */
    val restCount: Int,
    /** 나머지 중 나무(졸업) 비율 — 실루엣 구성을 실제 데이터에 맞춘다. */
    val restTreeFrac: Float,
)

private data class Star(val xFrac: Float, val yFrac: Float, val radiusDp: Float, val alpha: Float, val lime: Boolean)

/** 반딧불 — 데이터와 무관한 고정 개수의 분위기 요소. sp 는 정수(루프 이음새 방지). */
private data class Firefly(
    val xFrac: Float, val yFrac: Float,
    val ampX: Float, val ampY: Float,
    val speed: Int, val phase: Float,
)

/**
 * 큐레이션 + 원근 산포 배치. 입력만의 함수(결정적).
 *
 * 선정(성장 전시 중심): 다 자란 나무 → 나무직전 → 최근 물 준 순. due 우선 아님.
 * 배치: 골든비 수열로 x·깊이를 고르게 흩고, quizId 시드 지터로 격자 티를 없앤다.
 */
internal fun computeNightScene(garden: ReviewGarden, today: LocalDate): SceneLayout {
    val growingRanked = garden.growing.sortedWith(
        compareByDescending<GardenItem> { it.stage.ordinal }.thenByDescending { it.waterCount }
    )
    val ranked = garden.graduated.sortedByDescending { it.graduatedAtIso ?: "" } + growingRanked
    val front = ranked.take(FRONT_MAX)

    val legacyCount = (garden.graduatedTrees - garden.graduated.size).coerceAtLeast(0)
    val rest = ranked.drop(FRONT_MAX)
    val restCount = rest.size + legacyCount
    val restTrees = rest.count { it.graduatedAtIso != null } + legacyCount
    val restTreeFrac = if (restCount > 0) restTrees.toFloat() / restCount else 0f

    val plants = front.mapIndexed { i, item ->
        val rnd = Random(item.quizId * 31 + i)
        // R2 저불일치 수열(2D) — 두 축을 서로 다른 무리수로 돌려 상관(호 모양 몰림) 없이
        // 개수와 무관하게 x·깊이가 고르게 깔린다.
        val gx = (0.5f + i * 0.7548777f) % 1f
        val gd = (0.5f + i * 0.5698403f) % 1f
        ScenePlant(
            item = item,
            xFrac = (0.07f + gx * 0.86f + (rnd.nextFloat() - 0.5f) * 0.07f).coerceIn(0.06f, 0.94f),
            depth = (gd + (rnd.nextFloat() - 0.5f) * 0.18f).coerceIn(0f, 1f),
            rotationDeg = (rnd.nextFloat() - 0.5f) * 7f,
            // 서버가 뽑은 오늘 세트만 빛난다. dueDate 로 직접 판정하면 하루 캡에
            // 잘린 백로그까지 켜져서 "빛나는 건 10개인데 배지는 5개"가 된다.
            due = item.inTodayQueue,
        )
    }
    return SceneLayout(front = plants, restCount = restCount, restTreeFrac = restTreeFrac)
}

// ─────────────────────────────────────────────────────────────────────────────
// 씬 컴포저블
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 정원 밤 풍경 — 화면 전체 한 장면(카드·테두리 없음).
 *
 * 밤하늘(별·반딧불) → 능선 → 깊은 잔디밭이 연속 그라디언트로 이어지고,
 * 앞줄 큐레이션 식물(최대 [FRONT_MAX])만 클릭 가능. 나머지는 능선 뒤 실루엣.
 * 성능: 배경·별·실루엣·식물 모두 단일 Canvas, 반딧불만 무한 트랜지션 1개.
 */
@Composable
fun GardenNightScene(
    garden: ReviewGarden,
    onItemTap: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = remember(garden) { computeNightScene(garden, LocalDate.now()) }

    // 별 — 시드 고정, 밀도 그라데이션(상단 촘촘·수평선 성김), 일부 Lime.
    val stars = remember {
        val rnd = Random(20260727)
        List(STAR_COUNT) {
            Star(
                xFrac = rnd.nextFloat(),
                yFrac = rnd.nextFloat().pow(1.8f),
                radiusDp = 1f + rnd.nextFloat() * 1.5f,
                alpha = 0.3f + rnd.nextFloat() * 0.6f,
                lime = rnd.nextFloat() < 0.08f,
            )
        }
    }

    // 반딧불 — 개수 고정(데이터 늘어도 증가하지 않음), 능선 주변을 느리게 떠다닌다.
    val fireflies = remember {
        val rnd = Random(1206)
        List(FIREFLY_COUNT) {
            Firefly(
                xFrac = 0.08f + rnd.nextFloat() * 0.84f,
                yFrac = HORIZON - 0.06f + rnd.nextFloat() * 0.30f,
                ampX = 0.015f + rnd.nextFloat() * 0.03f,
                ampY = 0.010f + rnd.nextFloat() * 0.02f,
                speed = 1 + rnd.nextInt(2),
                phase = rnd.nextFloat(),
            )
        }
    }
    val fireflyT by rememberInfiniteTransition(label = "firefly")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 36_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "fireflyT",
        )

    val treePainter = painterResource(R.drawable.ic_stage_tree)
    val almostTreePainter = painterResource(R.drawable.ic_stage_almost_tree)
    val grassPainter = painterResource(R.drawable.ic_stage_grass)
    val sproutPainter = painterResource(R.drawable.ic_stage_sprout)

    Canvas(
        modifier = modifier.pointerInput(layout) {
            detectTapGestures { tap ->
                val w = size.width.toFloat()
                val h = size.height.toFloat()
                val minRadius = 24.dp.toPx()  // 시각 크기와 무관한 터치 최소 48dp 보장
                layout.front
                    .map { p ->
                        val (cx, groundY, side) = plantGeometry(p, w, h)
                        val center = Offset(cx, groundY - side * 0.4f)
                        Triple(p, (center - tap).getDistance(), maxOf(minRadius, side * 0.55f))
                    }
                    .filter { (_, dist, radius) -> dist <= radius }
                    .minByOrNull { (_, dist, _) -> dist }
                    ?.let { (p, _, _) -> onItemTap(p.item.quizId) }
            }
        },
    ) {
        val hillTop = size.height * HORIZON

        // ① 하늘 — 깊은 밤 → BgBase → 수평선 글로우(하드컷 금지, 능선까지 연속).
        drawRect(
            brush = Brush.verticalGradient(
                0.0f to NightTop,
                0.55f to BgBase,
                1.0f to HorizonGlow,
                endY = hillTop,
            ),
            size = size,
        )

        // ② 별 — 상단 헤더 밴드(yFrac<0.4)는 감쇠해 글자 가독성 확보(홈과 같은 방식).
        stars.forEach { s ->
            val readabilityDim = 0.3f + 0.7f * ((s.yFrac - 0.30f) / 0.35f).coerceIn(0f, 1f)
            drawCircle(
                color = (if (s.lime) Lime else Color.White).copy(alpha = s.alpha * readabilityDim),
                radius = s.radiusDp.dp.toPx() / 2f,
                center = Offset(s.xFrac * size.width, s.yFrac * hillTop * 0.92f),
            )
        }

        // ③ 뒷배경 실루엣 — 능선 뒤 어두운 무리. 실제 구성 반영(나무 vs 낮은 풀).
        drawBackSilhouettes(layout, hillTop)

        // ④ 잔디밭 — 완만한 능선에서 화면 하단까지 깊게.
        val hillPath = Path().apply {
            moveTo(0f, hillTop + ridgeOffsetAt(0f) * size.height)
            var x = 0f
            while (x <= size.width) {
                lineTo(x, hillTop + ridgeOffsetAt(x / size.width) * size.height)
                x += size.width / 24f
            }
            lineTo(size.width, hillTop + ridgeOffsetAt(1f) * size.height)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = hillPath,
            brush = Brush.verticalGradient(
                // Grass2 시작 금지 — 아이콘 줄기 색(#1E7A42)과 보호색 밴드가 생긴다.
                colors = listOf(HillTopColor, Grass1, GrassDeep),
                startY = hillTop,
                endY = size.height,
            ),
        )

        // ⑤ 풀결 텍스처 — 고정 패턴의 짧은 세로선. 깊이에 따라 살짝 길어진다.
        run {
            val n = 34
            val band = size.height - hillTop
            repeat(n) { i ->
                val xf = ((i * 37 % n) + 0.5f) / n
                val yf = (i * 17 % n).toFloat() / n
                val x = size.width * xf
                val y = hillTop + ridgeOffsetAt(xf) * size.height + band * (0.08f + yf * 0.82f)
                val h = size.height * (0.008f + 0.008f * yf)
                drawLine(
                    color = Grass3.copy(alpha = 0.22f),
                    start = Offset(x, y),
                    end = Offset(x, y - h),
                    strokeWidth = size.width / 340f,
                )
            }
        }

        // ⑥ 앞줄 식물 — 멀리(작은 depth)부터 그려 가까운 것이 자연스럽게 앞에 겹친다.
        layout.front.sortedBy { it.depth }.forEach { p ->
            drawScenePlant(p, treePainter, almostTreePainter, grassPainter, sproutPainter)
        }

        // ⑦ 반딧불 — 은은한 명멸 + 느린 부유. Lime 저알파, 개수 고정.
        fireflies.forEach { f ->
            val t = fireflyT
            val xf = f.xFrac + f.ampX * sin(2f * PI.toFloat() * (t * f.speed + f.phase))
            val yf = f.yFrac + f.ampY * sin(2f * PI.toFloat() * (t * f.speed * 2 + f.phase * 1.7f))
            val glow = 0.5f + 0.5f * sin(2f * PI.toFloat() * (t * f.speed * 3 + f.phase * 3f))
            val alpha = 0.08f + 0.26f * glow
            val c = Offset(xf * size.width, yf * size.height)
            drawCircle(color = Lime.copy(alpha = alpha * 0.30f), radius = 5.dp.toPx(), center = c)
            drawCircle(color = Lime.copy(alpha = alpha), radius = 1.6.dp.toPx(), center = c)
        }
    }
}

/** 능선의 x(0~1) 지점 오프셋(화면 높이 분율) — 좌우로 살짝 굽은 완만한 곡선. */
private fun ridgeOffsetAt(xFrac: Float): Float {
    val c = xFrac - 0.42f
    return 0.010f + c * c * 0.09f
}

/** 앞줄 식물의 픽셀 지오메트리(중심 x, 지면 y, 아이콘 변 길이) — 그리기·히트테스트 공용. */
private fun plantGeometry(p: ScenePlant, width: Float, height: Float): Triple<Float, Float, Float> {
    val hillTop = height * HORIZON
    val band = height - hillTop
    val cx = p.xFrac * width
    val groundY = hillTop + ridgeOffsetAt(p.xFrac) * height + band * (0.06f + p.depth * 0.72f)
    // 최종 크기 = 단계별 기본 × 깊이 배율 — 가까운 나무직전은 우뚝, 먼 새싹은 자잘하게.
    val unit = height * 0.040f * (0.55f + p.depth * 0.80f)
    // 배율은 아이콘이 뷰포트를 채우는 정도가 달라 광학 보정이 들어간다 —
    // 풀은 잎날이 폭을 꽉 채워 같은 변 길이에서도 크게 보이므로 낮추고,
    // 나무직전은 벡터 자체를 키운 만큼(1.24×) 배율을 내려 균형을 맞춘다.
    // 순서는 항상 새싹 < 풀 < 나무직전 < 나무 로 유지한다(성장 서사).
    val side = when {
        p.item.graduatedAtIso != null -> unit * 3.6f
        p.item.stage == ReviewStage.SPROUT -> unit * 2.0f
        p.item.stage == ReviewStage.GRASS -> unit * 2.1f
        else -> unit * 2.7f  // ALMOST_TREE — 벡터가 1.24× 커져 실효 크기는 3.35 상당
    }
    return Triple(cx, groundY, side)
}

private fun DrawScope.drawScenePlant(
    p: ScenePlant,
    treePainter: Painter,
    almostTreePainter: Painter,
    grassPainter: Painter,
    sproutPainter: Painter,
) {
    val (cx, groundY, side) = plantGeometry(p, size.width, size.height)
    val painter = when {
        p.item.graduatedAtIso != null -> treePainter
        p.item.stage == ReviewStage.SPROUT -> sproutPainter
        p.item.stage == ReviewStage.GRASS -> grassPainter
        else -> almostTreePainter
    }
    // 멀수록 흐릿·저채도(알파로 근사) — y축이 곧 깊이.
    val depthAlpha = 0.45f + 0.55f * p.depth

    // due 글로우 — 오늘 물 줄 수 있는 식물의 은은한 Lime 라디얼(클릭 유도 신호).
    if (p.due) {
        val glowCenter = Offset(cx, groundY - side * 0.35f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Lime.copy(alpha = 0.28f * depthAlpha), Lime.copy(alpha = 0f)),
                center = glowCenter,
                radius = side * 0.85f,
            ),
            radius = side * 0.85f,
            center = glowCenter,
        )
    }

    // 지면 앵커 그림자 — 밑동 절단면을 가려 "심긴" 것처럼.
    drawOval(
        color = GrassDeep.copy(alpha = 0.55f * depthAlpha),
        topLeft = Offset(cx - side * 0.30f, groundY - side * 0.055f),
        size = Size(side * 0.60f, side * 0.11f),
    )

    // ⚠️ 같은 VectorPainter 를 한 프레임에 서로 다른 size 로 그리면 내부 캐시가 꼬인다
    // (홈에서 확인된 버그). intrinsic 크기 한 가지로만 draw 하고 확대·축소·회전은
    // 캔버스 transform 으로 처리한다. 밑동은 뷰포트 ~0.875 지점 — top = 0.85·side.
    val intrinsic = painter.intrinsicSize
    val factor = side / intrinsic.width
    rotate(degrees = p.rotationDeg, pivot = Offset(cx, groundY)) {
        translate(left = cx - side / 2f, top = groundY - side * 0.85f) {
            scale(scaleX = factor, scaleY = factor, pivot = Offset.Zero) {
                with(painter) { draw(size = intrinsic, alpha = depthAlpha) }
            }
        }
    }
}

/**
 * 뒷배경 실루엣 — 앞줄에 못 들어간 나머지를 능선 뒤 어두운 무리로.
 * 나무 비율이 높으면 둥근 수관+줄기, 아니면 낮은 풀 무리. 클릭 불가(장식).
 */
private fun DrawScope.drawBackSilhouettes(layout: SceneLayout, hillTop: Float) {
    val count = layout.restCount.coerceAtMost(SILHOUETTE_MAX)
    if (count == 0) return
    val rnd = Random(77)
    repeat(count) { i ->
        val xf = ((i + 0.5f) / count + (rnd.nextFloat() - 0.5f) * 0.06f).coerceIn(0.02f, 0.98f)
        val x = xf * size.width
        val baseY = hillTop + ridgeOffsetAt(xf) * size.height
        val isTree = rnd.nextFloat() < layout.restTreeFrac
        if (isTree) {
            val r = size.height * (0.020f + rnd.nextFloat() * 0.018f)
            // 줄기 + 수관 — 어둡고 흐릿해 "만질 수 없는 것"으로 읽히게.
            drawLine(
                color = ForestSilhouette,
                start = Offset(x, baseY + r * 0.4f),
                end = Offset(x, baseY - r * 0.9f),
                strokeWidth = r * 0.28f,
            )
            drawCircle(color = ForestSilhouette, radius = r, center = Offset(x, baseY - r * 1.1f))
        } else {
            // 낮은 풀 무리 — 작은 둔덕 + 짧은 잎 두 가닥.
            val r = size.height * (0.008f + rnd.nextFloat() * 0.008f)
            drawOval(
                color = ForestSilhouette,
                topLeft = Offset(x - r * 1.6f, baseY - r),
                size = Size(r * 3.2f, r * 1.6f),
            )
            drawLine(
                color = ForestSilhouette,
                start = Offset(x - r * 0.5f, baseY - r * 0.6f),
                end = Offset(x - r * 0.9f, baseY - r * 2.2f),
                strokeWidth = r * 0.35f,
            )
            drawLine(
                color = ForestSilhouette,
                start = Offset(x + r * 0.5f, baseY - r * 0.6f),
                end = Offset(x + r * 0.8f, baseY - r * 2.4f),
                strokeWidth = r * 0.35f,
            )
        }
    }
}
