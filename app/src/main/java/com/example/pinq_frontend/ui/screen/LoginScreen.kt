package com.example.pinq_frontend.ui.screen

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 카카오 공식 색상
private val KakaoYellow = Color(0xFFFEE500)
private val KakaoLabel  = Color(0xFF191919)

// 구글 버튼 색상
private val GoogleWhite  = Color(0xFFFFFFFF)
private val GoogleBorder = Color(0xFFDADADA)
private val GoogleLabel  = Color(0xFF3C4043)

@Composable
fun LoginScreen(
    isLoading: Boolean,
    error: String?,
    onKakaoLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onClearError: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // 에러 발생 시 Snackbar 표시
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error)
            onClearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ── 앱 로고 / 타이틀 ───────────────────────────────────────────
            Text(
                text = "PinQ",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 48.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "오늘의 금융 퀴즈",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(64.dp))

            // ── 카카오 로그인 버튼 ─────────────────────────────────────────
            Button(
                onClick = onKakaoLogin,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KakaoYellow,
                    contentColor   = KakaoLabel,
                    disabledContainerColor = KakaoYellow.copy(alpha = 0.5f),
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "💬",
                        fontSize = 18.sp,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "카카오로 시작하기",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 구글 로그인 버튼 ───────────────────────────────────────────
            Button(
                onClick = onGoogleLogin,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoogleWhite,
                    contentColor   = GoogleLabel,
                    disabledContainerColor = GoogleWhite.copy(alpha = 0.5f),
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(text = "G", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF4285F4))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Google로 시작하기",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = GoogleLabel,
                    )
                }
            }

            // ── 로딩 인디케이터 ────────────────────────────────────────────
            if (isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        }

        // ── Snackbar ───────────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}
