package com.finq.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finq.app.R
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.BgElevated
import com.finq.app.ui.theme.BgSurface
import com.finq.app.ui.theme.Error
import com.finq.app.ui.theme.FinQTheme
import com.finq.app.ui.theme.GoogleBorder
import com.finq.app.ui.theme.GoogleLabel
import com.finq.app.ui.theme.GoogleWhite
import com.finq.app.ui.theme.Grass1
import com.finq.app.ui.theme.KakaoLabel
import com.finq.app.ui.theme.KakaoYellow
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.OnLime
import com.finq.app.ui.theme.Outline
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary
import com.finq.app.ui.theme.TextSecondary

/**
 * 로그인 **앞**에 놓이는 맛보기 문제 1개.
 *
 * "이 앱이 뭘 주는지"를 설명하는 대신 한 번 겪게 한다 — 로그인 화면이 첫 화면이면
 * 사용자는 아직 아무것도 받지 못한 채 계정부터 내주게 된다.
 *
 * 서버를 부르지 않는다. 문제·정답·해설이 아래에 하드코딩되어 있고 결과는 어디에도
 * 기록되지 않는다. 그래서 **관련 기사 블록이 없다** — 하드코딩한 기사는 시간이
 * 지나면 반드시 썩는데, 그게 앱의 첫인상 자리에 놓이면 안 된다.
 *
 * 출구는 로그인뿐이다. 건너뛰기·닫기를 두지 않는 이유는 이 화면 자체가 30초짜리
 * 관문이고, 빠져나갈 문을 만들면 그 문이 기본 경로가 되기 때문이다.
 */
@Composable
fun TasteQuizScreen(
    onKakaoLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var picked by rememberSaveable { mutableStateOf<Int?>(null) }
    val revealed = picked != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text(
            text = "경제잔디 맛보기",
            style = MaterialTheme.typography.labelLarge,
            color = Lime,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "먼저 한 문제 풀어보세요",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Q. $TASTE_QUESTION",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(14.dp))

        // 판정 밴드 — 채점 화면과 같은 자리(선지 바로 위), 같은 형태.
        if (revealed) {
            VerdictBandCompact(isCorrect = picked == TASTE_ANSWER_INDEX)
            Spacer(Modifier.height(14.dp))
        }

        // ── 선지 4개 — 고르기 전엔 전부 중립, 고른 뒤엔 채점 화면과 같은 3단 위계 ──
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TASTE_OPTIONS.forEachIndexed { index, label ->
                TasteOptionRow(
                    number = index + 1,
                    label = label,
                    revealed = revealed,
                    isCorrect = index == TASTE_ANSWER_INDEX,
                    isPicked = picked == index,
                    onClick = { if (!revealed) picked = index },
                )
            }
        }

        if (revealed) {
            Spacer(Modifier.height(20.dp))
            ExplanationBlock(title = "해설", body = TASTE_EXPLANATION)
            Spacer(Modifier.height(12.dp))
            ExplanationBlock(title = "알아두면 좋아요", body = TASTE_KEYWORD, accent = true)

            Spacer(Modifier.height(28.dp))
            Text(
                text = "매일 아침 새 문제가 도착해요",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onKakaoLogin,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KakaoYellow,
                    contentColor = KakaoLabel,
                    disabledContainerColor = KakaoYellow.copy(alpha = 0.5f),
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_kakao_bubble),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(text = "카카오로 시작하기", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onGoogleLogin,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoogleWhite,
                    contentColor = GoogleLabel,
                    disabledContainerColor = GoogleWhite.copy(alpha = 0.5f),
                ),
                border = BorderStroke(1.dp, GoogleBorder),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_google_g),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = "Google로 시작하기",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = GoogleLabel,
                    )
                }
            }
        } else {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "정답과 해설은 고른 뒤에 보여요",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
    }
}

/** 채점 화면 [QuizAnswerScreen] 의 판정 밴드와 같은 형태·같은 규칙(정답만 색). */
@Composable
private fun VerdictBandCompact(isCorrect: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCorrect) Grass1 else BgSurface)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(
                if (isCorrect) R.drawable.ic_check_circle else R.drawable.ic_x_circle,
            ),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.size(10.dp))
        Text(
            text = if (isCorrect) "맞혔어요" else "아쉬워요",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isCorrect) Lime else TextPrimary,
        )
    }
}

/**
 * 선지 한 줄. 고르기 전엔 전부 같은 무게(누를 수 있는 상태), 고른 뒤엔
 * 채점 화면과 같은 3단 위계 — 정답만 면, 내 오답은 테두리, 안 고른 건 뒤로 물린다.
 */
@Composable
private fun TasteOptionRow(
    number: Int,
    label: String,
    revealed: Boolean,
    isCorrect: Boolean,
    isPicked: Boolean,
    onClick: () -> Unit,
) {
    val wrongPick = revealed && isPicked && !isCorrect
    val marked = revealed && (isCorrect || wrongPick)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    !revealed -> BgSurface
                    isCorrect -> Grass1
                    wrongPick -> BgSurface
                    else -> Color.Transparent
                },
            )
            .border(
                width = if (marked) 2.dp else 1.dp,
                color = when {
                    !revealed -> Outline
                    isCorrect -> Lime
                    wrongPick -> Error
                    else -> Outline
                },
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(enabled = !revealed, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    when {
                        !revealed -> BgElevated
                        isCorrect -> Lime
                        wrongPick -> Error
                        else -> BgElevated
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$number",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (revealed && (isCorrect || wrongPick)) OnLime else TextSecondary,
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (marked) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                !revealed -> TextPrimary
                isCorrect || wrongPick -> TextPrimary
                else -> TextMuted
            },
            modifier = Modifier.weight(1f),
        )
        if (revealed && isCorrect) {
            Text(
                text = if (isPicked) "내 답 · 정답" else "정답",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Lime,
            )
        } else if (wrongPick) {
            Text(
                text = "내 답",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun ExplanationBlock(title: String, body: String, accent: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (accent) Lime else TextPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 맛보기 문제 — 서버가 아니라 여기에만 있다.
//
// 인플레이션을 고른 이유: 경제 용어를 하나도 모르는 사람도 "장롱 속 현금"은
// 실감하고, 답이 상식으로 도달 가능해서 첫 문제가 벽이 되지 않는다.
// ─────────────────────────────────────────────────────────────────────────────

private const val TASTE_QUESTION = "물가가 계속 오르면, 장롱에 넣어둔 현금은 어떻게 될까요?"

private val TASTE_OPTIONS = listOf(
    "살 수 있는 물건이 줄어든다",
    "금액이 저절로 늘어난다",
    "가치가 그대로 유지된다",
    "이자가 붙어 저절로 불어난다",
)

private const val TASTE_ANSWER_INDEX = 0

private const val TASTE_EXPLANATION =
    "돈의 액수는 그대로여도 물건값이 오르면 같은 돈으로 살 수 있는 게 줄어듭니다. " +
        "이걸 화폐의 실질 가치가 떨어졌다고 말합니다. " +
        "물가상승률보다 낮은 이자를 주는 예금은 사실상 손해인 것도 같은 이유입니다."

private const val TASTE_KEYWORD = "인플레이션 — 물가가 지속적으로 오르는 현상"

@Preview(showBackground = true, backgroundColor = 0xFF081A2E)
@Composable
private fun TasteQuizScreenPreview() {
    FinQTheme {
        TasteQuizScreen(onKakaoLogin = {}, onGoogleLogin = {})
    }
}
