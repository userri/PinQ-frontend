package com.finq.app.ui.components.garden

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
import androidx.compose.ui.unit.dp
import com.finq.app.data.repository.GrassCalendar
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.ui.components.GrassCalendarBody
import com.finq.app.ui.components.GrassCalendarError
import com.finq.app.ui.components.GrassCalendarSkeleton
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary

/**
 * 마이페이지 통합 "정원" 카드 — 위에는 정원 그림(잔디+나무), 아래에는 연간 잔디 그리드.
 * "경제잔디" 성장 메타포가 물리적으로 한 화면에 모인다.
 *
 * 정원 그림 탭 → 풀스크린 정원. 잔디 로드 실패 시 기존 재시도 카드로 폴백.
 */
@Composable
fun GardenSection(
    grass: GrassCalendar?,
    grassFailed: Boolean,
    garden: ReviewGarden?,
    onRetryGrass: () -> Unit,
    onOpenGarden: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (grass == null) {
        if (grassFailed) GrassCalendarError(onRetry = onRetryGrass, modifier = modifier)
        else GrassCalendarSkeleton(modifier = modifier)
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Outline),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "정원",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(
                    text = "🌳 키운 나무 ${grass.graduatedTrees}그루 →",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.clickable(onClick = onOpenGarden),
                )
            }
            Spacer(Modifier.height(12.dp))

            // 정원 그림 — garden 로딩 전엔 은은한 자리 표시(레이아웃 점프 방지).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgElevated)
                    .clickable(onClick = onOpenGarden),
            ) {
                if (garden != null) {
                    GardenCanvas(
                        garden = garden,
                        compact = true,
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            GrassCalendarBody(grass = grass)
        }
    }
}
