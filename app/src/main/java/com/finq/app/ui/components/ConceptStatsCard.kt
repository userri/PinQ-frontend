package com.finq.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.finq.app.data.repository.ConceptStat
import com.finq.app.data.repository.ConceptStats
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.Error
import com.finq.app.ui.theme.ErrorFaint
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

private const val BAR_MIN_FRACTION = 0.02f

/**
 * 카테고리별 정답률 + 취약 개념 진단.
 *
 * [ConceptStats.weakest] 가 null 이면(표본 부족) 진단 카드 대신 안내 문구를 띄운다.
 * 카테고리 목록 자체가 비면 아무것도 그리지 않는다 — 호출부가 판단한다.
 */
@Composable
fun ConceptStatsCard(
    stats: ConceptStats,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Outline),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "개념별 정답률",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))

            if (stats.weakest != null) {
                WeakestConceptBanner(stats.weakest)
            } else {
                Text(
                    text = "조금 더 풀면 취약 개념을 진단해드려요",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }

            Spacer(Modifier.height(14.dp))
            stats.categories.forEachIndexed { index, stat ->
                if (index > 0) Spacer(Modifier.height(10.dp))
                ConceptBar(stat = stat, isWeakest = stat.category == stats.weakest?.category)
            }
        }
    }
}

/** "요즘 {개념}이 흔들려요" — 오답 계열이므로 Error 톤을 쓴다. */
@Composable
private fun WeakestConceptBanner(weakest: ConceptStat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ErrorFaint)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "📉", fontSize = 18.sp)
        Spacer(Modifier.size(10.dp))
        Column {
            Text(
                text = "요즘 ${weakest.displayName} 개념이 흔들려요",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "정답률 ${weakest.correctRate.toPercent()}% · ${weakest.correct}/${weakest.total}문제",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun ConceptBar(stat: ConceptStat, isWeakest: Boolean) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stat.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Text(
                text = "${stat.correctRate.toPercent()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isWeakest) Error else TextSecondary,
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(BgElevated),
        ) {
            // 정답률 0% 도 막대가 보이도록 최소 폭을 준다.
            val fraction = stat.correctRate.coerceAtLeast(BAR_MIN_FRACTION)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isWeakest) Error else Lime),
            )
        }
    }
}

private fun Float.toPercent(): Int = (this * 100).roundToInt()

@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun ConceptStatsCardPreview() {
    val categories = listOf(
        ConceptStat("INTEREST_RATE", "금리", 20, 16, 0.8f),
        ConceptStat("EXCHANGE_RATE", "환율", 12, 4, 0.33f),
        ConceptStat("STOCK", "증시", 18, 13, 0.72f),
        ConceptStat("REAL_ESTATE", "부동산", 9, 6, 0.67f),
    )
    FinQTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ConceptStatsCard(ConceptStats(categories, weakest = categories[1]))
            ConceptStatsCard(ConceptStats(categories, weakest = null))
        }
    }
}
