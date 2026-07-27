package com.finq.app.ui.screen

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.data.repository.GardenItem
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.data.repository.ReviewStage
import com.finq.app.ui.components.ReviewTreeConceptSheet
import com.finq.app.ui.components.garden.GardenNightScene
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import java.time.LocalDate

/**
 * 정원 — 화면 전체가 밤하늘 아래 잔디밭 한 장면(순수 시각 보상).
 *
 * 앞줄 큐레이션 식물(성장 전시 중심 10~15개)만 클릭 가능 → 오답노트 해당 문제.
 * 나머지는 능선 뒤 실루엣 + "전체 N개 보기" 행이 담당한다.
 * "총 몇 그루"는 항상 [ReviewGarden.graduatedTrees] 카운터가 진실.
 */
@Composable
fun GardenScreen(
    garden: ReviewGarden?,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    /** 앞줄 식물 탭 → 오답노트 해당 문제로. */
    onOpenQuiz: (Long) -> Unit,
    /** "전체 N개 보기" → 오답노트 탭으로. */
    onOpenAll: () -> Unit = {},
    /** "오늘 물 줄 잔디 N개" → 복습 세션으로. 정원은 보상, 복습은 작업 공간. */
    onStartReview: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showHelp by remember { mutableStateOf(false) }

    if (showHelp) {
        ReviewTreeConceptSheet(
            title = "🌳 복습 나무란?",
            onDismiss = { showHelp = false },
        )
    }

    Box(modifier = modifier.fillMaxSize().background(BgBase)) {
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
            garden != null -> {
                // 배경 씬 — 화면 전체(카드·테두리 없음). 상단 바 뒤까지 하늘이 이어진다.
                GardenNightScene(
                    garden = garden,
                    onItemTap = onOpenQuiz,
                    modifier = Modifier.fillMaxSize(),
                )
                if (garden.growing.isEmpty() && garden.graduatedTrees == 0) {
                    Text(
                        text = "오답을 복습하면 나무가 자라요",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }

        // ── 오버레이 UI — 유리 톤으로 밤하늘에 녹인다. 씬은 풀블리드, UI 만 인셋.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
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
                Spacer(Modifier.weight(1f))
                // 복습 나무 개념 설명 — 레이아웃이 아닌 개념을 설명해 UI 변경에 강함.
                // 헤더가 좁아 아이콘만 두되, 터치 영역은 48dp 를 보장한다.
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button) { showHelp = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BgSurface.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_help_circle),
                            contentDescription = "복습 나무란?",
                            tint = TextSecondary,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
            }

            if (garden != null && !isLoading && error == null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(50))
                        .background(BgBase.copy(alpha = 0.40f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    // 나무 아이콘 없음 — "키운 나무"라는 텍스트가 이미 같은 말을 하고,
                    // 15dp 는 Material 광학 최소(20dp) 아래다. 게다가 바로 아래 씬이
                    // 실제 식물을 크게 렌더하고 있어 두 번 말하는 셈이 된다.
                    Text(
                        text = "자라는 중 ${garden.growing.size}  ·  키운 나무 ${garden.graduatedTrees}그루",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                    )
                }

                // due 안내 — 오늘 물 줄 수 있는 잔디가 있으면 복습 세션 진입 한 줄.
                val today = remember { LocalDate.now() }
                val dueCount = remember(garden) {
                    garden.growing.count { it.dueDate != null && !it.dueDate!!.isAfter(today) }
                }
                if (dueCount > 0) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Lime.copy(alpha = 0.14f))
                            .clickable(onClick = onStartReview)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text = "오늘 물 줄 잔디 ${dueCount}개 →",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Lime,
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── 전체 보기 — "+N" 배지 대신 명시적 행. 앞줄 밖 항목의 존재를 전달한다.
            if (garden != null && !isLoading && error == null) {
                val total = garden.growing.size + garden.graduatedTrees
                if (total > 0) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 20.dp)
                            .clip(RoundedCornerShape(50))
                            .background(BgBase.copy(alpha = 0.45f))
                            .clickable(onClick = onOpenAll)
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "전체 ${total}개 보기 →",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF081A2E, heightDp = 800)
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
