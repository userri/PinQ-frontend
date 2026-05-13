package com.example.pinq_frontend.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pinq_frontend.data.model.Category
import com.example.pinq_frontend.data.model.Quiz
import com.example.pinq_frontend.data.model.RelatedArticle
import com.example.pinq_frontend.data.repository.AnswerResult
import com.example.pinq_frontend.ui.theme.PinQ_frontendTheme

/**
 * 퀴즈 세션 완료 후 보여주는 결과 리포트 화면.
 *
 * 구성:
 *  1. 정답률 도넛 차트 (Canvas 기반)
 *  2. 점수 요약 텍스트
 *  3. 문제별 정오 리스트
 *  4. 홈으로 / 다시 풀기 버튼
 *
 * @param quizzes       오늘 풀이한 퀴즈 목록 (문제 텍스트 표시에 사용)
 * @param answerHistory 제출 완료된 결과 목록 (quizzes 와 같은 순서)
 */
@Composable
fun ResultReportScreen(
    quizzes: List<Quiz>,
    answerHistory: List<AnswerResult>,
    onGoHome: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val correctCount = answerHistory.count { it.isCorrect }
    val totalCount = quizzes.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── 헤더 ──────────────────────────────────────────────────
        Text(
            text = "오늘의 결과",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(32.dp))

        // ── 도넛 차트 ─────────────────────────────────────────────
        DonutChart(
            correctCount = correctCount,
            totalCount = totalCount,
        )

        Spacer(Modifier.height(12.dp))

        // ── 점수 평가 문구 ────────────────────────────────────────
        Text(
            text = gradeMessage(correctCount, totalCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(32.dp))

        // ── 문제별 결과 ───────────────────────────────────────────
        if (quizzes.isNotEmpty() && answerHistory.isNotEmpty()) {
            Text(
                text = "문제별 결과",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                quizzes.forEachIndexed { index, quiz ->
                    val answer = answerHistory.getOrNull(index)
                    QuizResultItem(
                        index = index,
                        question = quiz.question,
                        isCorrect = answer?.isCorrect,
                    )
                }
            }
        }

        Spacer(Modifier.height(36.dp))

        // ── 버튼 ──────────────────────────────────────────────────
        Button(
            onClick = onGoHome,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "홈으로",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "다시 풀기",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 도넛 차트
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DonutChart(
    correctCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    val fraction = if (totalCount > 0) correctCount.toFloat() / totalCount.toFloat() else 0f

    // 채워지는 애니메이션 (0f → fraction)
    val animatedFraction = remember { Animatable(0f) }
    LaunchedEffect(fraction) {
        animatedFraction.animateTo(
            targetValue = fraction,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        )
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val arcColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.size(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 22.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = androidx.compose.ui.geometry.Size(
                width = size.width - strokeWidth,
                height = size.height - strokeWidth,
            )
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)

            // 배경 트랙
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            // 정답 비율 호
            if (animatedFraction.value > 0f) {
                drawArc(
                    color = arcColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedFraction.value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }

        // 중앙 텍스트
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$correctCount / $totalCount",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "정답",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 문제별 결과 아이템
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuizResultItem(
    index: Int,
    question: String,
    isCorrect: Boolean?,
    modifier: Modifier = Modifier,
) {
    val (icon, containerColor, iconBg) = when (isCorrect) {
        true -> Triple("✅", MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primaryContainer)
        false -> Triple("❌", MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.errorContainer)
        null -> Triple("⬜", MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 번호 뱃지
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Q${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 점수 평가 문구
// ─────────────────────────────────────────────────────────────────────────────

private fun gradeMessage(correct: Int, total: Int): String {
    if (total == 0) return ""
    return when {
        correct == total -> "완벽해요! 모두 맞혔어요 🎉"
        correct.toFloat() / total >= 0.75f -> "잘했어요! 조금만 더 👍"
        correct.toFloat() / total >= 0.5f -> "절반 이상! 계속 도전해봐요 💪"
        correct > 0 -> "아쉽지만 내일 또 도전해요 🌱"
        else -> "오늘은 어려웠죠. 내일은 꼭! 🔥"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

private val previewQuizzes = listOf(
    Quiz(
        id = 1L, category = Category.INTEREST_RATE,
        question = "한국은행이 기준금리를 올리면 일반적으로 시중 대출금리는 어떻게 변하나요?",
        options = emptyList(),
    ),
    Quiz(
        id = 2L, category = Category.EXCHANGE_RATE,
        question = "소비자물가지수(CPI)가 상승한다는 것은 무엇을 의미하나요?",
        options = emptyList(),
    ),
    Quiz(
        id = 3L, category = Category.STOCK,
        question = "주식 시장에서 '베어 마켓'은 어떤 상황을 가리키나요?",
        options = emptyList(),
    ),
    Quiz(
        id = 4L, category = Category.REAL_ESTATE,
        question = "GDP 대비 국가 부채 비율이 높아질 경우 나타날 수 있는 우려는 무엇인가요?",
        options = emptyList(),
    ),
)

private val previewAnswers = listOf(
    AnswerResult(quizId = 1L, selectedOptionId = 1L, isCorrect = true,
        correctOptionId = 1L, explanation = "", keyword = null,
        relatedArticle = RelatedArticle.EMPTY),
    AnswerResult(quizId = 2L, selectedOptionId = 3L, isCorrect = false,
        correctOptionId = 2L, explanation = "", keyword = null,
        relatedArticle = RelatedArticle.EMPTY),
    AnswerResult(quizId = 3L, selectedOptionId = 2L, isCorrect = true,
        correctOptionId = 2L, explanation = "", keyword = null,
        relatedArticle = RelatedArticle.EMPTY),
    AnswerResult(quizId = 4L, selectedOptionId = 4L, isCorrect = true,
        correctOptionId = 4L, explanation = "", keyword = null,
        relatedArticle = RelatedArticle.EMPTY),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun ResultReportPreview() {
    PinQ_frontendTheme {
        ResultReportScreen(
            quizzes = previewQuizzes,
            answerHistory = previewAnswers,
            onGoHome = {},
            onRestart = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun ResultReportAllWrongPreview() {
    PinQ_frontendTheme {
        ResultReportScreen(
            quizzes = previewQuizzes,
            answerHistory = previewAnswers.map { it.copy(isCorrect = false) },
            onGoHome = {},
            onRestart = {},
        )
    }
}
