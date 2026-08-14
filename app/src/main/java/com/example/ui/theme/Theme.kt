package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = SchoolNavyPrimaryDark,
  onPrimary = Color(0xFF003258),
  primaryContainer = SchoolNavyDark,
  onPrimaryContainer = Color(0xFFD1E4FF),
  secondary = SchoolGoldDarkTheme,
  onSecondary = Color(0xFF452B00),
  secondaryContainer = Color(0xFF633F00),
  onSecondaryContainer = Color(0xFFFFDDB3),
  tertiary = Color(0xFF6EE7B7),
  background = SchoolSurfaceDark,
  surface = SchoolCardDark,
  onBackground = SchoolTextPrimaryDark,
  onSurface = SchoolTextPrimaryDark,
  surfaceVariant = SchoolBorderDark,
  outline = Color(0xFF64748B)
)

private val LightColorScheme = lightColorScheme(
  primary = SchoolNavyPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD8E6FF),
  onPrimaryContainer = Color(0xFF001B3F),
  secondary = SchoolGold,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFFFECCF),
  onSecondaryContainer = Color(0xFF2E1500),
  tertiary = SchoolAccentGreen,
  background = SchoolSurfaceLight,
  surface = SchoolCardLight,
  onBackground = SchoolTextPrimary,
  onSurface = SchoolTextPrimary,
  surfaceVariant = Color(0xFFF1F5F9),
  outline = SchoolBorderLight
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false to prioritize school branding
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
