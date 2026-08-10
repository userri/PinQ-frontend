package com.finq.app.ui.onboarding

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.ui.components.ScaledVectorIcon
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import com.finq.app.ui.theme.streakColor
import kotlin.math.pow
import kotlin.random.Random
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// 첫 실행 온보딩 — 3장 캐러셀.
//
// 원칙: 세부 규칙은 여기서 가르치지 않는다. 이 화면은 "앱이 무엇을 하는지"만 말하고
// 끝나면 홈에 내려놓는다(문제로 직행하지 않는다). 단계 간격(3일/7일/14일)·물주기 규칙 같은 메커니즘은
// 그것을 쓰는 자리(복습 나무 개념 시트, 정원 "?" 헤더 버튼)가 전담한다.
// 재열람은 마이페이지 "앱 소개 다시 보기" — 캐러셀을 강제로 다시 재생하지 않는다.
// ─────────────────────────────────────────────────────────────────────────────

private data class OnboardingPage(
    val title: String,
    val body: String,
    val hero: @Composable (Dp) -> Unit,
)

/**
 * 3장. 각 장은 큰 비주얼 + 제목 한 줄 + 보조 한 줄로 구조가 완전히 같다.
 *
 * · 1장은 발송 시각을 못박지 않는다 — 서버 정책이 바뀌면 바로 거짓말이 된다.
 * · 2장 규칙 문구는 잔디 규약 SSOT(docs/rules/grass-and-streak.md, 2026-07-27 개정)와
 *   어긋나면 안 된다. "완주"·"전부 정답" 프레임은 폐기됐다 — 그날 맞힌 수만 센다.
 *   단계 수(4개 이상)와 색 이름은 여기서 말하지 않는다 — 화면 어디에도 "라임"이라는
 *   색 이름이 적혀 있지 않아 무엇을 가리키는지 알 수 없었다. 히트맵이 이미 색으로
 *   보여주고 있으므로 방향("많이 맞힐수록 진해진다")만 말한다. 정확한 경계는
 *   마이페이지 잔디밭 범례(`맞힌 문제 1 ▪▪▪▪ 4+`)가 맡는다.
 * · 3장은 단계 간격을 말하지 않는다(개념 시트 담당).
 */
private val Pages = listOf(
    OnboardingPage(
        title = "매일 아침 경제 퀴즈가 와요",
        body = "오늘의 문제로 하루를 시작해요",
        hero = { OnboardingQuizHero(it) },
    ),
    OnboardingPage(
        title = "맞히면 잔디가 자라요",
        body = "많이 맞힐수록 선명한 색이 칠해져요",
        hero = { OnboardingGrassHero(it) },
    ),
    OnboardingPage(
        title = "틀린 문제는 나무가 돼요",
        body = "복습할수록 조금씩 자라요",
        hero = { OnboardingGrowthHero(it) },
    ),
)

/**
 * 첫 실행 온보딩 화면.
 *
 * @param onFinish 마지막 장 CTA — 온보딩을 걷고 홈으로 내려놓는다(호출부 책임).
 * @param onSkip   우상단 텍스트 버튼. 언제나 제공된다.
 * @param replay   마이페이지에서 다시 열어본 경우. CTA·닫기 문구만 바뀐다.
 * @param initialPage 디버그 쇼케이스·프리뷰가 특정 장을 바로 렌더하기 위한 진입 페이지.
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    onSkip: () -> Unit,
    replay: Boolean = false,
    initialPage: Int = 0,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, Pages.lastIndex),
        pageCount = { Pages.size },
    )
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == Pages.lastIndex

    // 캐러셀 안에서는 뒤로가기가 이전 장으로 — 첫 장에서만 시스템에 넘긴다.
    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
    }

    Box(modifier = modifier.fillMaxSize().background(BgBase)) {
        // 밤하늘 — 정원 씬과 같은 언어. 데이터가 없으므로 별만 깔고 애니메이션은 두지 않는다.
        OnboardingStarField(Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = if (replay) "닫기" else "건너뛰기",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) { page ->
                OnboardingPageBody(Pages[page])
            }

            PageDots(
                count = Pages.size,
                current = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            )

            Spacer(Modifier.height(20.dp))

            // 알약은 액션 전용 — 이 화면에서 배경을 가진 요소는 이 버튼 하나뿐이다.
            Button(
                onClick = {
                    if (isLast) onFinish()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = OnLime),
            ) {
                Text(
                    text = when {
                        !isLast -> "다음"
                        replay -> "알겠어요"
                        else -> "시작하기"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * 한 장의 본문 — 비주얼은 남는 높이를 먼저 내주고, 글자는 절대 잘리지 않는다.
 * 히어로 크기를 화면 높이의 분율로 잡아 폰트 스케일이 커져도 글자가 밀려나지 않는다.
 */
