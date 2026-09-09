package com.example.ui.dashboard

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperDashboardScreen(
  viewModel: SchoolViewModel,
  onNavigateToNotices: () -> Unit = {},
  onNavigateToHomework: () -> Unit = {},
  onNavigateToAttendance: () -> Unit = {},
  onNavigateToManagement: () -> Unit = {},
  onRoleSwitched: (UserRole) -> Unit = {},
  onNavigateToTab: ((NavigationTab) -> Unit)? = null
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val developerProfile by viewModel.developerProfile.collectAsState()
  val systemUsers by viewModel.systemUsers.collectAsState()
  val notices by viewModel.notices.collectAsState()
  val homeworks by viewModel.homeworks.collectAsState()
  val attendanceRecords by viewModel.attendanceRecords.collectAsState()

  var selectedSection by remember { mutableStateOf(0) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedRoleFilter by remember { mutableStateOf<UserRole?>(null) }
  var userToEdit by remember { mutableStateOf<SystemUserRecord?>(null) }
  var showAddUserDialog by remember { mutableStateOf(false) }

  // Root Alert Dialog
  var showBroadcastDialog by remember { mutableStateOf(false) }
  var broadcastTitle by remember { mutableStateOf("") }
  var broadcastMessage by remember { mutableStateOf("") }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF090D16))
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 0. User Profile Header
    item {
      currentUser?.let { user ->
        UserProfileHeader(
          user = user,
          subtitle = "Root Administrator • Node ID #${developerProfile?.devId ?: "DEV-ROOT-01"}",
          schoolSession = "Academic Session 2026–2027",
          statusLabel = "GOD MODE ACTIVE",
          testTag = "developer_user_profile_header"
        )
      }
    }

    // 1. Master System Controller Notice Card
    item {
      Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .background(
              Brush.horizontalGradient(
                colors = listOf(
                  Color(0xFF0F172A),
                  Color(0xFF064E3B).copy(alpha = 0.4f),
                  Color(0xFF0F172A)
                )
              )
            )
            .padding(16.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Surface(
              color = Color(0xFF10B981).copy(alpha = 0.2f),
              shape = CircleShape,
              modifier = Modifier.size(40.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.VpnKey,
                  contentDescription = null,
                  tint = Color(0xFF10B981),
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "FULL MASTER WRITE AUTHORITY",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  letterSpacing = 1.sp
                ),
                color = Color(0xFF10B981)
              )
              Text(
                text = "Full read/write access to user records, role switching, attendance, homework, notices & real-time telemetry.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE2E8F0)
              )
            }
          }
        }
      }
    }

    // 2. Telemetry Quick Stats
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Total Users Stat
        Surface(
          color = Color(0xFF1E293B),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, Color(0xFF334155)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("System Users", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
            Text("${systemUsers.size}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace), color = Color(0xFF10B981))
            Text("Editable in Live DB", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
          }
        }

        // Notices Stat
        Surface(
          color = Color(0xFF1E293B),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, Color(0xFF334155)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Live Notices", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
            Text("${notices.size}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace), color = Color(0xFF38BDF8))
            Text("Broadcasting Active", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
          }
        }

        // Attendance Overrides Stat
        Surface(
          color = Color(0xFF1E293B),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, Color(0xFF334155)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Attendance", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
            Text("${attendanceRecords.size}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace), color = Color(0xFFF59E0B))
            Text("Direct Override Ready", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
          }
        }
      }
    }

    // 3. Section Selector TabRow
    item {
      ScrollableTabRow(
        selectedTabIndex = selectedSection,
        containerColor = Color(0xFF1E293B),
        contentColor = Color(0xFF10B981),
        edgePadding = 0.dp
      ) {
        listOf("🔥 Firestore DB", "🌐 Omni Access", "👥 Users DB", "⚡ System Actions").forEachIndexed { idx, title ->
          Tab(
            selected = selectedSection == idx,
            onClick = { selectedSection = idx },
            text = {
              Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (selectedSection == idx) FontWeight.Bold else FontWeight.Normal,
                  fontFamily = FontFamily.Monospace
                ),
                color = if (selectedSection == idx) Color(0xFF10B981) else Color(0xFF94A3B8)
              )
            }
          )
        }
      }
    }

    // SECTION 0: FIRESTORE GOD MODE CLOUD DB
    if (selectedSection == 0) {
      item {
        FirestoreGodModeConsole(
          viewModel = viewModel,
          onNavigateToTab = onNavigateToTab,
          onRoleSwitched = onRoleSwitched
        )
      }
    }

    // SECTION 1: OMNI ACCESS SCREEN JUMPER
    if (selectedSection == 1) {
      item {
        Surface(
          color = Color(0xFF1E293B),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Launch, contentDescription = null, tint = Color(0xFF38BDF8))
              Text(
                text = "OMNI JUMP: ACCESS ANY SCREEN ACROSS THE APP",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                color = Color(0xFF38BDF8)
              )
            }
            Text(
              text = "Tap any module to navigate with complete root permissions across all features:",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFFCBD5E1)
            )
          }
        }
      }

      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf<Triple<String, String, NavigationTab>>(
            Triple("📝 Homework Desk", "Assign, grade & view all submissions", NavigationTab.HOMEWORK),
            Triple("📋 Attendance Roll-Call", "Take roll-call & override any class attendance", NavigationTab.ATTENDANCE),
            Triple("📢 Notices & Circulars", "Publish, update & delete school bulletins", NavigationTab.NOTICES),
            Triple("🏛️ Management DB", "Inspect student & teacher master database", NavigationTab.MANAGEMENT),
            Triple("🏫 Classes Matrix", "Homeroom teacher & student counts", NavigationTab.CLASSES),
            Triple("📅 Timetable Schedule", "Live class schedules & periods", NavigationTab.TIMETABLE),
            Triple("📆 School Calendar", "Academic & sports calendar events", NavigationTab.CALENDAR),
            Triple("🚌 Bus GPS Live Tracking", "Live route tracking & simulation", NavigationTab.BUS_TRACKING),
            Triple("🚨 Emergency Broadcasts", "Publish campus-wide emergency banners", NavigationTab.ANNOUNCEMENTS),
            Triple("👥 User Directory", "Search faculty, staff & students", NavigationTab.DIRECTORY),
            Triple("💼 Operations & Duties", "Supervision, lab & campus tasks", NavigationTab.DUTIES),
            Triple("⚙️ System Settings", "Theme, accounts & preferences", NavigationTab.SETTINGS)
          ).forEach { (title, subtitle, tab) ->
            Surface(
              color = Color(0xFF0F172A),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, Color(0xFF334155)),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToTab?.invoke(tab) }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                  )
                  Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                  )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
              }
            }
          }
        }
      }
    }

    // 4. Fast Command Actions
    if (selectedSection == 3) {
    item {
      Text(
        text = "⚡ QUICK GOD MODE ACTIONS",
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        ),
        color = Color(0xFF10B981)
      )
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = { showAddUserDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).testTag("dev_create_user_btn")
        ) {
          Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add New User", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }

        Button(
          onClick = { showBroadcastDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).testTag("dev_broadcast_alert_btn")
        ) {
          Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Broadcast Alert", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }
      }
    }

    item {
      PushNotificationControlCard(
        viewModel = viewModel,
        modifier = Modifier.fillMaxWidth()
      )
    }

      item {
        Surface(
          color = Color(0xFF1E293B),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, Color(0xFF334155)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "⚠️ Factory System Reset & Firestore Cloud Purge",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFFEF4444)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { viewModel.wipeEntireFirestoreDatabase() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("☢️ Nuclear Wipe DB", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }

              Button(
                onClick = { viewModel.pushLocalSeedToFirestore() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("🚀 Re-Seed Firestore", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }
            }

            OutlinedButton(
              onClick = { viewModel.clearAllFeedbackData() },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
              border = BorderStroke(1.dp, Color(0xFFF59E0B)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Clear Feedback Vault & Submissions")
            }

            OutlinedButton(
              onClick = { viewModel.resetAllSystemDefaults() },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
              border = BorderStroke(1.dp, Color(0xFFEF4444)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Reset All Records & Users to Factory Seed")
            }
          }
        }
      }
    }

    // SECTION 2: MASTER USER ROSTER & EDITOR
    if (selectedSection == 2) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "👥 MASTER USER ROSTER & EDITOR",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
              ),
              color = Color(0xFF10B981)
            )
            Text(
              text = "Tap '✏️ Edit' on any card to modify name, role, email, phone or student details in real time.",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF94A3B8)
            )
          }
        }
      }

    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Filter users by name, username, email...", color = Color(0xFF64748B)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF10B981)) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF1E293B),
          unfocusedContainerColor = Color(0xFF1E293B),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = Color(0xFF10B981),
          unfocusedBorderColor = Color(0xFF334155)
        ),
        modifier = Modifier.fillMaxWidth().testTag("dev_screen_user_search")
      )
    }

    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          FilterChip(
            selected = selectedRoleFilter == null,
            onClick = { selectedRoleFilter = null },
            label = { Text("All Roles (${systemUsers.size})") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
              selectedLabelColor = Color(0xFF10B981)
            )
          )
        }
        items(UserRole.values()) { role ->
          FilterChip(
            selected = selectedRoleFilter == role,
            onClick = { selectedRoleFilter = if (selectedRoleFilter == role) null else role },
            label = { Text(role.displayName) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(role.badgeColor).copy(alpha = 0.3f),
              selectedLabelColor = Color.White
            )
          )
        }
      }
    }

    // Filtered User List
    val filteredList = systemUsers.filter { user ->
      val matchQuery = searchQuery.isBlank() ||
          user.fullName.contains(searchQuery, ignoreCase = true) ||
          user.username.contains(searchQuery, ignoreCase = true) ||
          user.email.contains(searchQuery, ignoreCase = true) ||
          user.designation.contains(searchQuery, ignoreCase = true)
      val matchRole = selectedRoleFilter == null || user.role == selectedRoleFilter
      matchQuery && matchRole
    }

    items(filteredList, key = { it.id }) { record ->
      Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (record.role == UserRole.DEVELOPER) Color(0xFF10B981) else Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Surface(
            color = Color(record.role.badgeColor).copy(alpha = 0.2f),
            shape = CircleShape,
            modifier = Modifier.size(44.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = record.fullName.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(record.role.badgeColor)
              )
            }
          }

          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = record.fullName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
              )
              Surface(
                color = Color(record.role.badgeColor).copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = record.role.displayName,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Text(
              text = "${record.designation.ifBlank { "Registered User" }} • ${record.email}",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF94A3B8)
            )

            Text(
              text = "User: @${record.username} | Ph: ${record.phone} | ID: ${record.identifier}",
              style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
              color = Color(0xFF64748B)
            )
          }

          // Edit User Button
          FilledTonalButton(
            onClick = { userToEdit = record },
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
              contentColor = Color(0xFF10B981)
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("dev_edit_user_${record.username}")
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Edit", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }
        }
      }
    }
    }
  }

  // Edit User Dialog
  if (userToEdit != null) {
    EditUserDialog(
      userRecord = userToEdit,
      isNewUser = false,
      onDismiss = { userToEdit = null },
      onSave = { updated ->
        viewModel.updateSystemUser(updated)
      },
      onDelete = { id ->
        viewModel.deleteSystemUser(id)
      }
    )
  }

  // Add User Dialog
  if (showAddUserDialog) {
    EditUserDialog(
      userRecord = null,
      isNewUser = true,
      onDismiss = { showAddUserDialog = false },
      onSave = { newUser ->
        viewModel.addSystemUser(newUser)
      }
    )
  }

  // Broadcast Root Alert Dialog
  if (showBroadcastDialog) {
    AlertDialog(
      onDismissRequest = { showBroadcastDialog = false },
      containerColor = Color(0xFF0F172A),
      titleContentColor = Color(0xFFEF4444),
      textContentColor = Color(0xFFE2E8F0),
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFEF4444))
          Text("Broadcast Root Alert", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("This notification and circular will immediately appear on all student, teacher, staff, and admin dashboards.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
          OutlinedTextField(
            value = broadcastTitle,
            onValueChange = { broadcastTitle = it },
            label = { Text("Alert Title") },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = broadcastMessage,
            onValueChange = { broadcastMessage = it },
            label = { Text("Message Body") },
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (broadcastTitle.isNotBlank() && broadcastMessage.isNotBlank()) {
              viewModel.broadcastDeveloperNotice(broadcastTitle, broadcastMessage, isUrgent = true)
              showBroadcastDialog = false
              broadcastTitle = ""
              broadcastMessage = ""
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
          Text("Broadcast Now")
        }
      },
      dismissButton = {
        TextButton(onClick = { showBroadcastDialog = false }) {
          Text("Cancel", color = Color(0xFF94A3B8))
        }
      }
    )
  }
}
