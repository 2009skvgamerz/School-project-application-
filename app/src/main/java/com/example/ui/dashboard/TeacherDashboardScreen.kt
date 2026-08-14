package com.example.ui.dashboard

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
    // 1. Welcome Header Banner
    item {
      WelcomeGreetingBanner(
        user = profile.user,
        subtitle = "${profile.department} • Emp #${profile.employeeId} • ${profile.qualification}"
      )
    }

    // 2. Quick Action Buttons for Teacher Operations
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "Teacher Quick Actions",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = SchoolNavyPrimary
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            QuickActionButton(
              title = "Take Attendance",
              icon = Icons.Default.FactCheck,
              color = Color(0xFF059669),
              onClick = { onOpenMarkAttendance("Class 10-A") }
            )
            QuickActionButton(
              title = "Assign HW",
              icon = Icons.Default.PostAdd,
              color = Color(0xFFD97706),
              onClick = onOpenAssignHomeworkDialog
            )
            QuickActionButton(
              title = "Post Notice",
              icon = Icons.Default.Campaign,
              color = Color(0xFF2563EB),
              onClick = onOpenPostNoticeDialog
            )
            QuickActionButton(
              title = "My Classes",
              icon = Icons.Default.Groups,
              color = Color(0xFF7C3AED),
              onClick = onNavigateToClasses
            )
          }
        }
      }
    }

    // 3. Faculty Teaching Overview Metrics
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        StatCard(
          title = "Today's Periods",
          value = "4 Lectures",
          subtitle = "Next: Physics 10-A",
          icon = Icons.Default.MenuBook,
          accentColor = SchoolNavyPrimary,
          modifier = Modifier.weight(1f),
          testTag = "stat_periods_card"
        )

        StatCard(
          title = "Assigned Classes",
          value = "${profile.assignedClasses.size} Sections",
          subtitle = "124 Students Total",
          icon = Icons.Default.Groups,
          accentColor = SchoolAccentGreen,
          modifier = Modifier.weight(1f),
          testTag = "stat_assigned_classes_card",
          onClick = onNavigateToClasses
        )
      }
    }

    // 4. Today's Teaching Schedule
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
            text = "Today's Teaching Schedule",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }

    items(todaySchedule.take(3)) { entry ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
            shape = RoundedCornerShape(8.dp)
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "Period ${entry.periodNumber}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = SchoolAccentGreen
              )
              Text(
                text = entry.startTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "${entry.subjectName} • ${entry.className}",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Attendance", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }

    // 5. Assigned Classes Roster
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

    items(classesList.take(3)) { cls ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "${cls.totalStudents} Students  •  Room ${cls.roomNo}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(
              onClick = onOpenAssignHomeworkDialog,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Assign HW", style = MaterialTheme.typography.labelSmall)
            }

            Button(
              onClick = { onOpenMarkAttendance("${cls.name}-${cls.section}") },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Attendance", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }

    // 6. Notices
    item {
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