@Composable
private fun OnboardingPageBody(page: OnboardingPage) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        val heroSize = minOf(240.dp, maxHeight * 0.42f, maxWidth * 0.86f)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center) { page.hero(heroSize) }

            Spacer(Modifier.height(36.dp))

            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = page.body,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * 진행 표시 — 현재 장만 라임 막대로 늘어난다.
 * 점을 잘게 찍으면 안 보인다(과거 6dp 인디케이터가 반려됐다). 지름 9dp 가 하한.
 */
@Composable
private fun PageDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { i ->
            if (i > 0) Spacer(Modifier.width(8.dp))
            val active = i == current
            Box(
                Modifier
                    .width(if (active) 24.dp else 9.dp)
                    .height(9.dp)
                    .clip(CircleShape)
                    .background(if (active) Lime else BgSubtle),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 히어로 — 이모지 금지. 기존 커스텀 벡터와 Color.kt 토큰만 쓴다.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 1장 — 도착한 오늘의 퀴즈.
 * 문제 카드 한 장 + 라임 알림 뱃지. 카드 안 막대는 질문·보기의 자리표시일 뿐,
 * 정답/오답을 흉내 내지 않는다(정오 신호는 채점 화면의 몫).
 */
@Composable
private fun OnboardingQuizHero(size: Dp) {
    Box(
        modifier = Modifier.size(size).clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(18.dp))
                .background(BgSurface)
                .border(1.dp, Outline, RoundedCornerShape(18.dp))
                .padding(horizontal = 18.dp, vertical = 20.dp),
        ) {
            // 질문 두 줄 — 밝은 쪽이 첫 줄.
            PlaceholderBar(widthFraction = 1f, height = size * 0.038f, color = BgSubtle)
            Spacer(Modifier.height(size * 0.032f))
            PlaceholderBar(widthFraction = 0.62f, height = size * 0.038f, color = BgSubtle)

            Spacer(Modifier.height(size * 0.075f))

            // 보기 3개 — 테두리만. 채우면 어느 하나가 선택된 것처럼 읽힌다.
            repeat(3) { i ->
                if (i > 0) Spacer(Modifier.height(size * 0.038f))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(size * 0.085f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Outline, RoundedCornerShape(8.dp)),
                )
            }
        }

        // 알림 뱃지 — "매일 아침 온다"를 형태로 말한다.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = size * 0.05f, end = size * 0.02f)
                .size(size * 0.24f)
                .clip(CircleShape)
                .background(Lime),
            contentAlignment = Alignment.Center,
        ) {
            ScaledVectorIcon(R.drawable.ic_bell, size * 0.14f, tint = OnLime)
        }
    }
}

@Composable
private fun PlaceholderBar(widthFraction: Float, height: Dp, color: Color) {
    Box(
        Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(color),
    )
}

/** 2장 잔디 히트맵 샘플 — 마지막 칸이 오늘. 라임(4단계)이 여러 칸 섞여 있어야 규칙이 예외로 안 읽힌다. */
private val SampleGrassLevels = listOf(
    0, 1, 2, 0, 3, 1, 0,
    2, 4, 1, 0, 2, 3, 1,
    0, 2, 3, 4, 1, 0, 2,
    1, 3, 2, 4, 3, 2, 4,
)

/**
 * 2장 — 잔디 히트맵.
 * 색은 마이페이지·홈 그리드와 같은 [streakColor] 램프를 그대로 쓴다(이 화면 전용 색 없음).
 * 오늘 칸은 실제 잔디 카드와 같은 방식으로 밝은 테두리만 두른다.
 */
@Composable
private fun OnboardingGrassHero(size: Dp) {
    val columns = 7
    val gap = size * 0.032f
    val cell = (size - gap * (columns - 1)) / columns
    val rows = SampleGrassLevels.size / columns

    Column(
        modifier = Modifier.width(size).clearAndSetSemantics {},
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        repeat(rows) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                repeat(columns) { col ->
                    val index = row * columns + col
                    val isToday = index == SampleGrassLevels.lastIndex
                    Box(
                        Modifier
                            .size(cell)
                            .clip(RoundedCornerShape(cell * 0.22f))
                            .background(streakColor(SampleGrassLevels[index]))
                            .then(
                                if (isToday) Modifier.border(
                                    1.5.dp, TextPrimary, RoundedCornerShape(cell * 0.22f),
                                ) else Modifier
                            ),
                    )
                }
            }
        }
    }
}

/** 단계 벡터의 밑동 아래 남는 빈 공간(뷰포트 분율) — 정원 씬의 `top = 0.85·side` 와 같은 값. */
private const val PlantBaseInset = 0.125f

/** 3장 성장 히어로의 네 단계 — 개념 시트 타임라인과 같은 아이콘·같은 순서를 쓴다. */
private val GrowthStages: List<Pair<Int, Float>> = listOf(
    R.drawable.ic_stage_sprout to 0.13f,
    R.drawable.ic_stage_grass to 0.17f,
    R.drawable.ic_stage_almost_tree to 0.22f,
    R.drawable.ic_stage_tree to 0.30f,
)

