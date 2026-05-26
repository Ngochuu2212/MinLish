package com.example.english_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.english_app.notification.DailyReminderWorker
import com.example.english_app.ui.navigation.AppNavigation
import com.example.english_app.ui.theme.English_AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DailyReminderWorker.schedule(this)
        val app = application as MinLishApp
        setContent {
            English_AppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(app)
                }
            }
        }
    }
}
