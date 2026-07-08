package com.finq.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finq.app.ui.theme.BgSubtle
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.Outline
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (hasReviews) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (hasReviews) Lime else BgSubtle),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = if (hasReviews) "💧" else "🌿", fontSize = 18.sp)
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
                        hasReviews -> "틀린 문제를 다시 만나 잔디를 키워요"
                        nextDueDate != null -> "다음 물 주기 ${nextDueDate.format(DUE_DATE_FORMAT)}"
                        else -> "복습할 오답이 없어요"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
            if (hasReviews) {
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Lime,
                )
            }
        }
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
