package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.*
import com.example.ui.auth.LoginScreen
import com.example.ui.components.*
import com.example.ui.dashboard.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.SchoolViewModel

enum class NavigationTab(val label: String, val icon: ImageVector) {
  DASHBOARD("Dashboard", Icons.Default.Dashboard),
  TIMETABLE("Timetable", Icons.Default.CalendarMonth),
  HOMEWORK("Homework", Icons.Default.Assignment),
  ATTENDANCE("Attendance", Icons.Default.FactCheck),
  NOTICES("Circulars", Icons.Default.Campaign),
  DUTIES("Duties", Icons.Default.Checklist),
  CLASSES("Classes", Icons.Default.Groups),
  MANAGEMENT("Directory", Icons.Default.AdminPanelSettings),
  PROFILE("Profile", Icons.Default.Person),
  SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSchoolApp(
  viewModel: SchoolViewModel = viewModel()
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val currentThemeMode by viewModel.themeMode.collectAsState()
  val studentProfile by viewModel.studentProfile.collectAsState()
  val teacherProfile by viewModel.teacherProfile.collectAsState()
  val staffProfile by viewModel.staffProfile.collectAsState()
  val adminProfile by viewModel.adminProfile.collectAsState()

  val notices by viewModel.notices.collectAsState()
  val schoolEvents by viewModel.schoolEvents.collectAsState()
  val homeworks by viewModel.homeworks.collectAsState()
  val timetables by viewModel.timetables.collectAsState()
  val schoolClasses by viewModel.schoolClasses.collectAsState()
  val staffDuties by viewModel.staffDuties.collectAsState()
  val attendanceRecords by viewModel.attendanceRecords.collectAsState()
  val roomAttendanceRecords by viewModel.roomAttendanceRecords.collectAsState()

  val context = androidx.compose.ui.platform.LocalContext.current
  LaunchedEffect(Unit) {
    viewModel.initializeWithContext(context)
  }

  val selectedDay by viewModel.selectedDay.collectAsState()
  val selectedClassForAttendance by viewModel.selectedClassForAttendance.collectAsState()
  val selectedNoticeCategory by viewModel.selectedNoticeCategory.collectAsState()
  val pendingHomeworkCount by viewModel.pendingHomeworkCount.collectAsState()

  // Compulsory authentication on every app launch - requires successful login before accessing app
  var isAuthenticated by remember { mutableStateOf(false) }
  var loginErrorMessage by remember { mutableStateOf<String?>(null) }
  var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

  if (!isAuthenticated) {
    LoginScreen(
      errorMessage = loginErrorMessage,
      onLogin = { username, password, _ ->
        val result = viewModel.login(username, password)
        result.onSuccess {
          loginErrorMessage = null
          isAuthenticated = true
          currentTab = NavigationTab.DASHBOARD
        }.onFailure { error ->
          loginErrorMessage = error.message ?: "Authentication failed. Please check your credentials."
        }
      },
      onQuickRoleLogin = { role ->
        viewModel.switchRole(role)
        loginErrorMessage = null
        isAuthenticated = true
        currentTab = NavigationTab.DASHBOARD
      }
    )
    return
  }

  // Dialog States
  var showRoleSwitcherDialog by remember { mutableStateOf(false) }
  var showAssignHomeworkDialog by remember { mutableStateOf(false) }
  var showCreateNoticeDialog by remember { mutableStateOf(false) }
  var showAddDutyDialog by remember { mutableStateOf(false) }
  var selectedHomeworkForSubmission by remember { mutableStateOf<Homework?>(null) }
  var selectedNoticeDetail by remember { mutableStateOf<Notice?>(null) }

  val visibleTabs = when (currentUser.role) {
    UserRole.STUDENT -> listOf(
      NavigationTab.DASHBOARD,
      NavigationTab.TIMETABLE,
      NavigationTab.HOMEWORK,
      NavigationTab.ATTENDANCE,
      NavigationTab.NOTICES,
      NavigationTab.PROFILE
    )
    UserRole.TEACHER -> listOf(
      NavigationTab.DASHBOARD,
      NavigationTab.ATTENDANCE,
      NavigationTab.HOMEWORK,
      NavigationTab.CLASSES,
      NavigationTab.NOTICES,
      NavigationTab.PROFILE
    )
    UserRole.STAFF -> listOf(
      NavigationTab.DASHBOARD,
      NavigationTab.DUTIES,
      NavigationTab.ATTENDANCE,
      NavigationTab.NOTICES,
      NavigationTab.PROFILE
    )
    UserRole.ADMIN -> listOf(
      NavigationTab.DASHBOARD,
      NavigationTab.MANAGEMENT,
      NavigationTab.CLASSES,
      NavigationTab.ATTENDANCE,
      NavigationTab.NOTICES,
      NavigationTab.PROFILE
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SchoolNavyPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = SchoolGold,
                modifier = Modifier.size(24.dp)
              )
            }

            Column {
              Text(
                text = "St. Joseph's",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  letterSpacing = 0.5.sp
                ),
                color = SchoolNavyPrimary
              )
              Text(
                text = currentTab.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        actions = {
          // Switch Role Quick Button
          Surface(
            color = SchoolNavyPrimary.copy(alpha = 0.08f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .clickable { showRoleSwitcherDialog = true }
              .testTag("switch_role_top_bar_btn")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Switch Role",
                tint = SchoolNavyPrimary,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = currentUser.role.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = SchoolNavyPrimary
              )
            }
          }

          // Bulletins Notification Action
          IconButton(
            onClick = { currentTab = NavigationTab.NOTICES },
            modifier = Modifier.testTag("notifications_bell_btn")
          ) {
            BadgedBox(
              badge = {
                val count = notices.count { it.isUrgent }
                if (count > 0) {
                  Badge(containerColor = Color(0xFFDC2626)) {
                    Text("$count")
                  }
                }
              }
            ) {
              Icon(imageVector = Icons.Default.Notifications, contentDescription = "Bulletins")
            }
          }

          // Settings Action
          IconButton(
            onClick = { currentTab = NavigationTab.SETTINGS },
            modifier = Modifier.testTag("settings_top_bar_btn")
          ) {
            Icon(
              imageVector = if (currentTab == NavigationTab.SETTINGS) Icons.Filled.Settings else Icons.Default.Settings,
              contentDescription = "Settings",
              tint = if (currentTab == NavigationTab.SETTINGS) SchoolNavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface
        )
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
      ) {
        visibleTabs.forEach { tab ->
          val isSelected = currentTab == tab
          NavigationBarItem(
            icon = {
              if (tab == NavigationTab.HOMEWORK && currentUser.role == UserRole.STUDENT && pendingHomeworkCount > 0) {
                BadgedBox(badge = { Badge { Text("$pendingHomeworkCount") } }) {
                  Icon(imageVector = tab.icon, contentDescription = tab.label)
                }
              } else {
                Icon(imageVector = tab.icon, contentDescription = tab.label)
              }
            },
            label = {
              Text(
                text = tab.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 10.sp
                )
              )
            },
            selected = isSelected,
            onClick = { currentTab = tab },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = SchoolNavyPrimary,
              selectedTextColor = SchoolNavyPrimary,
              indicatorColor = SchoolNavyPrimary.copy(alpha = 0.12f),
              unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
              unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentTab) {
        NavigationTab.DASHBOARD -> {
          when (currentUser.role) {
            UserRole.STUDENT -> {
              studentProfile?.let { prof ->
                val todayTimetable = timetables.filter { it.day == DayOfWeek.MONDAY }
                StudentDashboardScreen(
                  profile = prof,
                  pendingHomeworkCount = pendingHomeworkCount,
                  todayTimetable = todayTimetable,
                  notices = notices,
                  events = schoolEvents,
                  onNavigateToTimetable = { currentTab = NavigationTab.TIMETABLE },
                  onNavigateToHomework = { currentTab = NavigationTab.HOMEWORK },
                  onNavigateToAttendance = { currentTab = NavigationTab.ATTENDANCE },
                  onNavigateToNotices = { currentTab = NavigationTab.NOTICES },
                  onNoticeClick = { selectedNoticeDetail = it }
                )
              }
            }

            UserRole.TEACHER -> {
              teacherProfile?.let { prof ->
                val todayTimetable = timetables.filter { it.day == DayOfWeek.MONDAY }
                TeacherDashboardScreen(
                  profile = prof,
                  todaySchedule = todayTimetable,
                  classesList = schoolClasses,
                  notices = notices,
                  onOpenAssignHomeworkDialog = { showAssignHomeworkDialog = true },
                  onOpenMarkAttendance = { cls ->
                    viewModel.setSelectedClass(cls)
                    currentTab = NavigationTab.ATTENDANCE
                  },
                  onOpenPostNoticeDialog = { showCreateNoticeDialog = true },
                  onNavigateToClasses = { currentTab = NavigationTab.CLASSES },
                  onNavigateToNotices = { currentTab = NavigationTab.NOTICES },
                  onNoticeClick = { selectedNoticeDetail = it }
                )
              }
            }

            UserRole.STAFF -> {
              staffProfile?.let { prof ->
                StaffDashboardScreen(
                  profile = prof,
                  duties = staffDuties,
                  notices = notices,
                  onUpdateDutyStatus = { dutyId, newStatus -> viewModel.updateDutyStatus(dutyId, newStatus) },
                  onOpenAddDutyDialog = { showAddDutyDialog = true },
                  onNavigateToDuties = { currentTab = NavigationTab.DUTIES },
                  onNavigateToNotices = { currentTab = NavigationTab.NOTICES },
                  onNoticeClick = { selectedNoticeDetail = it }
                )
              }
            }

            UserRole.ADMIN -> {
              adminProfile?.let { prof ->
                AdminDashboardScreen(
                  profile = prof,
                  classes = schoolClasses,
                  notices = notices,
                  onOpenBroadcastNoticeDialog = { showCreateNoticeDialog = true },
                  onNavigateToManagement = { currentTab = NavigationTab.MANAGEMENT },
                  onNavigateToNotices = { currentTab = NavigationTab.NOTICES },
                  onNoticeClick = { selectedNoticeDetail = it }
                )
              }
            }
          }
        }

        NavigationTab.TIMETABLE -> {
          TimetableScreen(
            timetables = timetables,
            selectedDay = selectedDay,
            onSelectDay = { viewModel.setSelectedDay(it) },
            userRoleName = currentUser.role.label
          )
        }

        NavigationTab.HOMEWORK -> {
          HomeworkScreen(
            userRole = currentUser.role,
            homeworks = homeworks,
            onSubmitHomework = { selectedHomeworkForSubmission = it },
            onOpenAssignDialog = { showAssignHomeworkDialog = true }
          )
        }

        NavigationTab.ATTENDANCE -> {
          AttendanceScreen(
            userRole = currentUser.role,
            studentProfile = studentProfile,
            teacherProfile = teacherProfile,
            adminProfile = adminProfile,
            attendanceRecords = attendanceRecords,
            roomAttendanceRecords = roomAttendanceRecords,
            selectedClass = selectedClassForAttendance,
            onSelectClass = { viewModel.setSelectedClass(it) },
            onUpdateStatus = { studentId, status, notes ->
              viewModel.updateStudentAttendanceStatus(studentId, status, notes)
            },
            onMarkAllFullDay = { cls ->
              viewModel.markAllFullDay(cls)
            }
          )
        }

        NavigationTab.NOTICES -> {
          NoticesScreen(
            notices = notices,
            selectedCategory = selectedNoticeCategory,
            onSelectCategory = { viewModel.setSelectedCategory(it) },
            onNoticeClick = { selectedNoticeDetail = it },
            onOpenCreateNoticeDialog = { showCreateNoticeDialog = true },
            canCreateNotice = currentUser.role != UserRole.STUDENT
          )
        }

        NavigationTab.DUTIES -> {
          DutiesScreen(
            duties = staffDuties,
            onUpdateDutyStatus = { dutyId, newStatus -> viewModel.updateDutyStatus(dutyId, newStatus) },
            onOpenAddDutyDialog = { showAddDutyDialog = true }
          )
        }

        NavigationTab.CLASSES -> {
          ClassesScreen(
            classes = schoolClasses,
            userRole = currentUser.role,
            teacherProfile = teacherProfile,
            onOpenAttendanceForClass = { cls ->
              viewModel.setSelectedClass(cls)
              currentTab = NavigationTab.ATTENDANCE
            },
            onOpenAssignHomework = { showAssignHomeworkDialog = true }
          )
        }

        NavigationTab.MANAGEMENT -> {
          ManagementScreen(classes = schoolClasses)
        }

        NavigationTab.PROFILE -> {
          ProfileScreen(
            currentUser = currentUser,
            studentProfile = studentProfile,
            teacherProfile = teacherProfile,
            staffProfile = staffProfile,
            adminProfile = adminProfile,
            onSwitchRole = { role ->
              viewModel.switchRole(role)
              currentTab = NavigationTab.DASHBOARD
            },
            onNavigateToSettings = {
              currentTab = NavigationTab.SETTINGS
            },
            onSignOut = {
              viewModel.logout()
              loginErrorMessage = null
              isAuthenticated = false
            }
          )
        }

        NavigationTab.SETTINGS -> {
          SettingsScreen(
            currentUser = currentUser,
            currentThemeMode = currentThemeMode,
            onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
            onSwitchRole = { showRoleSwitcherDialog = true },
            onSignOut = {
              viewModel.logout()
              loginErrorMessage = null
              isAuthenticated = false
            },
            onResetDatabase = {
              viewModel.resetDatabaseToDefaults()
            }
          )
        }
      }
    }
  }

  // DIALOGS & BOTTOM SHEETS

  // 1. Role Switcher Dialog
  if (showRoleSwitcherDialog) {
    AlertDialog(
      onDismissRequest = { showRoleSwitcherDialog = false },
      icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = SchoolNavyPrimary) },
      title = { Text("Select Portal User Role", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Switch between different user roles to test specialized workflows and interfaces:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))

          UserRole.values().forEach { role ->
            val isCurrent = currentUser.role == role
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isCurrent) SchoolNavyPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.switchRole(role)
                  currentTab = NavigationTab.DASHBOARD
                  showRoleSwitcherDialog = false
                }
                .testTag("dialog_switch_to_${role.name.lowercase()}")
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(
                  imageVector = when(role) {
                    UserRole.STUDENT -> Icons.Default.School
                    UserRole.TEACHER -> Icons.Default.MenuBook
                    UserRole.STAFF -> Icons.Default.Engineering
                    UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                  },
                  contentDescription = null,
                  tint = if (isCurrent) SchoolNavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = role.label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isCurrent) SchoolNavyPrimary else MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = when(role) {
                      UserRole.STUDENT -> "Alex Johnson (Class 10-A)"
                      UserRole.TEACHER -> "Prof. Sarah Jenkins (Physics)"
                      UserRole.STAFF -> "Marcus Vance (Head Facilities)"
                      UserRole.ADMIN -> "Dr. Anthony Edwards (Principal)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                if (isCurrent) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SchoolNavyPrimary)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showRoleSwitcherDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // 2. Assign Homework Dialog
  if (showAssignHomeworkDialog) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var targetClass by remember { mutableStateOf("Class 10-A") }
    var dueDate by remember { mutableStateOf("Tomorrow, 5:00 PM") }

    AlertDialog(
      onDismissRequest = { showAssignHomeworkDialog = false },
      icon = { Icon(Icons.Default.Assignment, contentDescription = null, tint = SchoolNavyPrimary) },
      title = { Text("Assign New Homework", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Assignment Title") },
            placeholder = { Text("e.g. Wave Optics Problem Set") },
            modifier = Modifier.fillMaxWidth().testTag("hw_title_input"),
            singleLine = true
          )
          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Instructions & Chapters") },
            placeholder = { Text("Solve Numerical Questions 1 to 15 from Chapter 4") },
            modifier = Modifier.fillMaxWidth().testTag("hw_desc_input"),
            maxLines = 3
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = subject,
              onValueChange = { subject = it },
              label = { Text("Subject") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
            OutlinedTextField(
              value = targetClass,
              onValueChange = { targetClass = it },
              label = { Text("Class") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
          }
          OutlinedTextField(
            value = dueDate,
            onValueChange = { dueDate = it },
            label = { Text("Due Date / Time") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank()) {
              viewModel.createHomework(
                title = title,
                description = description.ifBlank { "Complete the assigned exercises in notebook." },
                subjectName = subject,
                className = targetClass,
                dueDate = dueDate,
                maxMarks = 25
              )
              showAssignHomeworkDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary),
          modifier = Modifier.testTag("submit_assign_hw_btn")
        ) {
          Text("Publish Assignment")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAssignHomeworkDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // 3. Create Notice Dialog
  if (showCreateNoticeDialog) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(NoticeCategory.ACADEMIC) }
    var isUrgent by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { showCreateNoticeDialog = false },
      icon = { Icon(Icons.Default.Campaign, contentDescription = null, tint = SchoolNavyPrimary) },
      title = { Text("Broadcast New Circular", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Circular Heading") },
            placeholder = { Text("e.g. Annual Sports Meet 2026") },
            modifier = Modifier.fillMaxWidth().testTag("notice_title_input"),
            singleLine = true
          )
          OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Full Bulletin Content") },
            placeholder = { Text("Details of schedule, participation, uniform, and timing...") },
            modifier = Modifier.fillMaxWidth().testTag("notice_content_input"),
            maxLines = 4
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Mark as Urgent / Priority", style = MaterialTheme.typography.bodySmall)
            Switch(
              checked = isUrgent,
              onCheckedChange = { isUrgent = it },
              colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFDC2626))
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank() && content.isNotBlank()) {
              viewModel.publishNotice(title, content, category, isUrgent)
              showCreateNoticeDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary),
          modifier = Modifier.testTag("submit_create_notice_btn")
        ) {
          Text("Post Circular")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCreateNoticeDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // 4. Add Duty Task Dialog
  if (showAddDutyDialog) {
    var title by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("Main Gate & North Quad") }
    var time by remember { mutableStateOf("07:45 AM - 08:30 AM") }

    AlertDialog(
      onDismissRequest = { showAddDutyDialog = false },
      icon = { Icon(Icons.Default.Checklist, contentDescription = null, tint = SchoolNavyPrimary) },
      title = { Text("Assign Campus Duty", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Duty Description") },
            placeholder = { Text("Morning Gate Monitoring & Bus Dispersal") },
            modifier = Modifier.fillMaxWidth().testTag("duty_title_input"),
            singleLine = true
          )
          OutlinedTextField(
            value = area,
            onValueChange = { area = it },
            label = { Text("Campus Location") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time Window") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank()) {
              viewModel.addDutyTask(title, area, time)
              showAddDutyDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary),
          modifier = Modifier.testTag("submit_add_duty_btn")
        ) {
          Text("Add Duty")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDutyDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // 5. Submit Homework Dialog (Student)
  if (selectedHomeworkForSubmission != null) {
    val hw = selectedHomeworkForSubmission!!
    var submissionNote by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { selectedHomeworkForSubmission = null },
      icon = { Icon(Icons.Default.UploadFile, contentDescription = null, tint = SchoolAccentGreen) },
      title = { Text("Submit Homework", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(text = "Subject: ${hw.subjectName}", fontWeight = FontWeight.Bold, color = SchoolNavyPrimary)
          Text(text = hw.title, style = MaterialTheme.typography.bodyMedium)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = submissionNote,
            onValueChange = { submissionNote = it },
            label = { Text("Submission Comments / Link") },
            placeholder = { Text("Completed in Classwork notebook. Formulas verified.") },
            modifier = Modifier.fillMaxWidth().testTag("submission_note_input"),
            maxLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.submitHomework(hw.id, submissionNote.ifBlank { "Completed & Submitted via Student Portal." })
            selectedHomeworkForSubmission = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = SchoolAccentGreen),
          modifier = Modifier.testTag("confirm_submit_hw_btn")
        ) {
          Text("Confirm Submission")
        }
      },
      dismissButton = {
        TextButton(onClick = { selectedHomeworkForSubmission = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // 6. Notice Detail Dialog
  if (selectedNoticeDetail != null) {
    val notice = selectedNoticeDetail!!
    AlertDialog(
      onDismissRequest = { selectedNoticeDetail = null },
      icon = {
        Icon(
          imageVector = if (notice.isUrgent) Icons.Default.Warning else Icons.Default.Campaign,
          contentDescription = null,
          tint = if (notice.isUrgent) Color(0xFFDC2626) else SchoolNavyPrimary
        )
      },
      title = {
        Text(notice.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "By ${notice.authorName} (${notice.authorRole})",
              style = MaterialTheme.typography.labelSmall,
              color = SchoolNavyPrimary
            )
            Text(
              text = notice.date,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          Text(
            text = notice.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { selectedNoticeDetail = null },
          colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
        ) {
          Text("Close")
        }
      }
    )
  }
}
