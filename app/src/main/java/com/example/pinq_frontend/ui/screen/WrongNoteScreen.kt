package com.example.pinq_frontend.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pinq_frontend.data.model.Category
import com.example.pinq_frontend.data.model.Quiz
import com.example.pinq_frontend.data.model.QuizOption
import com.example.pinq_frontend.data.model.RelatedArticle
import com.example.pinq_frontend.data.repository.AnswerResult
import com.example.pinq_frontend.ui.theme.PinQ_frontendTheme

/**
 * 오답노트 화면 — Stateless View.
 *
 * [quizzes] 와 [answerHistory] 를 인덱스로 매칭해 isCorrect=false 인 항목만 표시한다.
 * 각 카드에는 내 답(❌) · 정답(✅) · 해설 · 키워드를 보여준다.
 *
 * @param quizzes       세션에서 풀었던 퀴즈 전체 (options 텍스트 참조용)
 * @param answerHistory 제출 결과 전체 (quizzes 와 같은 순서)
 * @param onBack        결과 화면으로 돌아가기
 */
@Composable
fun WrongNoteScreen(
    quizzes: List<Quiz>,
    answerHistory: List<AnswerResult>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // quiz + answerResult 를 join 해서 오답만 추림
    val wrongItems = remember(quizzes, answerHistory) {
        quizzes.zip(answerHistory)
            .filter { (_, answer) -> !answer.isCorrect }
            .mapIndexed { _, (quiz, answer) ->
                WrongItem(
                    quizNumber = quizzes.indexOf(quiz) + 1,
                    categoryLabel = quiz.category.displayName,
                    question = quiz.question,
                    myAnswerNumber = quiz.options
                        .find { it.id == answer.selectedOptionId }?.optionNumber,
                    myAnswerText = quiz.options
                        .find { it.id == answer.selectedOptionId }?.text ?: "-",
                    correctAnswerNumber = quiz.options
                        .find { it.id == answer.correctOptionId }?.optionNumber,
                    correctAnswerText = quiz.options
                        .find { it.id == answer.correctOptionId }?.text ?: "-",
                    explanation = answer.explanation,
                    keyword = answer.keyword,
                )
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        // ── 헤더 ──────────────────────────────────────────────────
        Text(
            text = "오답노트",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (wrongItems.isEmpty()) "모두 맞혔어요! 🎉"
                   else "${wrongItems.size}개 틀렸어요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        if (wrongItems.isEmpty()) {
            // 전부 정답인 경우
            AllCorrectPlaceholder()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                wrongItems.forEach { item ->
                    WrongNoteCard(item = item)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "결과로 돌아가기",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 내부 display 모델
// ─────────────────────────────────────────────────────────────────────────────

private data class WrongItem(
    val quizNumber: Int,
    val categoryLabel: String,
    val question: String,
    val myAnswerNumber: Int?,
    val myAnswerText: String,
    val correctAnswerNumber: Int?,
    val correctAnswerText: String,
    val explanation: String,
    val keyword: String?,
)

// ─────────────────────────────────────────────────────────────────────────────
// 오답 카드
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WrongNoteCard(item: WrongItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // 카테고리 + 번호
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = "#${item.categoryLabel}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Text(
                    text = "Q${item.quizNumber}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(10.dp))

            // 문제
            Text(
                text = item.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(14.dp))

            // 내 답 (오답)
            AnswerRow(
                label = "내 답",
                number = item.myAnswerNumber,
                text = item.myAnswerText,
                isCorrect = false,
            )
            Spacer(Modifier.height(8.dp))

            // 정답
            AnswerRow(
                label = "정답",
                number = item.correctAnswerNumber,
                text = item.correctAnswerText,
                isCorrect = true,
            )

            // 해설
            if (item.explanation.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "💡 해설",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // 키워드
            if (!item.keyword.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔑 키워드  ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = item.keyword,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerRow(
    label: String,
    number: Int?,
    text: String,
    isCorrect: Boolean,
) {
    val icon = if (isCorrect) "✅" else "❌"
    val bgColor = if (isCorrect)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.errorContainer
    val textColor = if (isCorrect)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onErrorContainer

    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "$icon $label  ",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp),
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = bgColor,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = if (number != null) "${number}번. $text" else text,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun AllCorrectPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "🏆", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "오답이 없어요!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "완벽하게 맞혔어요",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

private val previewQuizzes = listOf(
    Quiz(
        id = 1L, category = Category.INTEREST_RATE,
        question = "한국은행이 기준금리를 올리면 일반적으로 시중 대출금리는 어떻게 변하나요?",
        options = listOf(
            QuizOption(1L, 1, "변하지 않는다"),
            QuizOption(2L, 2, "올라간다"),
            QuizOption(3L, 3, "내려간다"),
            QuizOption(4L, 4, "은행마다 다르다"),
        ),
    ),
    Quiz(
        id = 2L, category = Category.STOCK,
        question = "주식 시장에서 '베어 마켓'은 어떤 상황을 가리키나요?",
        options = listOf(
            QuizOption(5L, 1, "주가가 급등하는 상황"),
            QuizOption(6L, 2, "주가가 20% 이상 하락한 상황"),
            QuizOption(7L, 3, "거래량이 급감하는 상황"),
            QuizOption(8L, 4, "외국인 매수가 집중되는 상황"),
        ),
    ),
)

private val previewAnswers = listOf(
    AnswerResult(
        quizId = 1L, selectedOptionId = 3L, isCorrect = false,
        correctOptionId = 2L,
        explanation = "기준금리가 오르면 은행의 자금 조달 비용이 높아져 대출금리도 함께 상승합니다.",
        keyword = "기준금리",
        relatedArticle = RelatedArticle.EMPTY,
    ),
    AnswerResult(
        quizId = 2L, selectedOptionId = 6L, isCorrect = false,
        correctOptionId = 6L,
        explanation = "베어 마켓은 주가가 최고점 대비 20% 이상 하락한 하락장을 의미합니다.",
        keyword = "베어 마켓",
        relatedArticle = RelatedArticle.EMPTY,
    ),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun WrongNoteScreenPreview() {
    PinQ_frontendTheme {
        WrongNoteScreen(
            quizzes = previewQuizzes,
            answerHistory = previewAnswers,
            onBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun WrongNoteAllCorrectPreview() {
    PinQ_frontendTheme {
        WrongNoteScreen(
            quizzes = previewQuizzes,
            answerHistory = previewAnswers.map { it.copy(isCorrect = true) },
            onBack = {},
        )
    }
}
