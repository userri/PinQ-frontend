package com.finq.app.ui.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finq.app.data.repository.GardenItem
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewStage
import com.finq.app.ui.components.garden.GardenCanvas
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import java.time.LocalDate

/**
 * 정원 — 잔디 위에 나무가 자란 그림 한 장 (순수 시각 보상).
 *
 * 목록 기능은 오답노트 복습 필터칩으로 이관됐다. 나무/새싹 탭 → 오답노트 해당 문제.
 * "총 몇 그루"는 항상 [ReviewGarden.graduatedTrees] 카운터가 진실.
 */
@Composable
fun GardenScreen(
    garden: ReviewGarden?,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    /** 나무/새싹 탭 → 오답노트 해당 문제로. */
    onOpenQuiz: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase),
    ) {
        // ── 상단 바 ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(12.dp),
            )
            Text(
                text = "정원",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Lime)
            }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onRetry) { Text("다시 시도") }
                }
            }
            garden != null -> Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "자라는 중 ${garden.growing.size} · 🌳 키운 나무 ${garden.graduatedTrees}그루",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
                Text(
                    text = "나무를 누르면 그 문제의 오답노트로 가요",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(8.dp))
                GardenCanvas(
                    garden = garden,
                    onItemTap = onOpenQuiz,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun GardenScreenPreview() {
    FinQTheme {
        GardenScreen(
            garden = ReviewGarden(
                growing = listOf(
                    GardenItem(
                        quizId = 101, categoryLabel = "주식", question = "PER이 낮다는 것은?",
                        keyword = "PER", stage = ReviewStage.GRASS,
                        dueDate = LocalDate.of(2026, 7, 24),
                        waterCount = 2, absorbedCount = 1, graduatedAtIso = null,
                    ),
                ),
                graduated = listOf(
                    GardenItem(
                        quizId = 88, categoryLabel = "경제", question = "기준금리 인상의 효과는?",
                        keyword = "기준금리", stage = ReviewStage.ALMOST_TREE,
                        dueDate = null, waterCount = 5, absorbedCount = 4,
                        graduatedAtIso = "2026-07-19T14:32:00",
                    ),
                ),
                graduatedTrees = 12,
            ),
            isLoading = false,
            error = null,
            onRetry = {},
            onBack = {},
            onOpenQuiz = {},
        )
    }
}
