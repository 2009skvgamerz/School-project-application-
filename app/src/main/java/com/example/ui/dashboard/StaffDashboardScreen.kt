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
fun StaffDashboardScreen(
  profile: StaffProfile,
  duties: List<DutyTask>,
  notices: List<Notice>,
  onUpdateDutyStatus: (String, DutyStatus) -> Unit,
  onOpenAddDutyDialog: () -> Unit,
  onNavigateToDuties: () -> Unit,
  onNavigateToNotices: () -> Unit,
  onNoticeClick: (Notice) -> Unit,
  modifier: Modifier = Modifier
) {
  val completedDutiesCount = duties.count { it.status == DutyStatus.COMPLETED }
  val pendingDutiesCount = duties.count { it.status == DutyStatus.PENDING }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("staff_dashboard_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Welcome Header Banner
    item {
      WelcomeGreetingBanner(
        user = profile.user,
        subtitle = "${profile.department} • Shift: ${profile.shiftTiming} • ${profile.emergencyRole}"
      )
    }

    // 2. Metric Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        StatCard(
          title = "Active Tasks",
          value = "$pendingDutiesCount Pending",
          subtitle = "$completedDutiesCount Completed",
          icon = Icons.Default.TaskAlt,
          accentColor = Color(0xFF7C3AED),
          modifier = Modifier.weight(1f),
          testTag = "stat_duties_card",
          onClick = onNavigateToDuties
        )

        StatCard(
          title = "Shift Status",
          value = "On Duty",
          subtitle = profile.shiftTiming,
          icon = Icons.Default.AccessTime,
          accentColor = SchoolAccentGreen,
          modifier = Modifier.weight(1f),
          testTag = "stat_shift_card"
        )
      }
    }

    // 3. Quick Action Buttons
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "Campus Operations",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            QuickActionButton(
              title = "New Task",
              icon = Icons.Default.AddCircleOutline,
              color = Color(0xFF7C3AED),
              onClick = onOpenAddDutyDialog
            )
            QuickActionButton(
              title = "All Duties",
              icon = Icons.Default.Checklist,
              color = Color(0xFF2563EB),
              onClick = onNavigateToDuties
            )
            QuickActionButton(
              title = "Campus Safety",
              icon = Icons.Default.Shield,
              color = Color(0xFFDC2626),
              onClick = onNavigateToDuties
            )
            QuickActionButton(
              title = "Notices",
              icon = Icons.Default.Campaign,
              color = Color(0xFF059669),
              onClick = onNavigateToNotices
            )
          }
        }
      }
    }

    // 4. Assigned Duties Checklist (Interactive)
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
            imageVector = Icons.Default.Checklist,
            contentDescription = null,
            tint = SchoolNavyPrimary
          )
          Text(
            text = "Today's Operational Duties",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }

        TextButton(onClick = onNavigateToDuties) {
          Text("Manage (${duties.size})")
        }
      }
    }

    items(duties.take(4)) { duty ->
      DutyTaskItemCard(
        duty = duty,
        onStatusChange = { newStatus -> onUpdateDutyStatus(duty.id, newStatus) }
      )
    }

    // 5. School Circulars
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "School Bulletins & Notices",
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

@Composable
fun DutyTaskItemCard(
  duty: DutyTask,
  onStatusChange: (DutyStatus) -> Unit,
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
      IconButton(
        onClick = {
          val nextStatus = when(duty.status) {
            DutyStatus.PENDING -> DutyStatus.IN_PROGRESS
            DutyStatus.IN_PROGRESS -> DutyStatus.COMPLETED
            DutyStatus.COMPLETED -> DutyStatus.PENDING
          }
          onStatusChange(nextStatus)
        }
      ) {
        Icon(
          imageVector = when(duty.status) {
            DutyStatus.COMPLETED -> Icons.Default.CheckCircle
            DutyStatus.IN_PROGRESS -> Icons.Default.Pending
            DutyStatus.PENDING -> Icons.Default.RadioButtonUnchecked
          },
          contentDescription = "Toggle status",
          tint = Color(duty.status.colorHex),
          modifier = Modifier.size(28.dp)
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = duty.title,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            textDecoration = if (duty.status == DutyStatus.COMPLETED) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
          ),
          color = if (duty.status == DutyStatus.COMPLETED) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Location: ${duty.area} • ${duty.scheduledTime}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Surface(
        color = Color(duty.status.colorHex).copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp)
      ) {
        Text(
          text = duty.status.label,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = Color(duty.status.colorHex),
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
      }
    }
  }
}