/**
 * 3장 — 오답이 나무가 되는 성장.
 *
 * 단계 이름·간격(+3일/+7일/+14일)은 일부러 쓰지 않는다. 여기서 필요한 건
 * "자란다"는 사실 하나뿐이고, 규칙은 복습 나무 개념 시트가 말한다.
 * 성장 신호는 아이콘 크기와 도착점의 라임 글로우로만 준다(라벨·범례 없음).
 */
@Composable
private fun OnboardingGrowthHero(size: Dp) {
    Column(
        modifier = Modifier.width(size).clearAndSetSemantics {},
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(size * 0.035f, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Bottom,
        ) {
            GrowthStages.forEachIndexed { i, (res, fraction) ->
                val isTree = i == GrowthStages.lastIndex
                Box(
                    // 단계 벡터는 밑동이 뷰포트 ~0.875 지점이라 아래에 빈 공간이 남는다.
                    // 그만큼 내려 그려야 크기가 다른 넷의 밑동이 한 지면에 놓인다.
                    modifier = Modifier.offset(y = size * fraction * PlantBaseInset),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isTree) {
                        // 도착점만 밤하늘에 은은하게 번진다 — 정원 씬의 due 글로우와 같은 언어.
                        Box(
                            Modifier.size(size * fraction * 1.7f).drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Lime.copy(alpha = 0.22f), Color.Transparent),
                                        center = center,
                                        radius = this.size.minDimension / 2f,
                                    ),
                                )
                            },
                        )
                    }
                    ScaledVectorIcon(res, size * fraction)
                }
            }
        }
        // 지면 — 식물이 허공에 뜨지 않게 받치는 헤어라인. 성장 신호는 여기 얹지 않는다.
        // 위 Row 의 밑동 보정만큼 아이콘이 이 선 위로 겹쳐 내려와 "심긴" 것처럼 보인다.
        Box(Modifier.fillMaxWidth().height(1.dp).background(Outline))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 밤하늘 별 — GardenNightScene 과 같은 규칙(위쪽 촘촘, 아래로 갈수록 감쇠, 일부 라임).
// 정원과 달리 데이터가 없으므로 무한 트랜지션은 두지 않는다.
// ─────────────────────────────────────────────────────────────────────────────

private data class OnboardingStar(
    val xFrac: Float,
    val yFrac: Float,
    val radiusDp: Float,
    val alpha: Float,
    val lime: Boolean,
)

private const val STAR_COUNT = 46

@Composable
private fun OnboardingStarField(modifier: Modifier = Modifier) {
    val stars = remember {
        val rnd = Random(20260728)
        List(STAR_COUNT) {
            OnboardingStar(
                xFrac = rnd.nextFloat(),
                yFrac = rnd.nextFloat().pow(1.6f),
                radiusDp = 1f + rnd.nextFloat() * 1.5f,
                alpha = 0.28f + rnd.nextFloat() * 0.5f,
                lime = rnd.nextFloat() < 0.08f,
            )
        }
    }
    Canvas(modifier) {
        stars.forEach { s ->
            // 본문(중앙 아래)으로 갈수록 감쇠 — 글자·히어로 뒤에서 별이 시끄러워지지 않게.
            val dim = 1f - ((s.yFrac - 0.30f) / 0.45f).coerceIn(0f, 1f) * 0.75f
            drawCircle(
                color = (if (s.lime) Lime else Color.White).copy(alpha = s.alpha * dim),
                radius = s.radiusDp.dp.toPx() / 2f,
                center = Offset(s.xFrac * size.width, s.yFrac * size.height),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 첫 실행 판정 — 서버 변경 없이 로컬에만 저장한다.
// ReviewTreeConceptSheet 의 "첫 오답 인트로" 플래그와 같은 저장소를 쓴다
// (인트로 계열 플래그가 파일마다 흩어지지 않게).
// ─────────────────────────────────────────────────────────────────────────────

private const val PREFS_NAME = "finq_intro"
private const val KEY_ONBOARDING_SEEN = "onboarding_seen"

/** 온보딩을 이미 봤는가(건너뛰기 포함). false 면 로그인 직후 온보딩으로 보낸다. */
fun hasSeenOnboarding(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_ONBOARDING_SEEN, false)

/** 온보딩을 봤다고 기록한다 — 완료·건너뛰기 어느 쪽이든 다시 뜨지 않는다. */
fun markOnboardingSeen(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putBoolean(KEY_ONBOARDING_SEEN, true).apply()
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun OnboardingQuizPreview() {
    FinQTheme { OnboardingScreen(onFinish = {}, onSkip = {}, initialPage = 0) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun OnboardingGrassPreview() {
    FinQTheme { OnboardingScreen(onFinish = {}, onSkip = {}, initialPage = 1) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun OnboardingGrowthPreview() {
    FinQTheme { OnboardingScreen(onFinish = {}, onSkip = {}, initialPage = 2) }
}
