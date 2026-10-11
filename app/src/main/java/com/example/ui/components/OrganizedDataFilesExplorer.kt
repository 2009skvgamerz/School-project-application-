package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.viewmodel.SchoolViewModel

/**
 * OrganizedDataFilesExplorer — Interactive, categorized inspector for all application
 * data files (`com.example.model`, `com.example.data.seed`, `com.example.data.local`,
 * and `com.example.data.firestore`) with live record counts, schema inspection, and
 * sample record previews.
 */
@Composable
fun OrganizedDataFilesExplorer(
  viewModel: SchoolViewModel,
  onNavigateToTab: ((NavigationTab) -> Unit)? = null,
  isDarkThemeStyle: Boolean = true,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current

  val systemUsers by viewModel.systemUsers.collectAsState()
  val notices by viewModel.notices.collectAsState()
  val homeworks by viewModel.homeworks.collectAsState()
  val timetables by viewModel.timetables.collectAsState()
  val attendanceRecords by viewModel.attendanceRecords.collectAsState()
  val roomAttendanceRecords by viewModel.roomAttendanceRecords.collectAsState()
  val duties by viewModel.staffDuties.collectAsState()
  val schoolEvents by viewModel.schoolEvents.collectAsState()
  val schoolClasses by viewModel.schoolClasses.collectAsState()
  val notifications by viewModel.notifications.collectAsState()
  val calendarEvents by viewModel.calendarEvents.collectAsState()
  val busRoutes by viewModel.busRoutes.collectAsState()
  val announcements by viewModel.announcements.collectAsState()
  val directoryContacts by viewModel.directoryContacts.collectAsState()
  val allFeedback by viewModel.allFeedbackSubmissions.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var selectedGroup by remember { mutableStateOf<DataLayerGroup?>(null) }
  var expandedFileIds by remember { mutableStateOf(setOf("file_user_models", "file_academic_seed")) }

  val catalogEntries = remember(
    systemUsers.size,
    notices.size,
    homeworks.size,
    timetables.size,
    attendanceRecords.size,
    roomAttendanceRecords.size,
    duties.size,
    schoolEvents.size,
    schoolClasses.size,
    notifications.size,
    calendarEvents.size,
    busRoutes.size,
    announcements.size,
    directoryContacts.size,
    allFeedback.size
  ) {
    listOf(
      // 1. DOMAIN MODELS
      OrganizedDataFileEntry(
        id = "file_user_models",
        fileName = "UserProfileModels.kt",
        packagePath = "app/src/main/java/com/example/model/UserProfileModels.kt",
        group = DataLayerGroup.DOMAIN_MODELS,
        summary = "Role definitions, core User identity, 6 role-specific profiles, and master SystemUserRecord schema.",
        primaryEntities = listOf("UserRole", "User", "StudentProfile", "TeacherProfile", "StaffProfile", "DriverProfile", "AdminProfile", "DeveloperProfile", "SystemUserRecord"),
        liveRecordCount = systemUsers.size,
        storageEngine = "Kotlin Data Classes",
        syncMode = "In-Memory + Session + Firestore",
        schemaFields = listOf(
          DataFileSchemaField("id", "String", "Unique institutional record identifier (e.g. usr_student_01)"),
          DataFileSchemaField("username", "String", "Portal login handle (student01, teacher01, dev)"),
          DataFileSchemaField("role", "UserRole", "STUDENT, TEACHER, STAFF, DRIVER, ADMIN, DEVELOPER"),
          DataFileSchemaField("identifier", "String", "Admission number (SJ-2024-1201) or Employee ID"),
          DataFileSchemaField("departmentOrGrade", "String", "Assigned academic grade or faculty department")
        ),
        sampleRecordPreview = systemUsers.take(3).map { "${it.fullName} (${it.role.displayName})" to "ID: ${it.identifier} • ${it.email}" },
        linkedTab = NavigationTab.MANAGEMENT
      ),
      OrganizedDataFileEntry(
        id = "file_academic_models",
        fileName = "AcademicModels.kt",
        packagePath = "app/src/main/java/com/example/model/AcademicModels.kt",
        group = DataLayerGroup.DOMAIN_MODELS,
        summary = "Academic classes, subjects, weekly timetable periods, roll-call attendance, homework, notices, duties & notifications.",
        primaryEntities = listOf("SchoolClass", "TimetableEntry", "AttendanceRecord", "Homework", "Notice", "DutyTask", "AppNotification"),
        liveRecordCount = attendanceRecords.size + homeworks.size + notices.size + timetables.size,
        storageEngine = "Kotlin Data Classes",
        syncMode = "Real-Time StateFlow + Room + Firestore",
        schemaFields = listOf(
          DataFileSchemaField("AttendanceRecord", "Entity", "id, studentId, studentName, rollNo, className, status (FD/HD/OD/AB)"),
          DataFileSchemaField("Homework", "Entity", "id, title, subjectName, className, dueDate, status, maxMarks"),
          DataFileSchemaField("TimetableEntry", "Entity", "id, day, periodNumber, startTime, endTime, subjectName, roomNo"),
          DataFileSchemaField("Notice", "Entity", "id, title, category, publisherName, date, isUrgent")
        ),
        sampleRecordPreview = listOf(
          "Classes" to "${schoolClasses.size} active homerooms (${schoolClasses.firstOrNull()?.name ?: "Class 12-A"})",
          "Attendance" to "${attendanceRecords.size} daily student status records",
          "Homework & Timetable" to "${homeworks.size} assignments • ${timetables.size} weekly periods"
        ),
        linkedTab = NavigationTab.ATTENDANCE
      ),
      OrganizedDataFileEntry(
        id = "file_campus_ops_models",
        fileName = "CampusOperationsModels.kt",
        packagePath = "app/src/main/java/com/example/model/CampusOperationsModels.kt",
        group = DataLayerGroup.DOMAIN_MODELS,
        summary = "Wave 1 ERP schemas for Academic Calendar, GPS Bus Fleet Routes & Passengers, Emergency Broadcasts, and Role-Gated Directory.",
        primaryEntities = listOf("CalendarEvent", "BusRoute", "BusStop", "BusPassenger", "SchoolAnnouncement", "DirectoryContact", "CloudSyncInfo"),
        liveRecordCount = calendarEvents.size + busRoutes.size + announcements.size + directoryContacts.size,
        storageEngine = "Kotlin Data Classes",
        syncMode = "Live Telemetry + Privacy Filter",
        schemaFields = listOf(
          DataFileSchemaField("BusRoute", "Entity", "routeNumber, busRegistration, currentLatitude, currentLongitude, stops"),
          DataFileSchemaField("SchoolAnnouncement", "Entity", "title, priority, targetAudience, isEmergency, acknowledgmentsCount"),
          DataFileSchemaField("DirectoryContact", "Entity", "name, category, phoneNumber (auto-redacted for Student role)"),
          DataFileSchemaField("CalendarEvent", "Entity", "title, date, time, location, category, isHoliday, hasReminder")
        ),
        sampleRecordPreview = listOf(
          "Bus Fleet" to "${busRoutes.size} GPS routes (${busRoutes.sumOf { it.studentsOnboard }} students onboard)",
          "Directory" to "${directoryContacts.size} contacts across 6 campus departments",
          "Calendar & Alerts" to "${calendarEvents.size} events • ${announcements.size} broadcasts"
        ),
        linkedTab = NavigationTab.BUS_TRACKING
      ),
      OrganizedDataFileEntry(
        id = "file_analytics_models",
        fileName = "StudentAnalyticsModels.kt",
        packagePath = "app/src/main/java/com/example/model/StudentAnalyticsModels.kt",
        group = DataLayerGroup.DOMAIN_MODELS,
        summary = "Subject performance breakdowns, exam trend series, GPA trajectories, and monthly attendance analytics.",
        primaryEntities = listOf("StudentAnalyticsData", "SubjectGradeMetric", "ExamTrendPoint", "MonthlyAttendanceMetric"),
        liveRecordCount = 18,
        storageEngine = "Analytics Computed Models",
        syncMode = "Derived State",
        schemaFields = listOf(
          DataFileSchemaField("SubjectGradeMetric", "Model", "subject, score, classAverage, gradeLetter, teacherRemarks"),
          DataFileSchemaField("MonthlyAttendanceMetric", "Model", "monthName, attendanceRate, presentDays, totalWorkingDays")
        ),
        sampleRecordPreview = listOf(
          "Student Dashboard" to "Interactive SpringProgressRing & subject bar charts",
          "Eligibility Threshold" to "75% minimum board exam attendance rule"
        ),
        linkedTab = NavigationTab.DASHBOARD
      ),

      // 2. SEED DATASETS
      OrganizedDataFileEntry(
        id = "file_users_seed",
        fileName = "UsersAndDirectorySeedData.kt",
        packagePath = "app/src/main/java/com/example/data/seed/UsersAndDirectorySeedData.kt",
        group = DataLayerGroup.SEED_DATASETS,
        summary = "Pre-configured Science Expo demo accounts, 12 master system users across all 6 roles, and 22 role-gated directory contacts.",
        primaryEntities = listOf("demoUsers (6)", "initialSystemUsers (${systemUsers.size})", "initialDirectoryContacts (${directoryContacts.size})"),
        liveRecordCount = systemUsers.size + directoryContacts.size,
        storageEngine = "Modular Kotlin Seed Object",
        syncMode = "Factory Reset & Cloud Seed Source",
        schemaFields = listOf(
          DataFileSchemaField("demoUsers", "List<DemoAccount>", "student01, teacher01, staff01, driver01, admin01, dev"),
          DataFileSchemaField("initialSystemUsers", "List<SystemUserRecord>", "Editable master records for Developer & Admin"),
          DataFileSchemaField("initialDirectoryContacts", "List<DirectoryContact>", "Helplines, Faculty, Admin, Staff, Transport, Students")
        ),
        sampleRecordPreview = directoryContacts.take(3).map { "${it.name} (${it.category.label})" to it.phoneNumber },
        linkedTab = NavigationTab.DIRECTORY
      ),
      OrganizedDataFileEntry(
        id = "file_academic_seed",
        fileName = "AcademicSeedData.kt",
        packagePath = "app/src/main/java/com/example/data/seed/AcademicSeedData.kt",
        group = DataLayerGroup.SEED_DATASETS,
        summary = "Organized seed records for notifications, school circulars, homework assignments, Mon-Sat timetable, multi-class attendance, and staff duties.",
        primaryEntities = listOf("initialNotices (${notices.size})", "initialHomeworks (${homeworks.size})", "initialTimetable (${timetables.size})", "initialAttendanceRecords (${attendanceRecords.size})"),
        liveRecordCount = notices.size + homeworks.size + timetables.size + attendanceRecords.size + duties.size,
        storageEngine = "Modular Kotlin Seed Object",
        syncMode = "Factory Reset & Cloud Seed Source",
        schemaFields = listOf(
          DataFileSchemaField("initialAttendanceRecords", "List<AttendanceRecord>", "30 students across Class 12-A, 10-A, 10-B, 9-A, 11-Science"),
          DataFileSchemaField("initialTimetable", "List<TimetableEntry>", "25 periods across Monday through Saturday"),
          DataFileSchemaField("initialHomeworks", "List<Homework>", "Physics, Mathematics, Chemistry & Computer Science tasks")
        ),
        sampleRecordPreview = homeworks.take(3).map { "${it.subjectName}: ${it.title}" to "${it.className} • Due ${it.dueDate}" },
        linkedTab = NavigationTab.HOMEWORK
      ),
      OrganizedDataFileEntry(
        id = "file_transport_seed",
        fileName = "TransportAndEventsSeedData.kt",
        packagePath = "app/src/main/java/com/example/data/seed/TransportAndEventsSeedData.kt",
        group = DataLayerGroup.SEED_DATASETS,
        summary = "GPS coordinates & passenger manifests for 4 Hosur/SIPCOT bus routes, 7 academic calendar events, and 4 school-wide announcements.",
        primaryEntities = listOf("initialBusRoutes (${busRoutes.size})", "initialCalendarEvents (${calendarEvents.size})", "initialAnnouncements (${announcements.size})"),
        liveRecordCount = busRoutes.size + calendarEvents.size + announcements.size,
        storageEngine = "Modular Kotlin Seed Object",
        syncMode = "Live Route Simulation & Cloud Sync",
        schemaFields = listOf(
          DataFileSchemaField("initialBusRoutes", "List<BusRoute>", "Routes #12, #05, #08, #15 with stop coordinates & passenger statuses"),
          DataFileSchemaField("initialCalendarEvents", "List<CalendarEvent>", "Holidays, Science Expo, PTM, Mid-Term Exams, Sports"),
          DataFileSchemaField("initialAnnouncements", "List<SchoolAnnouncement>", "Weather alerts, Expo layout, PTM booking, Route detours")
        ),
        sampleRecordPreview = busRoutes.map { "${it.routeNumber}: ${it.routeName}" to "${it.status.label} • ${it.studentsOnboard}/${it.capacity} onboard" },
        linkedTab = NavigationTab.CALENDAR
      ),

      // 3. LOCAL STORAGE (ROOM SQLITE & PREFERENCES)
      OrganizedDataFileEntry(
        id = "file_room_db",
        fileName = "AppDatabase.kt & Entities",
        packagePath = "app/src/main/java/com/example/data/local/AppDatabase.kt",
        group = DataLayerGroup.LOCAL_STORAGE,
        summary = "Room SQLite database (v2) storing offline-first attendance records, notifications history, students, and teachers with reactive Flow DAOs.",
        primaryEntities = listOf("AttendanceEntity", "NotificationEntity", "StudentEntity", "TeacherEntity", "AttendanceDao", "NotificationDao"),
        liveRecordCount = roomAttendanceRecords.size + notifications.size,
        storageEngine = "Room SQLite Database (st_josephs_school_db)",
        syncMode = "Offline-First Persistent Storage",
        schemaFields = listOf(
          DataFileSchemaField("attendance_records", "SQLite Table", "Primary key: id • Indexed by className & studentId"),
          DataFileSchemaField("notifications", "SQLite Table", "Persistent notification center feed & unread status"),
          DataFileSchemaField("students / teachers", "SQLite Tables", "Cached roster profiles for offline inspection")
        ),
        sampleRecordPreview = listOf(
          "SQLite Attendance Table" to "${roomAttendanceRecords.size} persisted rows",
          "Notification Feed" to "${notifications.size} items (${notifications.count { !it.isRead }} unread)"
        ),
        linkedTab = NavigationTab.ATTENDANCE
      ),
      OrganizedDataFileEntry(
        id = "file_session_prefs",
        fileName = "SessionPreferences.kt",
        packagePath = "app/src/main/java/com/example/data/local/SessionPreferences.kt",
        group = DataLayerGroup.LOCAL_STORAGE,
        summary = "Encrypted/SharedPreferences manager for user profile context, active class selection, and FCM topic subscriptions.",
        primaryEntities = listOf("SessionPreferences", "SavedSessionUser"),
        liveRecordCount = 1,
        storageEngine = "Android SharedPreferences",
        syncMode = "Device Local State",
        schemaFields = listOf(
          DataFileSchemaField("KEY_USER_ID / ROLE", "String", "Active user identity & role profile metadata"),
          DataFileSchemaField("Login Gate Policy", "Security Rule", "Always requires explicit authentication on fresh app cold start")
        ),
        sampleRecordPreview = listOf(
          "Cold Start Security" to "Enforced Login Screen on App Launch",
          "Active Role Context" to "Instant role switching supported post-login"
        ),
        linkedTab = NavigationTab.SETTINGS
      ),

      // 4. CLOUD FIRESTORE & REPOSITORY
      OrganizedDataFileEntry(
        id = "file_school_repo",
        fileName = "SchoolRepository.kt",
        packagePath = "app/src/main/java/com/example/data/SchoolRepository.kt",
        group = DataLayerGroup.CLOUD_FIRESTORE,
        summary = "Central single-source-of-truth state coordinator connecting UI ViewModels, modular seed files, Room SQLite, and Firestore.",
        primaryEntities = listOf("SchoolRepository", "StateFlow Streams (16)", "God Mode Mutators"),
        liveRecordCount = systemUsers.size + notices.size + homeworks.size + attendanceRecords.size + calendarEvents.size + busRoutes.size,
        storageEngine = "Kotlin Coroutines StateFlow",
        syncMode = "Bi-Directional Reactive Stream",
        schemaFields = listOf(
          DataFileSchemaField("Role Profiles", "6 StateFlows", "Student, Teacher, Staff, Driver, Admin, Developer"),
          DataFileSchemaField("ERP Modules", "10 StateFlows", "Notices, Homework, Timetable, Attendance, Duties, Events, Classes, Bus, Calendar, Directory")
        ),
        sampleRecordPreview = listOf(
          "Architecture" to "Modularized (< 1,100 lines) with zero duplicate state",
          "Total Live Objects" to "${systemUsers.size + notices.size + homeworks.size + attendanceRecords.size + calendarEvents.size + directoryContacts.size} active records in memory"
        ),
        linkedTab = NavigationTab.DASHBOARD
      ),
      OrganizedDataFileEntry(
        id = "file_firestore_service",
        fileName = "FirestoreService.kt & RetryPolicy",
        packagePath = "app/src/main/java/com/example/data/firestore/FirestoreService.kt",
        group = DataLayerGroup.CLOUD_FIRESTORE,
        summary = "Cloud Firestore real-time snapshot listeners, exponential backoff retry engine, diagnostics tester, and local vault fallback.",
        primaryEntities = listOf("FirestoreService", "FirestoreRetryPolicy", "FirestoreDiagnosticTester", "FeedbackSubmissionItem"),
        liveRecordCount = allFeedback.size + notices.size + announcements.size,
        storageEngine = "Google Cloud Firestore + Local JSON Vault",
        syncMode = "Real-Time Snapshot Listeners + WorkManager",
        schemaFields = listOf(
          DataFileSchemaField("Collections (9)", "Cloud NoSQL", "feedback, notices, announcements, homework, attendance, events, users, fcm_broadcasts, fcm_tokens"),
          DataFileSchemaField("Retry Engine", "Exponential Backoff", "Automatic transient failure recovery with jitter")
        ),
        sampleRecordPreview = listOf(
          "Monitored Collections" to "9 Cloud Firestore collections",
          "Feedback Vault" to "${allFeedback.size} user feedback submissions stored"
        ),
        linkedTab = NavigationTab.SETTINGS
      )
    )
  }

  val filteredEntries = remember(catalogEntries, searchQuery, selectedGroup) {
    catalogEntries.filter { entry ->
      val matchesGroup = selectedGroup == null || entry.group == selectedGroup
      val matchesQuery = searchQuery.isBlank() ||
        entry.fileName.contains(searchQuery, ignoreCase = true) ||
        entry.summary.contains(searchQuery, ignoreCase = true) ||
        entry.primaryEntities.any { it.contains(searchQuery, ignoreCase = true) } ||
        entry.packagePath.contains(searchQuery, ignoreCase = true)
      matchesGroup && matchesQuery
    }
  }

  val groupedEntries = remember(filteredEntries) {
    filteredEntries.groupBy { it.group }
  }

  val cardBg = if (isDarkThemeStyle) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
  val subCardBg = if (isDarkThemeStyle) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
  val borderColor = if (isDarkThemeStyle) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant
  val primaryTextColor = if (isDarkThemeStyle) Color.White else MaterialTheme.colorScheme.onSurface
  val secondaryTextColor = if (isDarkThemeStyle) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("organized_data_files_explorer"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Architecture Summary Banner
    Surface(
      color = subCardBg,
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .background(
            Brush.horizontalGradient(
              colors = if (isDarkThemeStyle) {
                listOf(Color(0xFF0F172A), Color(0xFF0C4A6E).copy(alpha = 0.45f), Color(0xFF0F172A))
              } else {
                listOf(
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                  MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                )
              }
            )
          )
          .padding(16.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Surface(
              color = Color(0xFF38BDF8).copy(alpha = 0.2f),
              shape = CircleShape,
              modifier = Modifier.size(38.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.AccountTree,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "ORGANIZED DATA ARCHITECTURE & FILE CATALOG",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace,
                  letterSpacing = 0.8.sp
                ),
                color = Color(0xFF38BDF8)
              )
              Text(
                text = "Modularized across Domain Models, Seed Datasets, Room SQLite & Cloud Firestore",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryTextColor
              )
            }
          }

          // 4-Pillar Mini Telemetry Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            DataLayerStatBadge(
              label = "Models",
              value = "4 Files",
              color = Color(0xFF38BDF8),
              isDark = isDarkThemeStyle,
              modifier = Modifier.weight(1f)
            )
            DataLayerStatBadge(
              label = "Seeds",
              value = "3 Files",
              color = Color(0xFF10B981),
              isDark = isDarkThemeStyle,
              modifier = Modifier.weight(1f)
            )
            DataLayerStatBadge(
              label = "Room SQL",
              value = "4 Tables",
              color = Color(0xFFF59E0B),
              isDark = isDarkThemeStyle,
              modifier = Modifier.weight(1f)
            )
            DataLayerStatBadge(
              label = "Firestore",
              value = "9 Colls",
              color = Color(0xFF8B5CF6),
              isDark = isDarkThemeStyle,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // 2. Search & Layer Filter Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = {
        Text(
          "Search data files, schemas, or entities (e.g. BusRoute, Attendance)...",
          color = secondaryTextColor,
          style = MaterialTheme.typography.bodySmall
        )
      },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8))
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Close, contentDescription = "Clear search", tint = secondaryTextColor)
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = cardBg,
        unfocusedContainerColor = cardBg,
        focusedTextColor = primaryTextColor,
        unfocusedTextColor = primaryTextColor,
        focusedBorderColor = Color(0xFF38BDF8),
        unfocusedBorderColor = borderColor
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("data_files_search_input")
    )

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      item {
        FilterChip(
          selected = selectedGroup == null,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedGroup = null
          },
          label = { Text("All Layers (${catalogEntries.size})") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f),
            selectedLabelColor = Color(0xFF38BDF8)
          )
        )
      }
      items(DataLayerGroup.entries) { group ->
        val groupColor = Color(group.badgeColorHex)
        val count = catalogEntries.count { it.group == group }
        FilterChip(
          selected = selectedGroup == group,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedGroup = if (selectedGroup == group) null else group
          },
          label = { Text("${group.title} ($count)") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = groupColor.copy(alpha = 0.22f),
            selectedLabelColor = if (isDarkThemeStyle) Color.White else groupColor
          )
        )
      }
    }

    // 3. Grouped File Cards
    DataLayerGroup.entries.forEach { group ->
      val entriesForGroup = groupedEntries[group].orEmpty()
      if (entriesForGroup.isNotEmpty()) {
        val groupColor = Color(group.badgeColorHex)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(groupColor)
              )
              Column {
                Text(
                  text = group.title.uppercase(),
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                  ),
                  color = groupColor
                )
                Text(
                  text = group.subtitle,
                  style = MaterialTheme.typography.labelSmall,
                  color = secondaryTextColor
                )
              }
            }

            Surface(
              color = groupColor.copy(alpha = 0.15f),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = "${entriesForGroup.size} files",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = groupColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          entriesForGroup.forEach { fileEntry ->
            val isExpanded = expandedFileIds.contains(fileEntry.id)
            OrganizedDataFileCard(
              entry = fileEntry,
              isExpanded = isExpanded,
              isDarkThemeStyle = isDarkThemeStyle,
              onToggleExpand = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                expandedFileIds = if (isExpanded) {
                  expandedFileIds - fileEntry.id
                } else {
                  expandedFileIds + fileEntry.id
                }
              },
              onOpenLinkedTab = fileEntry.linkedTab?.let { tab ->
                {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  onNavigateToTab?.invoke(tab)
                }
              }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DataLayerStatBadge(
  label: String,
  value: String,
  color: Color,
  isDark: Boolean,
  modifier: Modifier = Modifier
) {
  Surface(
    color = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(10.dp),
    border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          fontFamily = FontFamily.Monospace
        ),
        color = color,
        maxLines = 1
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrganizedDataFileCard(
  entry: OrganizedDataFileEntry,
  isExpanded: Boolean,
  isDarkThemeStyle: Boolean,
  onToggleExpand: () -> Unit,
  onOpenLinkedTab: (() -> Unit)?
) {
  val groupColor = Color(entry.group.badgeColorHex)
  val cardBg = if (isDarkThemeStyle) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
  val subBg = if (isDarkThemeStyle) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
  val primaryText = if (isDarkThemeStyle) Color.White else MaterialTheme.colorScheme.onSurface
  val secondaryText = if (isDarkThemeStyle) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant

  val scale by animateFloatAsState(
    targetValue = if (isExpanded) 1.0f else 0.995f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "data_file_card_scale"
  )

  Surface(
    color = cardBg,
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(
      width = if (isExpanded) 1.5.dp else 1.dp,
      color = if (isExpanded) groupColor.copy(alpha = 0.7f) else groupColor.copy(alpha = 0.25f)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .scale(scale)
      .clip(RoundedCornerShape(14.dp))
      .clickable { onToggleExpand() }
      .testTag("data_file_card_${entry.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      // Top Row: Icon, File Name, Live Record Badge, Expand Chevron
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          color = groupColor.copy(alpha = 0.18f),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.size(40.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = when (entry.group) {
                DataLayerGroup.DOMAIN_MODELS -> Icons.Default.DataObject
                DataLayerGroup.SEED_DATASETS -> Icons.Default.Dataset
                DataLayerGroup.LOCAL_STORAGE -> Icons.Default.Storage
                DataLayerGroup.CLOUD_FIRESTORE -> Icons.Default.CloudSync
              },
              contentDescription = null,
              tint = groupColor,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = entry.fileName,
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              ),
              color = primaryText,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
          Text(
            text = entry.packagePath,
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp
            ),
            color = secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Surface(
          color = groupColor.copy(alpha = 0.16f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "${entry.liveRecordCount} recs",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            ),
            color = groupColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }

        Icon(
          imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = if (isExpanded) "Collapse" else "Expand",
          tint = secondaryText
        )
      }

      // Summary description
      Text(
        text = entry.summary,
        style = MaterialTheme.typography.bodySmall,
        color = secondaryText
      )

      // Entity Pills
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        entry.primaryEntities.forEach { entityName ->
          Surface(
            color = subBg,
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(0.5.dp, groupColor.copy(alpha = 0.35f))
          ) {
            Text(
              text = entityName,
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
              ),
              color = primaryText,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
          }
        }
      }

      // Expanded Details: Storage Engine, Schema Fields & Live Sample Preview
      AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          HorizontalDivider(color = groupColor.copy(alpha = 0.2f))

          // Engine & Sync Mode Metadata
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              color = subBg,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                Text(
                  text = "STORAGE ENGINE",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                  color = groupColor
                )
                Text(
                  text = entry.storageEngine,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = primaryText
                )
              }
            }
            Surface(
              color = subBg,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                Text(
                  text = "SYNC PIPELINE",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                  color = Color(0xFF10B981)
                )
                Text(
                  text = entry.syncMode,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = primaryText
                )
              }
            }
          }

          // Schema Breakdown
          Surface(
            color = subBg,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "📋 SCHEMA & FIELD DEFINITIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = groupColor
                )
              )
              entry.schemaFields.forEach { field ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Top
                ) {
                  Text(
                    text = "• ${field.name} (${field.type})",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    ),
                    color = primaryText,
                    modifier = Modifier.weight(0.42f)
                  )
                  Text(
                    text = field.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryText,
                    modifier = Modifier.weight(0.58f)
                  )
                }
              }
            }
          }

          // Live Data Snapshot Preview
          if (entry.sampleRecordPreview.isNotEmpty()) {
            Surface(
              color = subBg,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
              ) {
                Text(
                  text = "⚡ LIVE DATA SNAPSHOT",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF10B981)
                  )
                )
                entry.sampleRecordPreview.forEach { (title, detail) ->
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = title,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                      color = primaryText,
                      modifier = Modifier.weight(0.45f)
                    )
                    Text(
                      text = detail,
                      style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                      color = secondaryText,
                      modifier = Modifier.weight(0.55f)
                    )
                  }
                }
              }
            }
          }

          if (onOpenLinkedTab != null && entry.linkedTab != null) {
            FilledTonalButton(
              onClick = onOpenLinkedTab,
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = groupColor.copy(alpha = 0.2f),
                contentColor = groupColor
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Open Live ${entry.linkedTab.label} Module",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }
      }
    }
  }
}
