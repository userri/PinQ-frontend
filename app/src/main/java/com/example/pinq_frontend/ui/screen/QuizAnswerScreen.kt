package com.example.pinq_frontend.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pinq_frontend.R
import com.example.pinq_frontend.data.DummyQuizData
import com.example.pinq_frontend.data.model.Quiz
import com.example.pinq_frontend.data.model.QuizOption
import com.example.pinq_frontend.data.model.RelatedArticle
import com.example.pinq_frontend.data.repository.AnswerResult
import com.example.pinq_frontend.data.repository.LibraryRepository
import com.example.pinq_frontend.ui.theme.FinQBlue
import com.example.pinq_frontend.ui.theme.FinQBlueSoft
import com.example.pinq_frontend.ui.theme.FinQDivider
import com.example.pinq_frontend.ui.theme.FinQNavy
import com.example.pinq_frontend.ui.theme.FinQRed
import com.example.pinq_frontend.ui.theme.FinQRedSoft
import com.example.pinq_frontend.ui.theme.FinQSurfaceMuted
import com.example.pinq_frontend.ui.theme.FinQTextMuted
import com.example.pinq_frontend.ui.theme.FinQTextStrong
import com.example.pinq_frontend.ui.theme.FinQYellowSoft
import com.example.pinq_frontend.ui.theme.PinQ_frontendTheme
import kotlinx.coroutines.launch

/**
 * 퀴즈 정답/해설 화면 — Stateless View.
 *
 * FinQ 디자인:
 *  - 상단: 뒤로 + 진행도 + N/M
 *  - 정답/오답 칩 (블루 또는 레드)
 *  - 보기 4개: 정답 옵션은 파란 보더 + "정답" 뱃지, 사용자 오답은 레드 보더
 *  - 해설 카드
 *  - 알아두면 좋아요 (옐로우 콜아웃)
 *  - 하단 다음/결과 보기 버튼 (네이비)
 */
