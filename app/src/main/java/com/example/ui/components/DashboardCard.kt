package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Data classes holding stats for Attendance, Assignments, and Announcements.
 */
data class AttendanceStatData(
  val percentage: Float = 94f,
  val presentDays: Int = 42,
  val totalDays: Int = 45,
  val statusLabel: String = "Excellent (>90%)",
  val onClick: (() -> Unit)? = null
)

data class AssignmentStatData(
  val pendingCount: Int = 3,
  val dueTodayCount: Int = 1,
  val completedCount: Int = 12,
  val nextTitle: String? = "Physics Ch. 4 Numericals",
  val onClick: (() -> Unit)? = null
)

data class AnnouncementStatData(
  val totalCount: Int = 5,
  val unreadCount: Int = 2,
  val latestTitle: String? = "Annual Sports Day Registration Open",
  val latestCategory: String? = "Urgent",
  val onClick: (() -> Unit)? = null
)

/**
 * Base Material3 Dashboard Card shell component with customizable container, header action, and elevation.
 */
@Composable
fun DashboardCard(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  icon: ImageVector? = null,
  accentColor: Color = MaterialTheme.colorScheme.primary,
  badgeText: String? = null,
  badgeColor: Color = accentColor,
  actionText: String? = "View All",
  onActionClick: (() -> Unit)? = null,
  testTag: String = "dashboard_card",
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 1.dp,
      pressedElevation = 4.dp
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Header Section
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          // Subtle vertical accent bar for premium structural alignment
          Box(
            modifier = Modifier
              .width(4.dp)
              .height(20.dp)
              .clip(CircleShape)
              .background(accentColor)
          )

          if (icon != null) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentColor.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )

              if (badgeText != null) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = badgeColor.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }

            if (subtitle != null) {
              Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        if (actionText != null && onActionClick != null) {
          TextButton(
            onClick = onActionClick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.testTag("${testTag}_action_btn")
          ) {
            Text(
              text = actionText,
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = accentColor
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }

      // Content Body Slot
      content()
    }
  }
}

/**
 * Reusable M3 Quick Stats Overview Card that combines Attendance, Assignments,
 * and Announcements into a unified 3-column / 3-card quick stats dashboard section.
 */
@Composable
fun QuickStatsOverviewCard(
  attendance: AttendanceStatData,
  assignments: AssignmentStatData,
  announcements: AnnouncementStatData,
  modifier: Modifier = Modifier,
  testTag: String = "quick_stats_overview_card"
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Header Label
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
              .size(28.dp)
              .clip(CircleShape)
              .background(SchoolNavyPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Insights,
              contentDescription = "Overview Stats",
              tint = SchoolNavyPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
          Text(
            text = "Quick Academic Stats",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ) {
          Text(
            text = "Live Sync",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      // 3 Stat Tiles Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Attendance Stat Tile
        QuickStatTile(
          title = "Attendance",
          value = "${attendance.percentage.toInt()}%",
          subtitle = attendance.statusLabel,
          icon = Icons.Default.CheckCircle,
          color = SchoolAccentGreen,
          modifier = Modifier.weight(1f),
          testTag = "quick_stat_attendance",
          onClick = attendance.onClick
        )

        // Assignments Stat Tile
        QuickStatTile(
          title = "Assignments",
          value = "${assignments.pendingCount}",
          subtitle = if (assignments.dueTodayCount > 0) "${assignments.dueTodayCount} due today" else "Pending tasks",
          icon = Icons.Default.Assignment,
          color = if (assignments.pendingCount > 0) SchoolGold else SchoolAccentGreen,
          badgeText = if (assignments.dueTodayCount > 0) "Today" else null,
          modifier = Modifier.weight(1f),
          testTag = "quick_stat_assignments",
          onClick = assignments.onClick
        )

        // Announcements Stat Tile
        QuickStatTile(
          title = "Announcements",
          value = "${announcements.totalCount}",
          subtitle = if (announcements.unreadCount > 0) "${announcements.unreadCount} unread" else "Latest updates",
          icon = Icons.Default.Campaign,
          color = SchoolBurgundy,
          badgeText = if (announcements.unreadCount > 0) "NEW" else null,
          modifier = Modifier.weight(1f),
          testTag = "quick_stat_announcements",
          onClick = announcements.onClick
        )
      }
    }
  }
}

/**
 * Individual Tile used inside QuickStatsOverviewCard
 */
@Composable
private fun QuickStatTile(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier,
  badgeText: String? = null,
  testTag: String = "",
  onClick: (() -> Unit)? = null
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = color.copy(alpha = 0.08f),
    border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    modifier = modifier
      .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
  ) {
    Column(
      modifier = Modifier
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(16.dp)
          )
        }

        if (badgeText != null) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = color
          ) {
            Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp
              ),
              color = Color.White,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
          }
        }
      }

      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          fontSize = 18.sp
        ),
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

