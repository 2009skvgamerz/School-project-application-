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
  CALENDAR("Calendar", Icons.Default.EventNote),
  BUS_TRACKING("Bus Live", Icons.Default.DirectionsBus),
  ANNOUNCEMENTS("Broadcasts", Icons.Default.Campaign),
  DIRECTORY("Directory", Icons.Default.ContactPhone),
  HOMEWORK("Homework", Icons.Default.Assignment),
  ATTENDANCE("Attendance", Icons.Default.FactCheck),
  NOTICES("Circulars", Icons.Default.Article),
  DUTIES("Duties", Icons.Default.Checklist),
  CLASSES("Classes", Icons.Default.Groups),
  MANAGEMENT("Admin DB", Icons.Default.AdminPanelSettings),
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
  val driverProfile by viewModel.driverProfile.collectAsState()

  val notices by viewModel.notices.collectAsState()
  val schoolEvents by viewModel.schoolEvents.collectAsState()
  val homeworks by viewModel.homeworks.collectAsState()
  val timetables by viewModel.timetables.collectAsState()
  val schoolClasses by viewModel.schoolClasses.collectAsState()
  val staffDuties by viewModel.staffDuties.collectAsState()
  val attendanceRecords by viewModel.attendanceRecords.collectAsState()
  val roomAttendanceRecords by viewModel.roomAttendanceRecords.collectAsState()

  // Wave 1 ERP Collected States
  val calendarEvents by viewModel.calendarEvents.collectAsState()
  val busRoutes by viewModel.busRoutes.collectAsState()
  val selectedBusRouteId by viewModel.selectedBusRouteId.collectAsState()
  val announcements by viewModel.announcements.collectAsState()
  val sanitizedDirectoryContacts by viewModel.sanitizedDirectoryContacts.collectAsState()

  val notifications by viewModel.notifications.collectAsState()
  val roomNotifications by viewModel.roomNotifications.collectAsState()
  val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()
  val isRefreshing by viewModel.isRefreshing.collectAsState()
  val deepLinkRoute by viewModel.deepLinkRoute.collectAsState()
  val deepLinkTargetId by viewModel.deepLinkTargetId.collectAsState()
  val networkState by viewModel.networkState.collectAsState()
  val isSimulatedOffline by viewModel.isSimulatedOffline.collectAsState()
  val cloudSyncInfo by viewModel.cloudSyncInfo.collectAsState()
  val refreshFeedbackMessage by viewModel.refreshFeedbackMessage.collectAsState()
  val fcmDeviceToken by viewModel.fcmDeviceToken.collectAsState()
  val fcmSubscribedTopics by viewModel.fcmSubscribedTopics.collectAsState()

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

  // Compulsory Notification Permission Setup on App Launch / Installation
  NotificationPermissionHandler()

  // Persistent authentication across app restarts - stays logged in until explicit logout
  var showSplash by remember { mutableStateOf(true) }
  val isAuthenticated by viewModel.isAuthenticated.collectAsState()
  var isLoggingIn by remember { mutableStateOf(false) }
  var loginErrorMessage by remember { mutableStateOf<String?>(null) }
  var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    kotlinx.coroutines.delay(600)
    showSplash = false
  }

  // Deep Link listener from system notification pop-ups outside app
  LaunchedEffect(deepLinkRoute) {
    deepLinkRoute?.let { route ->
      when (route.lowercase()) {
        "homework", "hw" -> currentTab = NavigationTab.HOMEWORK
        "attendance", "att" -> currentTab = NavigationTab.ATTENDANCE
        "timetable", "schedule" -> currentTab = NavigationTab.TIMETABLE
        "calendar", "events", "exam" -> currentTab = NavigationTab.CALENDAR
        "bus", "bustracking", "transport" -> currentTab = NavigationTab.BUS_TRACKING
        "announcements", "broadcast", "broadcasts" -> currentTab = NavigationTab.ANNOUNCEMENTS
        "directory", "contacts", "sos" -> currentTab = NavigationTab.DIRECTORY
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
      (!isAuthenticated || isLoggingIn) -> 1
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
          onLogin = { username, password, role ->
            coroutineScope.launch {
              isLoggingIn = true
              loginErrorMessage = null
              val localResult = viewModel.login(username, password)
              if (localResult.isSuccess) {
                kotlinx.coroutines.delay(1400)
                loginErrorMessage = null
                currentTab = NavigationTab.DASHBOARD
                isLoggingIn = false
              } else {
                viewModel.signInWithFirebaseEmail(
                  email = username,
                  pass = password,
                  role = role,
                  onSuccess = {
                    coroutineScope.launch {
                      kotlinx.coroutines.delay(1400)
                      loginErrorMessage = null
                      currentTab = NavigationTab.DASHBOARD
                      isLoggingIn = false
                    }
                  },
                  onError = { errorMsg ->
                    loginErrorMessage = errorMsg
                    isLoggingIn = false
                  }
                )
              }
            }
          },
          onQuickRoleLogin = { role ->
            coroutineScope.launch {
              isLoggingIn = true
              loginErrorMessage = null
              viewModel.switchRole(role)
              kotlinx.coroutines.delay(1400)
              loginErrorMessage = null
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
    UserRole.DRIVER -> listOf(
      NavigationTab.DASHBOARD,
      NavigationTab.BUS_TRACKING,
      NavigationTab.ANNOUNCEMENTS,
      NavigationTab.DIRECTORY,
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

  // Gracefully switch back to Dashboard if the active tab is not accessible in the new user role
  LaunchedEffect(currentUser.role) {
    if (currentTab != NavigationTab.SETTINGS && !visibleTabs.contains(currentTab)) {
      currentTab = NavigationTab.DASHBOARD
    }
  }

    BaseDashboardScaffold(
      currentUser = currentUser,
      currentTab = currentTab,
      onTabSelected = { currentTab = it },
      visibleBottomTabs = visibleTabs,
      networkState = networkState,
      unreadNotificationsCount = unreadNotificationsCount,
      pendingHomeworkCount = pendingHomeworkCount,
      studentProfile = studentProfile,
      teacherProfile = teacherProfile,
      staffProfile = staffProfile,
      adminProfile = adminProfile,
      onOpenRoleSwitcher = { showRoleSwitcherDialog = true },
      onOpenNotificationCenter = { showNotificationCenterSheet = true },
      onOpenDeveloperTerminal = { showDeveloperConsoleSheet = true },
      onSignOut = {
        viewModel.logout()
        loginErrorMessage = null
      },
      onRetryConnection = { viewModel.retryNetworkConnection() },
      onToggleSimulatedOffline = { viewModel.setSimulatedOffline(it) },
      isSimulatedOffline = isSimulatedOffline,
      cloudSyncInfo = cloudSyncInfo,
      snackbarHostState = snackbarHostState
    ) { innerPadding ->
      ConditionalPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshData() },
        enabled = currentTab != NavigationTab.SETTINGS,
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        AnimatedContent(
          targetState = currentTab,
          transitionSpec = {
            val forward = targetState.ordinal > initialState.ordinal
            if (forward) {
              (slideInHorizontally(
                initialOffsetX = { fullWidth -> (fullWidth * 0.18f).toInt() },
                animationSpec = tween(320, easing = FastOutSlowInEasing)
              ) + fadeIn(
                animationSpec = tween(300, easing = LinearOutSlowInEasing)
              ) + scaleIn(
                initialScale = 0.96f,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
              )).togetherWith(
                slideOutHorizontally(
                  targetOffsetX = { fullWidth -> -(fullWidth * 0.18f).toInt() },
                  animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) + fadeOut(
                  animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + scaleOut(
                  targetScale = 0.98f,
                  animationSpec = tween(280, easing = FastOutSlowInEasing)
                )
              )
            } else {
              (slideInHorizontally(
                initialOffsetX = { fullWidth -> -(fullWidth * 0.18f).toInt() },
                animationSpec = tween(320, easing = FastOutSlowInEasing)
              ) + fadeIn(
                animationSpec = tween(300, easing = LinearOutSlowInEasing)
              ) + scaleIn(
                initialScale = 0.96f,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
              )).togetherWith(
                slideOutHorizontally(
                  targetOffsetX = { fullWidth -> (fullWidth * 0.18f).toInt() },
                  animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) + fadeOut(
                  animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + scaleOut(
                  targetScale = 0.98f,
                  animationSpec = tween(280, easing = FastOutSlowInEasing)
                )
              )
            }
          },
          label = "dashboard_screens_directional_transition"
        ) { activeTab ->
        when (activeTab) {
          NavigationTab.DASHBOARD -> {
            AnimatedContent(
              targetState = currentUser.role,
              transitionSpec = {
                (fadeIn(animationSpec = tween(350, easing = LinearOutSlowInEasing)) +
                  scaleIn(initialScale = 0.94f, animationSpec = tween(350, easing = FastOutSlowInEasing)))
                  .togetherWith(
                    fadeOut(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                      scaleOut(targetScale = 1.04f, animationSpec = tween(220, easing = FastOutSlowInEasing))
                  )
              },
              label = "role_dashboard_animated_transition"
            ) { activeRole ->
              when (activeRole) {
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
                      onNavigateToCalendar = { currentTab = NavigationTab.CALENDAR },
                      onNavigateToBusTracking = { currentTab = NavigationTab.BUS_TRACKING },
                      onNavigateToAnnouncements = { currentTab = NavigationTab.ANNOUNCEMENTS },
                      onNavigateToDirectory = { currentTab = NavigationTab.DIRECTORY },
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

                UserRole.DRIVER -> {
                  driverProfile?.let { prof ->
                    DriverDashboardScreen(
                      driverProfile = prof,
                      busRoutes = busRoutes,
                      selectedRouteId = selectedBusRouteId,
                      onSelectRoute = { viewModel.selectBusRoute(it) },
                      onAdvanceStop = { viewModel.advanceBusToNextStop(it) },
                      onUpdatePassengerStatus = { routeId, stopId, passengerId, status ->
                        viewModel.updatePassengerBoardingStatus(routeId, stopId, passengerId, status)
                      },
                      onMarkAllBoarded = { routeId, stopId ->
                        viewModel.markAllStopPassengersBoarded(routeId, stopId)
                      },
                      onAddTemporaryStop = { routeId, stopName, time, lat, lng, note ->
                        viewModel.addTemporaryDetourStop(routeId, stopName, time, lat, lng, note)
                      },
                      onSkipStop = { routeId, stopId, reason ->
                        viewModel.skipStopWithReason(routeId, stopId, reason)
                      },
                      onBroadcastDelay = { routeId, delayMins, reason ->
                        viewModel.broadcastDriverDelayAlert(routeId, delayMins, reason)
                      },
                      onUpdateVehicleAndRoute = { driverId, busNo, busReg, routeId ->
                        viewModel.updateDriverVehicleAndRoute(driverId, busNo, busReg, routeId)
                      },
                      onOpenLiveMap = { currentTab = NavigationTab.BUS_TRACKING }
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
            currentUser = currentUser,
            studentProfile = studentProfile,
            teacherProfile = teacherProfile,
            adminProfile = adminProfile,
            classes = schoolClasses,
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
            initialSelectedNoticeId = deepLinkTargetId,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshData() }
          )
        }

        NavigationTab.CALENDAR -> {
          CalendarScreen(
            events = calendarEvents,
            userRole = currentUser.role,
            onAddEvent = { viewModel.addCalendarEvent(it) },
            onToggleReminder = { viewModel.toggleCalendarEventReminder(it) },
            initialSelectedEventId = deepLinkTargetId
          )
        }

        NavigationTab.BUS_TRACKING -> {
          BusTrackingScreen(
            routes = busRoutes,
            selectedRouteId = selectedBusRouteId,
            studentProfile = studentProfile,
            userRole = currentUser.role,
            onSelectRoute = { viewModel.selectBusRoute(it) },
            onSimulateMovement = { viewModel.simulateBusMovement(it) }
          )
        }

        NavigationTab.ANNOUNCEMENTS -> {
          AnnouncementsScreen(
            announcements = announcements,
            userRole = currentUser.role,
            onAddAnnouncement = { viewModel.addAnnouncement(it) },
            onAcknowledgeAnnouncement = { viewModel.acknowledgeAnnouncement(it) },
            initialSelectedAnnouncementId = deepLinkTargetId
          )
        }

        NavigationTab.DIRECTORY -> {
          DirectoryScreen(
            contacts = sanitizedDirectoryContacts,
            userRole = currentUser.role
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
            driverProfile = driverProfile,
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
      icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
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
              color = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                    UserRole.DRIVER -> Icons.Default.DirectionsBus
                    UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                    UserRole.DEVELOPER -> Icons.Default.Terminal
                  },
                  contentDescription = null,
                  tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = role.label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = when(role) {
                      UserRole.STUDENT -> "Keerthivasan (Class 12-A)"
                      UserRole.TEACHER -> "Prof. Sarah Jenkins (Physics)"
                      UserRole.STAFF -> "Marcus Vance (Head Facilities)"
                      UserRole.DRIVER -> "Ramesh Kumar (Bus Pilot #12)"
                      UserRole.ADMIN -> "Dr. Arthur Pendelton (Principal)"
                      UserRole.DEVELOPER -> "Alex Rivera (Root God Mode Master)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                if (isCurrent) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
      icon = { Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
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
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
      icon = { Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
      title = { Text("Broadcast New Circular", fontWeight = FontWeight.Bold) },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Info banner about automatic heads-up notification
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(8.dp),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Publishing will immediately trigger a system-level heads-up notification outside the app.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.primary
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
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
      icon = { Icon(Icons.Default.Checklist, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
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
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
          Text(text = "Subject: ${hw.subjectName}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
          tint = if (notice.isUrgent) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
        )
      },
      title = {
        Text(notice.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
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
              color = MaterialTheme.colorScheme.primary
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
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
      roomNotifications = roomNotifications,
      fcmDeviceToken = fcmDeviceToken,
      subscribedTopics = fcmSubscribedTopics,
      onToggleTopic = { topic ->
        viewModel.toggleTopicSubscription(topic)
      },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionalPullToRefreshBox(
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  if (enabled) {
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
      isRefreshing = isRefreshing,
      onRefresh = onRefresh,
      modifier = modifier,
      content = content
    )
  } else {
    Box(
      modifier = modifier,
      contentAlignment = Alignment.TopStart,
      content = content
    )
  }
}
