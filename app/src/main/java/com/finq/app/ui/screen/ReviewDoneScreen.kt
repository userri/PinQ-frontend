package com.finq.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.finq.app.R
import com.finq.app.ui.components.nextWateringText
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary
import java.time.LocalDate


/**
 * 복습 세션 완료 화면 — "잔디에 물 줬어요".
 *
 * 복습할 게 아예 없어서 진입한 경우([reviewedCount] == 0)에도 같은 화면을 쓰되,
 * 문구를 "오늘 물 줄 잔디가 없어요" 로 바꾼다.
 */
@Composable
fun ReviewDoneScreen(
    reviewedCount: Int,
    correctCount: Int,
    graduatedCount: Int,
    nextDueDate: LocalDate?,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nothingToReview = reviewedCount == 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (nothingToReview) {
            Image(
                painter = painterResource(R.drawable.ic_stage_grass),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
            )
        } else {
            // 이모지는 OEM 폰트마다 다른 그림이 나온다 — 히어로는 커스텀 벡터로.
            Image(
                painter = painterResource(R.drawable.ic_water_drop),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
            )
        }
        Spacer(Modifier.height(20.dp))

        Text(
            text = if (nothingToReview) "오늘 물 줄 잔디가 없어요" else "오늘 물주기 완료",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )

        if (!nothingToReview) {
            Spacer(Modifier.height(12.dp))
            Text(
                // 점수판이 아니라 진척으로 말한다. 오답 시 stage 리셋을 폐기했으므로
                // "맞힌 수"와 "자란 수"는 같은 숫자인데, 후자만 의미를 담는다.
                // 그리고 복습 정답 수는 잔디·스트릭·정답률 어디에도 반영되지 않는다 —
                // 아무 데도 안 쓰이는 숫자를 성적처럼 보여주면 오해만 만든다.
                // 틀린 개수를 분모에 세지 않는 이유: 틀려도 잃는 게 없다.
                text = "${reviewedCount}개에 물 주고 ${correctCount}개가 자랐어요",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Lime,
            )
            if (graduatedCount > 0) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_stage_tree),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "${graduatedCount}문제를 완전히 익혔어요",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        if (nextDueDate != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = nextWateringText(nextDueDate),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(10.dp))
            // "복습은 스트릭·정답률 기록에 영향 없어요" 는 뺐다. 정답률이 안 떨어지는 건
            // 통계 화면을 따로 봐야 아는 사실이라 미리 알리면 없던 불안을 만든다(관찰
            // 불가능한 건 설명하지 않는다). 복습 세션 헤더에서도 같은 이유로 이미 뺐고,
            // 무엇보다 여기는 성취의 순간이다 — 축하 아래에 "무엇이 일어나지 않았는지"를
            // 붙이면 김이 빠진다. 궁금한 사람을 위한 설명은 개념 시트가 맡는다.

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onGoHome,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = OnLime),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) {
            Text(
                text = "홈으로",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun ReviewDoneScreenPreview() {
    FinQTheme {
        ReviewDoneScreen(
            reviewedCount = 3,
            correctCount = 2,
            graduatedCount = 1,
            nextDueDate = LocalDate.of(2026, 7, 12),
            onGoHome = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun ReviewDoneScreenEmptyPreview() {
    FinQTheme {
        ReviewDoneScreen(
            reviewedCount = 0,
            correctCount = 0,
            graduatedCount = 0,
            nextDueDate = LocalDate.of(2026, 7, 12),
            onGoHome = {},
        )
    }
}
