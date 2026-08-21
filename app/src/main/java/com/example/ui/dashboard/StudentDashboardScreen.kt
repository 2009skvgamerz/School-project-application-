package com.example.ui.dashboard

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
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
  profile: StudentProfile,
  pendingHomeworkCount: Int,
  todayTimetable: List<TimetableEntry>,
  notices: List<Notice>,
  events: List<SchoolEvent>,
  onNavigateToTimetable: () -> Unit,
  onNavigateToHomework: () -> Unit,
  onNavigateToAttendance: () -> Unit,
  onNavigateToNotices: () -> Unit,
  onNoticeClick: (Notice) -> Unit,
  onOpenNotificationCenter: () -> Unit = {},
  onTriggerPopUpAlert: () -> Unit = {},
  networkState: com.example.util.NetworkState? = null,
  onRetryConnection: (() -> Unit)? = null,
  isRefreshing: Boolean = false,
  onRefresh: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    modifier = modifier.fillMaxSize().testTag("student_dashboard_pull_refresh")
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("student_dashboard_screen"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Welcome Greeting Banner
      item {
        WelcomeGreetingBanner(
          user = profile.user,
          subtitle = "Class ${profile.grade}-${profile.section} • Roll #${profile.rollNo} • ${profile.houseName}"
        )
      }

    // 2. Key Academic Metric Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        StatCard(
          title = "Attendance",
          value = "${profile.attendancePercentage}%",
          subtitle = "Excellent (>90%)",
          icon = Icons.Default.CheckCircle,
          accentColor = SchoolAccentGreen,
          modifier = Modifier.weight(1f),
          testTag = "stat_attendance_card",
          onClick = onNavigateToAttendance
        )

        StatCard(
          title = "Pending HW",
          value = "$pendingHomeworkCount Tasks",
          subtitle = "Due this week",
          icon = Icons.Default.Assignment,
          accentColor = if (pendingHomeworkCount > 0) SchoolGold else SchoolAccentGreen,
          modifier = Modifier.weight(1f),
          testTag = "stat_homework_card",
          onClick = onNavigateToHomework
        )
      }
    }

    // 3. Quick Action Shortcuts
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Quick Academics",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            QuickActionButton(
              title = "Timetable",
              icon = Icons.Default.CalendarMonth,
              color = Color(0xFF2563EB),
              onClick = onNavigateToTimetable
            )
            QuickActionButton(
              title = "Homework",
              icon = Icons.Default.Assignment,
              color = Color(0xFFD97706),
              onClick = onNavigateToHomework
            )
            QuickActionButton(
              title = "Attendance",
              icon = Icons.Default.FactCheck,
              color = Color(0xFF059669),
              onClick = onNavigateToAttendance
            )
            QuickActionButton(
              title = "Circulars",
              icon = Icons.Default.Campaign,
              color = Color(0xFF7C3AED),
              onClick = onNavigateToNotices
            )
          }
        }
      }
    }

    // 3.4 Offline Mode Notice Banner (Appears when offline)
    if (networkState is com.example.util.NetworkState.Offline) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFF87171))
          ),
          modifier = Modifier.fillMaxWidth().testTag("dashboard_offline_banner_card")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFDC2626).copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(22.dp)
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Offline Mode Active",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF991B1B)
              )
              Text(
                text = "Your timetable, attendance & homework are loaded from local Room Database.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7F1D1D)
              )
            }

            if (onRetryConnection != null) {
              IconButton(
                onClick = onRetryConnection,
                modifier = Modifier.size(32.dp).testTag("dashboard_retry_network_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Retry",
                  tint = Color(0xFFDC2626)
                )
              }
            }
          }
        }
      }
    }

    // 4. Today's Timetable Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = SchoolNavyPrimary
          )
          Text(
            text = "Today's Schedule (Monday)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }

        TextButton(onClick = onNavigateToTimetable) {
          Text("Full Week")
        }
      }
    }

    if (todayTimetable.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "No classes scheduled for today. Enjoy your day!",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      items(todayTimetable.take(3)) { entry ->
        TimetableRowCard(entry = entry)
      }
    }

    // 5. Upcoming School Events
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Celebration,
            contentDescription = null,
            tint = SchoolGold
          )
          Text(
            text = "Upcoming School Events",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }

    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(events) { ev ->
          Card(
            modifier = Modifier.width(260.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Surface(
                color = SchoolGold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = ev.date,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = SchoolGoldDark,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }

              Text(
                text = ev.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
              )

              Text(
                text = ev.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
              )

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Place,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = ev.location,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // 6. Latest Notices Preview
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = SchoolAccentBlue
          )
          Text(
            text = "Latest Notices",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }

        TextButton(onClick = onNavigateToNotices) {
          Text("View All (${notices.size})")
        }
      }
    }

    items(notices.take(2)) { notice ->
      NoticeCard(
        notice = notice,
        onNoticeClick = onNoticeClick
      )
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
  }
}

@Composable
fun QuickActionButton(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp),
    modifier = Modifier
      .clickable { onClick() }
      .padding(4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(color.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = color,
        modifier = Modifier.size(24.dp)
      )
    }
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun TimetableRowCard(
  entry: TimetableEntry,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp)
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "P${entry.periodNumber}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = entry.startTime.substringBefore(" "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = entry.subjectName,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Faculty: ${entry.teacherName}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(6.dp)
      ) {
        Text(
          text = entry.roomNo,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }
  }
}
