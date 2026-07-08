package com.finq.app.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finq.app.R
import com.finq.app.data.model.Category
import com.finq.app.data.model.Quiz
import com.finq.app.data.model.RelatedArticle
import com.finq.app.data.repository.AnswerResult
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary

/**
 * 퀴즈 세션 완료 후 결과 리포트.
 *
 * FinQ 디자인:
 *  - 헤더: "오늘의 결과"
 *  - 정답률 카드 (도넛 + 점수 + 격려 문구)
 *  - 문제별 결과 (check / x 마크)
 *  - 오답노트 보기 (네이비, 오답 1개 이상일 때만)
 *  - 홈으로 / 다시 풀기 (아웃라인 둘로 분할 표시)
 */
@Composable
fun ResultReportScreen(
    quizzes: List<Quiz>,
    answerHistory: List<AnswerResult>,
    onGoHome: () -> Unit,
    onRestart: () -> Unit,
    onWrongNote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val answerByQuizId = remember(answerHistory) {
        answerHistory.associateBy { it.quizId }
    }
    val resultQuizzes = quizzes.filter { quiz ->
        answerByQuizId.containsKey(quiz.id) || quiz.correct != null
    }
    val correctCount = resultQuizzes.count { quiz ->
        answerByQuizId[quiz.id]?.isCorrect ?: (quiz.correct == true)
    }
    val totalCount = resultQuizzes.size
    val wrongCount = resultQuizzes.count { quiz ->
        answerByQuizId[quiz.id]?.isCorrect == false || quiz.correct == false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        // ── 헤더 ──────────────────────────────────────────────────
        Text(
            text = "오늘의 결과",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
        )

        Spacer(Modifier.height(16.dp))

        // ── 정답률 카드 (도넛 + 격려) ─────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, Outline),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DonutChart(correctCount = correctCount, totalCount = totalCount)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = gradeMessage(correctCount, totalCount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── 문제별 결과 ───────────────────────────────────────────
        if (resultQuizzes.isNotEmpty()) {
            Text(
                text = "문제별 결과",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Outline),
            ) {
                Column {
                    resultQuizzes.forEachIndexed { index, quiz ->
                        val answer = answerByQuizId[quiz.id]
                        val isCorrect = answer?.isCorrect ?: quiz.correct
                        QuizResultRow(
                            index = index,
                            question = quiz.question,
                            isCorrect = isCorrect,
                        )
                        if (index < resultQuizzes.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                                    .height(1.dp)
                                    .background(Outline)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── 오답노트 (있을 때만) ─────────────────────────────────
        if (wrongCount > 0) {
            Button(
                onClick = onWrongNote,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Lime,
                    contentColor = OnLime,
                ),
            ) {
                Text(
                    text = "오답노트 보기 ($wrongCount)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        // ── 홈으로 / 다시 풀기 (가로 50:50) ─────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            ) {
                Text(
                    text = "홈으로",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            ) {
                Text(
                    text = "다시 풀기",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
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
    val animatedFraction = remember { Animatable(0f) }
    LaunchedEffect(fraction) {
        animatedFraction.animateTo(
            targetValue = fraction,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        )
    }

    val trackColor = BgElevated
    val arcColor = Lime

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = androidx.compose.ui.geometry.Size(
                width = size.width - strokeWidth,
                height = size.height - strokeWidth,
            )
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
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

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$correctCount/$totalCount",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "정답률",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 문제별 결과 행
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuizResultRow(
    index: Int,
    question: String,
    isCorrect: Boolean?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Q${index + 1}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            modifier = Modifier.width(28.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = question,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(12.dp))
        when (isCorrect) {
            true -> Image(
                painter = painterResource(R.drawable.ic_check_circle),
                contentDescription = "정답",
                modifier = Modifier.size(20.dp),
            )
            false -> Image(
                painter = painterResource(R.drawable.ic_x_circle),
                contentDescription = "오답",
                modifier = Modifier.size(20.dp),
            )
            null -> Box(modifier = Modifier.size(20.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 점수 평가 문구 (이모지 제거)
// ─────────────────────────────────────────────────────────────────────────────

private fun gradeMessage(correct: Int, total: Int): String {
    if (total == 0) return ""
    return when {
        correct == total -> "완벽해요! 모두 맞혔어요"
        correct.toFloat() / total >= 0.75f -> "잘하고 있어요, 조금만 더!"
        correct.toFloat() / total >= 0.5f -> "절반 이상! 계속 도전해봐요"
        correct > 0 -> "아쉽지만 내일 또 도전해요"
        else -> "오늘은 어려웠죠, 내일은 꼭"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

private val previewQuizzes = listOf(
    Quiz(id = 1L, category = Category.INTEREST_RATE,
        question = "기준금리 인상이 글로벌 경제에 미치는 영향은?",
        options = emptyList()),
    Quiz(id = 2L, category = Category.EXCHANGE_RATE,
        question = "원화가 약세를 보일 때 한국 경제에 미치는...",
        options = emptyList()),
    Quiz(id = 3L, category = Category.STOCK,
        question = "코스피 지수가 급등락을 반복하는 상황에...",
        options = emptyList()),
    Quiz(id = 4L, category = Category.REAL_ESTATE,
        question = "다주택자에 대한 양도소득세 중과 유예...",
        options = emptyList()),
)

private val previewAnswers = listOf(
    AnswerResult(quizId = 1L, selectedOptionId = 1L, isCorrect = true,
        correctOptionId = 1L, explanation = "", keyword = null,
        relatedArticle = RelatedArticle.EMPTY),
    AnswerResult(quizId = 2L, selectedOptionId = 4L, isCorrect = true,
        correctOptionId = 4L, explanation = "", keyword = null,
        relatedArticle = RelatedArticle.EMPTY),
    AnswerResult(quizId = 3L, selectedOptionId = 2L, isCorrect = false,
        correctOptionId = 1L, explanation = "", keyword = null,
        relatedArticle = RelatedArticle.EMPTY),
    AnswerResult(quizId = 4L, selectedOptionId = 4L, isCorrect = true,
        correctOptionId = 4L, explanation = "", keyword = null,
        relatedArticle = RelatedArticle.EMPTY),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun ResultReportPreview() {
    FinQTheme {
        ResultReportScreen(
            quizzes = previewQuizzes,
            answerHistory = previewAnswers,
            onGoHome = {},
            onRestart = {},
            onWrongNote = {},
        )
    }
}
