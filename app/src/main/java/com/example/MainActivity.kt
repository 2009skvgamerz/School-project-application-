package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.model.AppThemeMode
import com.example.ui.MainSchoolApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SchoolViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: SchoolViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val themeMode by viewModel.themeMode.collectAsState()
      val systemInDark = isSystemInDarkTheme()
      val isDarkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
      }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        MainSchoolApp(viewModel = viewModel)
      }
    }
  }
}
