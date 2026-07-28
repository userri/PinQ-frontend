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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
            title = "복습 나무란?",
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
            // ── 헤더 컨트롤 — 좌우 한 쌍. 둘 다 48dp 타깃 안 32dp 원으로 같은 언어를 쓴다.
            // Row 좌우 6dp + 48dp 타깃 안 20dp 아이콘 여백 14dp = 아이콘 에지 20dp 기준선.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BgBase.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_left),
                            contentDescription = "뒤로",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                // 복습 나무 개념 설명 — 레이아웃이 아닌 개념을 설명해 UI 변경에 강함.
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button) { showHelp = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BgBase.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_help_circle),
                            contentDescription = "복습 나무란?",
                            tint = TextSecondary,
                            // 셰브론과 같은 20dp. stroke 1.6 원형이라 더 무거워 보이면 18dp 까지만.
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── 정보 블록 — 제목 + 수치 각주. 누를 수 없으므로 배경을 두르지 않는다.
            // (알약은 액션 전용 신호로 남긴다.)
            Text(
                text = "정원",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(2.dp))

            if (garden != null && !isLoading && error == null) {
                Text(
                    text = "자라는 중 ${garden.growing.size}  ·  키운 나무 ${garden.graduatedTrees}그루",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                // due 안내 — 오늘 물 줄 수 있는 잔디가 있으면 복습 세션 진입 한 줄.
                val today = remember { LocalDate.now() }
                val dueCount = remember(garden) {
                    garden.growing.count { it.dueDate != null && !it.dueDate!!.isAfter(today) }
                }
                if (dueCount > 0) {
                    Spacer(Modifier.height(12.dp))
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        // due 식물 후광(GardenNightScene)·홈 물주기 버튼과 같은 라임 번짐.
                        // "빛나는 것 = 오늘 물 줄 것" 을 세 곳이 공유한다.
                        //
                        // 알약 안(clickable 체인)에 그리면 리플용 clip 에 잘려 하드 엣지 띠가
                        // 생기고 "면이 하나 더" 있는 것처럼 읽힌다 → clip 밖 형제 레이어로 뺀다.
                        // 원형 그라디언트 대신 캡슐을 그대로 확장해 모서리와 어긋나지 않게 한다.
                        Spacer(
                            Modifier
                                .matchParentSize()
                                .padding(vertical = 6.dp)
                                .drawBehind {
                                    val steps = 14
                                    val spreadMax = 20.dp.toPx()
                                    for (i in steps downTo 1) {
                                        val t = i / steps.toFloat()
                                        val s = spreadMax * t
                                        drawRoundRect(
                                            color = Lime.copy(alpha = 0.028f * (1f - t)),
                                            topLeft = Offset(-s, -s),
                                            size = Size(size.width + 2 * s, size.height + 2 * s),
                                            cornerRadius = CornerRadius(size.height / 2f + s),
                                        )
                                    }
                                },
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            // clickable 뒤의 vertical 6dp 는 터치에 포함되고 배경에는 빠진다
                            // → 시각 36dp / 터치 48dp.
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(role = Role.Button, onClick = onStartReview)
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Lime.copy(alpha = 0.14f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = "오늘 물 줄 잔디 ${dueCount}개",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Lime,
                            )
                            Spacer(Modifier.width(2.dp))
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint = Lime,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            } else {
                // 로딩→로드 전환 시 헤더 높이가 튀지 않도록 서브라인 자리를 미리 확보.
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.weight(1f))

            // ── 전체 보기 — "+N" 배지 대신 명시적 행. 앞줄 밖 항목의 존재를 전달한다.
            if (garden != null && !isLoading && error == null) {
                val total = garden.growing.size + garden.graduatedTrees
                if (total > 0) {
                    // 부액션 — 글로우 없음. "오늘 할 일"이 아니므로 라임을 쓰지 않는다.
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 14.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(role = Role.Button, onClick = onOpenAll)
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(BgBase.copy(alpha = 0.45f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "전체 ${total}개 보기",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp),
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
