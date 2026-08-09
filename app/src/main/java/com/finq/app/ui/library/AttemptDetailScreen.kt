package com.finq.app.ui.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.data.model.AttemptItem
import com.finq.app.data.model.Category
import com.finq.app.data.model.Quiz
import com.finq.app.data.model.QuizOption
import com.finq.app.data.model.RelatedArticle
import com.finq.app.data.model.ReviewStatus
import com.finq.app.data.repository.AnswerResult
import com.finq.app.ui.screen.QuizAnswerBody
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.components.AdBanner
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextPrimary

/**
 * 보관함 항목 상세 — 채점 화면([QuizAnswerBody])과 같은 본문을 전체화면으로 보여준다.
 *
 * 목록 행이 좁은 이유가 여기 있다: 문제·선지·정답·해설·알아두면 좋아요·관련 기사는
 * 카드 하나에 담을 분량이 아니라 화면 하나의 분량이다. 세션이 아니므로 진행도 도트와
 * "다음 문제" CTA 는 없고, 뒤로 가면 목록의 그 자리로 돌아간다.
 *
 * 헤더에 카테고리를 항상 세운다 — 목록에서 카테고리를 보고 들어왔는데 상세에서
 * 사라지면 "무엇에 관한 문제였는지"의 맥락이 끊긴다.
 */
@Composable
fun AttemptDetailScreen(
    /** 헤더에 쓸 가장 최신 정보(상세가 왔으면 상세, 아니면 목록 요약). 둘 다 없으면 null. */
    item: AttemptItem?,
    /** [item] 이 본문(선지·해설)까지 갖춘 상태인가. false 면 로딩/에러만 그린다. */
    detailReady: Boolean,
    isLoading: Boolean,
    error: String?,
    bookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onArticleClick: (RelatedArticle) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        DetailHeader(
            item = item,
            bookmarked = bookmarked,
            onToggleBookmark = onToggleBookmark,
            onBack = onBack,
        )

        Spacer(Modifier.height(18.dp))

        Box(modifier = Modifier.weight(1f)) {
            when {
                // 목록과 같은 컴포저블 — 실패 화면이 화면마다 달라 보이지 않게.
                error != null && !isLoading -> AttemptLoadErrorState(onRetry = onRetry)

                item != null && detailReady && !isLoading -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    QuizAnswerBody(
                        quiz = item.toQuiz(),
                        answer = item.toAnswerResult(),
                        onArticleClick = onArticleClick,
                    )
                    Spacer(Modifier.height(24.dp))
                }

                else -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Lime)
                }
            }
        }

        // ── 배너 광고 ─────────────────────────────────────────────
        // 해설 아래·화면 맨 아래에 고정. 채점 화면은 스크롤 콘텐츠 안에 두지만
        // 여기는 CTA 가 없어 아래가 비므로 화면에 붙이는 편이 자리가 분명하다.
        AdBanner(horizontalPaddingDp = 40, anchored = true) // 화면 좌우 패딩 20+20
    }
}

/** ‹ 뒤로 · 카테고리 · 날짜 · 북마크 — 채점 화면 헤더와 같은 자리·같은 크기. */
@Composable
private fun DetailHeader(
    item: AttemptItem?,
    bookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onBack: () -> Unit,
) {
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
                colorFilter = ColorFilter.tint(TextPrimary),
            )
        }
        Spacer(Modifier.weight(1f))
        if (item != null) {
            val date = formatSolvedDate(item.solvedAtIso)
            Text(
                text = if (date != null) "${item.categoryDisplay} · $date" else item.categoryDisplay,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clickable(onClick = onToggleBookmark),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(
                    if (bookmarked) R.drawable.ic_bookmark_star_filled
                    else R.drawable.ic_bookmark_star,
                ),
                contentDescription = if (bookmarked) "북마크 해제" else "북마크",
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 어댑터 — 보관함 항목을 채점 화면 본문이 먹는 모양으로.
// (ui/review/ReviewAdapters.kt 의 toQuiz()/toAnswerResult() 와 같은 패턴)
// ─────────────────────────────────────────────────────────────────────────────

/** 상세 본문이 쓰는 것은 question/options 뿐 — 나머지는 기본값. */
fun AttemptItem.toQuiz(): Quiz = Quiz(
    id = quizId,
    category = category,
    question = question,
    options = choices,
)

/**
 * 채점 결과 형태로 변환.
 *
 * 선택 기록이 없는 legacy 항목은 [AttemptItem.selectedChoiceId] 가 null 이라
 * 어떤 선지와도 매칭되지 않는 값을 넣는다 → 보기 4개 중 "내 답" 표시만 빠진다.
 */
fun AttemptItem.toAnswerResult(): AnswerResult = AnswerResult(
    quizId = quizId,
    selectedOptionId = selectedChoiceId ?: NO_OPTION,
    isCorrect = correct,
    correctOptionId = correctChoiceId ?: NO_OPTION,
    explanation = explanation,
    keyword = keyword,
    relatedArticle = article ?: RelatedArticle.EMPTY,
)

/** 어떤 선지 id 와도 겹치지 않는 값 — 서버 id 는 항상 양수다. */
private const val NO_OPTION = -1L

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun AttemptDetailScreenPreview() {
    FinQTheme {
        AttemptDetailScreen(
            item = AttemptItem(
                quizId = 1L,
                category = Category.selectable.first(),
                question = "물가가 계속 오를 때 중앙은행이 취하는 대표적 정책은?",
                choices = listOf(
                    QuizOption(1L, 1, "기준금리를 올린다"),
                    QuizOption(2L, 2, "기준금리를 내린다"),
                    QuizOption(3L, 3, "지급준비율을 낮춘다"),
                    QuizOption(4L, 4, "국채를 매입한다"),
                ),
                selectedChoiceId = 2L,
                correctChoiceId = 1L,
                correct = false,
                explanation = "물가 상승을 억제하려면 기준금리를 올려 시중 유동성을 줄인다.",
                keyword = "기준금리 — 중앙은행이 정하는 정책금리",
                article = null,
                bookmarked = true,
                solvedAtIso = "2026-07-20T09:00:00",
                review = ReviewStatus(1, 1, 1, false, null),
            ),
            detailReady = true,
            isLoading = false,
            error = null,
            bookmarked = true,
            onToggleBookmark = {},
            onRetry = {},
            onBack = {},
            onArticleClick = {},
        )
    }
}
