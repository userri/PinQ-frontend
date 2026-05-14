package com.example.pinq_frontend.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pinq_frontend.ui.theme.PinQBlue
import com.example.pinq_frontend.ui.theme.PinQDarkNavy
import com.example.pinq_frontend.ui.theme.PinQ_frontendTheme
import java.util.Calendar

/**
 * 홈 화면 — Stateless View.
 *
 * @param quizCount    오늘 퀴즈 개수
 * @param streak       연속 학습 일수 (0=오늘 처음)
 * @param isLoading    퀴즈 로딩 중 여부
 * @param error        에러 메시지 (null이면 정상)
 * @param onStartQuiz  퀴즈 시작 콜백
 * @param onRetry      재시도 콜백
 */
@Composable
fun HomeScreen(
    quizCount: Int,
    streak: Int,
    isLoading: Boolean,
    error: String?,
    onStartQuiz: () -> Unit,
    onRetry: () -> Unit,
    onMyPage: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        // ── 앱 바 ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "PinQ",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = PinQBlue,
                modifier = Modifier.weight(1f),
            )
            // 알림 아이콘 더미
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🔔", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── 그리팅 ───────────────────────────────────────────────
        Text(
            text = "안녕하세요, 유리님 👋",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "오늘의 퀴즈가 도착했어요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        // ── 주간 스트릭 도트 ─────────────────────────────────────
        WeeklyStreakRow(streak = streak)

        Spacer(Modifier.height(20.dp))

        // ── 히어로 카드 ──────────────────────────────────────────
        when {
            isLoading -> HeroCardLoading()
            error != null -> HeroCardError(error = error, onRetry = onRetry)
            else -> HeroCard(quizCount = quizCount, onStartQuiz = onStartQuiz)
        }

    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 서브 컴포넌트
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WeeklyStreakRow(streak: Int) {
    val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")
    // 오늘이 몇 번째 요일인지 (월=0 ~ 일=6)
    val todayDow = remember {
        val cal = Calendar.getInstance()
        // Calendar.MONDAY=2 ~ Calendar.SUNDAY=1
        (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = "이번 주 학습",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                dayLabels.forEachIndexed { index, label ->
                    // streak 개수만큼 오늘까지 역방향으로 채움
                    val daysFromToday = todayDow - index
                    val isFilled = daysFromToday in 0 until streak || index == todayDow
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) PinQBlue
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isFilled) PinQBlue
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(quizCount: Int, onStartQuiz: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(PinQDarkNavy, Color(0xFF2851A3)),
                )
            )
            .padding(24.dp),
    ) {
        Column {
            Text(
                text = "오늘의 퀴즈",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (quizCount > 0) "${quizCount}문제 준비됐어요" else "퀴즈를 준비 중이에요",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "예상 시간 3분  ·  난이도 중간",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onStartQuiz,
                enabled = quizCount > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = PinQDarkNavy,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.heightIn(min = 44.dp),
            ) {
                Text(
                    text = "풀기  >",
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
                    colors = listOf(PinQDarkNavy, Color(0xFF2851A3)),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Color.White)
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
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
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
    PinQ_frontendTheme {
        HomeScreen(
            quizCount = 4,
            streak = 3,
            isLoading = false,
            error = null,
            onStartQuiz = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomeScreenLoadingPreview() {
    PinQ_frontendTheme {
        HomeScreen(
            quizCount = 0,
            streak = 0,
            isLoading = true,
            error = null,
            onStartQuiz = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomeScreenErrorPreview() {
    PinQ_frontendTheme {
        HomeScreen(
            quizCount = 0,
            streak = 0,
            isLoading = false,
            error = "네트워크 연결을 확인해주세요",
            onStartQuiz = {},
            onRetry = {},
        )
    }
}
