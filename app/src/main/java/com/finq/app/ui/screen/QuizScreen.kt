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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.data.DummyQuizData
import com.finq.app.data.model.Quiz
import com.finq.app.data.model.QuizOption
import com.finq.app.ui.theme.FinQTheme
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // ── 진행도 + 닫기 + 즐겨찾기 ────────────────────────────
        ProgressDotsHeader(quizIndex = quizIndex, totalCount = totalCount)
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
            // ── 질문 카드 ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgElevated)
                    .border(
                        1.dp,
                        Outline,
                        RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 20.dp, vertical = 22.dp),
            ) {
                Text(
                    text = quiz.question,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
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

@Composable
private fun OptionCard(
    option: QuizOption,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            // 보기 카드 — 기본: BgSurface+Outline / 선택: BgSubtle+Lime 2dp
            .background(if (selected) BgSubtle else BgSurface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Lime else Outline,
                shape = RoundedCornerShape(14.dp),
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