/**
 * Dedicated Attendance Dashboard Card Component
 */
@Composable
fun AttendanceDashboardCard(
  attendanceData: AttendanceStatData,
  modifier: Modifier = Modifier,
  testTag: String = "attendance_dashboard_card"
) {
  val progress = (attendanceData.percentage / 100f).coerceIn(0f, 1f)

  DashboardCard(
    title = "Attendance Summary",
    subtitle = "${attendanceData.presentDays} of ${attendanceData.totalDays} Days Attended",
    icon = Icons.Default.CheckCircle,
    accentColor = SchoolAccentGreen,
    badgeText = "${attendanceData.percentage.toInt()}%",
    badgeColor = SchoolAccentGreen,
    actionText = "Details",
    onActionClick = attendanceData.onClick,
    modifier = modifier,
    testTag = testTag
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Overall Attendance",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = attendanceData.statusLabel,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = SchoolAccentGreen
        )
      }

      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(CircleShape),
        color = SchoolAccentGreen,
        trackColor = SchoolAccentGreen.copy(alpha = 0.15f)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        MiniStatPill(
          label = "Present",
          value = "${attendanceData.presentDays}d",
          icon = Icons.Default.Check,
          color = SchoolAccentGreen,
          modifier = Modifier.weight(1f)
        )
        MiniStatPill(
          label = "Absent",
          value = "${attendanceData.totalDays - attendanceData.presentDays}d",
          icon = Icons.Default.Close,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

/**
 * Dedicated Assignments Dashboard Card Component
 */
@Composable
fun AssignmentsDashboardCard(
  assignmentData: AssignmentStatData,
  modifier: Modifier = Modifier,
  testTag: String = "assignments_dashboard_card"
) {
  val isPending = assignmentData.pendingCount > 0

  DashboardCard(
    title = "Homework & Assignments",
    subtitle = if (isPending) "${assignmentData.pendingCount} Tasks Pending" else "All caught up!",
    icon = Icons.Default.Assignment,
    accentColor = if (isPending) SchoolGold else SchoolAccentGreen,
    badgeText = if (assignmentData.dueTodayCount > 0) "${assignmentData.dueTodayCount} Due Today" else null,
    badgeColor = MaterialTheme.colorScheme.error,
    actionText = "Open HW",
    onActionClick = assignmentData.onClick,
    modifier = modifier,
    testTag = testTag
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      if (assignmentData.nextTitle != null) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.EditNote,
              contentDescription = null,
              tint = SchoolNavyPrimary,
              modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Next Upcoming Task",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = assignmentData.nextTitle,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        MiniStatPill(
          label = "Pending",
          value = "${assignmentData.pendingCount}",
          icon = Icons.Default.Schedule,
          color = SchoolGold,
          modifier = Modifier.weight(1f)
        )
        MiniStatPill(
          label = "Completed",
          value = "${assignmentData.completedCount}",
          icon = Icons.Default.DoneAll,
          color = SchoolAccentGreen,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

/**
 * Dedicated Announcements Dashboard Card Component
 */
@Composable
fun AnnouncementsDashboardCard(
  announcementData: AnnouncementStatData,
  modifier: Modifier = Modifier,
  testTag: String = "announcements_dashboard_card"
) {
  DashboardCard(
    title = "Announcements",
    subtitle = "${announcementData.totalCount} School Notices",
    icon = Icons.Default.Campaign,
    accentColor = SchoolBurgundy,
    badgeText = if (announcementData.unreadCount > 0) "${announcementData.unreadCount} New" else null,
    badgeColor = SchoolBurgundy,
    actionText = "Notice Board",
    onActionClick = announcementData.onClick,
    modifier = modifier,
    testTag = testTag
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      if (announcementData.latestTitle != null) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = SchoolBurgundy.copy(alpha = 0.08f),
          border = BorderStroke(1.dp, SchoolBurgundy.copy(alpha = 0.2f)),
          modifier = Modifier
            .fillMaxWidth()
            .then(if (announcementData.onClick != null) Modifier.clickable { announcementData.onClick.invoke() } else Modifier)
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = SchoolBurgundy,
              modifier = Modifier.size(18.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
              if (announcementData.latestCategory != null) {
                Text(
                  text = announcementData.latestCategory.uppercase(),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                  ),
                  color = SchoolBurgundy
                )
              }
              Text(
                text = announcementData.latestTitle,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }
  }
}
