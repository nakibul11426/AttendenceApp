package com.abdur.rahman.attendanceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.abdur.rahman.attendanceapp.ui.screens.MainScreen
import com.abdur.rahman.attendanceapp.ui.theme.AttendanceAppTheme
import com.abdur.rahman.attendanceapp.util.ThemePreferences
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val themePreferences = ThemePreferences(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themePreferences.isDarkMode.collectAsState(initial = false)
            val scope = rememberCoroutineScope()
            
            AttendanceAppTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = {
                            scope.launch {
                                themePreferences.toggleTheme()
                            }
                        }
                    )
                }
            }
        }
    }
}