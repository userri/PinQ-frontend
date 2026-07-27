package com.finq.app.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finq.app.R
import com.finq.app.data.repository.GardenItem
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewStage
import com.finq.app.ui.components.NeonCtaPill
import com.finq.app.ui.components.WaterGrassCard
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Grass1
import com.finq.app.ui.theme.Grass2
import com.finq.app.ui.theme.Grass3
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import com.finq.app.ui.theme.streakColor
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.util.Calendar
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

// ─────────────────────────────────────────────────────────────────────────────
// 파생 색 — Color.kt 토큰의 lerp 만 사용(새 브랜드색 금지).
// ─────────────────────────────────────────────────────────────────────────────

/** 화면 최상단 깊은 밤 — BgBase 를 어둠 쪽으로 내린 값. */
private val NightTop = lerp(BgBase, Color.Black, 0.62f)

/** 수평선 부근 — 밤하늘이 잔디 쪽으로 아주 살짝 물드는 값. */
private val HorizonGlow = lerp(BgBase, Grass1, 0.35f)

/** 언덕 하단의 짙은 그린. */
private val GrassDeep = lerp(Grass1, Color.Black, 0.45f)

/**
 * 언덕 상단(능선) 색. Grass2 그대로 쓰면 단계 아이콘 줄기 색(#1E7A42 = Grass2)과
 * 정확히 겹치는 밴드가 생겨 줄기가 언덕에 보호색으로 사라진다(잎만 떠 보이는 버그).
 * Grass1 쪽으로 절반 넘게 내려 아이콘의 어떤 색과도 겹치지 않게 한다.
 */
private val HillTopColor = lerp(Grass2, Grass1, 0.55f)

/** 뒷줄 실루엣 숲 — 언덕보다 한 톤 어두운 단색. */
private val ForestSilhouette = lerp(Grass1, Color.Black, 0.25f)

/**
 * 홈 화면 — "밤하늘 아래 잔디밭" 한 장면.
 *
 * 화면 전체가 위(별이 촘촘한 밤 네이비)에서 아래(잔디 언덕)로 이어지는 배경 풍경이고,
 * UI 는 그 위에 뜬 반투명 유리 패널이다.
 * 구성: [워드마크·인사말] → [오늘의 퀴즈] → [복습(물주기)] → [이번 주 잔디 스트립]
 *       → (하늘 여백) → [잔디 언덕 + 나무들] (전체 탭 → 정원).
 * 역할 분리: 퀴즈 카드는 완료 시 조용한 상태, 복습 카드가 유일한 복습 진입점.
 */
