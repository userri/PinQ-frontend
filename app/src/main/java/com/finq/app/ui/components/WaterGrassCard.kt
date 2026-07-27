package com.finq.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finq.app.R
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("M/d")

/**
 * 홈의 오답 복습 진입 카드.
 *
 * 복습할 게 있으면 "오늘 물 줄 잔디 N개" (클릭 가능),
 * 없으면 [nextDueDate] 로 "다음 물 주기 7/12" 를 보여주고 클릭을 막는다.
 * 둘 다 없으면(복습 큐 자체가 빔) 카드를 그리지 않는다 — 호출부가 판단한다.
 */
@Composable
fun WaterGrassCard(
    reviewCount: Int,
    nextDueDate: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasReviews = reviewCount > 0

    // 유리 패널 — 밤하늘 배경이 비치는 반투명 카드(테두리 없음).
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (hasReviews) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    // 유리 톤 배지 — 라임 저채도 틴트(솔리드 라임 원 금지, 밤 풍경과 통일).
                    .background(Lime.copy(alpha = if (hasReviews) 0.16f else 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(
                        if (hasReviews) R.drawable.ic_water_drop else R.drawable.ic_stage_grass
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasReviews) "오늘 물 줄 잔디 ${reviewCount}개"
                           else "오늘 물 줄 잔디가 없어요",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = when {
                        hasReviews -> "물 주면 문제가 자라요 — 3번 맞히면 나무가 돼요"
                        nextDueDate != null -> "다음 물 주기 ${nextDueDate.format(DUE_DATE_FORMAT)}"
                        else -> "복습할 오답이 없어요"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
            if (hasReviews) {
                Spacer(Modifier.size(8.dp))
                NeonCtaPill(text = "물 주러 가기 →")
            }
        }
    }
}

/**
 * 홈 유리 카드의 CTA — 밤 풍경 속 네온사인(Lime 테두리 + 글로우).
 * 물주기·오늘의 퀴즈 카드가 공유해 홈 진입점 스타일을 통일한다.
 * 클릭은 카드 전체가 받으므로 필 자체는 표시 전용이다.
 */
@Composable
fun NeonCtaPill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .drawBehind {
                // 버튼 뒤 은은한 Lime 번짐.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Lime.copy(alpha = 0.30f), Lime.copy(alpha = 0f)),
                        center = center,
                        radius = size.maxDimension * 0.72f,
                    ),
                    radius = size.maxDimension * 0.72f,
                    center = center,
                )
            }
            .clip(RoundedCornerShape(50))
            .background(BgBase.copy(alpha = 0.55f))
            .border(1.dp, Lime, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Lime,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun WaterGrassCardPreview() {
    FinQTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WaterGrassCard(reviewCount = 3, nextDueDate = null, onClick = {})
            WaterGrassCard(reviewCount = 0, nextDueDate = LocalDate.of(2026, 7, 12), onClick = {})
            WaterGrassCard(reviewCount = 0, nextDueDate = null, onClick = {})
        }
    }
}
