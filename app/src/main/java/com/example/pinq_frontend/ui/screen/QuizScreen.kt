package com.example.pinq_frontend.ui.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pinq_frontend.data.DummyQuizData
import com.example.pinq_frontend.data.model.Quiz
import com.example.pinq_frontend.data.model.QuizOption
import com.example.pinq_frontend.ui.theme.PinQ_frontendTheme

/**
 * 퀴즈 풀이 화면 — Stateless View.
 *
 * MVVM 원칙:
 *  - 상태는 모두 파라미터로 받는다(외부 ViewModel 이 보유).
 *  - 사용자 액션은 콜백으로 위임한다.
 *  - 내부에 remember/mutableStateOf 가 없다.
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
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        ProgressHeader(quizIndex = quizIndex, totalCount = totalCount)
        Spacer(Modifier.height(16.dp))
        CategoryChip(label = quiz.category.displayName)
        Spacer(Modifier.height(24.dp))

        Text(
            text = "Q${quizIndex + 1}. ${quiz.question}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            quiz.options.forEach { option ->
                OptionCard(
                    option = option,
                    selected = option.id == selectedOptionId,
                    onClick = { onSelectOption(option.id) },
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onSubmit,
            enabled = selectedOptionId != null,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "정답 확인",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ProgressHeader(quizIndex: Int, totalCount: Int) {
    val progress = (quizIndex + 1).coerceAtMost(totalCount).toFloat() / totalCount.toFloat()
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "오늘의 퀴즈",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${quizIndex + 1} / $totalCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
        )
    }
}

@Composable
private fun CategoryChip(label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = "#$label",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun OptionCard(
    option: QuizOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${option.id}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = option.text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun QuizScreenUnselectedPreview() {
    PinQ_frontendTheme {
        QuizScreen(
            quizIndex = 0,
            totalCount = DummyQuizData.todayQuizzes.size,
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
    PinQ_frontendTheme {
        QuizScreen(
            quizIndex = 1,
            totalCount = DummyQuizData.todayQuizzes.size,
            quiz = DummyQuizData.todayQuizzes[1],
            selectedOptionId = 2L,
            onSelectOption = {},
            onSubmit = {},
        )
    }
}
