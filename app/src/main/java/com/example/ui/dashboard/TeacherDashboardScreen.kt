package com.example.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun TeacherDashboardScreen(
  profile: TeacherProfile,
  todaySchedule: List<TimetableEntry>,
  classesList: List<SchoolClass>,
  notices: List<Notice>,
  onOpenAssignHomeworkDialog: () -> Unit,
  onOpenMarkAttendance: (String) -> Unit,
  onOpenPostNoticeDialog: () -> Unit,
  onNavigateToClasses: () -> Unit,
  onNavigateToNotices: () -> Unit,
  onNoticeClick: (Notice) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("teacher_dashboard_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. User Profile Header
    item {
      ScrollEntranceItem(index = 0) {
        UserProfileHeader(
          user = profile.user,
          subtitle = "${profile.department} • Class Teacher (${profile.classTeacherOf ?: "Class 10-A"}) • Emp #${profile.employeeId}",
          schoolSession = "Academic Session 2026–2027",
          testTag = "teacher_user_profile_header"
        )
      }
    }

    // 2. Quick Action Buttons for Teacher Operations
    item {
      ScrollEntranceItem(index = 1) {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
              text = "Teacher Quick Actions",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              QuickActionButton(
                title = "Daily Roll Call",
                icon = Icons.Default.FactCheck,
                color = GoogleGreen,
                onClick = { onOpenMarkAttendance(profile.classTeacherOf ?: "Class 10-A") }
              )
              QuickActionButton(
                title = "Assign HW",
                icon = Icons.Default.PostAdd,
                color = GoogleYellow,
                onClick = onOpenAssignHomeworkDialog
              )
              QuickActionButton(
                title = "Post Notice",
                icon = Icons.Default.Campaign,
                color = GoogleBlue,
                onClick = onOpenPostNoticeDialog
              )
              QuickActionButton(
                title = "My Classes",
                icon = Icons.Default.Groups,
                color = GooglePurple,
                onClick = onNavigateToClasses
              )
            }
          }
        }
      }
    }

    // 3. Faculty Teaching & Class Quick Stats Card
    item {
      ScrollEntranceItem(index = 2) {
        QuickStatsOverviewCard(
          attendance = AttendanceStatData(
            percentage = 96f,
            presentDays = 119,
            totalDays = 124,
            statusLabel = "Roll Call Today (96%)",
            onClick = { onOpenMarkAttendance(profile.classTeacherOf ?: "Class 10-A") }
          ),
          assignments = AssignmentStatData(
            pendingCount = 4,
            dueTodayCount = 2,
            nextTitle = "Physics Assignment #3 Grading",
            onClick = onOpenAssignHomeworkDialog
          ),
          announcements = AnnouncementStatData(
            totalCount = notices.size.coerceAtLeast(4),
            unreadCount = notices.count { it.isUrgent },
            latestTitle = notices.firstOrNull()?.title ?: "Faculty Meeting at 3:30 PM",
            onClick = onNavigateToNotices
          ),
          testTag = "teacher_quick_stats_card"
        )
      }
    }

    // 4. Today's Teaching Schedule
    item {
      ScrollEntranceItem(index = 3) {
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
              text = "Today's Teaching Schedule",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }

    items(todaySchedule.take(3)) { entry ->
      ScrollEntranceItem(index = 4) {
        Card(
          modifier = Modifier.fillMaxWidth(),
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Surface(
              color = SchoolAccentGreen.copy(alpha = 0.12f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "P${entry.periodNumber}",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                  color = SchoolAccentGreen
                )
                Text(
                  text = entry.startTime,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${entry.subjectName} • ${entry.className}",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Location: ${entry.roomNo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Button(
              onClick = { onOpenMarkAttendance(entry.className) },
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Attendance", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
          }
        }
      }
    }

    // 5. Assigned Classes Roster
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
              imageVector = Icons.Default.Groups,
              contentDescription = null,
              tint = SchoolGold
            )
            Text(
              text = "Assigned Classes",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          TextButton(onClick = onNavigateToClasses) {
            Text("Manage All")
          }
        }
      }
    }

    items(classesList.take(3)) { cls ->
      ScrollEntranceItem(index = 6) {
        Card(
          modifier = Modifier.fillMaxWidth(),
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
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "${cls.name}-${cls.section}",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              )
              Text(
                text = "${cls.totalStudents} Students • Room ${cls.roomNo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedButton(
                onClick = onOpenAssignHomeworkDialog,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Assign HW", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
              }

              Button(
                onClick = { onOpenMarkAttendance("${cls.name}-${cls.section}") },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Attendance", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }
            }
          }
        }
      }
    }

    // 6. Notices
    item {
      ScrollEntranceItem(index = 7) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Faculty Circulars & Notices",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          TextButton(onClick = onNavigateToNotices) {
            Text("View All")
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
