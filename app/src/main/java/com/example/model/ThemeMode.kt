package com.example.model

/**
 * AppThemeMode - Controls the application's visual theme.
 */
enum class AppThemeMode(
  val label: String,
  val description: String
) {
  SYSTEM("System Default", "Follows device system light/dark mode settings"),
  LIGHT("Light Theme", "Crisp academic white & navy styling for daytime"),
  DARK("Dark Theme", "High-contrast slate & navy styling for night use")
}
