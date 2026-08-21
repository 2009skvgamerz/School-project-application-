package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.theme.*
import com.example.util.SystemNotificationHelper

/**
 * NotificationPermissionHandler
 *
 * Enforces compulsory notification setup on app installation / first launch.
 * - Automatically triggers the system permission dialog on Android 13+ (API 33+).
 * - Shows an informative school notification rationale if permission has not been granted.
 * - Auto-detects permission status changes when returning from device settings.
 */
@Composable
fun NotificationPermissionHandler(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var hasPermission by remember {
    mutableStateOf(SystemNotificationHelper.hasNotificationPermission(context))
  }
  var hasPromptedInitially by remember { mutableStateOf(false) }
  var showMandatoryRationale by remember { mutableStateOf(false) }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasPermission = isGranted
    if (isGranted) {
      showMandatoryRationale = false
      SystemNotificationHelper.createNotificationChannel(context)
    } else {
      showMandatoryRationale = true
    }
  }

  // Auto-check on lifecycle resume (e.g. user went to Settings and returned)
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        val currentPermission = SystemNotificationHelper.hasNotificationPermission(context)
        hasPermission = currentPermission
        if (currentPermission) {
          showMandatoryRationale = false
        }
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  // Trigger permission prompt automatically on initial launch if on Android 13+
  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (!SystemNotificationHelper.hasNotificationPermission(context)) {
        if (!hasPromptedInitially) {
          hasPromptedInitially = true
          permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
      }
    }
  }

  // If permission is not granted on Android 13+, show the compulsory notification prompt
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
    CompulsoryNotificationPermissionDialog(
      onRequestPermission = {
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      },
      onOpenSettings = {
        try {
          val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(intent)
        } catch (e: Exception) {
          val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(intent)
        }
      }
    )
  }
}

@Composable
fun CompulsoryNotificationPermissionDialog(
  onRequestPermission: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  Dialog(
    onDismissRequest = { /* Non-cancellable compulsory setup */ },
    properties = DialogProperties(
      dismissOnBackPress = false,
      dismissOnClickOutside = false,
      usePlatformDefaultWidth = false
    )
  ) {
    Card(
      modifier = modifier
        .fillMaxWidth(0.92f)
        .padding(16.dp)
        .testTag("compulsory_notification_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(24.dp)
          .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Icon Badge
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(SchoolNavyPrimary.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .background(SchoolNavyPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = SchoolGold,
              modifier = Modifier.size(28.dp)
            )
          }
        }

        // Header Title
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "Enable Notifications",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = 20.sp
            ),
            color = SchoolNavyPrimary,
            textAlign = TextAlign.Center
          )
          Surface(
            color = SchoolAccentRed.copy(alpha = 0.12f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "REQUIRED FOR SCHOOL APP",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = SchoolAccentRed,
                fontSize = 10.sp
              ),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }

        Text(
          text = "To ensure students, teachers, and parents receive real-time campus communications, please allow notifications for:",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )

        // Feature Highlights
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          NotificationFeatureRow(
            icon = Icons.Default.Campaign,
            iconTint = Color(0xFFDC2626),
            title = "Emergency & Holiday Circulars",
            desc = "Immediate alerts for weather holidays, fee notices, and official circulars."
          )
          NotificationFeatureRow(
            icon = Icons.Default.Assignment,
            iconTint = Color(0xFFD97706),
            title = "Homework & Submission Reminders",
            desc = "Daily subject homework deadlines and teacher feedback."
          )
          NotificationFeatureRow(
            icon = Icons.Default.CalendarMonth,
            iconTint = Color(0xFF2563EB),
            title = "Timetable & Room Adjustments",
            desc = "Instant notification for period changes and substitute teachers."
          )
          NotificationFeatureRow(
            icon = Icons.Default.FactCheck,
            iconTint = Color(0xFF059669),
            title = "Daily Attendance & Roll Call",
            desc = "Instant verification when morning attendance is recorded."
          )
        }

        // Action Buttons
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onRequestPermission,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("allow_notifications_btn"),
            colors = ButtonDefaults.buttonColors(
              containerColor = SchoolNavyPrimary,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Notifications,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Allow Notifications",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
          }

          OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("open_settings_permission_btn"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Open Device Settings", style = MaterialTheme.typography.labelMedium)
          }
        }
      }
    }
  }
}

@Composable
private fun NotificationFeatureRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  title: String,
  desc: String
) {
  Row(
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(iconTint.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(18.dp)
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
