package com.example.pinq_frontend.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.example.pinq_frontend.ui.theme.PinQBlue
import com.example.pinq_frontend.ui.theme.PinQ_frontendTheme

/**
 * 마이페이지 — Stateless View.
 *
 * @param streak          연속 학습 일수
 * @param totalSolved     누적 풀이 수
 * @param correctRate     정답률 0.0~1.0
 * @param activityGrid    최근 49일(7주×7일) 강도. 0=없음, 1=연파랑, 2=중파랑, 3=진파랑. 인덱스 0이 가장 과거.
 * @param appVersion      BuildConfig.VERSION_NAME
 */
@Composable
fun MyPageScreen(
    streak: Int,
    totalSolved: Int,
    correctRate: Float,
    activityGrid: List<Boolean>,
    appVersion: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // List<Boolean> → List<Int> 변환 (기존 API 호환)
    val intensityGrid = remember(activityGrid) {
        activityGrid.map { if (it) 2 else 0 }
    }

    MyPageContent(
        streak = streak,
        totalSolved = totalSolved,
        correctRate = correctRate,
        intensityGrid = intensityGrid,
        appVersion = appVersion,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun MyPageContent(
    streak: Int,
    totalSolved: Int,
    correctRate: Float,
    intensityGrid: List<Int>,
    appVersion: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        // ── 헤더 ──────────────────────────────────────────────────
        Text(
            text = "마이페이지",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(20.dp))

        // ── 프로필 카드 ───────────────────────────────────────────
        ProfileCard()

        Spacer(Modifier.height(20.dp))

        // ── 3단 통계 가로 배치 ────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard(
                label = "연속 학습",
                value = "${streak}일",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "총 풀이",
                value = "${totalSolved}회",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "정답률",
                value = "${(correctRate * 100).toInt()}%",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── 풀이 활동 카드 ────────────────────────────────────────
        ActivityHeatmapCard(intensityGrid = intensityGrid)

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))

        // ── 앱 버전 정보 ──────────────────────────────────────────
        Text(
            text = "앱 정보",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        InfoRow(label = "버전", value = appVersion)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 서브 컴포넌트
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(PinQBlue),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "유",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = "유리님",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Google 로그인은 Phase 3에서 지원됩니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = PinQBlue,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 풀이 활동 카드 — 8주×7일, 요일 라벨(월~일) 포함, 셀이 카드 너비를 꽉 채움.
 */
@Composable
private fun ActivityHeatmapCard(intensityGrid: List<Int>) {
    val weeks = 8
    val days = 7
    val gap = 4.dp
    val labelWidth = 20.dp
    val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")

    val heatmapColors = listOf(
        Color(0xFFEDF0F7),   // 0: 빈 셀
        Color(0xFFBFD3F8),   // 1: 연파랑
        Color(0xFF6B9BF2),   // 2: 중파랑
        PinQBlue,            // 3: 진파랑
    )

    val padded = List(weeks * days) { i -> intensityGrid.getOrElse(i) { 0 } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "풀이 활동",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "지난 8주",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(14.dp))

            // 요일 라벨 + 그리드: 셀 크기를 BoxWithConstraints로 동적 계산
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                // 가용 너비 = 전체 - 라벨 열 - 라벨-그리드 간격 - 셀 간 gap 합
                val cellGapTotal = (weeks - 1) * gap
                val gridWidth = maxWidth - labelWidth - gap - cellGapTotal
                val cellSize = gridWidth / weeks

                Row(verticalAlignment = Alignment.Top) {
                    // 요일 라벨 열
                    Column(
                        modifier = Modifier.width(labelWidth),
                        verticalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        dayLabels.forEach { label ->
                            Box(
                                modifier = Modifier.size(cellSize),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(gap))

                    // 히트맵 그리드 (열 = 주, 행 = 요일)
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        for (week in 0 until weeks) {
                            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                                for (day in 0 until days) {
                                    val idx = week * days + day
                                    val intensity = padded.getOrElse(idx) { 0 }.coerceIn(0, 3)
                                    Box(
                                        modifier = Modifier
                                            .size(cellSize)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(heatmapColors[intensity]),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 범례 (우하단)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "적게",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                heatmapColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color),
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "많이",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MyPageScreenPreview() {
    val intensityGrid = buildList {
        val pattern = listOf(0, 1, 2, 3, 1, 0, 2, 1, 3, 0, 2, 1, 0, 3)
        repeat(56) { i -> add(pattern[i % pattern.size]) }
    }
    PinQ_frontendTheme {
        MyPageContent(
            streak = 7,
            totalSolved = 28,
            correctRate = 0.75f,
            intensityGrid = intensityGrid,
            appVersion = "1.0",
            onBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MyPageEmptyPreview() {
    PinQ_frontendTheme {
        MyPageContent(
            streak = 0,
            totalSolved = 0,
            correctRate = 0f,
            intensityGrid = emptyList(),
            appVersion = "1.0",
            onBack = {},
        )
    }
}
