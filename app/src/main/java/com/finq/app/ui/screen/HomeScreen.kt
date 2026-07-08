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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finq.app.R
import com.finq.app.ui.theme.FinQTheme
import java.util.Calendar
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.LimeFaint
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
    maxStreak: Int,
    activityGrid: List<Int>,
    isLoading: Boolean,
    error: String?,
    onStartQuiz: () -> Unit,
    onRetry: () -> Unit,
    onMyPage: () -> Unit = {},
    nickname: String = "",
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

        // ── 위클리 스트릭 카드 ────────────────────────────────────
        val todayDowForGrid = remember {
            (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
        }
        val weekActivity = remember(activityGrid, todayDowForGrid) {
            List(7) { i ->
                val gridIndex = 55 - todayDowForGrid + i
                activityGrid.getOrElse(gridIndex) { 0 }
            }
        }
        WeeklyStreakCard(
            streak = streak,
            maxStreak = maxStreak,
            weekActivity = weekActivity,
            todayDow = todayDowForGrid,
        )

        Spacer(Modifier.height(16.dp))

        // ── 히어로 카드 ──────────────────────────────────────────
        when {
            isLoading -> HeroCardLoading()
            error != null -> HeroCardError(error = error, onRetry = onRetry)
            else -> HeroCard(quizCount = quizCount, onStartQuiz = onStartQuiz)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 위클리 스트릭
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WeeklyStreakCard(
    streak: Int,
    maxStreak: Int,
    weekActivity: List<Int>,
    todayDow: Int,
) {
    val solvedToday = weekActivity.getOrElse(todayDow) { 0 } > 0
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatPill(
                        iconRes = R.drawable.ic_sprout,
                        text = if (streak > 0) "${streak}일 연속" else "0일 연속",
                    )
                    StatPill(
                        iconRes = R.drawable.ic_star_rounded,
                        text = if (maxStreak > 0) "최고 ${maxStreak}일" else "최고 0일",
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                dayLabels.forEachIndexed { index, label ->
                    val isFuture = index > todayDow
                    val intensity = weekActivity.getOrElse(index) { 0 }
                    val isFilled = !isFuture && intensity > 0
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
                                    // 잔디 그리드: 연간 히트맵과 같은 라임 램프.
                                    // 빈칸/미래 칸은 반투명(BgElevated).
                                    if (isFilled) streakColor(intensity) else BgElevated
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
        }
    }
    // 오늘 풀지 않은 경우 살짝 푸시 문구를 카드 아래에 둔다 (선택적 UX, 데이터 추가 없음).
    if (!solvedToday && streak == 0) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "오늘 풀면 1일 연속 시작!",
            style = MaterialTheme.typography.labelMedium,
            color = Lime,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

/** 연속/최고 기록 칩 — 연한 라임 배경 + 진한 초록 아이콘 + 네이비 글씨. */
@Composable
private fun StatPill(iconRes: Int, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(LimeFaint)
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
private fun HeroCard(quizCount: Int, onStartQuiz: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(BgElevated, BgSurface, BgBase),
                )
            )
            .padding(24.dp),
    ) {
        // 우측 상단 반투명 원형 데코
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(140.dp)
                .clip(CircleShape)
                .background(Outline.copy(alpha = 0.4f)),
        )
        Column {
            Text(
                text = "오늘의 퀴즈",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (quizCount > 0) "${quizCount}문제 준비됐어요"
                       else "매일 오전 6시에 새 퀴즈가 도착해요",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (quizCount > 0) "예상 소요 3분 · 매일 오전 6시 발송"
                       else "오늘 분량은 다 풀었어요",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onStartQuiz,
                enabled = quizCount > 0,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Lime,
                    contentColor = OnLime,
                    disabledContainerColor = Lime.copy(alpha = 0.35f),
                    disabledContentColor = OnLime.copy(alpha = 0.5f),
                ),
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
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(BgElevated, BgSurface, BgBase),
                )
            ),
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
            maxStreak = 1,
            activityGrid = List(56) { i -> if (i % 3 == 0) 2 else 0 },
            isLoading = false,
            error = null,
            onStartQuiz = {},
            onRetry = {},
            nickname = "유저471194",
        )
    }
}
