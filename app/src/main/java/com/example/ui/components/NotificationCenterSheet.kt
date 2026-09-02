package com.example.ui.components

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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

enum class NotificationSheetViewMode(val label: String, val icon: ImageVector) {
  LIVE_FEED("Live Feed", Icons.Default.NotificationsActive),
  OFFLINE_ROOM_DB("Room History", Icons.Default.Storage),
  CHANNELS_PREFS("Channel Prefs", Icons.Default.Tune)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
  notifications: List<AppNotification>,
  roomNotifications: List<com.example.data.local.entity.NotificationEntity> = emptyList(),
  onDismiss: () -> Unit,
  onMarkAsRead: (String) -> Unit,
  onMarkAllAsRead: () -> Unit,
  onDeleteNotification: (String) -> Unit,
  onNavigateToRoute: (String) -> Unit,
  fcmDeviceToken: String? = null,
  subscribedTopics: Set<String> = setOf("all_school", "announcements", "events", "exams"),
  onToggleTopic: (String) -> Unit = {},
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
      Toast.makeText(context, "System Notification permissions granted!", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "Notifications permission was denied. Please enable in Settings.", Toast.LENGTH_LONG).show()
    }
  }

  var activeViewMode by remember { mutableStateOf(NotificationSheetViewMode.LIVE_FEED) }
  var selectedType by remember { mutableStateOf<NotificationType?>(null) }
  var showOnlyUnread by remember { mutableStateOf(false) }

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
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "Notification Center",
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
              text = "Live Updates • Room DB • Channels",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Action Buttons: Mark All as Read
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

      Spacer(modifier = Modifier.height(10.dp))

      // View Mode Switcher: Live Feed vs Room DB History vs Channels
      SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
      ) {
        NotificationSheetViewMode.values().forEachIndexed { index, mode ->
          SegmentedButton(
            selected = activeViewMode == mode,
            onClick = { activeViewMode = mode },
            shape = SegmentedButtonDefaults.itemShape(index = index, count = NotificationSheetViewMode.values().size),
            icon = {
              Icon(
                imageVector = mode.icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
            }
          ) {
            Text(
              text = if (mode == NotificationSheetViewMode.OFFLINE_ROOM_DB) "Room DB (${roomNotifications.size})" else mode.label,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
            )
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

      // Content based on selected View Mode
      when (activeViewMode) {
        NotificationSheetViewMode.LIVE_FEED -> {
          // Filter chips
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

          // Notification List or Empty State
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
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                  )
                }
                Text(
                  text = "You're all caught up!",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "No pending notifications in this filter",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 440.dp)
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
                  }
                )
              }
            }
          }
        }

        NotificationSheetViewMode.OFFLINE_ROOM_DB -> {
          // Room Database History View
          RoomDatabaseNotificationHistoryView(
            roomNotifications = roomNotifications,
            onNotificationClick = { entity ->
              onMarkAsRead(entity.id)
              entity.actionRoute?.let { route ->
                onDismiss()
                onNavigateToRoute(route)
              }
            },
            onDelete = { onDeleteNotification(it) }
          )
        }

        NotificationSheetViewMode.CHANNELS_PREFS -> {
          // Notification Channel Preferences View
          NotificationChannelsPreferenceView(
            onOpenChannelSettings = { channelId ->
              SystemNotificationHelper.openNotificationSettings(context, channelId)
            },
            onOpenAllSettings = {
              SystemNotificationHelper.openNotificationSettings(context)
            }
          )
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
    NotificationType.BUS -> Icons.Default.DirectionsBus
    NotificationType.ANNOUNCEMENT -> Icons.Default.Campaign
    NotificationType.ALL -> Icons.Default.Notifications
  }

  val typeColor = Color(notification.type.colorHex)

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (!notification.isRead) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
      }
    ),
    border = if (!notification.isRead) {
      CardDefaults.outlinedCardBorder().copy(
        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
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
                .background(MaterialTheme.colorScheme.primary)
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
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.primary
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

@Composable
fun RoomDatabaseNotificationHistoryView(
  roomNotifications: List<com.example.data.local.entity.NotificationEntity>,
  onNotificationClick: (com.example.data.local.entity.NotificationEntity) -> Unit,
  onDelete: (String) -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Surface(
      color = Color(0xFFF0FDF4),
      shape = RoundedCornerShape(10.dp),
      border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Storage,
          contentDescription = null,
          tint = Color(0xFF16A34A),
          modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Offline SQLite Storage (Room DB)",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF15803D)
          )
          Text(
            text = "All FCM push & system notifications are automatically persisted locally. Accessible offline without internet.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Color(0xFF166534)
          )
        }
      }
    }

    if (roomNotifications.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "No stored notifications in Room SQLite yet.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 380.dp)
      ) {
        items(roomNotifications, key = { it.id }) { entity ->
          val formattedTime = remember(entity.timestamp) {
            val date = java.util.Date(entity.timestamp)
            java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(date)
          }

          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (!entity.isRead) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onNotificationClick(entity) }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.Top
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(entity.type.colorHex).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (entity.isUrgent) Icons.Default.PriorityHigh else Icons.Default.Notifications,
                  contentDescription = null,
                  tint = Color(entity.type.colorHex),
                  modifier = Modifier.size(18.dp)
                )
              }

              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(
                    color = Color(entity.type.colorHex).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = "${entity.type.label.uppercase()} • ROOM",
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                      color = Color(entity.type.colorHex),
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                  Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Text(
                  text = entity.title,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                  text = entity.message,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )

                if (entity.targetId != null || entity.actionRoute != null) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "Deep-link: ${entity.actionRoute ?: "general"} ${if (entity.targetId != null) "(ID: ${entity.targetId.take(8)}...)" else ""}",
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                      color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                      onClick = { onDelete(entity.id) },
                      modifier = Modifier.size(24.dp)
                    ) {
                      Icon(Icons.Outlined.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun NotificationChannelsPreferenceView(
  onOpenChannelSettings: (String) -> Unit,
  onOpenAllSettings: () -> Unit
) {
  val channels = listOf(
    NotificationChannelInfo(
      id = SystemNotificationHelper.CHANNEL_EMERGENCY,
      name = SystemNotificationHelper.CHANNEL_EMERGENCY_NAME,
      description = SystemNotificationHelper.CHANNEL_EMERGENCY_DESC,
      importance = "HIGH (Heads-Up Pop-Up)",
      ledColor = "Red LED Light",
      vibration = "Urgent Pulse (400-200-400-200-600ms)",
      icon = Icons.Default.Emergency,
      color = Color(0xFFDC2626)
    ),
    NotificationChannelInfo(
      id = SystemNotificationHelper.CHANNEL_ACADEMIC,
      name = SystemNotificationHelper.CHANNEL_ACADEMIC_NAME,
      description = SystemNotificationHelper.CHANNEL_ACADEMIC_DESC,
      importance = "HIGH (Sound & Banner)",
      ledColor = "Blue LED Light",
      vibration = "Double Pulse (300-150-300ms)",
      icon = Icons.Default.School,
      color = Color(0xFF2563EB)
    ),
    NotificationChannelInfo(
      id = SystemNotificationHelper.CHANNEL_EVENTS,
      name = SystemNotificationHelper.CHANNEL_EVENTS_NAME,
      description = SystemNotificationHelper.CHANNEL_EVENTS_DESC,
      importance = "DEFAULT (Sound & Tray)",
      ledColor = "Green LED Light",
      vibration = "Standard Vibration",
      icon = Icons.Default.Event,
      color = Color(0xFF16A34A)
    ),
    NotificationChannelInfo(
      id = SystemNotificationHelper.CHANNEL_GENERAL,
      name = SystemNotificationHelper.CHANNEL_GENERAL_NAME,
      description = SystemNotificationHelper.CHANNEL_GENERAL_DESC,
      importance = "DEFAULT (Sound & Tray)",
      ledColor = "Cyan LED Light",
      vibration = "Standard Vibration",
      icon = Icons.Default.Campaign,
      color = Color(0xFF0D9488)
    )
  )

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Surface(
      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Tune,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Android System Notification Channels",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Control alerts, sound, vibration, and lockscreen privacy individually per category in Android Settings.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 380.dp)
    ) {
      items(channels) { channel ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(channel.color.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = channel.icon,
                    contentDescription = null,
                    tint = channel.color,
                    modifier = Modifier.size(16.dp)
                  )
                }
                Text(
                  text = channel.name,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              Surface(
                color = channel.color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = channel.importance.take(4),
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                  color = channel.color,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Text(
              text = channel.description,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(
                text = "💡 ${channel.ledColor}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "📳 ${channel.vibration.take(20)}...",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Button(
              onClick = { onOpenChannelSettings(channel.id) },
              colors = ButtonDefaults.buttonColors(containerColor = channel.color),
              shape = RoundedCornerShape(6.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Configure ${channel.name.take(18)}... in System", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
          }
        }
      }
    }

    OutlinedButton(
      onClick = onOpenAllSettings,
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(8.dp)
    ) {
      Icon(Icons.Default.AppSettingsAlt, contentDescription = null, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Open Android App Notification Settings", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
    }
  }
}

data class NotificationChannelInfo(
  val id: String,
  val name: String,
  val description: String,
  val importance: String,
  val ledColor: String,
  val vibration: String,
  val icon: ImageVector,
  val color: Color
)
