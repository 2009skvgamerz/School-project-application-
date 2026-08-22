package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.SchoolAccentGreen
import com.example.ui.theme.SchoolGoldLight
import com.example.ui.theme.SchoolNavyPrimary
import com.example.util.NetworkState
import kotlinx.coroutines.delay

@Composable
fun NetworkStatusBanner(
  networkState: NetworkState,
  onRetryConnection: () -> Unit = {},
  onToggleSimulatedOffline: ((Boolean) -> Unit)? = null,
  isSimulated: Boolean = false,
  modifier: Modifier = Modifier
) {
  var showDetailsDialog by remember { mutableStateOf(false) }
  var showRestoredBanner by remember { mutableStateOf(false) }
  var previousStateWasOffline by remember { mutableStateOf(false) }

  // Monitor transitions from offline to online to show brief "Back Online" banner
  LaunchedEffect(networkState) {
    if (networkState is NetworkState.Online) {
      if (previousStateWasOffline) {
        showRestoredBanner = true
        delay(3500)
        showRestoredBanner = false
      }
      previousStateWasOffline = false
    } else if (networkState is NetworkState.Offline) {
      previousStateWasOffline = true
      showRestoredBanner = false
    }
  }

  Column(modifier = modifier.fillMaxWidth().testTag("network_status_banner_container")) {
    // 1. OFFLINE BANNER (Slide in when offline)
    AnimatedVisibility(
      visible = networkState is NetworkState.Offline,
      enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
      exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    ) {
      val offlineReason = (networkState as? NetworkState.Offline)?.reason ?: "No internet connection"

      Surface(
        color = Color(0xFFB91C1C), // Deep Crimson/Amber
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Offline Mode",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }

            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "You are currently offline",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                  color = Color.White
                )
                if (isSimulated) {
                  Surface(
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = "SIMULATED",
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold),
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
              }

              Text(
                text = "Showing cached local records (Room DB). Refresh is paused.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.9f)
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            TextButton(
              onClick = { showDetailsDialog = true },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.testTag("offline_info_btn")
            ) {
              Text(
                text = "Why?",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFFEF08A) // Soft gold
              )
            }

            IconButton(
              onClick = onRetryConnection,
              modifier = Modifier.size(32.dp).testTag("offline_retry_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Retry Connection",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }

    // 2. BACK ONLINE RESTORED BANNER (Briefly shown after coming back online)
    AnimatedVisibility(
      visible = showRestoredBanner,
      enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
      exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
    ) {
      Surface(
        color = Color(0xFF047857), // Forest Emerald Green
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CloudDone,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "Back Online • Live sync & refresh restored",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
          )
        }
      }
    }
  }

  // Offline Details & Explanation Dialog
  if (showDetailsDialog) {
    OfflineInfoDialog(
      onDismiss = { showDetailsDialog = false },
      networkState = networkState,
      onRetry = onRetryConnection,
      isSimulated = isSimulated,
      onToggleSimulatedOffline = onToggleSimulatedOffline
    )
  }
}

@Composable
fun OfflineInfoDialog(
  onDismiss: () -> Unit,
  networkState: NetworkState,
  onRetry: () -> Unit,
  isSimulated: Boolean = false,
  onToggleSimulatedOffline: ((Boolean) -> Unit)? = null
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("offline_info_dialog")
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Header
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(Color(0xFFDC2626).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.SignalWifiOff,
              contentDescription = null,
              tint = Color(0xFFDC2626),
              modifier = Modifier.size(24.dp)
            )
          }

          Column {
            Text(
              text = "Offline Mode Status",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = SchoolNavyPrimary
            )
            Text(
              text = "Why data is not refreshing live",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Explanation bullets
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OfflineFeatureRow(
            icon = Icons.Default.Storage,
            title = "Room Database Active",
            desc = "Your attendance rolls, homework, and bulletins are stored in SQLite and remain 100% accessible offline."
          )

          OfflineFeatureRow(
            icon = Icons.Default.SyncProblem,
            title = "Network Refresh Paused",
            desc = "Pull-to-refresh will display your local cached records instead of making failing cloud API calls."
          )

          OfflineFeatureRow(
            icon = Icons.Default.CheckCircle,
            title = "Automatic Resync",
            desc = "As soon as Wi-Fi or cellular network returns, the app will automatically resume live data synchronization."
          )
        }

        if (onToggleSimulatedOffline != null) {
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Simulate Offline Mode",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Test offline banners & caching",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Switch(
                checked = isSimulated,
                onCheckedChange = { onToggleSimulatedOffline(it) },
                modifier = Modifier.testTag("offline_dialog_simulation_switch")
              )
            }
          }
        }

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Close")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              onRetry()
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
          ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Check Connection")
          }
        }
      }
    }
  }
}

@Composable
private fun OfflineFeatureRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  desc: String
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.Top
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = SchoolNavyPrimary,
      modifier = Modifier.size(20.dp).padding(top = 2.dp)
    )
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun NetworkStatusBarBadge(
  networkState: NetworkState,
  modifier: Modifier = Modifier
) {
  val isOnline = networkState is NetworkState.Online
  val color = if (isOnline) SchoolAccentGreen else Color(0xFFDC2626)

  Surface(
    color = color.copy(alpha = 0.15f),
    shape = RoundedCornerShape(6.dp),
    modifier = modifier
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(color)
      )
      Text(
        text = if (isOnline) "ONLINE" else "OFFLINE",
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 8.5.sp,
          fontWeight = FontWeight.Bold,
          color = color,
          letterSpacing = 0.3.sp
        ),
        maxLines = 1,
        softWrap = false
      )
    }
  }
}
