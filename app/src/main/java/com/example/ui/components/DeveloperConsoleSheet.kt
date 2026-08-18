package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperConsoleSheet(
  viewModel: SchoolViewModel,
  onDismiss: () -> Unit,
  onSwitchToRole: (UserRole) -> Unit
) {
  val systemUsers by viewModel.systemUsers.collectAsState()
  val notices by viewModel.notices.collectAsState()
  val homeworks by viewModel.homeworks.collectAsState()
  val attendanceRecords by viewModel.attendanceRecords.collectAsState()

  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf("👥 Users", "📢 Notices", "📚 Homework", "📋 Attendance", "⚡ System")

  var userToEdit by remember { mutableStateOf<SystemUserRecord?>(null) }
  var showAddUserDialog by remember { mutableStateOf(false) }
  var userSearchQuery by remember { mutableStateOf("") }
  var selectedRoleFilter by remember { mutableStateOf<UserRole?>(null) }

  // Broadcast Alert Dialog
  var showBroadcastDialog by remember { mutableStateOf(false) }
  var broadcastTitle by remember { mutableStateOf("") }
  var broadcastMessage by remember { mutableStateOf("") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = Color(0xFF0F172A),
    contentColor = Color(0xFFE2E8F0),
    dragHandle = {
      Column(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Surface(
          color = Color(0xFF10B981),
          shape = CircleShape,
          modifier = Modifier.size(width = 44.dp, height = 4.dp)
        ) {}
      }
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.92f)
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // Terminal Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            color = Color(0xFF10B981).copy(alpha = 0.2f),
            shape = CircleShape,
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = "Terminal",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(20.dp)
              )
            }
          }
          Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "ROOT DEVELOPER CONSOLE",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace,
                  letterSpacing = 1.sp
                ),
                color = Color(0xFF10B981)
              )
              Surface(
                color = Color(0xFFEF4444).copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "GOD MODE",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                  color = Color(0xFFF87171),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = "Universal Write Access • Identity & Entity Override Engine",
              style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
              color = Color(0xFF94A3B8)
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF94A3B8))
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Tab selector
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color(0xFF1E293B),
        contentColor = Color(0xFF10B981),
        edgePadding = 0.dp
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = {
              Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                  fontFamily = FontFamily.Monospace
                ),
                color = if (selectedTab == index) Color(0xFF10B981) else Color(0xFF94A3B8)
              )
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Tab Contents
      when (selectedTab) {
        0 -> {
          // Users Master Tab
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = userSearchQuery,
              onValueChange = { userSearchQuery = it },
              placeholder = { Text("Search users by name, email, role...", color = Color(0xFF64748B)) },
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
              modifier = Modifier.weight(1f).testTag("dev_user_search_input")
            )

            Button(
              onClick = { showAddUserDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
              modifier = Modifier.testTag("dev_add_user_button")
            ) {
              Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add User", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Filter by role chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            FilterChip(
              selected = selectedRoleFilter == null,
              onClick = { selectedRoleFilter = null },
              label = { Text("All (${systemUsers.size})") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                selectedLabelColor = Color(0xFF10B981)
              )
            )
            UserRole.values().forEach { r ->
              FilterChip(
                selected = selectedRoleFilter == r,
                onClick = { selectedRoleFilter = if (selectedRoleFilter == r) null else r },
                label = { Text(r.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = Color(r.badgeColor).copy(alpha = 0.3f),
                  selectedLabelColor = Color.White
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          val filteredUsers = systemUsers.filter { user ->
            val matchesQuery = userSearchQuery.isBlank() ||
                user.fullName.contains(userSearchQuery, ignoreCase = true) ||
                user.email.contains(userSearchQuery, ignoreCase = true) ||
                user.username.contains(userSearchQuery, ignoreCase = true) ||
                user.designation.contains(userSearchQuery, ignoreCase = true)
            val matchesRole = selectedRoleFilter == null || user.role == selectedRoleFilter
            matchesQuery && matchesRole
          }

          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(filteredUsers, key = { it.id }) { record ->
              Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Surface(
                    color = Color(record.role.badgeColor).copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = record.fullName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(record.role.badgeColor)
                      )
                    }
                  }

                  Column(modifier = Modifier.weight(1f)) {
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
                      text = "Username: @${record.username} | Phone: ${record.phone} | ID: ${record.identifier}",
                      style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                      color = Color(0xFF64748B)
                    )
                  }

                  // Action Buttons
                  Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                      onClick = { userToEdit = record },
                      colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF10B981)),
                      modifier = Modifier.size(36.dp).testTag("edit_user_${record.username}")
                    ) {
                      Icon(Icons.Default.Edit, contentDescription = "Edit User", modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                      onClick = {
                        onSwitchToRole(record.role)
                        viewModel.login(record.username, "password123")
                      },
                      colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF38BDF8)),
                      modifier = Modifier.size(36.dp)
                    ) {
                      Icon(Icons.Default.SwitchAccount, contentDescription = "Impersonate", modifier = Modifier.size(18.dp))
                    }
                  }
                }
              }
            }
          }
        }

        1 -> {
          // Notices Master Tab
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Live Circulars (${notices.size})",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFF10B981)
            )

            Button(
              onClick = { showBroadcastDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Broadcast Root Alert", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(notices, key = { it.id }) { notice ->
              Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      if (notice.isUrgent) {
                        Surface(color = Color(0xFFEF4444), shape = RoundedCornerShape(4.dp)) {
                          Text("URGENT", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                      }
                      Text(notice.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                    Text(notice.content, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8), maxLines = 2)
                    Text("By: ${notice.publisherName} (${notice.publisherRole}) • ${notice.date}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = Color(0xFF64748B))
                  }

                  IconButton(
                    onClick = { viewModel.deleteNotice(notice.id) },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
                  ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                  }
                }
              }
            }
          }
        }

        2 -> {
          // Homework Master Tab
          Text(
            text = "Live Assignments Master (${homeworks.size})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF10B981)
          )

          Spacer(modifier = Modifier.height(10.dp))

          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(homeworks, key = { it.id }) { hw ->
              Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(hw.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("${hw.subjectName} • ${hw.className} • Due: ${hw.dueDate}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    Text("Submissions: ${hw.submissionsCount}/${hw.totalStudents} • Max Marks: ${hw.maxMarks}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = Color(0xFF10B981))
                  }

                  IconButton(
                    onClick = { viewModel.deleteHomework(hw.id) },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
                  ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                  }
                }
              }
            }
          }
        }

        3 -> {
          // Attendance Override Tab
          Text(
            text = "Direct Attendance Override Engine",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF10B981)
          )
          Text(
            text = "Tap on status badge to cycle: Full Day ➔ Present ➔ On Duty ➔ Absent",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8)
          )

          Spacer(modifier = Modifier.height(10.dp))

          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(attendanceRecords, key = { it.id }) { record ->
              Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text("${record.rollNo}. ${record.studentName}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("${record.className} • ${record.markedBy}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    if (record.notes.isNotBlank()) {
                      Text("Note: ${record.notes}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF59E0B))
                    }
                  }

                  Surface(
                    color = Color(record.status.colorHex).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(record.status.colorHex)),
                    modifier = Modifier.clickable {
                      val nextStatus = when (record.status) {
                        AttendanceStatus.FULL_DAY -> AttendanceStatus.ON_DUTY
                        AttendanceStatus.ON_DUTY -> AttendanceStatus.HALF_DAY
                        AttendanceStatus.HALF_DAY -> AttendanceStatus.ABSENT
                        AttendanceStatus.ABSENT -> AttendanceStatus.FULL_DAY
                      }
                      viewModel.updateAttendanceRecordDirect(record.id, record.studentName, nextStatus, "Developer Override")
                    }
                  ) {
                    Text(
                      text = record.status.code,
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                      color = Color(record.status.colorHex),
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                  }
                }
              }
            }
          }
        }

        4 -> {
          // System God Mode Controls
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Surface(
              color = Color(0xFF1E293B),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                  text = "⚡ System Telemetry & God Mode Info",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color(0xFF10B981)
                )
                Text("• Active Core: St. Joseph's St. Cloud v4.2.0-PRO", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE2E8F0))
                Text("• Permissions: Universal Write, Root Database, User Bypass", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE2E8F0))
                Text("• Storage Engine: Room SQLite + In-Memory Reactive Cache", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE2E8F0))
              }
            }

            // Impersonate Quick Roles
            Text(
              text = "⚡ Instant Role Switcher (Live Impersonation)",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFF38BDF8)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  onSwitchToRole(UserRole.STUDENT)
                  viewModel.switchRole(UserRole.STUDENT)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.weight(1f)
              ) {
                Text("Student", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }
              Button(
                onClick = {
                  onSwitchToRole(UserRole.TEACHER)
                  viewModel.switchRole(UserRole.TEACHER)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                modifier = Modifier.weight(1f)
              ) {
                Text("Teacher", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  onSwitchToRole(UserRole.STAFF)
                  viewModel.switchRole(UserRole.STAFF)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                modifier = Modifier.weight(1f)
              ) {
                Text("Staff", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }
              Button(
                onClick = {
                  onSwitchToRole(UserRole.ADMIN)
                  viewModel.switchRole(UserRole.ADMIN)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                modifier = Modifier.weight(1f)
              ) {
                Text("Admin", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Danger Zone
            Text(
              text = "⚠️ Factory System Reset",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFFEF4444)
            )

            OutlinedButton(
              onClick = {
                viewModel.resetAllSystemDefaults()
              },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
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
