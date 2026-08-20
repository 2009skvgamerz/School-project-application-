package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.model.*
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.SplashScreen
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

  val notifications by viewModel.notifications.collectAsState()
  val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()
  val isRefreshing by viewModel.isRefreshing.collectAsState()
  val deepLinkRoute by viewModel.deepLinkRoute.collectAsState()
  val networkState by viewModel.networkState.collectAsState()
  val isSimulatedOffline by viewModel.isSimulatedOffline.collectAsState()
  val refreshFeedbackMessage by viewModel.refreshFeedbackMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }

  val context = androidx.compose.ui.platform.LocalContext.current
  LaunchedEffect(Unit) {
    viewModel.initializeWithContext(context)
  }

  // Refresh feedback snackbar handler
  LaunchedEffect(refreshFeedbackMessage) {
    refreshFeedbackMessage?.let { msg ->
      snackbarHostState.showSnackbar(
        message = msg,
        duration = SnackbarDuration.Short
      )
      viewModel.clearRefreshFeedbackMessage()
    }
  }

  val selectedDay by viewModel.selectedDay.collectAsState()
  val selectedClassForAttendance by viewModel.selectedClassForAttendance.collectAsState()
  val selectedNoticeCategory by viewModel.selectedNoticeCategory.collectAsState()
  val pendingHomeworkCount by viewModel.pendingHomeworkCount.collectAsState()

  // Compulsory authentication on every app launch - requires successful login before accessing app
  var showSplash by remember { mutableStateOf(true) }
  var isAuthenticated by remember { mutableStateOf(false) }
  var isLoggingIn by remember { mutableStateOf(false) }
  var loginErrorMessage by remember { mutableStateOf<String?>(null) }
  var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    kotlinx.coroutines.delay(1000)
    showSplash = false
  }

  // Deep Link listener from system notification pop-ups outside app
  LaunchedEffect(deepLinkRoute) {
    deepLinkRoute?.let { route ->
      when (route.lowercase()) {
        "homework", "hw" -> currentTab = NavigationTab.HOMEWORK
        "attendance", "att" -> currentTab = NavigationTab.ATTENDANCE
        "timetable", "schedule" -> currentTab = NavigationTab.TIMETABLE
        "notices", "bulletin", "bulletins", "circular" -> currentTab = NavigationTab.NOTICES
        "profile" -> currentTab = NavigationTab.PROFILE
        "classes" -> currentTab = NavigationTab.CLASSES
        "duties" -> currentTab = NavigationTab.DUTIES
        else -> currentTab = NavigationTab.DASHBOARD
      }
      viewModel.clearDeepLinkRoute()
    }
  }

  AnimatedContent(
    targetState = when {
      showSplash -> 0
      !isAuthenticated -> 1
      else -> 2
    },
    transitionSpec = {
      fadeIn(animationSpec = tween(300, easing = LinearOutSlowInEasing)) togetherWith
        fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
    },
    label = "app_root_state_transition"
  ) { rootState ->
    when (rootState) {
      0 -> {
        SplashScreen()
      }
      1 -> {
        LoginScreen(
          errorMessage = loginErrorMessage,
          isLoading = isLoggingIn,
          networkState = networkState,
          onLogin = { username, password, _ ->
            coroutineScope.launch {
              isLoggingIn = true
              loginErrorMessage = null
              kotlinx.coroutines.delay(500)
              val result = viewModel.login(username, password)
              result.onSuccess {
                loginErrorMessage = null
                isAuthenticated = true
                currentTab = NavigationTab.DASHBOARD
                isLoggingIn = false
              }.onFailure { error ->
                loginErrorMessage = error.message ?: "Authentication failed. Please check your credentials."
                isLoggingIn = false
              }
            }
          },
          onQuickRoleLogin = { role ->
            coroutineScope.launch {
              isLoggingIn = true
              loginErrorMessage = null
              kotlinx.coroutines.delay(400)
              viewModel.switchRole(role)
              loginErrorMessage = null
              isAuthenticated = true
              currentTab = NavigationTab.DASHBOARD
              isLoggingIn = false
            }
          }
        )
      }
      2 -> {
        // Authenticated App Shell

  // Dialog States
  var showRoleSwitcherDialog by remember { mutableStateOf(false) }
  var showDeveloperConsoleSheet by remember { mutableStateOf(false) }
  var showAssignHomeworkDialog by remember { mutableStateOf(false) }
  var showCreateNoticeDialog by remember { mutableStateOf(false) }
  var showAddDutyDialog by remember { mutableStateOf(false) }
  var showNotificationCenterSheet by remember { mutableStateOf(false) }
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
    UserRole.DEVELOPER -> listOf(
      NavigationTab.DASHBOARD,
      NavigationTab.MANAGEMENT,
      NavigationTab.ATTENDANCE,
      NavigationTab.HOMEWORK,
      NavigationTab.NOTICES,
      NavigationTab.PROFILE
    )
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      Column(modifier = Modifier.fillMaxWidth()) {
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
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = "St. Joseph's",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.ExtraBold,
                      letterSpacing = 0.5.sp
                    ),
                    color = SchoolNavyPrimary
                  )
                  com.example.ui.components.NetworkStatusBarBadge(
                    networkState = networkState,
                    modifier = Modifier.testTag("top_bar_network_status_badge")
                  )
                }
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
              color = if (currentUser.role == UserRole.DEVELOPER) Color(0xFF10B981).copy(alpha = 0.15f) else SchoolNavyPrimary.copy(alpha = 0.08f),
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
                  imageVector = if (currentUser.role == UserRole.DEVELOPER) Icons.Default.Terminal else Icons.Default.SwapHoriz,
                  contentDescription = "Switch Role",
                  tint = if (currentUser.role == UserRole.DEVELOPER) Color(0xFF059669) else SchoolNavyPrimary,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = currentUser.role.label,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = if (currentUser.role == UserRole.DEVELOPER) Color(0xFF059669) else SchoolNavyPrimary
                )
              }
            }

            // Developer God Mode Terminal Action (Only visible when Developer God Mode is Active)
            if (currentUser.role == UserRole.DEVELOPER) {
              IconButton(
                onClick = { showDeveloperConsoleSheet = true },
                modifier = Modifier.testTag("dev_terminal_top_bar_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.Terminal,
                  contentDescription = "Developer God Mode Terminal",
                  tint = Color(0xFF10B981)
                )
              }
            }

            // School Notification Center Action
            IconButton(
              onClick = { showNotificationCenterSheet = true },
              modifier = Modifier.testTag("notifications_bell_btn")
            ) {
              BadgedBox(
                badge = {
                  if (unreadNotificationsCount > 0) {
                    Badge(containerColor = Color(0xFFDC2626)) {
                      Text(if (unreadNotificationsCount > 9) "9+" else "$unreadNotificationsCount")
                    }
                  }
                }
              ) {
                Icon(
                  imageVector = if (unreadNotificationsCount > 0) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                  contentDescription = "Notifications ($unreadNotificationsCount unread)",
                  tint = if (unreadNotificationsCount > 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                )
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

        // Real-Time Animated Network Status Banner (Offline / Online / Reconnecting)
        com.example.ui.components.NetworkStatusBanner(
          networkState = networkState,
          onRetryConnection = { viewModel.retryNetworkConnection() },
          onToggleSimulatedOffline = { viewModel.setSimulatedOffline(it) },
          isSimulated = isSimulatedOffline
        )
      }
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
      AnimatedContent(
        targetState = currentTab,
        transitionSpec = {
          (fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)) +
            scaleIn(initialScale = 0.98f, animationSpec = tween(220, easing = FastOutSlowInEasing)))
            .togetherWith(
              fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing))
            )
        },
        label = "tab_animated_content"
      ) { activeTab ->
        when (activeTab) {
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
                  onNoticeClick = { selectedNoticeDetail = it },
                  onOpenNotificationCenter = { showNotificationCenterSheet = true },
                  onTriggerPopUpAlert = {
                    viewModel.sendTestNotification(
                      context = context,
                      title = "Physics Laboratory Session (Class 12-A)",
                      message = "Electromagnetic induction experiments start at 10:15 AM in Lab 1.",
                      type = NotificationType.ACADEMIC,
                      actionRoute = "timetable",
                      isUrgent = true,
                      showSystemPopUp = true
                    )
                  },
                  networkState = networkState,
                  onRetryConnection = { viewModel.retryNetworkConnection() },
                  isRefreshing = isRefreshing,
                  onRefresh = { viewModel.refreshData() }
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

            UserRole.DEVELOPER -> {
              DeveloperDashboardScreen(
                viewModel = viewModel,
                onNavigateToNotices = { currentTab = NavigationTab.NOTICES },
                onNavigateToHomework = { currentTab = NavigationTab.HOMEWORK },
                onNavigateToAttendance = { currentTab = NavigationTab.ATTENDANCE },
                onNavigateToManagement = { currentTab = NavigationTab.MANAGEMENT },
                onRoleSwitched = { role ->
                  viewModel.switchRole(role)
                  currentTab = NavigationTab.DASHBOARD
                }
              )
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
            canCreateNotice = currentUser.role != UserRole.STUDENT,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshData() }
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
          ManagementScreen(classes = schoolClasses, viewModel = viewModel)
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
            onOpenNotificationCenter = {
              showNotificationCenterSheet = true
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
            },
            onOpenNotificationCenter = {
              showNotificationCenterSheet = true
            },
            networkState = networkState,
            isSimulatedOffline = isSimulatedOffline,
            onToggleSimulatedOffline = { viewModel.setSimulatedOffline(it) },
            onRetryConnection = { viewModel.retryNetworkConnection() }
          )
        }
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

          val allowedRoles = UserRole.values().filter { it != UserRole.DEVELOPER }

          allowedRoles.forEach { role ->
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
                    UserRole.DEVELOPER -> Icons.Default.Terminal
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
                      UserRole.STUDENT -> "Keerthivasan (Class 12-A)"
                      UserRole.TEACHER -> "Prof. Sarah Jenkins (Physics)"
                      UserRole.STAFF -> "Marcus Vance (Head Facilities)"
                      UserRole.ADMIN -> "Dr. Arthur Pendelton (Principal)"
                      UserRole.DEVELOPER -> "Alex Rivera (Root God Mode Master)"
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
                maxMarks = 25,
                context = context
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
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Info banner about automatic heads-up notification
          Surface(
            color = Color(0xFFEFF6FF),
            shape = RoundedCornerShape(8.dp),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF93C5FD))
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = SchoolNavyPrimary,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Publishing will immediately trigger a system-level heads-up notification outside the app.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = SchoolNavyPrimary
              )
            }
          }

          // Category Chips
          Text(text = "Select Category", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(NoticeCategory.values().filter { it != NoticeCategory.ALL }) { cat ->
              FilterChip(
                selected = category == cat,
                onClick = { category = cat },
                label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                  Surface(
                    color = Color(cat.colorHex),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(8.dp)
                  ) {}
                }
              )
            }
          }

          // Quick Templates for easy demonstration
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Templates:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SuggestionChip(
              onClick = {
                title = "Science Expo 2026 Registration"
                content = "All student project exhibits for the Science & Technology Expo must be submitted to the STEM department by Friday 4:00 PM."
                category = NoticeCategory.EVENT
                isUrgent = false
              },
              label = { Text("Expo", fontSize = 10.sp) }
            )
            SuggestionChip(
              onClick = {
                title = "Rain Holiday Announcement"
                content = "Due to heavy weather advisory, the school will remain closed tomorrow. Online revision classes will be held as per regular schedule."
                category = NoticeCategory.GENERAL
                isUrgent = true
              },
              label = { Text("Emergency", fontSize = 10.sp) }
            )
          }

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
            Column {
              Text("Mark as Urgent / Priority", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
              Text("Shows emergency red badge & max alert priority", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = isUrgent,
              onCheckedChange = { isUrgent = it },
              colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFDC2626)),
              modifier = Modifier.testTag("notice_urgent_switch")
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank() && content.isNotBlank()) {
              viewModel.publishNotice(
                title = title,
                content = content,
                category = category,
                isUrgent = isUrgent,
                context = context
              )
              showCreateNoticeDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary),
          modifier = Modifier.testTag("submit_create_notice_btn")
        ) {
          Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Post & Broadcast")
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

  // 7. Notification Center Bottom Sheet
  if (showNotificationCenterSheet) {
    NotificationCenterSheet(
      notifications = notifications,
      onDismiss = { showNotificationCenterSheet = false },
      onMarkAsRead = { notifId ->
        viewModel.markNotificationAsRead(notifId)
      },
      onMarkAllAsRead = {
        viewModel.markAllNotificationsAsRead()
      },
      onDeleteNotification = { notifId ->
        viewModel.deleteNotification(notifId)
      },
      onSendTestNotification = {
        viewModel.sendTestNotification(
          context = context,
          showSystemPopUp = true
        )
      },
      onTriggerImmediatePopUp = { title, msg, type, route ->
        viewModel.sendTestNotification(
          context = context,
          title = title,
          message = msg,
          type = type,
          actionRoute = route,
          isUrgent = true,
          showSystemPopUp = true
        )
      },
      onTriggerDelayedPopUp = { delaySecs, title, msg, type, route ->
        viewModel.triggerDelayedSystemPopUp(
          context = context,
          delaySeconds = delaySecs,
          title = title,
          message = msg,
          type = type,
          actionRoute = route
        )
      },
      onNavigateToRoute = { route ->
        showNotificationCenterSheet = false
        when (route.lowercase()) {
          "homework", "hw" -> currentTab = NavigationTab.HOMEWORK
          "attendance", "att" -> currentTab = NavigationTab.ATTENDANCE
          "timetable", "schedule" -> currentTab = NavigationTab.TIMETABLE
          "notices", "bulletin", "bulletins", "circular" -> currentTab = NavigationTab.NOTICES
          "profile" -> currentTab = NavigationTab.PROFILE
          "classes" -> currentTab = NavigationTab.CLASSES
          "duties" -> currentTab = NavigationTab.DUTIES
          else -> currentTab = NavigationTab.DASHBOARD
        }
      }
    )
  }

  // 8. Developer Console Modal Sheet
  val isDevConsoleOpenByVm by viewModel.isDeveloperConsoleOpen.collectAsState()
  if (showDeveloperConsoleSheet || isDevConsoleOpenByVm) {
    DeveloperConsoleSheet(
      viewModel = viewModel,
      onDismiss = {
        showDeveloperConsoleSheet = false
        viewModel.closeDeveloperConsole()
      },
      onSwitchToRole = { role ->
        showDeveloperConsoleSheet = false
        viewModel.closeDeveloperConsole()
        viewModel.switchRole(role)
        currentTab = NavigationTab.DASHBOARD
      }
    )
  }
      }
    }
  }
}