@Composable
fun QuizAnswerScreen(
    quiz: Quiz,
    answer: AnswerResult,
    isLast: Boolean,
    quizIndex: Int,
    totalCount: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onArticleClick: (RelatedArticle) -> Unit,
    libraryRepository: LibraryRepository? = null,
    initialBookmarked: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var bookmarked by remember(quiz.id) { mutableStateOf(initialBookmarked) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // ── 상단 진행도 ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "뒤로",
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.size(8.dp))
            LinearProgressIndicator(
                progress = {
                    (quizIndex + 1).toFloat() / totalCount.coerceAtLeast(1).toFloat()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = FinQBlue,
                trackColor = FinQDivider,
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = "${quizIndex + 1}/$totalCount",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = FinQTextMuted,
            )
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── 정답/오답 칩 + 북마크 ─────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResultChip(isCorrect = answer.isCorrect, modifier = Modifier.weight(1f))
                Spacer(Modifier.size(8.dp))
                if (libraryRepository != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                val next = !bookmarked
                                bookmarked = next
                                scope.launch {
                                    runCatching {
                                        if (next) libraryRepository.addBookmark(quiz.id)
                                        else libraryRepository.removeBookmark(quiz.id)
                                    }.onFailure {
                                        bookmarked = !next
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(
                                if (bookmarked) R.drawable.ic_bookmark_star_filled
                                else R.drawable.ic_bookmark_star,
                            ),
                            contentDescription = "북마크",
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))

            // ── 문제 ────────────────────────────────────────────
            Text(
                text = "Q. ${quiz.question}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FinQTextStrong,
            )
            Spacer(Modifier.height(14.dp))

            // ── 보기 4개 ─────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                quiz.options.forEach { option ->
                    AnswerOptionRow(
                        option = option,
                        isCorrect = option.id == answer.correctOptionId,
                        isUserSelected = option.id == answer.selectedOptionId,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            // ── 해설 카드 ────────────────────────────────────────
            ExplanationCard(explanation = answer.explanation)

            // ── 알아두면 좋아요 (옐로우 콜아웃) ───────────────────
            if (!answer.keyword.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                KeywordCard(keyword = answer.keyword)
            }

            // ── 관련 기사 ────────────────────────────────────────
            if (answer.relatedArticle != RelatedArticle.EMPTY &&
                answer.relatedArticle.title.isNotBlank()
            ) {
                Spacer(Modifier.height(12.dp))
                RelatedArticleCard(
                    article = answer.relatedArticle,
                    onClick = { onArticleClick(answer.relatedArticle) },
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── 다음/결과 보기 버튼 ─────────────────────────────────
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FinQNavy,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = if (isLast) "결과 보기" else "다음 문제",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(6.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 정답/오답 칩
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ResultChip(isCorrect: Boolean, modifier: Modifier = Modifier) {
    val bg = if (isCorrect) FinQBlueSoft else FinQRedSoft
    val fg = if (isCorrect) FinQBlue else FinQRed
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(fg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Q",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
        }
        Spacer(Modifier.size(10.dp))
        Text(
            text = if (isCorrect) "정답이에요!" else "아쉬워요, 다음에 맞혀봐요",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 보기 행
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnswerOptionRow(
    option: QuizOption,
    isCorrect: Boolean,
    isUserSelected: Boolean,
) {
    val container = Color.White
    val (border, accent) = when {
        isCorrect -> FinQBlue to FinQBlue
        isUserSelected -> FinQRed to FinQRed
        else -> Color.Transparent to FinQTextMuted
    }
    val highlightBg = when {
        isCorrect -> FinQBlueSoft
        isUserSelected -> FinQRedSoft
        else -> container
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(highlightBg)
            .border(
                width = if (isCorrect || isUserSelected) 1.5.dp else 1.dp,
                color = if (border == Color.Transparent) FinQDivider else border,
                shape = RoundedCornerShape(14.dp),
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    if (isCorrect || isUserSelected) accent
                    else Color(0xFFE5E7EB)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${option.optionNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCorrect || isUserSelected) Color.White else Color(0xFF6B7280),
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = option.text,
            style = MaterialTheme.typography.bodyLarge,
            color = FinQTextStrong,
            fontWeight = if (isCorrect || isUserSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        if (isCorrect) {
            Spacer(Modifier.size(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(FinQBlue)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "정답",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        } else if (isUserSelected) {
            Spacer(Modifier.size(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(FinQRed)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "내 선택",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 해설 카드
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExplanationCard(explanation: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FinQSurfaceMuted),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(FinQBlueSoft)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "해설",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = FinQNavy,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = FinQTextStrong,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 알아두면 좋아요 (옐로우 콜아웃)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun KeywordCard(keyword: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FinQYellowSoft)
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Row {
            Image(
                painter = painterResource(R.drawable.ic_lightbulb),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.size(10.dp))
            Column {
                Text(
                    text = "알아두면 좋아요",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7A5B00),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = keyword,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5A4400),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 관련 기사
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RelatedArticleCard(article: RelatedArticle, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, FinQDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "관련 기사",
                style = MaterialTheme.typography.labelMedium,
                color = FinQBlue,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FinQTextStrong,
                maxLines = 2,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = article.source,
                style = MaterialTheme.typography.bodySmall,
                color = FinQTextMuted,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

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
                keyword = "환율이 한국 경제 전반에 미치는 효과는 산업·소비 등 다양한 측면에서 복합적으로 나타납니다.",
                relatedArticle = q.relatedArticle,
            ),
            isLast = false,
            quizIndex = 1,
            totalCount = 4,
            onNext = {},
            onBack = {},
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
                keyword = "원화 강세는 환율 하락을 의미합니다.",
                relatedArticle = q.relatedArticle,
            ),
            isLast = true,
            quizIndex = 3,
            totalCount = 4,
            onNext = {},
            onBack = {},
            onArticleClick = {},
        )
    }
}
