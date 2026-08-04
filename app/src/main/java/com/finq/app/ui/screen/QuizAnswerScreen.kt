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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
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
import com.finq.app.data.repository.ReviewStage
import com.finq.app.ui.components.AdBanner
import com.finq.app.ui.theme.FinQTheme
import kotlinx.coroutines.launch
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.Error
import com.finq.app.ui.theme.ErrorFaint
import androidx.compose.ui.graphics.ColorFilter
import com.finq.app.ui.theme.Grass1
import com.finq.app.ui.theme.Grass3
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
 *  - 본문은 [QuizAnswerBody] — 세션 밖(오답노트 상세)에서도 같은 모양으로 재사용한다.
 *  - 하단 다음/결과 보기 (라임 풀블리드 — 퀴즈 화면 CTA 와 동일)
 *
 * 세션 전용 chrome(진행도 도트 · 하단 CTA)은 이 컴포저블에만 있다.
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
    /** 복습에서 이 문제를 완전히 익혔을 때 축하 배너를 띄운다. */
    graduated: Boolean = false,
    /** 졸업 배너 **부제**. 제목("나무가 됐어요")은 배너가 고정으로 갖는다. */
    graduatedMessage: String? = null,
    /** 졸업하지 않은 복습에서 "다음 물 주기: M월 D일" 안내. graduated 면 무시. */
    nextReviewText: String? = null,
    /** 채점 후 복습 단계(0~2). 복습이 아니면 null — [QuizAnswerBody] 참조. */
    reviewStage: Int? = null,
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
            // 단계 아이콘 없음 — categoryLabel 이 이미 단계명을 말하고,
            // 18dp 는 Material 광학 최소(20dp) 아래라 획이 뭉갠다.
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
            QuizAnswerBody(
                quiz = quiz,
                answer = answer,
                onArticleClick = onArticleClick,
                graduated = graduated,
                graduatedMessage = graduatedMessage,
                nextReviewText = nextReviewText,
                reviewStage = reviewStage,
            )

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

/**
 * 채점 결과 본문 — 세션 chrome 없이 "무엇을 틀렸고 왜 그런가"만 담는다.
 *
 * 퀴즈 세션의 [QuizAnswerScreen] 과 보관함의 오답노트 상세가 이걸 공유한다.
 * 같은 내용을 두 레이아웃으로 각각 유지하지 않기 위한 유일한 본문이다 —
 * 스크롤은 호출자가 감싼다(세션은 CTA 위에서, 상세는 화면 전체에서).
 */
