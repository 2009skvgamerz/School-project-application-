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

// ==========================================
// St. Joseph's School - Material 3 Theme Configuration
// ==========================================

private val StJosephLightColorScheme = lightColorScheme(
  // Primary (Deep Academic Navy)
  primary = SchoolNavyPrimary,
  onPrimary = Color.White,
  primaryContainer = SchoolNavyContainer,
  onPrimaryContainer = SchoolNavyOnContainer,
  inversePrimary = SchoolNavyPrimaryDark,

  // Secondary (Heritage Academic Gold)
  secondary = SchoolGold,
  onSecondary = Color.White,
  secondaryContainer = SchoolGoldContainer,
  onSecondaryContainer = SchoolGoldOnContainer,

  // Tertiary (Academic Emerald)
  tertiary = SchoolEmerald,
  onTertiary = Color.White,
  tertiaryContainer = SchoolEmeraldContainer,
  onTertiaryContainer = SchoolEmeraldOnContainer,

  // Background & Surfaces
  background = SchoolSurfaceLight,
  onBackground = SchoolTextPrimary,
  surface = SchoolCardLight,
  onSurface = SchoolTextPrimary,
  surfaceVariant = SchoolSurfaceVariantLight,
  onSurfaceVariant = SchoolTextSecondary,
  surfaceTint = SchoolNavyPrimary,

  // Outlines & Borders
  outline = SchoolBorderLight,
  outlineVariant = SchoolOutlineVariantLight,

  // Error & Status
  error = SchoolError,
  onError = Color.White,
  errorContainer = SchoolErrorContainer,
  onErrorContainer = SchoolOnErrorContainer,

  // Scrim
  scrim = Color.Black
)

private val StJosephDarkColorScheme = darkColorScheme(
  // Primary
  primary = SchoolNavyPrimaryDark,
  onPrimary = SchoolNavyOnPrimaryDark,
  primaryContainer = SchoolNavyContainerDark,
  onPrimaryContainer = SchoolNavyOnContainerDark,
  inversePrimary = SchoolNavyPrimary,

  // Secondary
  secondary = SchoolGoldDarkTheme,
  onSecondary = SchoolGoldOnDark,
  secondaryContainer = SchoolGoldContainerDark,
  onSecondaryContainer = SchoolGoldOnContainerDark,

  // Tertiary
  tertiary = SchoolEmeraldDark,
  onTertiary = SchoolEmeraldOnDark,
  tertiaryContainer = SchoolEmeraldContainerDark,
  onTertiaryContainer = SchoolEmeraldOnContainerDark,

  // Background & Surfaces
  background = SchoolSurfaceDark,
  onBackground = SchoolTextPrimaryDark,
  surface = SchoolCardDark,
  onSurface = SchoolTextPrimaryDark,
  surfaceVariant = SchoolSurfaceVariantDark,
  onSurfaceVariant = SchoolTextSecondaryDark,
  surfaceTint = SchoolNavyPrimaryDark,

  // Outlines & Borders
  outline = SchoolBorderDark,
  outlineVariant = Color(0xFF64748B),

  // Error & Status
  error = Color(0xFFFFB4AB),
  onError = Color(0xFF690005),
  errorContainer = Color(0xFF93000A),
  onErrorContainer = Color(0xFFFFDAD6),

  // Scrim
  scrim = Color.Black
)

@Composable
fun StJosephTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep false by default to ensure St. Joseph's brand identity consistency
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> StJosephDarkColorScheme
    else -> StJosephLightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = Shapes,
    content = content
  )
}

// Backward-compatible alias for existing Composable references
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  StJosephTheme(
    darkTheme = darkTheme,
    dynamicColor = dynamicColor,
    content = content
  )
}
