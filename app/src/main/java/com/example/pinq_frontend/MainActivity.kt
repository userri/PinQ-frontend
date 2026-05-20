package com.example.pinq_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.pinq_frontend.ui.navigation.FinQNavHost
import com.example.pinq_frontend.ui.theme.PinQ_frontendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PinQ_frontendTheme {
                // 단일 Scaffold 구조 — FinQNavHost 내부의 Scaffold 가
                // 시스템 인셋과 컨테이너 색상까지 모두 책임진다.
                FinQNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
