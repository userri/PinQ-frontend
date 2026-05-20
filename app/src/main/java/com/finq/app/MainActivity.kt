package com.finq.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.finq.app.ui.navigation.FinQNavHost
import com.finq.app.ui.theme.FinQTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinQTheme {
                // 단일 Scaffold 구조 — FinQNavHost 내부의 Scaffold 가
                // 시스템 인셋과 컨테이너 색상까지 모두 책임진다.
                FinQNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
