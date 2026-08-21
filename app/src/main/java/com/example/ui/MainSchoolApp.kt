package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.UserRole
import com.example.ui.auth.LoginScreen
import com.example.ui.dashboard.*
import com.example.ui.screens.*
import com.example.ui.theme.StJosephTheme
import com.example.viewmodel.AuthenticationViewModel
import com.example.viewmodel.SchoolViewModel

sealed class AppTab(val route: String, val label: String, val icon: ImageVector) {
    object Home : AppTab("home", "Home", Icons.Default.Home)
    object Attendance : AppTab("attendance", "Attendance", Icons.Default.FactCheck)
    object Classes : AppTab("classes", "Classes", Icons.Default.Groups)
    object Homework : AppTab("homework", "Homework", Icons.Default.MenuBook)
    object Timetable : AppTab("timetable", "Timetable", Icons.Default.CalendarMonth)
    object Notices : AppTab("notices", "Circulars", Icons.Default.Campaign)
    object Fees : AppTab("fees", "Fees", Icons.Default.AccountBalanceWallet)
    object Duties : AppTab("duties", "Duties", Icons.Default.Assignment)
    object Management : AppTab("management", "Gov.", Icons.Default.AdminPanelSettings)
    object Settings : AppTab("settings", "Settings", Icons.Default.Settings)
    object Profile : AppTab("profile", "Profile", Icons.Default.AccountCircle)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSchoolApp(
    authViewModel: AuthenticationViewModel = viewModel(),
    schoolViewModel: SchoolViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val themeMode by authViewModel.themeMode.collectAsState()
    var currentTab by remember { mutableStateOf<AppTab>(AppTab.Home) }

    StJosephTheme(themeMode = themeMode) {
        val user = currentUser
        if (user == null) {
            LoginScreen(authViewModel = authViewModel)
        } else {
            val tabs = remember(user.role) {
                when (user.role) {
                    UserRole.STUDENT -> listOf(
                        AppTab.Home,
                        AppTab.Attendance,
                        AppTab.Homework,
                        AppTab.Timetable,
                        AppTab.Profile
                    )
                    UserRole.TEACHER -> listOf(
                        AppTab.Home,
                        AppTab.Attendance,
                        AppTab.Classes,
                        AppTab.Homework,
                        AppTab.Timetable,
                        AppTab.Profile
                    )
                    UserRole.STAFF -> listOf(
                        AppTab.Home,
                        AppTab.Duties,
                        AppTab.Notices,
                        AppTab.Settings,
                        AppTab.Profile
                    )
                    UserRole.ADMIN -> listOf(
                        AppTab.Home,
                        AppTab.Attendance,
                        AppTab.Classes,
                        AppTab.Management,
                        AppTab.Settings,
                        AppTab.Profile
                    )
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "St. Joseph's H.S.S.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${user.fullName} (${user.role.displayName})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { currentTab = AppTab.Profile }) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab.route == tab.route,
                                onClick = { currentTab = tab },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentTab) {
                        AppTab.Home -> {
                            when (user.role) {
                                UserRole.STUDENT -> StudentDashboard(
                                    user = user,
                                    schoolViewModel = schoolViewModel,
                                    onNavigateToAttendance = { currentTab = AppTab.Attendance },
                                    onNavigateToHomework = { currentTab = AppTab.Homework },
                                    onNavigateToTimetable = { currentTab = AppTab.Timetable }
                                )
                                UserRole.TEACHER -> TeacherDashboard(
                                    user = user,
                                    schoolViewModel = schoolViewModel,
                                    onNavigateToAttendance = { currentTab = AppTab.Attendance },
                                    onNavigateToClasses = { currentTab = AppTab.Classes },
                                    onNavigateToTimetable = { currentTab = AppTab.Timetable }
                                )
                                UserRole.STAFF -> StaffDashboard(
                                    user = user,
                                    schoolViewModel = schoolViewModel,
                                    onNavigateToDuties = { currentTab = AppTab.Duties }
                                )
                                UserRole.ADMIN -> AdminDashboard(
                                    user = user,
                                    schoolViewModel = schoolViewModel,
                                    onNavigateToAttendance = { currentTab = AppTab.Attendance },
                                    onNavigateToClasses = { currentTab = AppTab.Classes },
                                    onNavigateToManagement = { currentTab = AppTab.Management }
                                )
                            }
                        }
                        AppTab.Attendance -> AttendanceScreen(user = user, schoolViewModel = schoolViewModel)
                        AppTab.Classes -> ClassesScreen(schoolViewModel = schoolViewModel)
                        AppTab.Homework -> HomeworkScreen(schoolViewModel = schoolViewModel)
                        AppTab.Timetable -> TimetableScreen(schoolViewModel = schoolViewModel)
                        AppTab.Notices -> NoticesScreen(schoolViewModel = schoolViewModel)
                        AppTab.Fees -> FeesScreen(schoolViewModel = schoolViewModel)
                        AppTab.Duties -> DutiesScreen(schoolViewModel = schoolViewModel)
                        AppTab.Management -> ManagementScreen(schoolViewModel = schoolViewModel)
                        AppTab.Settings -> SettingsScreen(authViewModel = authViewModel)
                        AppTab.Profile -> ProfileScreen(user = user, authViewModel = authViewModel)
                    }
                }
            }
        }
    }
}
