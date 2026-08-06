package com.finq.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finq.app.R
import com.finq.app.ui.theme.GoogleBorder
import com.finq.app.ui.theme.GoogleLabel
import com.finq.app.ui.theme.GoogleWhite
import com.finq.app.ui.theme.KakaoLabel
import com.finq.app.ui.theme.KakaoYellow
import com.finq.app.ui.theme.Lime
import com.finq.app.ui.theme.TextMuted
import com.finq.app.ui.theme.TextPrimary

@Composable
fun LoginScreen(
    isLoading: Boolean,
    error: String?,
    onKakaoLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onClearError: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error)
            onClearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ── 경제잔디 로고 — 런처 아이콘 에셋을 그대로 겹쳐 그린다 ──
            //
            // 전용 로고 파일(ic_finq_logo)을 지우고 여기로 왔다. 그 파일은 런처 아이콘의
            // **사본**이었고 두 세대 뒤처져 있었다 — 네이비 면 + 라임 후광(걷어낸 중간 톤),
            // grass_2 줄기(같은 병), 1.8 두께 잎맥(축소하면 먼저 뭉개는 획). 96dp 로 크게만
            // 떠서 눈에 안 걸렸을 뿐이다. 사본을 없애면 런처를 고칠 때 여기가 따라온다.
            //
            // 안쪽 아트를 1.8 배로 키워 넘치는 테두리를 잘라낸다. 어댑티브 아이콘은 108
            // 캔버스 중 가운데 72 만 마스크 뒤에 보이므로, 그대로 그리면 런처보다 새싹이
            // 훨씬 작게 뜬다. 1.5 배가 그 안전영역(=스토어 512 와 같은 밀도)이고 여기선
            // 로고가 축소될 일이 없어 한 단계 더 키웠다.
            //
            // ⚠️ requiredSize 여야 한다 — Modifier.size 는 부모 제약(96dp)을 따르므로
            // 172dp 를 줘도 96 으로 눌려 확대가 통째로 사라진다(실기기에서 확인).
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center,
            ) {
                val art = Modifier.requiredSize(172.dp)  // 96 × 1.8
                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = art,
                )
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "경제잔디",
                    modifier = art,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 워드마크: 경제(네이비) + 잔디(라임 강조) ──────────
            Row {
                Text(
                    text = "경제",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = "잔디",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Lime,
                    letterSpacing = (-0.5).sp,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "오늘의 금융 퀴즈",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(64.dp))

            // ── 카카오 로그인 버튼 ──────────────────────────────
            Button(
                onClick = onKakaoLogin,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KakaoYellow,
                    contentColor = KakaoLabel,
                    disabledContainerColor = KakaoYellow.copy(alpha = 0.5f),
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_kakao_bubble),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "카카오로 시작하기",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── 구글 로그인 버튼 ────────────────────────────────
            Button(
                onClick = onGoogleLogin,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoogleWhite,
                    contentColor = GoogleLabel,
                    disabledContainerColor = GoogleWhite.copy(alpha = 0.5f),
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoogleBorder),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_google_g),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "Google로 시작하기",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = GoogleLabel,
                    )
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(28.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = TextPrimary,
                    strokeWidth = 2.5.dp,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}