@Composable
fun QuizAnswerBody(
    quiz: Quiz,
    answer: AnswerResult,
    onArticleClick: (RelatedArticle) -> Unit,
    modifier: Modifier = Modifier,
    /** 복습에서 이 문제를 완전히 익혔을 때 축하 배너. */
    graduated: Boolean = false,
    /** 졸업 배너 부제(예: "당신의 5번째 나무"). */
    graduatedMessage: String? = null,
    /** 졸업하지 않은 복습의 "다음 물 주기" 안내. [graduated] 면 무시. */
    nextReviewText: String? = null,
    /**
     * 채점 **후** 복습 단계(0~2). 복습이 아니면 null.
     * 맞히면 오르고 틀리면 0으로 리셋되는 값이라 "얼마나 자랐나"의 유일한 진실이다
     * (waterCount 는 누적 시도라 진척이 아니다).
     */
    reviewStage: Int? = null,
) {
    Column(modifier = modifier) {
        // ── 복습 보상: 졸업(나무) / 성장 게이지 / 다음 물 주기 (헤더 자리) ──
        //
        // 오답일 땐 게이지를 아예 그리지 않는다. 틀리면 stage 가 0 으로 리셋되는데,
        // 그 사실을 화면이 말하면 "쌓은 게 날아갔다"가 결과 화면의 주제가 된다.
        // 지금까지 이 규칙은 조용히 돌아갔고 아무도 불편해하지 않았다 — 굳이 꺼내지 않는다.
        when {
            graduated -> {
                GraduatedBanner(message = graduatedMessage)
                Spacer(Modifier.height(18.dp))
            }

            reviewStage != null && answer.isCorrect -> {
                ReviewGrowthBand(stage = reviewStage, nextReviewText = nextReviewText)
                Spacer(Modifier.height(18.dp))
            }

            nextReviewText != null -> {
                Text(
                    text = nextReviewText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(18.dp))
            }
        }

        // ── 문제 (다크 배경 위 흰 텍스트) ────────────────────
        Text(
            text = "Q. ${quiz.question}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(14.dp))

        // ── 판정 밴드 — 선지 '바로 위'가 의도된 자리 ──────────
        // 문제 위에 두면 시선이 판정을 지나쳐 정답(라임 면)에 먼저 닿아
        // "맞혔다"로 오독한다(실사용 보고). 선지를 읽기 직전에 판정을
        // 통과시켜 "초록 = 내 결과"라는 오해를 차단한다.
        VerdictBand(isCorrect = answer.isCorrect)
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

        // ── 해설 카드 ───────────────────────────────────────
        ExplanationCard(explanation = answer.explanation)

        // ── 알아두면 좋아요 ──────────────────────────────────
        if (!answer.keyword.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            KeywordCard(keyword = answer.keyword)
        }

        // ── 관련 기사 ────────────────────────────────────────
        if (answer.relatedArticle != RelatedArticle.EMPTY &&
            answer.relatedArticle.title.isNotBlank()
        ) {
            // 카드들과 다른 종류의 블록이므로 여백을 한 단 더 준다
            // (블록 간 여백 > 블록 내 여백이어야 경계가 보인다).
            Spacer(Modifier.height(20.dp))
            RelatedArticleRow(
                article = answer.relatedArticle,
                onClick = { onArticleClick(answer.relatedArticle) },
            )
        }
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

/**
 * 복습 졸업 — 마지막 물주기를 마친 순간.
 *
 * 성장 밴드([ReviewGrowthBand])와 **같은 형태**를 쓴다. 종전엔 졸업만 아이콘 하나짜리
 * 배너로 형태가 바뀌어서, 사다리가 네 칸을 약속해놓고 마지막 칸이 채워지는 걸
 * 사용자가 끝내 못 봤다. 같은 자리에서 끝 칸이 라임으로 켜져야 "세 번 물을 주면
 * 나무"가 눈으로 완결된다.
 */
@Composable
private fun GraduatedBanner(message: String? = null) {
    StageBand(
        title = "나무가 됐어요",
        subtitle = message,
        stage = 0,
        graduated = true,
    )
}

/**
 * 복습 성장 밴드 — 맞혀서 한 단계 자란 순간.
 *
 * 사다리를 **단계 아이콘 4개**(새싹 · 풀 · 나무 직전 · 나무)로 그린다. 종전엔 추상 도트
 * 2개 뒤에 나무 하나였는데, 같은 줄에서 추상과 구체가 섞여 셋이 한 종류로 안 읽혔다.
 * 게다가 복습 세션 헤더는 이미 "새싹 · 증시"처럼 **단계 이름**으로 말하고 있어서,
 * 같은 개념을 화면마다 다른 언어(동그라미 vs 이름)로 설명하는 상태였다.
 *
 * **3개가 아니라 4개인 이유**: 실제 사다리가 넷이다. 오답 등록이 새싹(stage 0)이고
 * 맞힐 때마다 풀(1) → 나무 직전(2) → 나무(졸업)로 간다 — [ReviewStageTimeline] 이
 * 그리는 것과 같은 순서이고, 아이콘도 같은 자산을 쓴다.
 */
@Composable
private fun ReviewGrowthBand(stage: Int, nextReviewText: String?) {
    StageBand(
        title = "${ReviewStage.of(stage).label}까지 자랐어요",
        subtitle = nextReviewText,
        stage = stage,
        graduated = false,
    )
}

/**
 * 복습 결과 밴드 — 문구는 왼쪽, 진행 사다리는 오른쪽 끝.
 *
 * 사다리를 문구 **앞**에 두면 문구 시작점이 아이콘 개수에 따라 정해져 화면의 어떤
 * 기준선에도 안 붙는다(문제·선지·해설은 전부 왼쪽 패딩에서 시작한다). 진행 표시를
 * 뒤로 보내면 문구가 그 기준선에 붙고, 사다리가 몇 칸이 되든 레이아웃이 안 흔들린다.
 *
 * 문구 쪽에 weight 를 줘서, 큰 글꼴에서는 사다리를 밀어내는 대신 문구가 줄바꿈된다.
 */
@Composable
private fun StageBand(
    title: String,
    subtitle: String?,
    stage: Int,
    graduated: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Grass1)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Lime,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
        Spacer(Modifier.size(10.dp))
        StageLadder(stage = stage, graduated = graduated)
    }
}

/**
 * 단계 사다리 — 새싹 · 풀 · 나무 직전 · 나무. [ReviewStageTimeline] 과 같은 자산·같은 순서.
 *
 * 상태를 색과 모양 **둘 다**로 말한다 — 지나온 단계 Grass3, 현재 Lime, 남은 단계는
 * 흐린 Lime. 모양(새싹/풀/나무)까지 다르므로 색각 이상이나 저조도에서도 진행이 읽힌다.
 * 종전 도트는 색에만 의존했고 안 채운 도트는 Outline(네이비)이라 밴드 배경에 묻혔다.
 *
 * 26dp — 디테일 아이콘 하한(20dp, Material Symbols opsz 최소)보다 넉넉히 위다.
 * 사다리 폭은 4×26 + 3×9 = 125dp 라, 좁은 기기에서도 문구 쪽 weight 가 먼저 줄면서
 * 줄바꿈으로 흡수한다(사다리를 밀어내지 않는다).
 */
@Composable
private fun StageLadder(stage: Int, graduated: Boolean) {
    val ladder = ReviewStage.entries.map { it.iconRes } + R.drawable.ic_stage_tree
    // 졸업이면 끝 칸(나무)이 현재 위치다 — 나머지는 전부 지나온 단계.
    val currentIndex = if (graduated) ladder.lastIndex else stage
    Row(
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ladder.forEachIndexed { index, iconRes ->
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    when {
                        index < currentIndex -> Grass3
                        index == currentIndex -> Lime
                        // 0.30 은 실기기에서 "나무·나무 직전이 흐리다"는 지적을 받았다.
                        // 아직 아니라는 건 알리되 형태는 읽혀야 한다.
                        else -> Lime.copy(alpha = 0.45f)
                    },
                ),
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

/**
 * 채점 판정 밴드 — 정답/오답에 같은 시각적 무게를 준다.
 *
 * 종전에는 오답이 회색 캡션이라 화면에서 가장 센 신호가 정답(라임 면)이었고,
 * 시선이 초록에 먼저 닿아 "맞혔다"로 오독하는 일이 실제로 있었다. 판정이
 * 절실한 쪽(오답)에 정답과 같은 무게의 밴드를 준다. 오답 강조는 이 밴드가
 * 가져가고 선지 쪽은 면 없이 테두리·라벨만 — 면 대 면 경쟁(52.5% 오해 실험)을
 * 피하는 원칙은 그대로다.
 *
 * 알약이 아니라 밴드인 이유: 알약은 액션 전용(디자인 규칙). 누를 수 없는
 * 판정은 GraduatedBanner 와 같은 풀폭 밴드 형태를 쓴다 — "이 자리의 밴드 =
 * 이번 결과"라는 규칙이 생긴다.
 *
 * liveRegion: 화면 내 전이(재풀이 시도 → 결과)에서도 TalkBack 이 판정을
 * 읽어주도록 한다 — 색·아이콘만으로 알리지 않는다.
 */
@Composable
private fun VerdictBand(isCorrect: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCorrect) Grass1 else ErrorFaint)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(
                if (isCorrect) R.drawable.ic_check_circle else R.drawable.ic_x_circle,
            ),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.size(10.dp))
        Text(
            text = if (isCorrect) "맞혔어요" else "틀렸어요",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isCorrect) Lime else Error,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 보기 행 — 정답 하나만 강조, 내 답은 중립 라벨
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 보기 한 줄 — 3단 위계.
 *
 *  1) 정답: 틴트 면 + 라임 테두리 + 라임 라벨. **면(fill)은 여전히 정답만** 가진다.
 *  2) 내가 고른 오답: 면 없이 Error 테두리·번호·라벨로만 지목한다.
 *     색 면을 주지 않는 이유 — 면 대 면이 되면 화면에서 정답과 세기가 맞붙는다
 *     (내 답·정답·정오표시를 다 강조하면 52.5% 가 정답을 오해한 실험).
 *  3) 안 고른 선지: 배경 면을 지우고 글자를 muted 로 낮춰 **뒤로 물린다** —
 *     내 답과 1dp 차이 테두리만으로는 구별이 안 된다는 실사용 보고의 답이다.
 *     강조를 더하는 대신 나머지를 빼서 위계를 만든다.
 *
 * 맞힌 경우엔 두 라벨이 같은 줄을 가리키므로 "내 답 · 정답" 하나로 합친다.
 */
