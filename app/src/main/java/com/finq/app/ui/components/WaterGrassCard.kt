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
import androidx.compose.material3.Icon
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


/**
 * 홈의 오답 복습 진입 카드.
 *
 * 복습할 게 있으면 "오늘 물 줄 잔디 N개" (클릭 가능),
 * 오늘 다 했으면 "오늘 물주기 완료 · N개가 자랐어요",
 * 없으면 [nextDueDate] 로 "다음 물주기 내일" 을 보여주고 클릭을 막는다([nextWateringText]).
 * 둘 다 없으면(복습 큐 자체가 빔) 카드를 그리지 않는다 — 호출부가 판단한다.
 */
@Composable
fun WaterGrassCard(
    reviewCount: Int,
    nextDueDate: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** 오늘 복습으로 물 준 개수 / 그중 자란 개수. 큐가 비었을 때 "없어요"와 "다 했어요"를 가른다. */
    reviewedToday: Int = 0,
    grownToday: Int = 0,
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
                    text = when {
                        hasReviews -> "오늘 물 줄 잔디 ${reviewCount}개"
                        // 오늘 물을 준 적이 있으면 "없어요"가 아니라 "다 했어요"다.
                        // 두 상태를 한 문구로 뭉치면, 5개를 다 한 사람과 애초에 할 게
                        // 없던 사람이 같은 말을 듣는다.
                        reviewedToday > 0 -> "오늘 물주기 완료"
                        else -> "오늘 물 줄 잔디가 없어요"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = when {
                        // 규칙 설명("3번 맞히면 나무")은 개념 시트가 맡는다. 여기선 한 줄이면 충분하고,
                        // 길면 좁은 카드에서 어색하게 줄바꿈된다(3번 / 맞히면).
                        hasReviews -> "복습할수록 자라요"
                        // 오늘 물을 줬으면 한 일을 말한다. 데일리 카드는 완주 시 "4/5 정답"
                        // 이라는 성취를 보여주는데 여기만 "없어요"라는 부정형이면, 방금
                        // 물 준 사람에게 한 일을 지우는 셈이다.
                        reviewedToday > 0 && nextDueDate != null ->
                            "${grownToday}개가 자랐어요 · ${nextWateringText(nextDueDate)}"
                        reviewedToday > 0 -> "${grownToday}개가 자랐어요"
                        nextDueDate != null -> nextWateringText(nextDueDate)
                        else -> "복습할 오답이 없어요"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
            if (hasReviews) {
                Spacer(Modifier.size(8.dp))
                NeonCtaPill(text = "물 주러 가기")
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Lime,
            )
            Spacer(Modifier.size(2.dp))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                // 드로어블 기본색이 text_primary 라 tint 생략 시 라임 라벨 옆에서 회백색이 된다.
                tint = Lime,
                modifier = Modifier.size(16.dp),
            )
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
