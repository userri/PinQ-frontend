package com.finq.app.ui.components.garden

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finq.app.data.repository.GrassCalendar
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.ui.components.GrassCalendarBody
import com.finq.app.ui.components.GrassCalendarError
import com.finq.app.ui.components.GrassCalendarSkeleton
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextPrimary

/**
 * 마이페이지 잔디밭 카드 — 위는 연간 잔디 그리드(주역), 아래는 복습 나무 기록 밴드.
 *
 * 예전엔 여기에도 정원 그림을 축소해 넣었지만, 홈 하단이 정원 씬을 맡은 뒤로는
 * 두 화면이 똑같이 "정원 보러 가기" 냄새를 풍겨 중복이었다. 마이페이지는
 * 누적 기록을 읽는 자리로 역할을 갈랐다 — [TreeRecordBlock] 참고.
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
        Column {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "잔디밭",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(12.dp))
                GrassCalendarBody(grass = grass)
            }

            TreeRecordBlock(
                // 카운터가 진실 — 정원 목록이 아직 없어도 이 숫자는 바로 그릴 수 있다.
                graduatedTrees = grass.graduatedTrees,
                garden = garden,
                onOpenGarden = onOpenGarden,
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(RecordBandColor),
            )
        }
    }
}
