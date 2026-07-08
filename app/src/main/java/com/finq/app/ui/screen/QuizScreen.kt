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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.data.DummyQuizData
import com.finq.app.data.model.Quiz
import com.finq.app.data.model.QuizOption
import com.finq.app.ui.theme.BrandNavy
import com.finq.app.ui.theme.BrandNavyDeep
import com.finq.app.ui.theme.TextStrong
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.OutlineStrong
import com.finq.app.ui.theme.TextMuted

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
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyDeep)
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
            Text(
                text = "Q${quizIndex + 1} · ${quiz.category.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.weight(1f))
            // 닫기 아이콘과 좌우 시각 균형을 맞추기 위한 보이지 않는 placeholder.
            Box(modifier = Modifier.size(32.dp))
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
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.18f),
                        RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 20.dp, vertical = 22.dp),
            ) {
                Text(
                    text = quiz.question,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
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
                containerColor = Color.White,
                contentColor = BrandNavy,
                disabledContainerColor = Color.White.copy(alpha = 0.4f),
                disabledContentColor = BrandNavy.copy(alpha = 0.6f),
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
                    color = BrandNavy,
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
                        if (done) Color.White
                        else Color.White.copy(alpha = 0.25f)
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
            .background(Color.White)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) BrandNavy else Color.Transparent,
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
                    if (selected) BrandNavy
                    else OutlineStrong
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${option.optionNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else TextMuted,
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = option.text,
            style = MaterialTheme.typography.bodyLarge,
            color = TextStrong,
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
