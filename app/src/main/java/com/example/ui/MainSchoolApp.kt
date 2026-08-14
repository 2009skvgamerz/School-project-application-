package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
  PROFILE("Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSchoolApp(
  viewModel: SchoolViewModel = viewModel()
) {
  val currentUser by viewModel.currentUser.collectAsState()
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

  val selectedDay by viewModel.selectedDay.collectAsState()
  val selectedClassForAttendance by viewModel.selectedClassForAttendance.collectAsState()
  val selectedNoticeCategory by viewModel.selectedNoticeCategory.collectAsState()
  val pendingHomeworkCount by viewModel.pendingHomeworkCount.collectAsState()

  var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

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
                .clip(CircleShape)
                .background(SchoolGold),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = SchoolNavyPrimary,
                modifier = Modifier.size(22.dp)
              )
            }
            Column {
              Text(
                text = "St. Joseph's School",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SchoolNavyPrimary
              )
              Text(
                text = "${currentUser.role.label} Portal • 2026-27",
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
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Switch Role",
                tint = SchoolNavyPrimary,
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = currentUser.role.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = SchoolNavyPrimary
              )
            }
          }

          IconButton(
            onClick = { currentTab = NavigationTab.NOTICES },
            modifier = Modifier.testTag("notifications_bell_btn")
          ) {
            BadgedBox(
              badge = {
                Badge(containerColor = Color(0xFFDC2626)) {
                  Text("${notices.count { it.isUrgent }}")
                }
              }
            ) {
              Icon(imageVector = Icons.Default.Notifications, contentDescription = "Bulletins")
            }
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
            attendanceRecords = attendanceRecords,
            selectedClass = selectedClassForAttendance,
            onSelectClass = { viewModel.setSelectedClass(it) },
            onUpdateStatus = { studentId, status -> viewModel.updateStudentAttendanceStatus(studentId, status) },
            onMarkAllPresent = { viewModel.markAllPresent() }
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
                      UserRole.STUDENT -> "Timetable, Homework submission, Attendance view"
                      UserRole.TEACHER -> "Mark attendance, Assign HW, Manage classes"
                      UserRole.STAFF -> "Campus duties, Operations checklist, Safety logs"
                      UserRole.ADMIN -> "Institutional stats, School directory, Circular broadcasts"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                if (isCurrent) {
                  Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SchoolNavyPrimary)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showRoleSwitcherDialog = false }) {
          Text("Close")
        }
      }
    )
  }

  // 2. Submit Homework Dialog (for Students)
  selectedHomeworkForSubmission?.let { hw ->
    var notes by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { selectedHomeworkForSubmission = null },
      icon = { Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, tint = SchoolNavyPrimary) },
      title = { Text("Submit Homework: ${hw.subjectName}", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = hw.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
          Text(text = hw.description, style = MaterialTheme.typography.bodySmall)

          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Submission notes / Attachment link") },
            placeholder = { Text("e.g. Completed worksheets & exercises uploaded") },
            modifier = Modifier.fillMaxWidth().testTag("homework_submission_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.submitHomework(hw.id, if (notes.isBlank()) "Submitted on-time via Student Portal" else notes)
            selectedHomeworkForSubmission = null
          },
          modifier = Modifier.testTag("confirm_submit_hw_btn")
        ) {
          Text("Confirm Submit")
        }
      },
      dismissButton = {
        TextButton(onClick = { selectedHomeworkForSubmission = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // 3. Assign New Homework Dialog (Teachers/Admins)
  if (showAssignHomeworkDialog) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var className by remember { mutableStateOf("Class 10-A") }
    var dueDate by remember { mutableStateOf("Tomorrow, 5:00 PM") }

    AlertDialog(
      onDismissRequest = { showAssignHomeworkDialog = false },
      title = { Text("Assign New Homework", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Assignment Title") },
            modifier = Modifier.fillMaxWidth().testTag("assign_hw_title_input")
          )
          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description & Tasks") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = className,
            onValueChange = { className = it },
            label = { Text("Target Class") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = dueDate,
            onValueChange = { dueDate = it },
            label = { Text("Due Date / Time") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank()) {
              viewModel.createHomework(
                title = title,
                description = description.ifBlank { "Complete the given exercises." },
                subjectName = subject,
                className = className,
                dueDate = dueDate,
                maxMarks = 25
              )
            }
            showAssignHomeworkDialog = false
          },
          modifier = Modifier.testTag("save_hw_btn")
        ) {
          Text("Assign")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAssignHomeworkDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // 4. Create Notice Dialog (Teachers/Staff/Admin)
  if (showCreateNoticeDialog) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(NoticeCategory.GENERAL) }
    var isUrgent by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { showCreateNoticeDialog = false },
      title = { Text("Publish Circular / Notice", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Notice Title") },
            modifier = Modifier.fillMaxWidth().testTag("notice_title_input")
          )
          OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Notice Content") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth().testTag("notice_content_input")
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Checkbox(
              checked = isUrgent,
              onCheckedChange = { isUrgent = it },
              modifier = Modifier.testTag("notice_urgent_checkbox")
            )
            Text("Mark as High Priority / Urgent")
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank()) {
              viewModel.publishNotice(
                title = title,
                content = content.ifBlank { "Important announcement for all school members." },
                category = category,
                isUrgent = isUrgent
              )
            }
            showCreateNoticeDialog = false
          },
          modifier = Modifier.testTag("publish_notice_btn")
        ) {
          Text("Publish")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCreateNoticeDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // 5. Add Duty Dialog (Staff/Admin)
  if (showAddDutyDialog) {
    var dutyTitle by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("Main Campus Ground") }
    var time by remember { mutableStateOf("11:30 AM - 12:30 PM") }

    AlertDialog(
      onDismissRequest = { showAddDutyDialog = false },
      title = { Text("Assign Operational Task", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = dutyTitle,
            onValueChange = { dutyTitle = it },
            label = { Text("Task Description") },
            modifier = Modifier.fillMaxWidth().testTag("duty_title_input")
          )
          OutlinedTextField(
            value = area,
            onValueChange = { area = it },
            label = { Text("Location / Campus Area") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Scheduled Time") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (dutyTitle.isNotBlank()) {
              viewModel.addDutyTask(
                title = dutyTitle,
                area = area,
                time = time
              )
            }
            showAddDutyDialog = false
          },
          modifier = Modifier.testTag("save_duty_btn")
        ) {
          Text("Add Task")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDutyDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // 6. Notice Detail Dialog
  selectedNoticeDetail?.let { notice ->
    AlertDialog(
      onDismissRequest = { selectedNoticeDetail = null },
      icon = {
        Icon(
          imageVector = Icons.Default.Campaign,
          contentDescription = null,
          tint = if (notice.isUrgent) Color(0xFFDC2626) else SchoolNavyPrimary
        )
      },
      title = {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(text = notice.title, fontWeight = FontWeight.Bold)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
              color = Color(notice.category.colorHex).copy(alpha = 0.15f),
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = notice.category.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(notice.category.colorHex),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            if (notice.isUrgent) {
              Surface(
                color = Color(0xFFDC2626).copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "Urgent",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color(0xFFDC2626),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = notice.content, style = MaterialTheme.typography.bodyMedium)
          HorizontalDivider()
          Text(
            text = "Issued by: ${notice.authorName} (${notice.authorRole.label})\nDate: ${notice.date}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(onClick = { selectedNoticeDetail = null }) {
          Text("Got It")
        }
      }
    )
  }
}
