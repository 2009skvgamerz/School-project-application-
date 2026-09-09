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
  onNavigateToCalendar: () -> Unit = {},
  onNavigateToBusTracking: () -> Unit = {},
  onNavigateToAnnouncements: () -> Unit = {},
  onNavigateToDirectory: () -> Unit = {},
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
      // 1. User Profile Header
      item {
        ScrollEntranceItem(index = 0) {
          UserProfileHeader(
            user = profile.user,
            subtitle = "Class ${profile.grade}-${profile.section} • Roll #${profile.rollNo} • ${profile.houseName}",
            schoolSession = "Academic Session 2026–2027",
            testTag = "student_user_profile_header"
          )
        }
      }

    // 2. Key Academic Quick Stats Card (Attendance, Assignments, Announcements)
    item {
      ScrollEntranceItem(index = 1) {
        QuickStatsOverviewCard(
          attendance = AttendanceStatData(
            percentage = profile.attendancePercentage.toFloat(),
            statusLabel = if (profile.attendancePercentage >= 90) "Excellent (>90%)" else "Regular",
            onClick = onNavigateToAttendance
          ),
          assignments = AssignmentStatData(
            pendingCount = pendingHomeworkCount,
            dueTodayCount = if (pendingHomeworkCount > 0) 1 else 0,
            onClick = onNavigateToHomework
          ),
          announcements = AnnouncementStatData(
            totalCount = notices.size.coerceAtLeast(3),
            unreadCount = notices.count { it.isUrgent },
            latestTitle = notices.firstOrNull()?.title ?: "Annual School Meet Scheduled",
            onClick = onNavigateToNotices
          ),
          testTag = "student_quick_stats_card"
        )
      }
    }

    // 2.1 Academic Performance Trends & Attendance Metrics (D3 / Recharts Integration)
    item {
      ScrollEntranceItem(index = 2) {
        StudentAnalyticsVisualizer(
          title = "Academic & Attendance Analytics",
          subtitle = "${profile.user.fullName} • Class ${profile.grade}-${profile.section} (Roll #${profile.rollNo})"
        )
      }
    }

    // 3. Quick Action Shortcuts (Google Workspace Style)
    item {
      ScrollEntranceItem(index = 3) {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
              text = "Academic Shortcuts",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              ),
              color = MaterialTheme.colorScheme.onSurface
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              QuickActionButton(
                title = "Timetable",
                icon = Icons.Default.CalendarMonth,
                color = GoogleBlue,
                onClick = onNavigateToTimetable
              )
              QuickActionButton(
                title = "Classroom",
                icon = Icons.Default.Assignment,
                color = GoogleGreen,
                onClick = onNavigateToHomework
              )
              QuickActionButton(
                title = "Attendance",
                icon = Icons.Default.FactCheck,
                color = GoogleYellow,
                onClick = onNavigateToAttendance
              )
              QuickActionButton(
                title = "Circulars",
                icon = Icons.Default.Campaign,
                color = GoogleRed,
                onClick = onNavigateToNotices
              )
            }
          }
        }
      }
    }

    // 3.1 Campus ERP Services (Wave 1 Features)
    item {
      ScrollEntranceItem(index = 3) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth().testTag("student_erp_services_card")
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Campus ERP Services",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
              Surface(
                color = SchoolGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "WAVE 1 ACTIVE",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp
                  ),
                  color = SchoolNavyDark,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              QuickActionButton(
                title = "Calendar",
                icon = Icons.Default.EventNote,
                color = Color(0xFF0284C7),
                onClick = onNavigateToCalendar
              )
              QuickActionButton(
                title = "Bus GPS",
                icon = Icons.Default.DirectionsBus,
                color = Color(0xFFEA580C),
                onClick = onNavigateToBusTracking
              )
              QuickActionButton(
                title = "Broadcasts",
                icon = Icons.Default.Campaign,
                color = Color(0xFFDC2626),
                onClick = onNavigateToAnnouncements
              )
              QuickActionButton(
                title = "Directory",
                icon = Icons.Default.ContactPhone,
                color = Color(0xFF059669),
                onClick = onNavigateToDirectory
              )
            }
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
            tint = MaterialTheme.colorScheme.primary
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
        ScrollEntranceItem(index = 4) {
          TimetableRowCard(entry = entry)
        }
      }
    }

    // 5. Upcoming School Events
    item {
      ScrollEntranceItem(index = 5) {
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
    }

    item {
      ScrollEntranceItem(index = 6) {
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
    }

    // 6. Latest Notices Preview
    item {
      ScrollEntranceItem(index = 7) {
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
    }

    items(notices.take(2)) { notice ->
      ScrollEntranceItem(index = 8) {
        NoticeCard(
          notice = notice,
          onNoticeClick = onNoticeClick
        )
      }
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
        .size(52.dp)
        .clip(RoundedCornerShape(16.dp))
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
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
      ),
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
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "P${entry.periodNumber}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = entry.startTime.substringBefore(" "),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = entry.subjectName,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          ),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Faculty: ${entry.teacherName}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text(
          text = entry.roomNo,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }
  }
}
