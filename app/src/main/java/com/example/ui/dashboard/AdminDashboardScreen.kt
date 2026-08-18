package com.example.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
  profile: AdminProfile,
  classes: List<SchoolClass>,
  notices: List<Notice>,
  onOpenBroadcastNoticeDialog: () -> Unit,
  onNavigateToManagement: () -> Unit,
  onNavigateToNotices: () -> Unit,
  onNoticeClick: (Notice) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("admin_dashboard_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Welcome Greeting Banner
    item {
      WelcomeGreetingBanner(
        user = profile.user,
        subtitle = "${profile.adminRole} • ${profile.officeLocation}"
      )
    }

    // 2. Institution Overall Metrics (4-stat grid)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          StatCard(
            title = "Total Students",
            value = "1,420",
            subtitle = "95.2% avg attendance",
            icon = Icons.Default.School,
            accentColor = SchoolNavyPrimary,
            modifier = Modifier.weight(1f),
            testTag = "admin_stat_students",
            onClick = onNavigateToManagement
          )

          StatCard(
            title = "Faculty Teachers",
            value = "84 Active",
            subtitle = "14 Departments",
            icon = Icons.Default.MenuBook,
            accentColor = SchoolAccentGreen,
            modifier = Modifier.weight(1f),
            testTag = "admin_stat_teachers",
            onClick = onNavigateToManagement
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          StatCard(
            title = "Support Staff",
            value = "32 Members",
            subtitle = "Facilities & Transport",
            icon = Icons.Default.Engineering,
            accentColor = Color(0xFF7C3AED),
            modifier = Modifier.weight(1f),
            testTag = "admin_stat_staff",
            onClick = onNavigateToManagement
          )

          StatCard(
            title = "Total Classes",
            value = "${classes.size * 6} Sections",
            subtitle = "Grades 1 to 12",
            icon = Icons.Default.MeetingRoom,
            accentColor = SchoolGold,
            modifier = Modifier.weight(1f),
            testTag = "admin_stat_classes",
            onClick = onNavigateToManagement
          )
        }
      }
    }

    // 3. Quick Management Actions
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "Institutional Governance",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = SchoolNavyPrimary
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            QuickActionButton(
              title = "Broadcast",
              icon = Icons.Default.Campaign,
              color = Color(0xFFDC2626),
              onClick = onOpenBroadcastNoticeDialog
            )
            QuickActionButton(
              title = "Directory",
              icon = Icons.Default.ContactPhone,
              color = Color(0xFF2563EB),
              onClick = onNavigateToManagement
            )
            QuickActionButton(
              title = "Classes",
              icon = Icons.Default.MeetingRoom,
              color = Color(0xFF059669),
              onClick = onNavigateToManagement
            )
            QuickActionButton(
              title = "Audit Logs",
              icon = Icons.Default.Assessment,
              color = Color(0xFF7C3AED),
              onClick = onNavigateToManagement
            )
          }
        }
      }
    }

    // 4. Live Class Attendance Overview
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
            imageVector = Icons.Default.FactCheck,
            contentDescription = null,
            tint = SchoolAccentGreen
          )
          Text(
            text = "Live Classroom Roster",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }

        TextButton(onClick = onNavigateToManagement) {
          Text("Manage All")
        }
      }
    }

    items(classes.take(3)) { cls ->
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
              text = "Class Teacher: ${cls.classTeacherName} • ${cls.roomNo}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Surface(
            color = SchoolAccentGreen.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = "${cls.averageAttendance}% Present",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = SchoolAccentGreen,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    // 5. Recent Official Circulars
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Published Circulars & Notices",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Button(
          onClick = onOpenBroadcastNoticeDialog,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("New Circular", style = MaterialTheme.typography.labelSmall)
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
