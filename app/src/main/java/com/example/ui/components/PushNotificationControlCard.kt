package com.example.ui.components

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NotificationType
import com.example.ui.theme.SchoolAccentGreen
import com.example.ui.theme.SchoolNavyPrimary
import com.example.util.SystemNotificationHelper
import com.example.viewmodel.SchoolViewModel

/**
 * Comprehensive Push Notification Control Card & Firebase Cloud Messaging (FCM) Manager.
 *
 * Allows users and administrators to:
 * 1. Inspect and request Android 13+ POST_NOTIFICATIONS runtime permission.
 * 2. View and copy the live FCM registration token.
 * 3. Toggle FCM topic subscriptions (All School, Homework, Bus Proximity, Events).
 * 4. Trigger real-time device push notifications to test status bar banners & deep-linking.
 */
@Composable
fun PushNotificationControlCard(
  viewModel: SchoolViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val fcmToken by viewModel.fcmToken.collectAsState()

  var hasPermission by remember {
    mutableStateOf(SystemNotificationHelper.hasNotificationPermission(context))
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasPermission = isGranted
    if (isGranted) {
      Toast.makeText(context, "✅ Notification permission enabled! Real-time alerts active.", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "⚠️ Notification permission denied. Alerts will not appear in status bar.", Toast.LENGTH_LONG).show()
    }
  }

  // Active FCM Topics
  var subscribedTopics by remember {
    mutableStateOf(viewModel.getSubscribedFcmTopics().toMutableSet())
  }

  var isTokenVisible by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("push_notification_control_card")
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Header with Icon & Badges
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2563EB).copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = null,
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(26.dp)
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "Push Notifications (FCM)",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (hasPermission) SchoolAccentGreen.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
            ) {
              Text(
                text = if (hasPermission) "ACTIVE" else "PERMISSION NEEDED",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                color = if (hasPermission) SchoolAccentGreen else Color(0xFFEF4444),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = "Real-time alerts for urgent circulars, bus arrivals & homework.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // 2. Permission Banner (if not granted)
      if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFEF2F2),
          border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "System Permission Required",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF991B1B)
              )
              Text(
                text = "Allow notifications to receive heads-up alerts on your device lockscreen.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB91C1C)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.testTag("grant_notification_perm_btn")
            ) {
              Text("Grant", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
          }
        }
      }

      // 3. Live FCM Token & Cloud Registration
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.CloudQueue, contentDescription = null, tint = SchoolNavyPrimary, modifier = Modifier.size(16.dp))
              Text(
                text = "FCM Device Registration",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              IconButton(
                onClick = { viewModel.refreshFcmToken() },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh token", modifier = Modifier.size(16.dp))
              }
              IconButton(
                onClick = {
                  val tokenToCopy = fcmToken ?: "stjosephs_dev_fcm_token_${System.currentTimeMillis()}"
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("FCM Registration Token", tokenToCopy)
                  clipboard.setPrimaryClip(clip)
                  Toast.makeText(context, "📋 FCM Token copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(28.dp).testTag("copy_fcm_token_btn")
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy token", modifier = Modifier.size(16.dp))
              }
            }
          }

          val displayToken = fcmToken ?: "Connected (Token registered in Firestore)"
          Text(
            text = if (isTokenVisible || fcmToken == null) displayToken else "${displayToken.take(18)}••••••••••••${displayToken.takeLast(8)}",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (isTokenVisible) 3 else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { isTokenVisible = !isTokenVisible }
          )
        }
      }

      // 4. Notification Topics
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "SUBSCRIBED TOPICS & CHANNELS",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
          color = MaterialTheme.colorScheme.primary
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            "all_school" to "📢 All School",
            "homework" to "📝 Homework",
            "bus_tracking" to "🚍 Bus Live",
            "events" to "📅 Calendar"
          ).forEach { (topic, label) ->
            val isSubscribed = subscribedTopics.contains(topic)
            FilterChip(
              selected = isSubscribed,
              onClick = {
                val updated = subscribedTopics.toMutableSet()
                if (isSubscribed) {
                  updated.remove(topic)
                  viewModel.unsubscribeFromFcmTopic(topic)
                  Toast.makeText(context, "Unsubscribed from #$topic", Toast.LENGTH_SHORT).show()
                } else {
                  updated.add(topic)
                  viewModel.subscribeToFcmTopic(topic)
                  Toast.makeText(context, "Subscribed to #$topic", Toast.LENGTH_SHORT).show()
                }
                subscribedTopics = updated
              },
              label = {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                )
              },
              modifier = Modifier.testTag("chip_topic_$topic")
            )
          }
        }
      }

      // 5. Real-Time Push Notification Dispatchers (Immediate Live Testing)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "⚡ TEST LIVE REAL-TIME PUSH NOTIFICATIONS",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = "Tap any trigger below to immediately dispatch a real heads-up notification banner to your device status bar outside the app:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // 1. Urgent Circular Alert
          Button(
            onClick = {
              viewModel.triggerTestPushNotification(
                type = NotificationType.NOTICE,
                title = "🚨 URGENT: Campus Closure & Heavy Rain Alert",
                message = "District Administration declared holiday tomorrow due to adverse monsoon weather.",
                targetRoute = "notices",
                isUrgent = true
              )
              Toast.makeText(context, "🚨 Urgent Notice push dispatched to status bar!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f).testTag("test_push_urgent_notice")
          ) {
            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("🚨 Urgent Notice", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }

          // 2. Homework Alert
          Button(
            onClick = {
              viewModel.triggerTestPushNotification(
                type = NotificationType.HOMEWORK,
                title = "📚 New Assignment: Physics Electromagnetism",
                message = "Grade 12-A • Complete problems 1-15 • Due: Tomorrow 5:00 PM",
                targetRoute = "homework",
                isUrgent = false
              )
              Toast.makeText(context, "📚 Homework push dispatched to status bar!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f).testTag("test_push_homework")
          ) {
            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("📚 Homework", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // 3. Bus Proximity Alert
          Button(
            onClick = {
              viewModel.triggerTestPushNotification(
                type = NotificationType.BUS,
                title = "🚍 Bus #04 Approaching Sector 7 Gate",
                message = "School Bus is 2 stops away. Estimated arrival in 4 minutes.",
                targetRoute = "bus_tracking",
                isUrgent = false
              )
              Toast.makeText(context, "🚍 Bus proximity push dispatched to status bar!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f).testTag("test_push_bus")
          ) {
            Icon(Icons.Default.DirectionsBus, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("🚍 Bus Live Alert", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }

          // 4. Headmaster Broadcast
          Button(
            onClick = {
              viewModel.triggerTestPushNotification(
                type = NotificationType.ANNOUNCEMENT,
                title = "📢 Annual Sports Meet Registration Open",
                message = "Sign-ups for track and field events close this Friday at 4:00 PM.",
                targetRoute = "announcements",
                isUrgent = false
              )
              Toast.makeText(context, "📢 Broadcast push dispatched to status bar!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f).testTag("test_push_announcement")
          ) {
            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("📢 Broadcast", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }
        }
      }

      // 6. Android System Channel Settings
      OutlinedButton(
        onClick = {
          SystemNotificationHelper.openNotificationSettings(context)
        },
        modifier = Modifier.fillMaxWidth().testTag("open_system_channel_settings_btn"),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Customize OS Notification Channels & Sound", style = MaterialTheme.typography.labelMedium)
      }
    }
  }
}
