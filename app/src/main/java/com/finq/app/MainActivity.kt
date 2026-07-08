package com.finq.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.finq.app.ui.navigation.FinQNavHost
import com.finq.app.ui.theme.FinQTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 단일 다크 테마 — 시스템 바 투명 + 아이콘은 밝게 고정.
        // SystemBarStyle.dark 는 "다크 배경용"이라는 뜻이라 바 아이콘이 밝게 그려진다.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            FinQTheme {
                // 단일 Scaffold 구조 — FinQNavHost 내부의 Scaffold 가
                // 시스템 인셋과 컨테이너 색상까지 모두 책임진다.
                FinQNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
