package com.finq.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary

/** 의견 수집 폼. 앱 안에 폼을 만들지 않고 외부 폼으로 보낸다 — 서버 스키마가 필요 없다. */
const val FEEDBACK_FORM_URL = "https://forms.gle/nGfqqck2x1JMvsiA6"

/** 폼을 브라우저로 연다. 열 앱이 없어도 앱이 죽지 않게 감싼다. */
fun openFeedbackForm(context: Context) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(FEEDBACK_FORM_URL)))
    }
}

/**
 * 마이페이지 상시 항목 — "의견 보내기".
 *
 * 아이콘을 중립색(TextSecondary)으로 낮춘다. 이 줄은 앱정보·알림·계정과 같은
 * 부수 정보 층이고, 라임은 이 화면에서 행동을 부르는 자리가 따로 가져간다.
 * 배너가 사라진 뒤에도 창구가 남아 있다는 게 이 항목의 존재 이유다.
 */
@Composable
fun FeedbackMenuRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_paper_plane),
            contentDescription = null,
            colorFilter = ColorFilter.tint(TextSecondary),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = "의견 보내기",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
        )
    }
}

/**
 * 홈 1회성 피드백 배너 — 첫 실행 +3일 뒤에 딱 한 번 자리를 요구한다.
 *
 * 홈 CTA(풀러 가기·물 주러 가기) **아래**에 둔다. 오늘 할 일이 먼저고 부탁은 그 다음이다.
 * 닫으면 영구히 사라지고, 창구는 마이페이지 [FeedbackMenuRow] 로 남는다.
 *
 * 상시 FAB 는 만들지 않는다 — 라임을 쓰면 홈 CTA 와 색 역할이 부딪히고,
 * 안 쓰면 눈에 안 띄어 자리만 차지한다.
 */
@Composable
fun FeedbackBanner(
    onOpenForm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgSurface)
            .clickable(onClick = onOpenForm)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_paper_plane),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "쓰면서 불편한 점 있으셨나요?",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Text(
                text = "2분이면 됩니다 · 익명",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
        Spacer(Modifier.size(8.dp))
        // 닫기는 48dp 타깃 안에 20dp 글리프 — 배너 본문 탭과 겹치지 않게 별도 clickable.
        Row(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onDismiss),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "닫기",
                colorFilter = ColorFilter.tint(TextMuted),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
