package com.grensil.nhn_gmail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.grensil.network.HttpClient
import com.grensil.nhn_gmail.di.AppModule

class MainActivity : ComponentActivity() {

    private val appModules = AppModule(HttpClient())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MainScreen()
        }
    }
}
