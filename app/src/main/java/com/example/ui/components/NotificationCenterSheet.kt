package com.example.ui.components

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppNotification
import com.example.model.NotificationType
import com.example.ui.theme.*
import com.example.util.SystemNotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
  notifications: List<AppNotification>,
  onDismiss: () -> Unit,
  onMarkAsRead: (String) -> Unit,
  onMarkAllAsRead: () -> Unit,
  onDeleteNotification: (String) -> Unit,
  onSendTestNotification: () -> Unit,
  onNavigateToRoute: (String) -> Unit,
  onTriggerImmediatePopUp: (title: String, message: String, type: NotificationType, route: String) -> Unit = { _, _, _, _ -> },
  onTriggerDelayedPopUp: (delaySeconds: Long, title: String, message: String, type: NotificationType, route: String) -> Unit = { _, _, _, _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var hasPermission by remember {
    mutableStateOf(SystemNotificationHelper.hasNotificationPermission(context))
  }

  // Permission launcher for Android 13+ (POST_NOTIFICATIONS)
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasPermission = isGranted
    if (isGranted) {
      Toast.makeText(context, "System Notification permissions granted! Pop-ups active.", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "Notifications permission was denied. Please enable in Settings.", Toast.LENGTH_LONG).show()
    }
  }

  var selectedType by remember { mutableStateOf<NotificationType?>(null) }
  var showOnlyUnread by remember { mutableStateOf(false) }
  var showPopUpTools by remember { mutableStateOf(true) }
  var delayedCountdownActive by remember { mutableStateOf(false) }

  val filteredNotifications = remember(notifications, selectedType, showOnlyUnread) {
    notifications.filter { item ->
      val matchesType = selectedType == null || item.type == selectedType
      val matchesUnread = !showOnlyUnread || !item.isRead
      matchesType && matchesUnread
    }
  }

  val unreadCount = remember(notifications) {
    notifications.count { !it.isRead }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = modifier.testTag("notification_center_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp)
        .padding(bottom = 28.dp)
    ) {
      // 1. Header with Title, Unread Count & Quick Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(SchoolNavyPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = SchoolNavyPrimary,
              modifier = Modifier.size(24.dp)
            )
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "Live Notifications",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              if (unreadCount > 0) {
                Badge(
                  containerColor = Color(0xFFDC2626),
                  contentColor = Color.White
                ) {
                  Text("$unreadCount new", style = MaterialTheme.typography.labelSmall)
                }
              }
            }
            Text(
              text = "System pop-up alerts & institutional bulletins",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Action Buttons: Toggle Pop-up tools & Mark All
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          IconButton(
            onClick = { showPopUpTools = !showPopUpTools },
            modifier = Modifier.testTag("toggle_popup_tools_btn")
          ) {
            Icon(
              imageVector = if (showPopUpTools) Icons.Default.Campaign else Icons.Outlined.Campaign,
              contentDescription = "External Pop-up Tools",
              tint = if (showPopUpTools) SchoolAccentGreen else SchoolNavyPrimary
            )
          }

          if (unreadCount > 0) {
            TextButton(
              onClick = onMarkAllAsRead,
              contentPadding = PaddingValues(horizontal = 8.dp),
              modifier = Modifier.testTag("mark_all_read_btn")
            ) {
              Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = SchoolAccentGreen
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Read All",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = SchoolAccentGreen
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Permission Banner if Android 13+ permission not granted
      if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFF87171))
          ),
          modifier = Modifier.fillMaxWidth().testTag("permission_request_card")
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.NotificationImportant,
              contentDescription = null,
              tint = Color(0xFFDC2626),
              modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Enable Pop-Up Notifications",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF991B1B)
              )
              Text(
                text = "Allow high-priority heads-up pop-ups to receive alerts outside this app.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7F1D1D)
              )
            }
            Button(
              onClick = {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("grant_permission_btn")
            ) {
              Text("Allow", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      // 3. Pop-Up Notification Outside The App Control Panel
      AnimatedVisibility(visible = showPopUpTools) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3875).copy(alpha = 0.05f)),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(SchoolNavyPrimary.copy(alpha = 0.2f))
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .testTag("external_popup_panel")
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.OpenInNew,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "Pop-Up Window (Outside App)",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              Surface(
                color = SchoolAccentGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "HEADS-UP BANNER",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                  color = SchoolAccentGreen,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Text(
              text = "Test instant heads-up notifications floating over Android home screen or any other apps.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Trigger Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Button 1: Immediate Pop-up
              Button(
                onClick = {
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  } else {
                    onTriggerImmediatePopUp(
                      "⚡ Urgent: Physics Lab (Grade 12-A)",
                      "Experiment on Electromagnetic Induction starting in Science Lab 1. Bring record notebooks.",
                      NotificationType.ACADEMIC,
                      "timetable"
                    )
                    Toast.makeText(context, "Heads-up pop-up notification triggered!", Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("trigger_immediate_popup_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
              ) {
                Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pop-Up Now", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }

              // Button 2: Pop-up in 5 Seconds (User can minimize app to see outside)
              OutlinedButton(
                onClick = {
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  } else {
                    delayedCountdownActive = true
                    onTriggerDelayedPopUp(
                      5L,
                      "📢 School Notice: Science & AI Expo 2026",
                      "Final prototype registrations close at 4:00 PM today. Deep-link ready!",
                      NotificationType.NOTICE,
                      "notices"
                    )
                    Toast.makeText(context, "Pop-up scheduled in 5s! Press Home button to see it outside the app.", Toast.LENGTH_LONG).show()
                  }
                },
                modifier = Modifier
                  .weight(1.2f)
                  .testTag("trigger_delayed_popup_btn"),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(imageVector = Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (delayedCountdownActive) "In 5s (Exit App!)" else "Pop-Up in 5s (Exit App)",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
              }
            }
          }
        }
      }

      // 4. Filter chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          FilterChip(
            selected = selectedType == null && !showOnlyUnread,
            onClick = {
              selectedType = null
              showOnlyUnread = false
            },
            label = { Text("All (${notifications.size})") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          )
        }

        item {
          FilterChip(
            selected = showOnlyUnread,
            onClick = {
              showOnlyUnread = !showOnlyUnread
            },
            label = { Text("Unread ($unreadCount)") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.MarkEmailUnread, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          )
        }

        items(NotificationType.values().filter { it != NotificationType.ALL }) { type ->
          val count = notifications.count { it.type == type }
          if (count > 0) {
            FilterChip(
              selected = selectedType == type,
              onClick = {
                selectedType = if (selectedType == type) null else type
              },
              label = { Text("${type.label} ($count)") }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 5. Notification List or Empty State
      if (filteredNotifications.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(SchoolNavyPrimary.copy(alpha = 0.08f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                tint = SchoolNavyPrimary,
                modifier = Modifier.size(32.dp)
              )
            }
            Text(
              text = "You're all caught up!",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = SchoolNavyPrimary
            )
            Text(
              text = "No pending notifications in this filter",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
              onClick = {
                onTriggerImmediatePopUp(
                  "Campus Notification Alert",
                  "Mathematics Calculus Assignment due tomorrow morning for Grade 12-A.",
                  NotificationType.HOMEWORK,
                  "homework"
                )
              },
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Generate Pop-Up Alert", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
        ) {
          items(filteredNotifications, key = { it.id }) { item ->
            NotificationCardItem(
              notification = item,
              onClick = {
                onMarkAsRead(item.id)
                item.actionRoute?.let { route ->
                  onDismiss()
                  onNavigateToRoute(route)
                }
              },
              onDelete = { onDeleteNotification(item.id) },
              onAction = {
                onMarkAsRead(item.id)
                item.actionRoute?.let { route ->
                  onDismiss()
                  onNavigateToRoute(route)
                }
              },
              onPopUpOutside = {
                onTriggerImmediatePopUp(
                  item.title,
                  item.message,
                  item.type,
                  item.actionRoute ?: "dashboard"
                )
                Toast.makeText(context, "Pop-up alert shown outside app!", Toast.LENGTH_SHORT).show()
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun NotificationCardItem(
  notification: AppNotification,
  onClick: () -> Unit,
  onDelete: () -> Unit,
  onAction: () -> Unit,
  onPopUpOutside: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val icon: ImageVector = when (notification.type) {
    NotificationType.HOMEWORK -> Icons.Default.Assignment
    NotificationType.ATTENDANCE -> Icons.Default.CheckCircle
    NotificationType.NOTICE -> Icons.Default.Campaign
    NotificationType.EXAM -> Icons.Default.MenuBook
    NotificationType.FEE -> Icons.Default.ReceiptLong
    NotificationType.EVENT -> Icons.Default.Event
    NotificationType.ACADEMIC -> Icons.Default.School
    NotificationType.ALL -> Icons.Default.Notifications
  }

  val typeColor = Color(notification.type.colorHex)

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (!notification.isRead) {
        SchoolNavyPrimary.copy(alpha = 0.05f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
      }
    ),
    border = if (!notification.isRead) {
      CardDefaults.outlinedCardBorder().copy(
        brush = androidx.compose.ui.graphics.SolidColor(SchoolNavyPrimary.copy(alpha = 0.25f))
      )
    } else null,
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("notification_item_${notification.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.Top
    ) {
      // Icon Box
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(typeColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = typeColor,
          modifier = Modifier.size(22.dp)
        )
      }

      // Content Column
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Tag row
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            color = typeColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = notification.type.label.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
              color = typeColor,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          if (notification.isUrgent) {
            Surface(
              color = Color(0xFFDC2626).copy(alpha = 0.15f),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = "URGENT",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                color = Color(0xFFDC2626),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.weight(1f))

          Text(
            text = notification.timeAgo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          if (!notification.isRead) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(SchoolNavyPrimary)
            )
          }
        }

        // Title
        Text(
          text = notification.title,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold
          ),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        // Message
        Text(
          text = notification.message,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis
        )

        // Action button row
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (notification.actionRoute != null) {
            FilledTonalButton(
              onClick = onAction,
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = SchoolNavyPrimary.copy(alpha = 0.1f),
                contentColor = SchoolNavyPrimary
              ),
              modifier = Modifier.height(28.dp)
            ) {
              Text(
                text = when (notification.actionRoute) {
                  "homework" -> "View Homework"
                  "attendance" -> "View Attendance"
                  "notices" -> "Open Notice"
                  "timetable" -> "View Timetable"
                  else -> "View Details"
                },
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp))
            }
          } else {
            Spacer(modifier = Modifier.width(1.dp))
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            // Button to test pop-up outside for this specific card
            IconButton(
              onClick = onPopUpOutside,
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.OpenInNew,
                contentDescription = "Pop-up Outside App",
                tint = SchoolNavyPrimary,
                modifier = Modifier.size(16.dp)
              )
            }

            IconButton(
              onClick = onDelete,
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Dismiss",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }
  }
}
