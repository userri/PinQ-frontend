package com.finq.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finq.app.R
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.ui.components.WaterGrassCard
import com.finq.app.ui.components.garden.GardenCanvas
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import com.finq.app.ui.theme.streakColor
import java.time.LocalDate
import java.util.Calendar

/**
 * 홈 화면 — Stateless View.
 *
 * G1 "정원 히어로" 구성:
 *  [정원 히어로(내 정원 미니 프리뷰 + 이번 주 잔디 스트립)] → [오늘의 퀴즈 카드] → [복습 카드].
 * 히어로가 정원 진입점, 복습 카드가 유일한 복습 진입점이다.
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
    /** 히어로 미니 프리뷰용 정원. null(로드 실패/이전)이면 빈 정원으로 그린다. */
    garden: ReviewGarden? = null,
    onOpenGarden: () -> Unit = {},
    /** 오늘 세트 전체/정답 수 — 퀴즈 완료 상태 "N/M 정답" 표기용. */
    todayTotal: Int = 0,
    todayCorrect: Int = 0,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        // ── 앱 바: 워드마크만 ─────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
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
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── 정원 히어로 — 인사말 + 내 정원 프리뷰 + 이번 주 잔디 스트립 ──
        val todayDow = remember {
            (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
        }
        GardenHero(
            nickname = nickname,
            garden = garden,
            streak = streak,
            solvedToday = solvedToday,
            maxStreak = maxStreak,
            weekLevels = weekLevels,
            todayDow = todayDow,
            onOpenGarden = onOpenGarden,
        )

        Spacer(Modifier.height(16.dp))

        // ── 오늘의 퀴즈 카드 — 퀴즈만 담당(복습 광고 금지) ─────────────
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

        Spacer(Modifier.height(16.dp))

        // ── 복습 카드 — 유일한 복습 진입점, 항상 노출 ────────────────
        WaterGrassCard(
            reviewCount = reviewCount,
            nextDueDate = nextReviewDate,
            onClick = onWaterGrass,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 정원 히어로
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 하늘~잔디 언덕 한 장면. 위 = 인사말 + 나무 카운트 + 정원 프리뷰(탭 → 정원 화면),
 * 아래 = "이번 주" 잔디 스트립(구분선 + 톤 변화로 시각 분리).
 *
 * 프리뷰의 나무 배치는 GardenCanvas(computeGardenLayout)의 지터·원근 배치를 그대로 써서
 * 유기적으로 흩어진 군집으로 보인다 — 요일과 1:1 매칭되어 보이면 안 된다.
 */
@Composable
private fun GardenHero(
    nickname: String,
    garden: ReviewGarden?,
    streak: Int,
    solvedToday: Boolean,
    maxStreak: Int,
    weekLevels: List<Int>,
    todayDow: Int,
    onOpenGarden: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, Outline, shape)
            .background(BgSurface),
    ) {
        val treeCount = garden?.graduatedTrees ?: 0
        val growingCount = garden?.growing?.size ?: 0

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(196.dp)
                .clickable(onClick = onOpenGarden),
        ) {
            GardenCanvas(
                garden = garden ?: ReviewGarden.EMPTY,
                compact = true,
                modifier = Modifier.fillMaxSize(),
            )
            // 하늘 영역(위 40%) 위 오버레이 — 인사말 + 카운트.
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    text = if (nickname.isNotEmpty()) "안녕하세요, ${nickname}님" else "안녕하세요",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "🌳 ${treeCount}그루 · 자라는 중 $growingCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
            Text(
                text = "내 정원 →",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Lime,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
            )
        }

        // ── 이번 주 잔디 스트립 — 히어로 안의 자체 띠(톤 변화 + 구분선) ──
        HorizontalDivider(color = Outline, thickness = 1.dp)
        WeekGrassStrip(
            streak = streak,
            solvedToday = solvedToday,
            maxStreak = maxStreak,
            weekLevels = weekLevels,
            todayDow = todayDow,
        )
    }
}

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

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
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
                            .background(if (isFilled) streakColor(level) else BgElevated)
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

        // 잔디≠스트릭 축 분리 안내 — "복습만 한 날 스트릭 1일" 혼동 방지.
        Spacer(Modifier.height(10.dp))
        Text(
            text = "연속 학습은 데일리 퀴즈 기준이에요. 복습만 한 날은 연한 잔디만 심어져요 🌱",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }
}

/** 최고 기록 칩. */
@Composable
private fun StatPill(iconRes: Int, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(BgSubtle)
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
// 오늘의 퀴즈 카드
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 오늘의 퀴즈 카드 — 퀴즈만 담당.
 * 다 풀면 "오늘 분량 완료 ✓ · N/M 정답" 조용한 완료 상태(CTA·복습 유도 없음).
 */
@Composable
private fun TodayQuizCard(
    quizCount: Int,
    todayTotal: Int,
    todayCorrect: Int,
    onStartQuiz: () -> Unit,
) {
    val hasQuiz = quizCount > 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgElevated)
            .border(1.dp, Outline, RoundedCornerShape(20.dp))
            .padding(24.dp),
    ) {
        Column {
            Text(
                text = "오늘의 퀴즈",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (hasQuiz) "${quizCount}문제 준비됐어요"
                       else "오늘 분량을 다 풀었어요",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (hasQuiz) "예상 소요 3분 · 매일 오전 6시 발송"
                       else "내일 오전 6시에 새 퀴즈가 도착해요",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(20.dp))
            if (hasQuiz) {
                Button(
                    onClick = onStartQuiz,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = OnLime),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier.heightIn(min = 42.dp),
                ) {
                    Text(
                        text = "풀기",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                // 조용한 완료 칩 — 비활성 톤(BgSubtle + TextMuted), 라임 저채도 금지.
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(BgSubtle)
                        .heightIn(min = 42.dp)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (todayTotal > 0) "오늘 분량 완료 ✓ · ${todayCorrect}/${todayTotal} 정답"
                               else "오늘 분량 완료 ✓",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCardLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BgElevated)
            .border(1.dp, Outline, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Lime)
    }
}

@Composable
private fun HeroCardError(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
