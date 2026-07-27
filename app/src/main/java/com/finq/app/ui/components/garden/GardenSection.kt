package com.finq.app.ui.components.garden

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finq.app.data.repository.GrassCalendar
import com.finq.app.data.repository.ReviewGarden
import com.finq.app.ui.components.GrassCalendarBody
import com.finq.app.ui.components.GrassCalendarError
import com.finq.app.ui.components.GrassCalendarSkeleton
import com.finq.app.ui.theme.TextPrimary

/**
 * 마이페이지 잔디밭 **섹션** — 위는 연간 잔디 그리드(주역), 아래는 복습 나무 기록 밴드.
 *
 * 예전엔 여기에도 정원 그림을 축소해 넣었지만, 홈 하단이 정원 씬을 맡은 뒤로는
 * 두 화면이 똑같이 "정원 보러 가기" 냄새를 풍겨 중복이었다. 마이페이지는
 * 누적 기록을 읽는 자리로 역할을 갈랐다 — [TreeRecordBlock] 참고.
 *
 * 카드 래퍼를 쓰지 않는다. 마이페이지는 한 번에 훑는 단일 스크롤이고 각 섹션은
 * 상세로 가는 진입점이 아니라 그냥 구획이라, 카드는 오용이었다(Material 가이드).
 * 게다가 카드 안에 요약칩 박스가 또 들어가 테두리가 두 겹이었다.
 * 지금은 섹션 헤더 + 여백으로 나누고, [TreeRecordBlock] 의 톤 밴드만 유일한 면으로 남긴다
 * — 그건 정원으로 가는 탭 대상이라 면을 가질 이유가 있다.
 *
 * 헤더는 로딩/실패 상태에서도 항상 그린다 — 상태가 바뀔 때 제목이 나타났다 사라지지 않게.
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
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "잔디밭",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(12.dp))

        when {
            grass != null -> GrassCalendarBody(grass = grass)
            grassFailed -> GrassCalendarError(onRetry = onRetryGrass)
            else -> GrassCalendarSkeleton()
        }
    }
}
