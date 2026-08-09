package com.finq.app.ui.screen

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finq.app.R
import com.finq.app.data.DummyQuizData
import com.finq.app.data.model.Quiz
import com.finq.app.data.model.QuizOption
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.components.QuizNightSky
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary

/**
 * 퀴즈 풀이 화면 — 풀블리드 네이비 배경 + 흰 카드.
 */
@Composable
fun QuizScreen(
    quizIndex: Int,
    totalCount: Int,
    quiz: Quiz,
    selectedOptionId: Long?,
    onSelectOption: (Long) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    isSubmitting: Boolean = false,
    /** 헤더 카테고리 라벨 override. 복습처럼 서버가 라벨을 직접 주는 경우에 쓴다. */
    categoryLabel: String? = null,
    /** 헤더 아래 안내 한 줄 (예: "복습은 기록에 영향 없어요"). null 이면 표시하지 않는다. */
    headerNote: String? = null,
    /** 북마크 토글 상태. [onToggleBookmark] 가 null 이면 아이콘을 그리지 않는다(복습 화면 등). */
    bookmarked: Boolean = false,
    onToggleBookmark: (() -> Unit)? = null,
) {
    Box(modifier.fillMaxSize()) {
        QuizNightSky(Modifier.matchParentSize())
        QuizContent(
            quizIndex = quizIndex,
            totalCount = totalCount,
            quiz = quiz,
            selectedOptionId = selectedOptionId,
            onSelectOption = onSelectOption,
            onSubmit = onSubmit,
            onClose = onClose,
            isSubmitting = isSubmitting,
            categoryLabel = categoryLabel,
            headerNote = headerNote,
            bookmarked = bookmarked,
            onToggleBookmark = onToggleBookmark,
        )
    }
}