@Composable
fun HomeScreen(
    quizCount: Int,
    streak: Int,
    /** 오늘 데일리 퀴즈를 풀었는가 — 서버 solvedToday. 잔디 level 로 유추하지 않는다. */
    solvedToday: Boolean,
    maxStreak: Int,
    /** 이번 주(월~일) 잔디 level. grass days[].level 슬라이스. 미래 날짜는 -1. */
    weekLevels: List<Int>,
    isLoading: Boolean,
    error: String?,
    onStartQuiz: () -> Unit,
    onRetry: () -> Unit,
    onMyPage: () -> Unit = {},
    nickname: String = "",
    /** 오늘 복습할 오답 수 — 복습 카드는 항상 노출된다(0이면 안내 상태). */
    reviewCount: Int = 0,
    nextReviewDate: LocalDate? = null,
    onWaterGrass: () -> Unit = {},
    /** 하단 잔디밭용 정원. null(로드 실패/이전)이면 빈 언덕으로 그린다. */
    garden: ReviewGarden? = null,
    onOpenGarden: () -> Unit = {},
    /** 오늘 세트 전체/정답 수 — 퀴즈 완료 상태 "N/M 정답" 표기용. */
    todayTotal: Int = 0,
    todayCorrect: Int = 0,
    modifier: Modifier = Modifier,
) {
    val treeCount = garden?.graduatedTrees ?: 0
    val growingCount = garden?.growing?.size ?: 0
    // 언덕 미세 성장 — 나무가 늘수록 로그 스케일로 완만히 상승. 화면 높이 40% 상한.
    val hillFraction = min(0.19f + 0.045f * ln(1f + treeCount), 0.40f)

    Box(modifier = modifier.fillMaxSize().background(NightTop)) {
        NightSceneBackground(
            garden = garden ?: ReviewGarden.EMPTY,
            hillFraction = hillFraction,
            modifier = Modifier.fillMaxSize(),
        )

        // ── 잔디밭 탭 영역 — 언덕 + 라벨 전체가 정원 진입점 ──────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(min(hillFraction + 0.08f, 0.46f))
                .clickable(onClick = onOpenGarden),
        ) {
            GardenLabel(
                treeCount = treeCount,
                growingCount = growingCount,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        // ── 유리 패널 UI — 상단 고정, 아래는 하늘 여백(Spacer weight) ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "경제",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = "잔디",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Lime,
                    letterSpacing = (-0.5).sp,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (nickname.isNotEmpty()) "안녕하세요, ${nickname}님" else "안녕하세요",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── 오늘의 퀴즈 카드 — 퀴즈만 담당(복습 광고 금지) ──────────
            when {
                isLoading -> HeroCardLoading()
                error != null -> HeroCardError(error = error, onRetry = onRetry)
                else -> TodayQuizCard(
                    quizCount = quizCount,
                    todayTotal = todayTotal,
                    todayCorrect = todayCorrect,
                    onStartQuiz = onStartQuiz,
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── 복습 카드 — 유일한 복습 진입점, 항상 노출 ──────────────
            WaterGrassCard(
                reviewCount = reviewCount,
                nextDueDate = nextReviewDate,
                onClick = onWaterGrass,
            )

            Spacer(Modifier.height(12.dp))

            // ── 이번 주 잔디 스트립 — 유리 패널 톤 ────────────────────
            val todayDow = remember {
                (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
            }
            WeekGrassStrip(
                streak = streak,
                solvedToday = solvedToday,
                maxStreak = maxStreak,
                weekLevels = weekLevels,
                todayDow = todayDow,
            )

            // 구성된 여백 — 카드와 언덕 사이 하늘. 콘텐츠로 채우지 않는다.
            Spacer(Modifier.weight(1f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 밤 풍경 배경 — 하늘 그라디언트 + 별 + 별똥별 + 잔디 언덕 + 나무 (단일 Canvas)
// ─────────────────────────────────────────────────────────────────────────────

private const val STAR_COUNT = 56

/** 시드 고정 별 한 개 — 좌표는 분율, y 는 하늘 밴드 내 분율(0=꼭대기). */
private data class Star(val xFrac: Float, val yFrac: Float, val radiusDp: Float, val alpha: Float, val lime: Boolean)

@Composable
private fun NightSceneBackground(
    garden: ReviewGarden,
    hillFraction: Float,
    modifier: Modifier = Modifier,
) {
    // 별 — remember 로 시드 고정(매 프레임 랜덤 금지). 밀도 그라데이션: 상단 촘촘·수평선 성김.
    val stars = remember {
        val rnd = Random(20260723)
        List(STAR_COUNT) {
            Star(
                xFrac = rnd.nextFloat(),
                yFrac = rnd.nextFloat().pow(1.8f),  // 위쪽으로 몰리는 분포
                radiusDp = 1f + rnd.nextFloat() * 1.5f,
                alpha = 0.3f + rnd.nextFloat() * 0.6f,
                lime = rnd.nextFloat() < 0.08f,
            )
        }
    }

    // 별똥별 — 20~50초 간격으로 1초짜리 사선 낙하 1개.
    val shootProgress = remember { Animatable(0f) }
    var shootStart by remember { mutableStateOf(Offset(0.7f, 0.12f)) }
    LaunchedEffect(Unit) {
        val rnd = Random(System.nanoTime())
        while (true) {
            delay(rnd.nextLong(20_000L, 50_000L))
            shootStart = Offset(0.15f + rnd.nextFloat() * 0.65f, 0.05f + rnd.nextFloat() * 0.2f)
            shootProgress.snapTo(0f)
            shootProgress.animateTo(1f, tween(durationMillis = 1000, easing = LinearEasing))
            shootProgress.snapTo(0f)
        }
    }

    // 앞줄 나무 — 최근 졸업 우선 최대 5그루, 모자라면 자라는 중 항목으로 채운다.
    // 나머지는 뒷줄 실루엣 숲이 담당(개별 렌더 폭증 방지).
    val frontItems = remember(garden) {
        (garden.graduated.sortedByDescending { it.graduatedAtIso ?: "" } + garden.growing).take(5)
    }
    val treeCount = garden.graduatedTrees
    // 뒷줄 실루엣 — 나무 8그루 초과분만큼 밀도 증가(상한 18개).
    val silhouetteCount = (treeCount - 8).coerceIn(0, 18)

    val treePainter = painterResource(R.drawable.ic_stage_tree)
    val almostTreePainter = painterResource(R.drawable.ic_stage_almost_tree)
    val grassPainter = painterResource(R.drawable.ic_stage_grass)
    val sproutPainter = painterResource(R.drawable.ic_stage_sprout)

    Canvas(modifier = modifier) {
        val hillTop = size.height * (1f - hillFraction)

        // ① 하늘 — 깊은 밤 → BgBase → 수평선 글로우.
        // 렉트는 전체 높이로 채운다(능선 곡선 아래 틈이 배경색으로 노출되지 않게;
        // endY 이후는 마지막 색으로 클램프되고 언덕이 위에 덮인다).
        drawRect(
            brush = Brush.verticalGradient(
                0.0f to NightTop,
                0.55f to BgBase,
                1.0f to HorizonGlow,
                endY = hillTop,
            ),
            size = size,
        )

        // ② 별 — 하늘 밴드 안에서만.
        stars.forEach { s ->
            drawCircle(
                color = (if (s.lime) Lime else Color.White).copy(alpha = s.alpha),
                radius = s.radiusDp.dp.toPx() / 2f,
                center = Offset(s.xFrac * size.width, s.yFrac * hillTop * 0.92f),
            )
        }

        // ③ 별똥별 — 진행 중일 때만 사선 낙하 + 꼬리 페이드.
        val p = shootProgress.value
        if (p > 0f && p < 1f) {
            val travel = size.width * 0.28f
            val head = Offset(
                shootStart.x * size.width + travel * p,
                shootStart.y * hillTop + travel * 0.6f * p,
            )
            val tail = head - Offset(travel * 0.22f, travel * 0.132f)
            val fade = if (p < 0.2f) p / 0.2f else 1f - (p - 0.2f) / 0.8f
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0f), Color.White.copy(alpha = 0.8f * fade)),
                    start = tail,
                    end = head,
                ),
                start = tail,
                end = head,
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        // ④ 뒷줄 실루엣 숲 — 언덕 능선 뒤 어두운 라운드 셰이프들(개별 렌더 아님).
        if (silhouetteCount > 0) {
            val rnd = Random(77)
            repeat(silhouetteCount) { i ->
                val x = size.width * ((i + 0.5f) / silhouetteCount + (rnd.nextFloat() - 0.5f) * 0.05f)
                val r = size.height * (0.022f + rnd.nextFloat() * 0.022f)
                drawCircle(
                    color = ForestSilhouette,
                    radius = r,
                    center = Offset(x, hillTop + hillOffsetAt(x / size.width) * size.height - r * 0.5f),
                )
            }
        }

        // ⑤ 잔디 언덕 — 완만한 능선의 앞 언덕.
        val hillPath = Path().apply {
            moveTo(0f, hillTop + hillOffsetAt(0f) * size.height)
            var x = 0f
            while (x <= size.width) {
                lineTo(x, hillTop + hillOffsetAt(x / size.width) * size.height)
                x += size.width / 24f
            }
            lineTo(size.width, hillTop + hillOffsetAt(1f) * size.height)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = hillPath,
            brush = Brush.verticalGradient(
                // Grass2 시작 금지 — 아이콘 줄기 색과 동일해 보호색 밴드가 생긴다. HillTopColor 참고.
                colors = listOf(HillTopColor, Grass1, GrassDeep),
                startY = hillTop,
                endY = size.height,
            ),
        )

        // ⑥ 풀결 텍스처 — 능선 근처 지면 커버(화면 하단까지 내려가 잘려 보이지 않게).
        run {
            val n = 22
            val band = size.height - hillTop
            repeat(n) { i ->
                val xf = ((i * 37 % n) + 0.5f) / n
                val yf = (i * 17 % n).toFloat() / n
                val x = size.width * xf
                val y = hillTop + hillOffsetAt(xf) * size.height +
                    band * 0.10f + yf * band * 0.32f
                val h = size.height * 0.012f
                drawLine(
                    color = Grass3.copy(alpha = 0.25f),
                    start = Offset(x, y),
                    end = Offset(x, y - h),
                    strokeWidth = size.width / 340f,
                )
            }
        }

        // ⑦ 앞줄 나무 — 커스텀 단계 아이콘, 유기적 흩뿌림(요일과 무관).
        // 깊이 범위를 능선 근처로 제한해 화면 하단 클리핑(밑동 잘림)을 막는다.
        val slotRnd = Random(4242)
        val baseXs = listOf(0.14f, 0.82f, 0.40f, 0.64f, 0.26f)
        val band = size.height - hillTop
        frontItems.forEachIndexed { i, item ->
            val xf = (baseXs[i] + (slotRnd.nextFloat() - 0.5f) * 0.06f).coerceIn(0.08f, 0.92f)
            val depth = slotRnd.nextFloat()  // 0 = 능선 근처(멀리), 1 = 아래(가까이)
            val y = hillTop + hillOffsetAt(xf) * size.height + band * (0.10f + depth * 0.28f)
            drawGardenItem(
                item = item,
                cx = xf * size.width,
                groundY = y,
                scale = 0.75f + depth * 0.45f,
                treePainter = treePainter,
                almostTreePainter = almostTreePainter,
                grassPainter = grassPainter,
                sproutPainter = sproutPainter,
            )
        }
    }
}

/** 언덕 능선의 x(0~1) 지점 오프셋(화면 높이 분율) — 좌우로 살짝 굽은 완만한 곡선. */
private fun hillOffsetAt(xFrac: Float): Float {
    val c = (xFrac - 0.42f)
    return 0.012f + c * c * 0.10f
}

/** 정원 항목 하나를 커스텀 단계 아이콘으로 — 밑동이 지면(groundY)에 닿게 그린다. */
private fun DrawScope.drawGardenItem(
    item: GardenItem,
    cx: Float,
    groundY: Float,
    scale: Float,
    treePainter: Painter,
    almostTreePainter: Painter,
    grassPainter: Painter,
    sproutPainter: Painter,
) {
    val unit = size.height * 0.035f * scale
    val (painter, side) = when {
        item.graduatedAtIso != null -> treePainter to unit * 3.6f
        item.stage == ReviewStage.SPROUT -> sproutPainter to unit * 2.3f
        item.stage == ReviewStage.GRASS -> grassPainter to unit * 2.5f
        else -> almostTreePainter to unit * 3.1f  // ALMOST_TREE
    }
    // 지면 앵커 — 아이콘 밑동(벡터가 평평하게 끝나는 절단면)을 가리는 어두운 풀 그림자.
    // 아이콘보다 먼저 그려 밑동이 그림자에 "심긴" 것처럼 보이게 한다.
    drawOval(
        color = GrassDeep.copy(alpha = 0.55f),
        topLeft = Offset(cx - side * 0.30f, groundY - side * 0.055f),
        size = Size(side * 0.60f, side * 0.11f),
    )
    // ⚠️ 같은 VectorPainter 를 한 프레임에 서로 다른 size 로 여러 번 draw 하면
    // 내부 캐시 비트맵이 꼬여 큰 아이템의 중심부가 잘려 그려진다(잎 조각만 남는 버그).
    // 그래서 painter 는 항상 고유 크기(intrinsicSize) 한 가지로만 그리고,
    // 확대·축소는 캔버스 transform(scale)으로 처리한다.
    // 벡터 콘텐츠는 뷰포트의 ~0.875 지점에서 끝난다(24 중 21) — top 을 0.85·side 로 잡아
    // 밑동을 지면에 살짝(-0.025·side) 심는다. 예전 0.9 는 밑동이 지면 위에 떠 끊겨 보였다.
    val intrinsic = painter.intrinsicSize
    val factor = side / intrinsic.width
    translate(left = cx - side / 2f, top = groundY - side * 0.85f) {
        scale(scaleX = factor, scaleY = factor, pivot = Offset.Zero) {
            with(painter) { draw(size = intrinsic) }
        }
    }
}

/**
 * 잔디밭 라벨. 숫자는 graduatedTrees 카운터 신뢰.
 * 나무가 아직 없으면 "0그루의 숲" 같은 초라한 표기 대신
 * 자라는 중인 잔디와 첫 나무 유도를 보여준다(아이콘도 새싹으로).
 */
@Composable
private fun GardenLabel(
    treeCount: Int,
    growingCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(BgBase.copy(alpha = 0.45f))
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(
                if (treeCount > 0) R.drawable.ic_stage_tree else R.drawable.ic_stage_sprout
            ),
            contentDescription = null,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = when {
                treeCount == 0 && growingCount == 0 -> "오답을 복습하면 숲이 자라요 →"
                treeCount == 0 -> "자라는 중 $growingCount · 첫 나무를 키워보세요 →"
                else -> "${treeCount}그루의 숲 · 자라는 중 $growingCount →"
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 이번 주 잔디 스트립 — 유리 패널
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 이번 주(월~일) 잔디 스트립 — 요일별 학습량을 잔디 블록 높이로 표현.
 * 도메인 규칙 유지: level 은 grass days[].level 그대로(잔디≠스트릭),
 * 복습만 한 날은 level 1 = 연한(짙은 초록) 블록.
 */
@Composable
private fun WeekGrassStrip(
    streak: Int,
    /** 서버 stats.solvedToday — 잔디 level 로 유추 금지(복습만 한 날 오판). */
    solvedToday: Boolean,
    maxStreak: Int,
    /** 월~일 7일치 잔디 level. 미래 날짜는 -1. */
    weekLevels: List<Int>,
    todayDow: Int,
) {
    val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgSurface.copy(alpha = 0.45f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "이번 주",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
            )
            StatPill(
                iconRes = R.drawable.ic_star_rounded,
                text = "최고 ${maxStreak.coerceAtLeast(0)}일",
            )
        }

        // 스트릭 문구 — streak 은 "어제까지" 값일 수 있으므로 미풀이 상태에선 +1 로 보여준다.
        Spacer(Modifier.height(6.dp))
        Text(
            text = when {
                solvedToday -> "🔥 ${streak}일 연속 학습 중!"
                streak > 0 -> "오늘 풀면 ${streak + 1}일 연속!"
                else -> "오늘 풀고 연속 학습을 시작해보세요"
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (solvedToday) Lime else TextPrimary,
        )

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            dayLabels.forEachIndexed { index, label ->
                // level 은 grass days[].level 그대로(자체 계산 없음). -1 = 미래.
                val level = weekLevels.getOrElse(index) { 0 }
                val isFilled = level > 0
                val isToday = index == todayDow

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 잔디 블록 — 학습량(level)을 높이로. 빈 날/미래는 낮은 스텁.
                    val barHeight = if (isFilled) (10 + level * 7).dp else 6.dp
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                            .background(
                                if (isFilled) streakColor(level)
                                else BgElevated.copy(alpha = 0.6f)
                            )
                            .then(
                                if (isToday && !isFilled)
                                    Modifier.border(
                                        1.5.dp, Lime,
                                        RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp),
                                    )
                                else Modifier
                            ),
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFilled || isToday) Lime else TextMuted,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
        // 잔디≠스트릭 안내문은 마이페이지 잔디밭으로 위임 — 홈에서는 밀도 위해 생략.
    }
}

/** 최고 기록 칩. */
@Composable
private fun StatPill(iconRes: Int, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(BgSubtle.copy(alpha = 0.55f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Lime,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.size(5.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 오늘의 퀴즈 카드 — 유리 패널
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 오늘의 퀴즈 카드 — 퀴즈만 담당. 복습(물주기) 카드와 같은 컴팩트 유리 행 스타일:
 * [원형 배지] + [제목/부제] + [네온 CTA]. 다 풀면 조용한 완료 상태(CTA 없음).
 */
@Composable
private fun TodayQuizCard(
    quizCount: Int,
    todayTotal: Int,
    todayCorrect: Int,
    onStartQuiz: () -> Unit,
) {
    val hasQuiz = quizCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgSurface.copy(alpha = 0.45f))
            .then(if (hasQuiz) Modifier.clickable(onClick = onStartQuiz) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50))
                // 물주기 카드와 동일한 유리 톤 배지 — 라임 저채도 틴트.
                .background(Lime.copy(alpha = if (hasQuiz) 0.16f else 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            if (hasQuiz) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_lightbulb),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Text(text = "✓", color = Lime, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (hasQuiz) "오늘의 퀴즈 ${quizCount}문제" else "오늘 분량 완료",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = when {
                    hasQuiz -> "예상 소요 3분 · 매일 오전 6시 발송"
                    todayTotal > 0 -> "${todayCorrect}/${todayTotal} 정답 · 내일 오전 6시 새 퀴즈"
                    else -> "내일 오전 6시에 새 퀴즈가 도착해요"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
        if (hasQuiz) {
            Spacer(Modifier.size(8.dp))
            NeonCtaPill(text = "풀러 가기 →")
        }
    }
}

@Composable
private fun HeroCardLoading() {
    // 로드 후 컴팩트 행 카드와 같은 높이로 잡아 레이아웃 점프를 막는다.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BgSurface.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Lime, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun HeroCardError(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "퀴즈를 불러오지 못했어요",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
        )
        OutlinedButton(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    FinQTheme {
        HomeScreen(
            quizCount = 4,
            streak = 1,
            solvedToday = false,
            maxStreak = 1,
            weekLevels = listOf(2, 0, 4, 1, 0, -1, -1),
            reviewCount = 3,
            isLoading = false,
            error = null,
            onStartQuiz = {},
            onRetry = {},
            nickname = "유저471194",
        )
    }
}
