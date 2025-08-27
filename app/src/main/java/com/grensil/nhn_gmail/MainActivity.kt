package com.grensil.nhn_gmail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            enableEdgeToEdge()
            Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                Box(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
                    Text(text = "text", fontSize = 24.sp, color = Color.Cyan)
                }
            }
        }
    }
}
