package com.example

import android.content.Intent
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
import com.example.util.SystemNotificationHelper
import com.example.viewmodel.SchoolViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: SchoolViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize notification channel for heads-up pop-up alerts
    SystemNotificationHelper.createNotificationChannel(this)

    // Handle deep link route from notification if launched via notification
    intent?.getStringExtra(SystemNotificationHelper.EXTRA_TARGET_ROUTE)?.let { route ->
      viewModel.setDeepLinkRoute(route)
    }

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

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    intent.getStringExtra(SystemNotificationHelper.EXTRA_TARGET_ROUTE)?.let { route ->
      viewModel.setDeepLinkRoute(route)
    }
  }
}
