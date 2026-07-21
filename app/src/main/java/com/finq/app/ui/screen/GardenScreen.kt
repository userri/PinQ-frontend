package com.finq.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finq.app.data.repository.GardenItem
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewStage
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val GARDEN_DATE_FORMAT = DateTimeFormatter.ofPattern("M/d")

/**
 * 정원 — 자라는 복습(새싹/풀/나무 직전)과 완성된 나무 현황.
 *
 * "총 몇 그루"는 항상 [ReviewGarden.graduatedTrees] 카운터를 쓴다 —
 * 기능 배포 이전 졸업분은 graduated 목록에 없어 목록 길이와 다를 수 있다.
 */
@Composable
fun GardenScreen(
    garden: ReviewGarden?,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
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
            garden != null -> GardenContent(garden)
        }
    }
}

@Composable
private fun GardenContent(garden: ReviewGarden) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ── 헤더: 나무 총계 (카운터가 진실) ──────────────────────
        item {
            Column {
                Text(
                    text = "🌳 키운 나무 ${garden.graduatedTrees}그루",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                )
                if (garden.graduatedTrees > garden.graduated.size) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (garden.graduated.isEmpty())
                            "예전에 완성한 나무는 목록에 나오지 않아요"
                        else
                            "그중 ${garden.graduated.size}그루는 아래에서 자세히 볼 수 있어요",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── 빈 정원 ──────────────────────────────────────────────
        if (garden.growing.isEmpty() && garden.graduated.isEmpty() && garden.graduatedTrees == 0) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "🌱", fontSize = 44.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "아직 심은 나무가 없어요",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "오답을 복습하면 나무가 자라요",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
            }
        }

        // ── 자라는 중 (due 오름차순 — 서버 정렬 그대로) ──────────
        if (garden.growing.isNotEmpty()) {
            item { SectionTitle("자라는 중 ${garden.growing.size}") }
            items(garden.growing, key = { "g${it.quizId}" }) { item ->
                GardenItemCard(item = item, graduated = false)
            }
        }

        // ── 완성된 나무 (졸업 최신순 — 서버 정렬 그대로) ─────────
        if (garden.graduated.isNotEmpty()) {
            item { SectionTitle("완성된 나무 ${garden.graduated.size}") }
            items(garden.graduated, key = { "d${it.quizId}" }) { item ->
                GardenItemCard(item = item, graduated = true)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
    )
}

@Composable
private fun GardenItemCard(item: GardenItem, graduated: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (graduated) "🌳" else item.stage.emoji,
                fontSize = 24.sp,
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.keyword?.takeIf { it.isNotBlank() } ?: item.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${item.categoryLabel} · 💧 물 ${item.waterCount}번 · 흡수 ${item.absorbedCount}번",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
            Spacer(Modifier.size(8.dp))
            Text(
                text = if (graduated) {
                    formatGraduatedDate(item.graduatedAtIso)?.let { "$it 완성" } ?: "완성"
                } else {
                    item.dueDate?.let { "물 주기 ${it.format(GARDEN_DATE_FORMAT)}" }
                        ?: item.stage.label
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (graduated) Lime else TextSecondary,
            )
        }
    }
}

/** "2026-07-19T14:32:00" → "7/19". 파싱 실패 시 null. */
private fun formatGraduatedDate(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return runCatching {
        LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME).format(GARDEN_DATE_FORMAT)
    }.getOrNull()
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
        )
    }
}
