package com.example.pinq_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.pinq_frontend.ui.navigation.FinQNavHost
import com.example.pinq_frontend.ui.theme.PinQ_frontendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PinQ_frontendTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FinQNavHost(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
