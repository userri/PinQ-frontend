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
import com.finq.app.ui.theme.AccentText
import com.finq.app.ui.theme.BrandNavy
import com.finq.app.ui.theme.GoogleBorder
import com.finq.app.ui.theme.GoogleLabel
import com.finq.app.ui.theme.GoogleWhite
import com.finq.app.ui.theme.KakaoLabel
import com.finq.app.ui.theme.KakaoYellow
import com.finq.app.ui.theme.TextMuted

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
            // ── 경제잔디 로고 ─────────────────────────────────────
            Image(
                painter = painterResource(R.drawable.ic_finq_logo),
                contentDescription = "경제잔디",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(22.dp)),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── 워드마크: 경제(네이비) + 잔디(라임 강조) ──────────
            Row {
                Text(
                    text = "경제",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandNavy,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = "잔디",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentText,
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
                    color = BrandNavy,
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
