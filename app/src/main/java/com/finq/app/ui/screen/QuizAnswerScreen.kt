package com.finq.app.ui.screen

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.finq.app.R
import com.finq.app.data.DummyQuizData
import com.finq.app.data.model.Quiz
import com.finq.app.data.model.QuizOption
import com.finq.app.data.model.RelatedArticle
import com.finq.app.data.repository.AnswerResult
import com.finq.app.data.repository.LibraryRepository
import com.finq.app.ui.components.AdBanner
import com.finq.app.ui.theme.FinQTheme
import kotlinx.coroutines.launch
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.Error
import com.finq.app.ui.theme.ErrorFaint
import com.finq.app.ui.theme.Grass1
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary

/**
 * 퀴즈 정답/해설 화면 — Stateless View.
 *
 * QuizScreen 과 동일한 풀블리드 네이비 톤으로 통일.
 *  - 상단: 닫기/뒤로 + 진행도 도트 + 카테고리
 *  - 정답/오답 칩 (블루 또는 레드 채움)
 *  - 보기 4개 (흰 카드, 정답은 블루, 사용자 오답은 레드)
 *  - 해설 카드 (흰 배경)
 *  - 알아두면 좋아요 (옐로우 콜아웃)
 *  - 하단 다음/결과 보기 (흰 풀블리드 — 퀴즈 화면 CTA 와 동일)
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
    /**
     * 세션 공유 북마크 상태 — 풀이 화면에서 켠 북마크가 여기서도 켜져 보이도록
     * ViewModel 상태를 직접 받는다. null 이면 기존 내부 상태(initialBookmarked) 방식.
     */
    bookmarked: Boolean? = null,
    onToggleBookmark: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    /** 헤더 카테고리 라벨 override. 복습처럼 서버가 라벨을 직접 주는 경우에 쓴다. */
    categoryLabel: String? = null,
    /** 복습 헤더 등에서 카테고리 앞에 붙일 단계 아이콘. null 이면 아이콘 없음. */
    categoryIconRes: Int? = null,
    /** 복습에서 이 문제를 완전히 익혔을 때 축하 배너를 띄운다. */
    graduated: Boolean = false,
    /** 졸업 배너 문구 override. null 이면 기본 "이 문제를 완전히 익혔어요! 나무가 됐어요". */
    graduatedMessage: String? = null,
    /** 졸업하지 않은 복습에서 "다음 물 주기: M월 D일" 안내. graduated 면 무시. */
    nextReviewText: String? = null,
    /** 하단 CTA 라벨 override (예: "다음 복습"). */
    nextLabel: String? = null,
) {
    // 외부(세션 ViewModel) 상태가 오면 그걸 쓰고, 아니면 화면 내부 상태로 동작한다.
    var localBookmarked by remember(quiz.id) { mutableStateOf(initialBookmarked) }
    val shownBookmarked = bookmarked ?: localBookmarked
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // ── 상단 진행도 도트 (퀴즈 화면과 동일) ──────────────────
        ProgressDotsHeader(quizIndex = quizIndex, totalCount = totalCount)
        Spacer(Modifier.height(12.dp))

        // ── 뒤로 + 카테고리 + 북마크 ────────────────────────────
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
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(TextPrimary),
                )
            }
            Spacer(Modifier.weight(1f))
            if (categoryIconRes != null) {
                Image(
                    painter = painterResource(categoryIconRes),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(5.dp))
            }
            Text(
                text = "Q${quizIndex + 1} · ${categoryLabel ?: quiz.category.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.weight(1f))
            if (onToggleBookmark != null || libraryRepository != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            if (onToggleBookmark != null) {
                                onToggleBookmark()
                            } else if (libraryRepository != null) {
                                // 레거시 경로 — 화면 내부에서 낙관적 토글 + 실패 롤백.
                                val next = !localBookmarked
                                localBookmarked = next
                                scope.launch {
                                    runCatching {
                                        if (next) libraryRepository.addBookmark(quiz.id)
                                        else libraryRepository.removeBookmark(quiz.id)
                                    }.onFailure { localBookmarked = !next }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(
                            if (shownBookmarked) R.drawable.ic_bookmark_star_filled
                            else R.drawable.ic_bookmark_star,
                        ),
                        contentDescription = if (shownBookmarked) "북마크 해제" else "북마크",
                        modifier = Modifier.size(22.dp),
                    )
                }
            } else {
                Box(modifier = Modifier.size(32.dp))
            }
        }

        Spacer(Modifier.height(18.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── 정답/오답 칩 ─────────────────────────────────────
            ResultChip(isCorrect = answer.isCorrect)

            // ── 복습 보상: 졸업(나무) 또는 다음 물 주기 안내 ──────────
            if (graduated) {
                Spacer(Modifier.height(12.dp))
                GraduatedBanner(message = graduatedMessage)
            } else if (nextReviewText != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = nextReviewText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.height(18.dp))

            // ── 문제 (다크 배경 위 흰 텍스트) ────────────────────
            Text(
                text = "Q. ${quiz.question}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
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

            // ── 해설 카드 (흰 풀블리드) ─────────────────────────
            ExplanationCard(explanation = answer.explanation)

            // ── 알아두면 좋아요 (옐로우) ────────────────────────
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

            // ── 배너 광고 (스크롤 콘텐츠 맨 아래 — CTA 를 가리지 않는 자리) ──
            Spacer(Modifier.height(16.dp))
            AdBanner(horizontalPaddingDp = 40) // 화면 좌우 패딩 20+20
            Spacer(Modifier.height(4.dp))
        }

        // 스크롤 영역과 하단 CTA 사이 여백
        Spacer(Modifier.height(4.dp))

        // ── 다음 / 결과 보기 (흰 풀블리드 — 퀴즈 화면 CTA 와 동일) ──
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = Lime,
                contentColor = OnLime,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                text = nextLabel ?: if (isLast) "결과 보기" else "다음 문제",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 상단 진행도 도트 (QuizScreen 과 동일 비주얼)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProgressDotsHeader(quizIndex: Int, totalCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(totalCount.coerceAtLeast(1)) { i ->
            val done = i <= quizIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (done) Lime
                        else Outline
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 정답/오답 칩 — 다크 배경 위에 채움형
// ─────────────────────────────────────────────────────────────────────────────

/** 복습 졸업 — 이 문제를 완전히 익혔을 때. */
@Composable
private fun GraduatedBanner(message: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Grass1)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_stage_tree),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.size(10.dp))
        Text(
            text = message ?: "이 문제를 완전히 익혔어요! 나무가 됐어요",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
    }
}

@Composable
private fun ResultChip(isCorrect: Boolean) {
    val accent = if (isCorrect) Lime else Error
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(accent)
            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(BgSurface),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Q",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accent,
            )
        }
        Spacer(Modifier.size(8.dp))
        Text(
            text = if (isCorrect) "정답이에요!" else "아쉬워요, 다음에 맞혀봐요",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = OnLime,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 보기 행 — 흰 카드, 정답/오답에 따라 컬러 보더
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnswerOptionRow(
    option: QuizOption,
    isCorrect: Boolean,
    isUserSelected: Boolean,
) {
    // 보기 카드 4상태 — 기본: BgSurface+Outline
    //   정답 공개: Grass1 배경 + Lime 테두리 / 오답 공개: ErrorFaint 배경 + Error 테두리
    val (background, border, accent) = when {
        isCorrect -> Triple(Grass1, Lime, Lime)
        isUserSelected -> Triple(ErrorFaint, Error, Error)
        else -> Triple(BgSurface, Outline, TextMuted)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(
                width = if (isCorrect || isUserSelected) 2.dp else 1.dp,
                color = border,
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
                    else BgElevated
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${option.optionNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCorrect || isUserSelected) OnLime else TextMuted,
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = option.text,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            fontWeight = if (isCorrect || isUserSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        if (isCorrect) {
            Spacer(Modifier.size(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Lime)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "정답",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnLime,
                )
            }
        } else if (isUserSelected) {
            Spacer(Modifier.size(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Error)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "내 선택",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnLime,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 해설 카드 (다크 배경 위 흰 카드)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExplanationCard(explanation: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgSurface)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "해설",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 알아두면 좋아요 (옐로우)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun KeywordCard(keyword: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgElevated)
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
                    color = TextPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = keyword,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 관련 기사 — 다크 배경에 어울리는 톤
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RelatedArticleCard(article: RelatedArticle, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = BgSubtle),
        border = BorderStroke(1.dp, Outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "관련 기사",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 2,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = article.source,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
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
    FinQTheme {
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
    FinQTheme {
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
