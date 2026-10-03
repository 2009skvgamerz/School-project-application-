package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavigationTab
import com.example.model.User
import com.example.model.UserRole

data class QuickActionItem(
  val id: String,
  val label: String,
  val icon: ImageVector,
  val containerColor: Color,
  val contentColor: Color = Color.White,
  val onClick: () -> Unit
)

/**
 * QuickActionSpeedDialFab
 *
 * A modern, role-aware Floating Action Button with an animated expandable speed dial.
 * Provides instant 1-tap shortcuts to the most critical actions based on the current user role.
 */
@Composable
fun QuickActionSpeedDialFab(
  currentUser: User,
  roleColor: Color,
  onNavigateToTab: (NavigationTab) -> Unit,
  onOpenIdCard: () -> Unit,
  onOpenCreateNotice: () -> Unit,
  onOpenAssignHomework: () -> Unit,
  onOpenDeveloperTerminal: () -> Unit,
  onOpenRoleSwitcher: () -> Unit,
  onTriggerEmergencySos: () -> Unit,
  onTriggerGpsPing: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }
  val haptic = LocalHapticFeedback.current

  // Rotation animation for the main FAB icon (+ to x)
  val rotationAngle by animateFloatAsState(
    targetValue = if (isExpanded) 135f else 0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "fab_rotation"
  )

  // Contextual actions tailored specifically to the active role
  val actions = remember(currentUser.role) {
    when (currentUser.role) {
      UserRole.STUDENT -> listOf(
        QuickActionItem(
          id = "student_id",
          label = "Smart Digital ID",
          icon = Icons.Default.Badge,
          containerColor = Color(0xFF2563EB),
          onClick = { onOpenIdCard() }
        ),
        QuickActionItem(
          id = "student_bus",
          label = "Live Bus Radar",
          icon = Icons.Default.DirectionsBus,
          containerColor = Color(0xFFD97706),
          onClick = { onNavigateToTab(NavigationTab.BUS_TRACKING) }
        ),
        QuickActionItem(
          id = "student_hw",
          label = "Homework Pipeline",
          icon = Icons.Default.Assignment,
          containerColor = Color(0xFF0D9488),
          onClick = { onNavigateToTab(NavigationTab.HOMEWORK) }
        ),
        QuickActionItem(
          id = "student_schedule",
          label = "Class Timetable",
          icon = Icons.Default.CalendarMonth,
          containerColor = Color(0xFF7C3AED),
          onClick = { onNavigateToTab(NavigationTab.TIMETABLE) }
        )
      )

      UserRole.TEACHER -> listOf(
        QuickActionItem(
          id = "teacher_attendance",
          label = "Take Roll Call",
          icon = Icons.Default.FactCheck,
          containerColor = Color(0xFF059669),
          onClick = { onNavigateToTab(NavigationTab.ATTENDANCE) }
        ),
        QuickActionItem(
          id = "teacher_assign_hw",
          label = "Assign Homework",
          icon = Icons.Default.PostAdd,
          containerColor = Color(0xFF2563EB),
          onClick = { onOpenAssignHomework() }
        ),
        QuickActionItem(
          id = "teacher_notice",
          label = "Publish Circular",
          icon = Icons.Default.Campaign,
          containerColor = Color(0xFFEA580C),
          onClick = { onOpenCreateNotice() }
        )
      )

      UserRole.DRIVER -> listOf(
        QuickActionItem(
          id = "driver_radar",
          label = "Route Radar (OSM)",
          icon = Icons.Default.Map,
          containerColor = Color(0xFF0284C7),
          onClick = { onNavigateToTab(NavigationTab.BUS_TRACKING) }
        ),
        QuickActionItem(
          id = "driver_ping",
          label = "Broadcast GPS Ping",
          icon = Icons.Default.GpsFixed,
          containerColor = Color(0xFF16A34A),
          onClick = { onTriggerGpsPing() }
        ),
        QuickActionItem(
          id = "driver_sos",
          label = "Emergency SOS Alert",
          icon = Icons.Default.Emergency,
          containerColor = Color(0xFFDC2626),
          onClick = { onTriggerEmergencySos() }
        )
      )

      UserRole.ADMIN -> listOf(
        QuickActionItem(
          id = "admin_broadcast",
          label = "Emergency Broadcast",
          icon = Icons.Default.NotificationImportant,
          containerColor = Color(0xFFDC2626),
          onClick = { onOpenCreateNotice() }
        ),
        QuickActionItem(
          id = "admin_management",
          label = "Campus Management",
          icon = Icons.Default.DashboardCustomize,
          containerColor = Color(0xFF7C3AED),
          onClick = { onNavigateToTab(NavigationTab.MANAGEMENT) }
        ),
        QuickActionItem(
          id = "admin_directory",
          label = "Staff Directory",
          icon = Icons.Default.ContactPhone,
          containerColor = Color(0xFF0D9488),
          onClick = { onNavigateToTab(NavigationTab.DIRECTORY) }
        )
      )

      UserRole.DEVELOPER -> listOf(
        QuickActionItem(
          id = "dev_console",
          label = "Terminal & SQLite",
          icon = Icons.Default.Terminal,
          containerColor = Color(0xFF10B981),
          onClick = { onOpenDeveloperTerminal() }
        ),
        QuickActionItem(
          id = "dev_switch_role",
          label = "Switch Active Role",
          icon = Icons.Default.SwapHoriz,
          containerColor = Color(0xFF6366F1),
          onClick = { onOpenRoleSwitcher() }
        ),
        QuickActionItem(
          id = "dev_id_card",
          label = "Preview ID Badge",
          icon = Icons.Default.Badge,
          containerColor = Color(0xFF0284C7),
          onClick = { onOpenIdCard() }
        )
      )

      UserRole.STAFF -> listOf(
        QuickActionItem(
          id = "staff_duties",
          label = "My Shift Duties",
          icon = Icons.Default.CheckCircle,
          containerColor = Color(0xFF0D9488),
          onClick = { onNavigateToTab(NavigationTab.DUTIES) }
        ),
        QuickActionItem(
          id = "staff_id",
          label = "Staff Pass",
          icon = Icons.Default.Badge,
          containerColor = Color(0xFF2563EB),
          onClick = { onOpenIdCard() }
        )
      )
    }
  }

  Column(
    horizontalAlignment = Alignment.End,
    verticalArrangement = Arrangement.spacedBy(10.dp),
    modifier = modifier.padding(bottom = 8.dp, end = 4.dp)
  ) {
    // Speed Dial Options Column
    AnimatedVisibility(
      visible = isExpanded,
      enter = fadeIn(tween(180)) + slideInVertically(
        initialOffsetY = { 40 },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
      ),
      exit = fadeOut(tween(120)) + slideOutVertically(
        targetOffsetY = { 40 },
        animationSpec = tween(120)
      )
    ) {
      Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 6.dp)
      ) {
        actions.forEachIndexed { index, action ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) {
                if (action.id.contains("sos", ignoreCase = true) ||
                    action.id.contains("emergency", ignoreCase = true) ||
                    action.id.contains("broadcast", ignoreCase = true) ||
                    action.id.contains("alert", ignoreCase = true)
                ) {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } else {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                isExpanded = false
                action.onClick()
              }
              .testTag("speed_dial_item_${action.id}")
          ) {
            // Label Pill
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceContainerHigh,
              shadowElevation = 4.dp,
              tonalElevation = 2.dp,
              modifier = Modifier.padding(vertical = 2.dp)
            ) {
              Text(
                text = action.label,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  letterSpacing = 0.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }

            // Action Circle
            Surface(
              shape = CircleShape,
              color = action.containerColor,
              shadowElevation = 4.dp,
              modifier = Modifier.size(44.dp)
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
              ) {
                Icon(
                  imageVector = action.icon,
                  contentDescription = action.label,
                  tint = action.contentColor,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    }

    // Main Floating Action Button
    FloatingActionButton(
      onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        isExpanded = !isExpanded
      },
      containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceContainerHighest else roleColor,
      contentColor = if (isExpanded) MaterialTheme.colorScheme.onSurface else Color.White,
      shape = CircleShape,
      elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
      modifier = Modifier
        .size(56.dp)
        .testTag("main_speed_dial_fab")
    ) {
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = if (isExpanded) "Close Quick Actions" else "Open Quick Actions",
        modifier = Modifier
          .size(26.dp)
          .rotate(rotationAngle)
      )
    }
  }
}
