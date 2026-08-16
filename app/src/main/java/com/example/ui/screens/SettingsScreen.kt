package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemeMode
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.theme.*

/**
 * SettingsScreen - Application Settings, Theme Selection, Version & System Info.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  currentUser: User,
  currentThemeMode: AppThemeMode,
  onThemeModeChange: (AppThemeMode) -> Unit,
  onSwitchRole: () -> Unit,
  onSignOut: () -> Unit,
  onResetDatabase: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showResetSuccessBanner by remember { mutableStateOf(false) }
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  // App Meta constants
  val appVersion = "2.4.0"
  val buildNumber = "2026.08.16.1"
  val releaseChannel = "Enterprise Production"
  val composeVersion = "Jetpack Compose M3 (Material 3)"
  val databaseEngine = "Room SQLite Database v1"

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("settings_screen")
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. App Banner & Version Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SchoolNavyPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = SchoolGold,
                modifier = Modifier.size(32.dp)
              )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "St. Joseph's School Portal",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SchoolNavyPrimary
              )
              Text(
                text = "Academic ERP & Attendance Management",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  color = SchoolAccentGreen.copy(alpha = 0.12f),
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = "v$appVersion",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SchoolAccentGreen,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
                Text(
                  text = "• Build $buildNumber",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // 2. Theme & Appearance Section
    item {
      SettingsSectionTitle(title = "App Theme & Appearance", icon = Icons.Default.Palette)
    }

    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "Select Application Theme",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )

          AppThemeMode.entries.forEach { mode ->
            val isSelected = currentThemeMode == mode
            val icon = when (mode) {
              AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
              AppThemeMode.LIGHT -> Icons.Default.LightMode
              AppThemeMode.DARK -> Icons.Default.DarkMode
            }

            Surface(
              color = if (isSelected) SchoolNavyPrimary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
              shape = RoundedCornerShape(12.dp),
              border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SchoolNavyPrimary) else null,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onThemeModeChange(mode) }
                .testTag("theme_option_${mode.name.lowercase()}")
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) SchoolNavyPrimary else MaterialTheme.colorScheme.surfaceVariant),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                  )
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = mode.label,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) SchoolNavyPrimary else MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = mode.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                RadioButton(
                  selected = isSelected,
                  onClick = { onThemeModeChange(mode) },
                  colors = RadioButtonDefaults.colors(selectedColor = SchoolNavyPrimary)
                )
              }
            }
          }
        }
      }
    }

    // 3. Application Details & Version Info Section
    item {
      SettingsSectionTitle(title = "Application & Build Information", icon = Icons.Default.Info)
    }

    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          InfoRow(label = "Application Version", value = "v$appVersion")
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          InfoRow(label = "Build Number", value = buildNumber)
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          InfoRow(label = "Release Channel", value = releaseChannel)
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          InfoRow(label = "UI Architecture", value = composeVersion)
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          InfoRow(label = "Persistence Engine", value = databaseEngine)
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          InfoRow(label = "Academic Session", value = "2026 - 2027 (Term 1)")
        }
      }
    }

    // 4. Data Management & Reset
    item {
      SettingsSectionTitle(title = "Database & Expo Controls", icon = Icons.Default.Storage)
    }

    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(imageVector = Icons.Default.Dataset, contentDescription = null, tint = SchoolNavyPrimary)
            Column {
              Text(
                text = "Room Database Seed Records",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Pre-populate classes, students, and attendance records for expo demonstration.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          OutlinedButton(
            onClick = { showResetConfirmDialog = true },
            modifier = Modifier.fillMaxWidth().testTag("reset_demo_data_btn"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reload / Reset Demo Data")
          }

          AnimatedVisibility(visible = showResetSuccessBanner) {
            Surface(
              color = SchoolAccentGreen.copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SchoolAccentGreen, modifier = Modifier.size(16.dp))
                Text(
                  text = "Room Database successfully reloaded with fresh student & attendance data.",
                  style = MaterialTheme.typography.bodySmall,
                  color = SchoolAccentGreen
                )
              }
            }
          }
        }
      }
    }

    // 5. User Account & Session
    item {
      SettingsSectionTitle(title = "Account & Session", icon = Icons.Default.Person)
    }

    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(SchoolGold.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = SchoolGoldDark, modifier = Modifier.size(28.dp))
            }
            Column {
              Text(
                text = currentUser.fullName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "${currentUser.role.label} • ${currentUser.email}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = onSwitchRole,
              modifier = Modifier.weight(1f).testTag("settings_switch_role_btn"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Switch Role")
            }

            Button(
              onClick = onSignOut,
              modifier = Modifier.weight(1f).testTag("settings_sign_out_btn"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = SchoolError)
            ) {
              Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Sign Out")
            }
          }
        }
      }
    }

    // 6. IT Support & Helpdesk Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(imageVector = Icons.Default.HeadsetMic, contentDescription = null, tint = SchoolNavyPrimary, modifier = Modifier.size(18.dp))
            Text(
              text = "Institutional IT Support Desk",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = SchoolNavyPrimary
            )
          }
          Text(
            text = "For credentials assistance, attendance corrections, or report card queries, contact IT Helpdesk at support@stjosephs.edu or +91 98765 00100.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }

  // Confirmation dialog for resetting data
  if (showResetConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showResetConfirmDialog = false },
      title = { Text("Reset Demo Database?") },
      text = { Text("This will refresh all student records, roll-call statuses, and assignments with default sample records.") },
      confirmButton = {
        Button(
          onClick = {
            onResetDatabase()
            showResetConfirmDialog = false
            showResetSuccessBanner = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
        ) {
          Text("Reset Data")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun SettingsSectionTitle(title: String, icon: ImageVector) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.padding(vertical = 4.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = SchoolNavyPrimary,
      modifier = Modifier.size(18.dp)
    )
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
      color = SchoolNavyPrimary
    )
  }
}

@Composable
private fun InfoRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
