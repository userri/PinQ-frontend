package com.example.pinq_frontend.ui.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pinq_frontend.data.DummyQuizData
import com.example.pinq_frontend.data.model.Quiz
import com.example.pinq_frontend.data.model.QuizOption
import com.example.pinq_frontend.data.model.RelatedArticle
import com.example.pinq_frontend.data.repository.AnswerResult
import com.example.pinq_frontend.ui.theme.PinQ_frontendTheme

/**
 * 퀴즈 정답/해설 화면 — Stateless View.
 *
 * 입력:
 *  - [quiz]: 문제 본문과 옵션 텍스트
 *  - [answer]: 채점 결과 (정답 여부, 정답 옵션 id, 해설, 관련 기사)
 *  - [isLast]: 마지막 문제 여부 → 버튼 라벨이 "결과 보기" 로 바뀜
 *
 * 이 화면은 [QuizSessionViewModel] 의 lastAnswer 가 채워진 직후에만 노출된다.
 */
@Composable
fun QuizAnswerScreen(
    quiz: Quiz,
    answer: AnswerResult,
    isLast: Boolean,
    onNext: () -> Unit,
    onArticleClick: (RelatedArticle) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            ResultBanner(isCorrect = answer.isCorrect)
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Q. ${quiz.question}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                quiz.options.forEach { option ->
                    AnswerOptionRow(
                        option = option,
                        isCorrect = option.id == answer.correctOptionId,
                        isUserSelected = option.id == answer.selectedOptionId,
                    )
                }
            }
            Spacer(Modifier.height(28.dp))

            ExplanationCard(explanation = answer.explanation)
            Spacer(Modifier.height(16.dp))

            RelatedArticleCard(
                article = answer.relatedArticle,
                onClick = { onArticleClick(answer.relatedArticle) },
            )
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = if (isLast) "결과 보기" else "다음 문제",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ResultBanner(isCorrect: Boolean) {
    val style = if (isCorrect) {
        ResultStyle(
            bg = MaterialTheme.colorScheme.tertiaryContainer,
            fg = MaterialTheme.colorScheme.onTertiaryContainer,
            emoji = "🎉",
            label = "정답이에요!",
        )
    } else {
        ResultStyle(
            bg = MaterialTheme.colorScheme.errorContainer,
            fg = MaterialTheme.colorScheme.onErrorContainer,
            emoji = "💡",
            label = "아쉬워요. 다음엔 맞혀봐요!",
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(style.bg)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = style.emoji, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.size(12.dp))
        Text(
            text = style.label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = style.fg,
        )
    }
}

private data class ResultStyle(
    val bg: Color,
    val fg: Color,
    val emoji: String,
    val label: String,
)

@Composable
private fun AnswerOptionRow(
    option: QuizOption,
    isCorrect: Boolean,
    isUserSelected: Boolean,
) {
    val (container, border, fg) = when {
        isCorrect -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onTertiaryContainer,
        )
        isUserSelected -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onErrorContainer,
        )
        else -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.outlineVariant,
            MaterialTheme.colorScheme.onSurface,
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(50))
                .background(border),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${option.optionNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface,
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = option.text,
            style = MaterialTheme.typography.bodyLarge,
            color = fg,
            fontWeight = if (isCorrect || isUserSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        if (isCorrect) {
            Text(
                text = "정답",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
            )
        } else if (isUserSelected) {
            Text(
                text = "내 선택",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ExplanationCard(explanation: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Text(
                        text = "AI",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "해설",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RelatedArticleCard(
    article: RelatedArticle,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "관련 기사",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = article.source,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun QuizAnswerScreenCorrectPreview() {
    PinQ_frontendTheme {
        val q = DummyQuizData.todayQuizzes.first()
        QuizAnswerScreen(
            quiz = q,
            answer = AnswerResult(
                quizId = q.id,
                selectedOptionId = q.correctOptionId,
                isCorrect = true,
                correctOptionId = q.correctOptionId,
                explanation = q.explanation,
                keyword = "금융통화위원회 — 한국은행 정책금리 결정 기구.",
                relatedArticle = q.relatedArticle,
            ),
            isLast = false,
            onNext = {},
            onArticleClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun QuizAnswerScreenWrongPreview() {
    PinQ_frontendTheme {
        val q = DummyQuizData.todayQuizzes[1]
        val wrongOption = q.options.first { it.id != q.correctOptionId }
        QuizAnswerScreen(
            quiz = q,
            answer = AnswerResult(
                quizId = q.id,
                selectedOptionId = wrongOption.id,
                isCorrect = false,
                correctOptionId = q.correctOptionId,
                explanation = q.explanation,
                keyword = "원화 강세 — 환율 하락 = 원화 가치 상승.",
                relatedArticle = q.relatedArticle,
            ),
            isLast = true,
            onNext = {},
            onArticleClick = {},
        )
    }
}
