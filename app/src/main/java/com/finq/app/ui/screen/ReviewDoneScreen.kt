package com.finq.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("M/d")

/**
 * 복습 세션 완료 화면 — "잔디에 물 줬어요".
 *
 * 복습할 게 아예 없어서 진입한 경우([reviewedCount] == 0)에도 같은 화면을 쓰되,
 * 문구를 "오늘 물 줄 잔디가 없어요" 로 바꾼다.
 */
@Composable
fun ReviewDoneScreen(
    reviewedCount: Int,
    correctCount: Int,
    graduatedCount: Int,
    nextDueDate: LocalDate?,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nothingToReview = reviewedCount == 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = if (nothingToReview) "🌿" else "💧", fontSize = 56.sp)
        Spacer(Modifier.height(20.dp))

        Text(
            text = if (nothingToReview) "오늘 물 줄 잔디가 없어요" else "잔디에 물 줬어요",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )

        if (!nothingToReview) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "$correctCount / $reviewedCount 정답",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Lime,
            )
            if (graduatedCount > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "🌳 ${graduatedCount}문제를 완전히 익혔어요",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (nextDueDate != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "다음 물 주기 ${nextDueDate.format(DUE_DATE_FORMAT)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = "복습은 스트릭·정답률 기록에 영향 없어요",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onGoHome,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = OnLime),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) {
            Text(
                text = "홈으로",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun ReviewDoneScreenPreview() {
    FinQTheme {
        ReviewDoneScreen(
            reviewedCount = 3,
            correctCount = 2,
            graduatedCount = 1,
            nextDueDate = LocalDate.of(2026, 7, 12),
            onGoHome = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun ReviewDoneScreenEmptyPreview() {
    FinQTheme {
        ReviewDoneScreen(
            reviewedCount = 0,
            correctCount = 0,
            graduatedCount = 0,
            nextDueDate = LocalDate.of(2026, 7, 12),
            onGoHome = {},
        )
    }
}
