package com.finq.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finq.app.R
import com.finq.app.ui.components.WaterGrassCard
import com.finq.app.ui.theme.FinQTheme
import java.time.LocalDate
import java.util.Calendar
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import com.finq.app.ui.theme.streakColor

/**
 * 홈 화면 — Stateless View.
 *
 * FinQ 디자인 시스템 적용. 풀블리드 네이비 히어로 카드 + 화이트 위클리 스트릭 카드 구성.
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
    /** 오늘 복습할 오답 수. 0 이고 nextReviewDate 도 null 이면 카드를 숨긴다. */
    reviewCount: Int = 0,
    nextReviewDate: LocalDate? = null,
    onWaterGrass: () -> Unit = {},
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
            // ── 워드마크: 경제(밝은 본문색) + 잔디(라임 강조) ──────────
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

        Spacer(Modifier.height(20.dp))

        // ── 그리팅 ───────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Lime),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_sprout),
                    contentDescription = null,
                    tint = OnLime,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.size(12.dp))
            Column {
                Text(
                    text = if (nickname.isNotEmpty()) "안녕하세요, ${nickname}님"
                           else "안녕하세요",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "오늘의 퀴즈가 도착했어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── 히어로 카드 (오늘의 퀴즈 = 제1 CTA) ─────────────────────
        when {
            isLoading -> HeroCardLoading()
            error != null -> HeroCardError(error = error, onRetry = onRetry)
            else -> HeroCard(
                quizCount = quizCount,
                reviewCount = reviewCount,
                onStartQuiz = onStartQuiz,
                onWaterGrass = onWaterGrass,
            )
        }

        // ── 오답 복습 진입 ("잔디에 물 주기") ─────────────────────
        // 복습 큐가 아예 비었으면(개수 0 + 다음 날짜 없음) 카드를 그리지 않는다.
        if (reviewCount > 0 || nextReviewDate != null) {
            Spacer(Modifier.height(16.dp))
            WaterGrassCard(
                reviewCount = reviewCount,
                nextDueDate = nextReviewDate,
                onClick = onWaterGrass,
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── 이번 주 학습 (주간 스트릭) ─────────────────────────────
        val todayDow = remember {
            (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
        }
        WeeklyStreakCard(
            streak = streak,
            solvedToday = solvedToday,
            maxStreak = maxStreak,
            weekLevels = weekLevels,
            todayDow = todayDow,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 위클리 스트릭
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WeeklyStreakCard(
    streak: Int,
    /**
     * 서버 stats.solvedToday. ⚠️ 잔디 level(weekLevels/activityGrid)로 유추 금지 —
     * 그 값들은 정답 수 기반이라 "풀었지만 전부 틀린 날"을 놓칠 수 있고,
     * 복습만 한 날도 level 1 이 심겨 오판한다.
     */
    solvedToday: Boolean,
    maxStreak: Int,
    /** 월~일 7일치 잔디 level (grass days[].level). 미래 날짜는 -1. */
    weekLevels: List<Int>,
    todayDow: Int,
) {
    val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Outline),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "이번 주 학습",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                StatPill(
                    iconRes = R.drawable.ic_star_rounded,
                    text = if (maxStreak > 0) "최고 ${maxStreak}일" else "최고 0일",
                )
            }

            // ── 메인 스트릭 문구 — 서버 solvedToday 기준 3분기 ──────────
            // streak 은 "어제까지" 값일 수 있으므로(하루 유예) 미풀이 상태에선 +1 로 보여준다.
            Spacer(Modifier.height(10.dp))
            Text(
                text = when {
                    solvedToday -> "🔥 ${streak}일 연속 학습 중!"
                    streak > 0 -> "오늘 풀면 ${streak + 1}일 연속!"
                    else -> "오늘 풀고 연속 학습을 시작해보세요"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (solvedToday) Lime else TextPrimary,
            )

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                dayLabels.forEachIndexed { index, label ->
                    // level 은 grass days[].level 를 그대로 사용(자체 계산 없음). -1 = 미래.
                    val level = weekLevels.getOrElse(index) { 0 }
                    val isFuture = level < 0
                    val isFilled = level > 0
                    val isToday = index == todayDow

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isToday && !isFilled)
                                        Modifier.border(2.dp, Lime, CircleShape)
                                    else
                                        Modifier
                                )
                                .background(
                                    // level 1=Grass1 … 4(만점)=Lime. 빈칸/미래는 BgElevated.
                                    if (isFilled) streakColor(level) else BgElevated
                                ),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                isFilled || isToday -> Lime
                                isFuture -> TextMuted
                                else -> TextMuted
                            },
                            fontWeight = if (isFilled || isToday) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }

            // ── 스트릭 규칙 안내 — "복습만 한 날 스트릭 1일" 혼동 방지 피드백 반영 ──
            Spacer(Modifier.height(12.dp))
            Text(
                text = "연속 학습은 데일리 퀴즈 기준이에요. 복습만 한 날은 연한 잔디만 심어져요 🌱",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
    }
}

/** 연속/최고 기록 칩 — 연한 라임 배경 + 진한 초록 아이콘 + 네이비 글씨. */
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
// 히어로 카드 (네이비 풀블리드)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(
    quizCount: Int,
    reviewCount: Int,
    onStartQuiz: () -> Unit,
    onWaterGrass: () -> Unit,
) {
    val hasQuiz = quizCount > 0
    // 다 풀었고 복습할 게 있으면 CTA 를 "복습하러 가기"(활성 라임)로 전환한다.
    val fallbackToReview = !hasQuiz && reviewCount > 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // 다른 카드와 동일한 문법: BgElevated 배경 + Outline 1dp 테두리.
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
                text = when {
                    hasQuiz -> "예상 소요 3분 · 매일 오전 6시 발송"
                    fallbackToReview -> "물 줄 잔디 ${reviewCount}개가 기다려요"
                    else -> "내일 오전 6시에 새 퀴즈가 도착해요"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(20.dp))
            HeroCta(
                hasQuiz = hasQuiz,
                fallbackToReview = fallbackToReview,
                onStartQuiz = onStartQuiz,
                onWaterGrass = onWaterGrass,
            )
        }
    }
}

/**
 * 히어로 CTA — 세 상태.
 *  1) 퀴즈 있음 → "풀기" (활성 라임)
 *  2) 퀴즈 없고 복습 있음 → "복습하러 가기" (활성 라임)
 *  3) 둘 다 없음 → "오늘 분량 완료" (비활성: BgSubtle + TextMuted, 라임 저채도 금지)
 */
@Composable
private fun HeroCta(
    hasQuiz: Boolean,
    fallbackToReview: Boolean,
    onStartQuiz: () -> Unit,
    onWaterGrass: () -> Unit,
) {
    val active = hasQuiz || fallbackToReview
    if (!active) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(BgSubtle)
                .heightIn(min = 42.dp)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "오늘 분량 완료",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
            )
        }
        return
    }

    Button(
        onClick = if (hasQuiz) onStartQuiz else onWaterGrass,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = OnLime),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = Modifier.heightIn(min = 42.dp),
    ) {
        Text(
            text = if (hasQuiz) "풀기" else "복습하러 가기",
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