@Composable
private fun QuizContent(
    quizIndex: Int,
    totalCount: Int,
    quiz: Quiz,
    selectedOptionId: Long?,
    onSelectOption: (Long) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
    isSubmitting: Boolean,
    categoryLabel: String?,
    headerNote: String?,
    bookmarked: Boolean,
    onToggleBookmark: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // ── 진행도 + 닫기 + 즐겨찾기 ────────────────────────────
        // 아직 풀지 않은 문제이므로 채워진 칸은 quizIndex 개다.
        ProgressDotsHeader(doneCount = quizIndex, totalCount = totalCount)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            // 단계 아이콘 없음 — categoryLabel 이 이미 단계명("새싹 · 금리")을 말하고,
            // 18dp 는 Material 광학 최소(20dp) 아래라 획이 뭉갠다.
            Text(
                text = "Q${quizIndex + 1} · ${categoryLabel ?: quiz.category.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.weight(1f))
            if (onToggleBookmark != null) {
                // 북마크 토글 — 채워짐(Lime)/빈 별(TextMuted)은 드로어블이 테마 토큰으로 정의.
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
            } else {
                // 닫기 아이콘과 좌우 시각 균형을 맞추기 위한 보이지 않는 placeholder.
                Box(modifier = Modifier.size(32.dp))
            }
        }

        if (headerNote != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = headerNote,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── 질문 ─────────────────────────────────────────────
            //
            // 면을 두르지 않는다. 종전엔 BgElevated + Outline 테두리 카드였는데,
            // 질문은 **누를 수 없는 텍스트**인데 바로 아래 선지(면 + 테두리)와 같은
            // 문법이라 화면에 같은 모양 박스가 다섯 개였다. 어느 것이 고를 수 있는
            // 것인지 형태로 갈리지 않았다("왜 카드인지 모르겠다"는 실사용 지적).
            // 면을 걷으면 이 화면에서 **테두리를 가진 것은 선지뿐**이 되어,
            // 테두리가 "고를 수 있는 것"이라는 뜻을 얻는다.
            //
            // 채점 후 화면은 이미 질문을 면 없는 텍스트로 두고 있었다. 같은 것을 두
            // 화면이 다르게 그리고 있었고, 그쪽이 규칙에 맞았다.
            //
            // 굵기를 Bold 에서 낮추고 행간을 키운다. 지문이 길 때 크고 굵은 글씨는
            // 읽히는 게 아니라 벽이 된다("너무 하얗고 두껍다").
            Text(
                text = quiz.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 27.sp,
                color = TextPrimary,
            )
            Spacer(Modifier.height(20.dp))

            // ── 보기 카드 4개 ────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                quiz.options.forEach { option ->
                    OptionCard(
                        option = option,
                        selected = option.id == selectedOptionId,
                        enabled = !isSubmitting,
                        onClick = { onSelectOption(option.id) },
                    )
                }
            }
            // 광고 없음 — **문제를 푸는 화면에는 배너를 두지 않는다**(§7).
            // 선지 아래는 `정답 확인` 이 고정으로 앉는 자리라, 배너를 넣으면 한 세션에
            // 다섯 번 누르는 버튼 바로 위에 광고가 붙는다.
            Spacer(Modifier.height(4.dp))
        }

        // 스크롤 영역과 하단 CTA 사이 여백
        Spacer(Modifier.height(4.dp))

        // ── 하단 정답 확인 버튼 (화이트 풀블리드) ────────────────
        Button(
            onClick = onSubmit,
            enabled = selectedOptionId != null && !isSubmitting,
            colors = ButtonDefaults.buttonColors(
                containerColor = Lime,
                contentColor = OnLime,
                disabledContainerColor = Lime.copy(alpha = 0.35f),
                disabledContentColor = OnLime.copy(alpha = 0.5f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = OnLime,
                )
            } else {
                Text(
                    text = "정답 확인",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

/**
 * 진행 막대 — 채워진 칸은 **푼 문제 수**다.
 *
 * 종전엔 `i <= quizIndex` 라 **지금 풀고 있는 문제까지** 채웠다. 아직 답을 고르지도
 * 않았는데 완료로 보였다(§4 가장 큰 참말). 화면마다 "푼 문제"의 값이 다르므로
 * 판단을 호출자에게 넘긴다 — 풀이 중이면 quizIndex, 채점 후면 quizIndex + 1.
 */
@Composable
private fun ProgressDotsHeader(doneCount: Int, totalCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(totalCount.coerceAtLeast(1)) { i ->
            val done = i < doneCount
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

@Composable
private fun OptionCard(
    option: QuizOption,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            // 유리 선지 — 뒤의 밤하늘이 비친다. 면을 얇게 깔고 흰 hairline 으로
            // 가장자리를 세운 뒤, 위쪽에 광원(sheen)을 준다.
            .background(BgSurface.copy(alpha = GLASS_ALPHA))
            .background(
                Brush.verticalGradient(
                    0f to Color.White.copy(alpha = 0.07f),
                    0.55f to Color.Transparent,
                )
            )
            // 선택은 **테두리만**으로 말한다. 유리에서는 면을 밝히려 해도 뒤의
            // 어두운 하늘이 비쳐 오히려 어두워진다 — 신호가 거꾸로 간다.
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Lime else GlassBorder,
                shape = shape,
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (selected) Lime
                    else BgElevated
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${option.optionNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) OnLime else TextMuted,
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = option.text,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun QuizScreenUnselectedPreview() {
    FinQTheme {
        QuizScreen(
            quizIndex = 1,
            totalCount = 4,
            quiz = DummyQuizData.todayQuizzes.first(),
            selectedOptionId = null,
            onSelectOption = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun QuizScreenSelectedPreview() {
    FinQTheme {
        QuizScreen(
            quizIndex = 1,
            totalCount = 4,
            quiz = DummyQuizData.todayQuizzes[1],
            selectedOptionId = DummyQuizData.todayQuizzes[1].options.last().id,
            onSelectOption = {},
            onSubmit = {},
        )
    }
}

/**
 * 유리 선지의 면 알파. 뒤의 밤하늘이 비칠 만큼 얇다.
 * 0.28 도 봤지만 0.16 에서 별이 카드 안팎으로 이어져 유리로 읽힌다.
 */
private const val GLASS_ALPHA = 0.16f

/** 유리 가장자리 — 불투명 [Outline] 은 유리가 아니라 상자로 읽힌다. */
private val GlassBorder = Color.White.copy(alpha = 0.18f)
