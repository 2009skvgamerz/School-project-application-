package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.NavigationTab
import com.example.ui.theme.*
import com.example.util.NetworkState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * BaseDashboardScaffold
 *
 * A reusable, role-adaptive dashboard scaffold for Jetpack Compose featuring:
 * 1. Role-Adaptive Navigation Drawer (ModalNavigationDrawer) with categorised items,
 *    user profile card, badges, and quick role controls.
 * 2. Common Top App Bar with user profile avatar, status indicators, role switchers,
 *    and quick profile drop-down menu.
 * 3. Bottom Navigation Bar synchronized with the user's primary active role tabs.
 * 4. Slots for Snackbars, Network Status Banners, and Floating Action Buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseDashboardScaffold(
  currentUser: User,
  currentTab: NavigationTab,
  onTabSelected: (NavigationTab) -> Unit,
  visibleBottomTabs: List<NavigationTab>,
  networkState: NetworkState,
  unreadNotificationsCount: Int = 0,
  pendingHomeworkCount: Int = 0,
  drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
  studentProfile: StudentProfile? = null,
  teacherProfile: TeacherProfile? = null,
  staffProfile: StaffProfile? = null,
  adminProfile: AdminProfile? = null,
  developerProfile: DeveloperProfile? = null,
  onOpenRoleSwitcher: () -> Unit = {},
  onOpenNotificationCenter: () -> Unit = {},
  onOpenDeveloperTerminal: () -> Unit = {},
  onSignOut: () -> Unit = {},
  onRetryConnection: () -> Unit = {},
  onToggleSimulatedOffline: ((Boolean) -> Unit)? = null,
  isSimulatedOffline: Boolean = false,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  floatingActionButton: @Composable () -> Unit = {},
  modifier: Modifier = Modifier,
  content: @Composable (PaddingValues) -> Unit
) {
  var showProfileDropdown by remember { mutableStateOf(false) }

  val roleColor = when (currentUser.role) {
    UserRole.STUDENT -> RoleStudentColor
    UserRole.TEACHER -> RoleTeacherColor
    UserRole.STAFF -> RoleStaffColor
    UserRole.ADMIN -> RoleAdminColor
    UserRole.DEVELOPER -> Color(0xFF10B981)
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    modifier = modifier.testTag("base_dashboard_modal_drawer"),
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier
          .widthIn(max = 340.dp)
          .fillMaxHeight()
          .testTag("drawer_sheet_content"),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
      ) {
        RoleSpecificDrawerContent(
          currentUser = currentUser,
          currentTab = currentTab,
          onTabSelected = { tab ->
            onTabSelected(tab)
            coroutineScope.launch { drawerState.close() }
          },
          pendingHomeworkCount = pendingHomeworkCount,
          unreadNotificationsCount = unreadNotificationsCount,
          studentProfile = studentProfile,
          teacherProfile = teacherProfile,
          staffProfile = staffProfile,
          adminProfile = adminProfile,
          onOpenRoleSwitcher = {
            coroutineScope.launch { drawerState.close() }
            onOpenRoleSwitcher()
          },
          onOpenNotificationCenter = {
            coroutineScope.launch { drawerState.close() }
            onOpenNotificationCenter()
          },
          onOpenDeveloperTerminal = {
            coroutineScope.launch { drawerState.close() }
            onOpenDeveloperTerminal()
          },
          onSignOut = {
            coroutineScope.launch { drawerState.close() }
            onSignOut()
          }
        )
      }
    }
  ) {
    Scaffold(
      modifier = Modifier.fillMaxSize().testTag("base_dashboard_scaffold"),
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        Column(modifier = Modifier.fillMaxWidth()) {
          CommonDashboardTopAppBar(
            currentUser = currentUser,
            currentTab = currentTab,
            networkState = networkState,
            unreadNotificationsCount = unreadNotificationsCount,
            roleColor = roleColor,
            showProfileDropdown = showProfileDropdown,
            onToggleProfileDropdown = { showProfileDropdown = !showProfileDropdown },
            onDismissProfileDropdown = { showProfileDropdown = false },
            onNavigationIconClick = {
              coroutineScope.launch {
                if (drawerState.isClosed) drawerState.open() else drawerState.close()
              }
            },
            onOpenRoleSwitcher = onOpenRoleSwitcher,
            onOpenNotificationCenter = onOpenNotificationCenter,
            onOpenDeveloperTerminal = onOpenDeveloperTerminal,
            onNavigateToTab = { tab ->
              onTabSelected(tab)
              showProfileDropdown = false
            },
            onSignOut = {
              showProfileDropdown = false
              onSignOut()
            }
          )

          // Real-time Network Status Banner
          NetworkStatusBanner(
            networkState = networkState,
            onRetryConnection = onRetryConnection,
            onToggleSimulatedOffline = onToggleSimulatedOffline,
            isSimulated = isSimulatedOffline
          )
        }
      },
      bottomBar = {
        if (visibleBottomTabs.isNotEmpty()) {
          NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.testTag("base_dashboard_bottom_bar")
          ) {
            visibleBottomTabs.forEach { tab ->
              val isSelected = currentTab == tab
              NavigationBarItem(
                icon = {
                  if (tab == NavigationTab.HOMEWORK && currentUser.role == UserRole.STUDENT && pendingHomeworkCount > 0) {
                    BadgedBox(badge = { Badge { Text("$pendingHomeworkCount") } }) {
                      Icon(imageVector = tab.icon, contentDescription = tab.label)
                    }
                  } else if (tab == NavigationTab.NOTICES && unreadNotificationsCount > 0) {
                    BadgedBox(badge = { Badge { Text("$unreadNotificationsCount") } }) {
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
                onClick = { onTabSelected(tab) },
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
      },
      floatingActionButton = floatingActionButton
    ) { innerPadding ->
      content(innerPadding)
    }
  }
}

/**
 * Common Top App Bar for Dashboard
 * Standardized across all user profiles with drawer toggle, school branding,
 * network badge, role switcher, notification bell, and user avatar dropdown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonDashboardTopAppBar(
  currentUser: User,
  currentTab: NavigationTab,
  networkState: NetworkState,
  unreadNotificationsCount: Int,
  roleColor: Color,
  showProfileDropdown: Boolean,
  onToggleProfileDropdown: () -> Unit,
  onDismissProfileDropdown: () -> Unit,
  onNavigationIconClick: () -> Unit,
  onOpenRoleSwitcher: () -> Unit,
  onOpenNotificationCenter: () -> Unit,
  onOpenDeveloperTerminal: () -> Unit,
  onNavigateToTab: (NavigationTab) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  TopAppBar(
    modifier = modifier.testTag("common_dashboard_top_app_bar"),
    navigationIcon = {
      IconButton(
        onClick = onNavigationIconClick,
        modifier = Modifier.testTag("navigation_drawer_toggle_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Menu,
          contentDescription = "Open Navigation Drawer",
          tint = SchoolNavyPrimary
        )
      }
    },
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SchoolNavyPrimary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            tint = SchoolGold,
            modifier = Modifier.size(22.dp)
          )
        }

        Column(
          modifier = Modifier.weight(1f, fill = false),
          verticalArrangement = Arrangement.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "St. Joseph's",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.3.sp
              ),
              color = SchoolNavyPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false)
            )
            NetworkStatusBarBadge(
              networkState = networkState,
              modifier = Modifier.testTag("top_bar_network_status_badge")
            )
          }
          Text(
            text = currentTab.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    },
    actions = {
      // 1. Role Badge & Quick Switch Action
      Surface(
        color = if (currentUser.role == UserRole.DEVELOPER) Color(0xFF10B981).copy(alpha = 0.15f) else SchoolNavyPrimary.copy(alpha = 0.08f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .clickable { onOpenRoleSwitcher() }
          .testTag("switch_role_top_bar_btn")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = if (currentUser.role == UserRole.DEVELOPER) Icons.Default.Terminal else Icons.Default.SwapHoriz,
            contentDescription = "Switch Role",
            tint = if (currentUser.role == UserRole.DEVELOPER) Color(0xFF059669) else SchoolNavyPrimary,
            modifier = Modifier.size(15.dp)
          )
          Text(
            text = currentUser.role.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
            color = if (currentUser.role == UserRole.DEVELOPER) Color(0xFF059669) else SchoolNavyPrimary,
            maxLines = 1,
            softWrap = false
          )
        }
      }

      // 2. Developer God Mode Terminal Shortcut
      if (currentUser.role == UserRole.DEVELOPER) {
        IconButton(
          onClick = onOpenDeveloperTerminal,
          modifier = Modifier.testTag("dev_terminal_top_bar_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = "Developer God Mode Terminal",
            tint = Color(0xFF10B981)
          )
        }
      }

      // 3. Notification Center Bell Action
      IconButton(
        onClick = onOpenNotificationCenter,
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

      // 4. User Profile Avatar with Dropdown Menu
      Box {
        IconButton(
          onClick = onToggleProfileDropdown,
          modifier = Modifier.testTag("profile_top_bar_btn")
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(roleColor),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = currentUser.avatarInitials,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp
              ),
              color = Color.White
            )
          }
        }

        // Profile Quick Dropdown Menu
        DropdownMenu(
          expanded = showProfileDropdown,
          onDismissRequest = onDismissProfileDropdown,
          modifier = Modifier
            .width(240.dp)
            .testTag("profile_dropdown_menu")
        ) {
          // User Card Header
          Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text(
              text = currentUser.fullName,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = currentUser.email,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              color = roleColor.copy(alpha = 0.15f),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = currentUser.role.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = roleColor
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          HorizontalDivider()

          DropdownMenuItem(
            text = { Text("My Profile & ID Card") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            onClick = { onNavigateToTab(NavigationTab.PROFILE) },
            modifier = Modifier.testTag("dropdown_menu_profile")
          )

          DropdownMenuItem(
            text = { Text("Notifications Center") },
            leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            trailingIcon = {
              if (unreadNotificationsCount > 0) {
                Badge { Text("$unreadNotificationsCount") }
              }
            },
            onClick = {
              onDismissProfileDropdown()
              onOpenNotificationCenter()
            },
            modifier = Modifier.testTag("dropdown_menu_notifications")
          )

          DropdownMenuItem(
            text = { Text("App Settings") },
            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
            onClick = { onNavigateToTab(NavigationTab.SETTINGS) },
            modifier = Modifier.testTag("dropdown_menu_settings")
          )

          DropdownMenuItem(
            text = { Text("Switch Role") },
            leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
            onClick = {
              onDismissProfileDropdown()
              onOpenRoleSwitcher()
            },
            modifier = Modifier.testTag("dropdown_menu_switch_role")
          )

          HorizontalDivider()

          DropdownMenuItem(
            text = { Text("Sign Out", color = MaterialTheme.colorScheme.error) },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            onClick = onSignOut,
            modifier = Modifier.testTag("dropdown_menu_sign_out")
          )
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface,
      titleContentColor = MaterialTheme.colorScheme.onSurface
    )
  )
}

/**
 * Role-Specific Navigation Drawer Content
 * Dynamically organizes navigation drawers tailored to the logged-in user role
 * (Student, Teacher, Staff, Admin, Developer).
 */
@Composable
fun RoleSpecificDrawerContent(
  currentUser: User,
  currentTab: NavigationTab,
  onTabSelected: (NavigationTab) -> Unit,
  pendingHomeworkCount: Int,
  unreadNotificationsCount: Int,
  studentProfile: StudentProfile?,
  teacherProfile: TeacherProfile?,
  staffProfile: StaffProfile?,
  adminProfile: AdminProfile?,
  onOpenRoleSwitcher: () -> Unit,
  onOpenNotificationCenter: () -> Unit,
  onOpenDeveloperTerminal: () -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  val roleColor = when (currentUser.role) {
    UserRole.STUDENT -> RoleStudentColor
    UserRole.TEACHER -> RoleTeacherColor
    UserRole.STAFF -> RoleStaffColor
    UserRole.ADMIN -> RoleAdminColor
    UserRole.DEVELOPER -> Color(0xFF10B981)
  }

  val subtitleInfo = when (currentUser.role) {
    UserRole.STUDENT -> studentProfile?.let { "Class ${it.grade}-${it.section} • Roll #${it.rollNo}" } ?: "St. Joseph's Student"
    UserRole.TEACHER -> teacherProfile?.let { "${it.department} Department • ${it.employeeId}" } ?: "Faculty Member"
    UserRole.STAFF -> staffProfile?.let { "${it.department} • ${it.shiftTiming}" } ?: "Campus Operations Staff"
    UserRole.ADMIN -> adminProfile?.let { it.adminRole } ?: "Administration Bureau"
    UserRole.DEVELOPER -> "System Master Root • Dev Console Active"
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    // 1. HERO DRAWER HEADER (School Crest & User Profile)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.verticalGradient(
            listOf(SchoolNavyDark, SchoolNavyPrimary)
          )
        )
        .padding(20.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // School Identity
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(Color.White)
              .padding(2.dp),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(SchoolNavyPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = "School Crest",
                tint = SchoolGold,
                modifier = Modifier.size(24.dp)
              )
            }
          }

          Column {
            Text(
              text = "ST. JOSEPH'S SCHOOL",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
              ),
              color = Color.White
            )
            Text(
              text = "SHINE AND LET SHINE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
              ),
              color = SchoolGoldLight
            )
          }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

        // User Profile Card inside Drawer
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable { onTabSelected(NavigationTab.PROFILE) }
            .padding(10.dp)
            .testTag("drawer_profile_header_card")
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(roleColor),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = currentUser.avatarInitials,
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = currentUser.fullName,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = subtitleInfo,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.8f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
              color = roleColor,
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = currentUser.role.label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "View Profile",
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 2. ROLE-SPECIFIC MENU ITEMS
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = "PORTAL NAVIGATION",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
      )

      // Dashboard Home (Universal)
      DrawerNavigationItem(
        label = "Dashboard",
        icon = Icons.Default.Dashboard,
        isSelected = currentTab == NavigationTab.DASHBOARD,
        onClick = { onTabSelected(NavigationTab.DASHBOARD) },
        testTag = "drawer_item_dashboard"
      )

      // Role-specific items mapping
      when (currentUser.role) {
        UserRole.STUDENT -> {
          DrawerNavigationItem(
            label = "Weekly Timetable",
            icon = Icons.Default.CalendarMonth,
            isSelected = currentTab == NavigationTab.TIMETABLE,
            onClick = { onTabSelected(NavigationTab.TIMETABLE) },
            testTag = "drawer_item_timetable"
          )
          DrawerNavigationItem(
            label = "Homework & Tasks",
            icon = Icons.Default.Assignment,
            badgeCount = pendingHomeworkCount,
            badgeColor = SchoolGold,
            isSelected = currentTab == NavigationTab.HOMEWORK,
            onClick = { onTabSelected(NavigationTab.HOMEWORK) },
            testTag = "drawer_item_homework"
          )
          DrawerNavigationItem(
            label = "Attendance Record",
            icon = Icons.Default.FactCheck,
            isSelected = currentTab == NavigationTab.ATTENDANCE,
            onClick = { onTabSelected(NavigationTab.ATTENDANCE) },
            testTag = "drawer_item_attendance"
          )
        }

        UserRole.TEACHER -> {
          DrawerNavigationItem(
            label = "Daily Roll Call",
            icon = Icons.Default.FactCheck,
            isSelected = currentTab == NavigationTab.ATTENDANCE,
            onClick = { onTabSelected(NavigationTab.ATTENDANCE) },
            testTag = "drawer_item_attendance"
          )
          DrawerNavigationItem(
            label = "Homework & Grading",
            icon = Icons.Default.Assignment,
            isSelected = currentTab == NavigationTab.HOMEWORK,
            onClick = { onTabSelected(NavigationTab.HOMEWORK) },
            testTag = "drawer_item_homework"
          )
          DrawerNavigationItem(
            label = "Assigned Classes",
            icon = Icons.Default.Groups,
            isSelected = currentTab == NavigationTab.CLASSES,
            onClick = { onTabSelected(NavigationTab.CLASSES) },
            testTag = "drawer_item_classes"
          )
          DrawerNavigationItem(
            label = "Teaching Schedule",
            icon = Icons.Default.CalendarMonth,
            isSelected = currentTab == NavigationTab.TIMETABLE,
            onClick = { onTabSelected(NavigationTab.TIMETABLE) },
            testTag = "drawer_item_timetable"
          )
        }

        UserRole.STAFF -> {
          DrawerNavigationItem(
            label = "Duties & Tasks",
            icon = Icons.Default.Checklist,
            isSelected = currentTab == NavigationTab.DUTIES,
            onClick = { onTabSelected(NavigationTab.DUTIES) },
            testTag = "drawer_item_duties"
          )
          DrawerNavigationItem(
            label = "Campus Attendance",
            icon = Icons.Default.FactCheck,
            isSelected = currentTab == NavigationTab.ATTENDANCE,
            onClick = { onTabSelected(NavigationTab.ATTENDANCE) },
            testTag = "drawer_item_attendance"
          )
        }

        UserRole.ADMIN -> {
          DrawerNavigationItem(
            label = "School Directory & DB",
            icon = Icons.Default.AdminPanelSettings,
            isSelected = currentTab == NavigationTab.MANAGEMENT,
            onClick = { onTabSelected(NavigationTab.MANAGEMENT) },
            testTag = "drawer_item_management"
          )
          DrawerNavigationItem(
            label = "Classes & Sections",
            icon = Icons.Default.Groups,
            isSelected = currentTab == NavigationTab.CLASSES,
            onClick = { onTabSelected(NavigationTab.CLASSES) },
            testTag = "drawer_item_classes"
          )
          DrawerNavigationItem(
            label = "Attendance Master",
            icon = Icons.Default.FactCheck,
            isSelected = currentTab == NavigationTab.ATTENDANCE,
            onClick = { onTabSelected(NavigationTab.ATTENDANCE) },
            testTag = "drawer_item_attendance"
          )
        }

        UserRole.DEVELOPER -> {
          DrawerNavigationItem(
            label = "School Management DB",
            icon = Icons.Default.AdminPanelSettings,
            isSelected = currentTab == NavigationTab.MANAGEMENT,
            onClick = { onTabSelected(NavigationTab.MANAGEMENT) },
            testTag = "drawer_item_management"
          )
          DrawerNavigationItem(
            label = "Attendance Register",
            icon = Icons.Default.FactCheck,
            isSelected = currentTab == NavigationTab.ATTENDANCE,
            onClick = { onTabSelected(NavigationTab.ATTENDANCE) },
            testTag = "drawer_item_attendance"
          )
          DrawerNavigationItem(
            label = "Homework Management",
            icon = Icons.Default.Assignment,
            isSelected = currentTab == NavigationTab.HOMEWORK,
            onClick = { onTabSelected(NavigationTab.HOMEWORK) },
            testTag = "drawer_item_homework"
          )
          DrawerNavigationItem(
            label = "Dev God Mode Terminal",
            icon = Icons.Default.Terminal,
            isSelected = false,
            badgeText = "ROOT",
            badgeColor = Color(0xFF10B981),
            onClick = onOpenDeveloperTerminal,
            testTag = "drawer_item_dev_terminal"
          )
        }
      }

      // Universal Circulars & Notices
      DrawerNavigationItem(
        label = "Circulars & Notices",
        icon = Icons.Default.Campaign,
        isSelected = currentTab == NavigationTab.NOTICES,
        onClick = { onTabSelected(NavigationTab.NOTICES) },
        testTag = "drawer_item_notices"
      )

      Spacer(modifier = Modifier.height(6.dp))
      HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "ACCOUNT & TOOLS",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
      )

      DrawerNavigationItem(
        label = "My ID Card & Profile",
        icon = Icons.Default.Badge,
        isSelected = currentTab == NavigationTab.PROFILE,
        onClick = { onTabSelected(NavigationTab.PROFILE) },
        testTag = "drawer_item_profile"
      )

      DrawerNavigationItem(
        label = "Notification Center",
        icon = Icons.Default.Notifications,
        badgeCount = unreadNotificationsCount,
        badgeColor = SchoolAccentRed,
        isSelected = false,
        onClick = onOpenNotificationCenter,
        testTag = "drawer_item_notifications"
      )

      DrawerNavigationItem(
        label = "Switch Portal Role",
        icon = Icons.Default.SwapHoriz,
        isSelected = false,
        onClick = onOpenRoleSwitcher,
        testTag = "drawer_item_switch_role"
      )

      DrawerNavigationItem(
        label = "Settings & Preferences",
        icon = Icons.Default.Settings,
        isSelected = currentTab == NavigationTab.SETTINGS,
        onClick = { onTabSelected(NavigationTab.SETTINGS) },
        testTag = "drawer_item_settings"
      )
    }

    Spacer(modifier = Modifier.weight(1f, fill = false))
    Spacer(modifier = Modifier.height(16.dp))

    // 3. FOOTER ACTIONS
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = onSignOut,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("drawer_sign_out_btn"),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.errorContainer,
          contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Logout,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Sign Out (${currentUser.username})",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
      }

      Text(
        text = "St. Joseph's SMS Prototype • v2.4.0 (Room DB)",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.align(Alignment.CenterHorizontally)
      )
    }
  }
}

/**
 * Individual Drawer Item with support for icon, label, selection indicator, and count badge.
 */
@Composable
fun DrawerNavigationItem(
  label: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  badgeCount: Int = 0,
  badgeText: String? = null,
  badgeColor: Color = SchoolNavyPrimary,
  testTag: String = ""
) {
  NavigationDrawerItem(
    label = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        )
        if (badgeCount > 0) {
          Surface(
            color = badgeColor,
            shape = CircleShape
          ) {
            Text(
              text = if (badgeCount > 99) "99+" else "$badgeCount",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              ),
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
            )
          }
        } else if (badgeText != null) {
          Surface(
            color = badgeColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = badgeColor
              ),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }
    },
    icon = {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isSelected) SchoolNavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
      )
    },
    selected = isSelected,
    onClick = onClick,
    colors = NavigationDrawerItemDefaults.colors(
      selectedContainerColor = SchoolNavyPrimary.copy(alpha = 0.12f),
      selectedIconColor = SchoolNavyPrimary,
      selectedTextColor = SchoolNavyPrimary,
      unselectedContainerColor = Color.Transparent,
      unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
      unselectedTextColor = MaterialTheme.colorScheme.onSurface
    ),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier.testTag(testTag)
  )
}
