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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.data.DummyQuizData
import com.finq.app.data.model.Quiz
import com.finq.app.data.model.QuizOption
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.Error
import com.finq.app.ui.theme.ErrorFaint
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Grass1
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary

/**
 * 이미 푼 문제(solved=true) 재진입 화면 — 결과 보기 모드.
 *
 * GET /api/quizzes/today 는 이미 푼 문제에 대해 정오답(`correct`)만 알려주고
 * 어떤 보기를 골랐었는지·정답이 무엇이었는지·해설은 주지 않는다(치팅 방지 마스킹).
 * 그래서 이 화면은:
 *  - 보기를 전부 잠근다 — 클릭 핸들러가 없고 선택/정답 표시도 하지 않는다(그 정보가 없으므로).
 *  - `correct` 값 하나로만 정답/오답 배너를 보여준다.
 *  - 오답이었으면 자세한 해설을 볼 수 있는 오답노트(서버 기반, 전체 상세 보유)로 안내한다.
 *
 * 채점 자체가 없으므로 제출 버튼이 없다 — "다음"만 있다. 공식 재도전(다시 풀기)은
 * 이 화면이 아니라 오답노트 → 복습 경로로만 가능하다.
 */
@Composable
fun SolvedQuizReviewScreen(
    quizIndex: Int,
    totalCount: Int,
    quiz: Quiz,
    isLast: Boolean,
    onNext: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onViewWrongNote: (() -> Unit)? = null,
) {
    val isCorrect = quiz.correct

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ReviewProgressDots(quizIndex = quizIndex, totalCount = totalCount)
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
                color = TextPrimary,
            )
            Spacer(Modifier.weight(1f))
            // 닫기 아이콘과 좌우 시각 균형을 맞추기 위한 보이지 않는 placeholder.
            Box(modifier = Modifier.size(32.dp))
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "이미 푼 문제예요 · 다시 채점되지 않아요",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            ReviewResultBanner(isCorrect = isCorrect)
            Spacer(Modifier.height(18.dp))

            // ── 질문 카드 (잠김 — QuizScreen 과 동일한 시각 언어) ──────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgElevated)
                    .border(1.dp, Outline, RoundedCornerShape(16.dp))
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

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                quiz.options.forEach { option -> LockedOptionRow(option = option) }
            }

            if (isCorrect == false && onViewWrongNote != null) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.clickable(onClick = onViewWrongNote),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "자세한 해설은 오답노트에서 확인할 수 있어요",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Lime,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.size(2.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = null,
                        // 드로어블 기본색이 text_primary 라 tint 생략 시 라벨 색과 어긋난다.
                        tint = Lime,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = OnLime),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                text = if (isLast) "결과 보기" else "다음 문제",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun ReviewProgressDots(quizIndex: Int, totalCount: Int) {
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
                    .background(if (done) Lime else Outline)
            )
        }
    }
}

@Composable
private fun ReviewResultBanner(isCorrect: Boolean?) {
    val bg = when (isCorrect) {
        true -> Grass1
        false -> ErrorFaint
        null -> BgElevated
    }
    val accent = when (isCorrect) {
        true -> Lime
        false -> Error
        null -> TextMuted
    }
    val label = when (isCorrect) {
        true -> "정답이었어요!"
        false -> "오답이었어요"
        null -> "이미 푼 문제예요"
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, accent, RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
    }
}

/** 잠긴 보기 행 — 클릭 불가. 어떤 보기를 골랐었는지/정답이었는지는 표시하지 않는다(정보 없음). */
@Composable
private fun LockedOptionRow(option: QuizOption) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgSurface)
            .border(1.dp, Outline, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(BgElevated),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${option.optionNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = option.text,
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun SolvedQuizReviewScreenCorrectPreview() {
    FinQTheme {
        SolvedQuizReviewScreen(
            quizIndex = 1,
            totalCount = 4,
            quiz = DummyQuizData.todayQuizzes.first().copy(solved = true, correct = true),
            isLast = false,
            onNext = {},
            onClose = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun SolvedQuizReviewScreenWrongPreview() {
    FinQTheme {
        SolvedQuizReviewScreen(
            quizIndex = 2,
            totalCount = 4,
            quiz = DummyQuizData.todayQuizzes[1].copy(solved = true, correct = false),
            isLast = true,
            onNext = {},
            onClose = {},
            onViewWrongNote = {},
        )
    }
}