@Composable
private fun AnswerOptionRow(
    option: QuizOption,
    isCorrect: Boolean,
    isUserSelected: Boolean,
) {
    val wrongPick = isUserSelected && !isCorrect
    val marked = isCorrect || wrongPick
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isCorrect -> Grass1
                    wrongPick -> BgSurface
                    else -> Color.Transparent // 안 고른 선지는 면을 지워 뒤로 물린다
                },
            )
            .border(
                width = if (marked) 2.dp else 1.dp,
                color = when {
                    isCorrect -> Lime
                    wrongPick -> Error
                    else -> Outline
                },
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
                    when {
                        isCorrect -> Lime
                        wrongPick -> Error
                        else -> BgElevated
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${option.optionNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    isCorrect -> OnLime
                    wrongPick -> ErrorFaint
                    else -> TextMuted
                },
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = option.text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (marked) TextPrimary else TextMuted,
            fontWeight = if (marked) FontWeight.SemiBold else FontWeight.Normal,
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
                    // 맞힌 문제에선 색 블록이 하나로 유지되도록 라벨을 병합한다.
                    text = if (isUserSelected) "내 답 · 정답" else "정답",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnLime,
                )
            }
        } else if (isUserSelected) {
            Spacer(Modifier.size(8.dp))
            Text(
                text = "내 답",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Error,
            )
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
// 관련 기사 — 이 화면에서 유일한 "누를 수 있는" 블록
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 관련 기사 — 이 화면에서 **유일하게 누를 수 있는 콘텐츠 블록**.
 *
 * 카드를 쓰지 않는다. 해설·알아두면 좋아요가 이미 같은 모양의 카드라, 배경을 한 단계
 * 밝히고 테두리를 두르는 정도로는 "다른 종류"가 전달되지 않았다(실사용 지적) —
 * 다크 화면에서 그 명도차는 그냥 "조금 밝은 카드"로 읽힌다. 색을 더 밝히는 대신
 * **형태를 바꾼다**: 면을 지우고 구분선 위의 행으로 두면 위 두 블록과 즉시 갈린다.
 * 오답노트 목록이 카드→구분선 행으로 간 것과 같은 문법이고, 이 화면의 면도 하나 줄어든다.
 *
 * 라벨과 글리프에 라임을 쓰는 건 이 앱 규칙(라임 = 누를 수 있음)에 맞춘 것이다.
 * 글리프가 셰브론이 아니라 **외부 링크**인 이유: 탭하면 브라우저로 나간다.
 * 셰브론은 "앱 안으로 들어간다"는 뜻이라 여기선 거짓말이 된다.
 */
@Composable
private fun RelatedArticleRow(article: RelatedArticle, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = Outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "관련 기사",
                    style = MaterialTheme.typography.labelMedium,
                    color = Lime,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = article.source,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
            Spacer(Modifier.size(12.dp))
            Image(
                painter = painterResource(R.drawable.ic_external_link),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Lime),
                modifier = Modifier.size(20.dp),
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
