package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SchoolRepository
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AttendanceEntity
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SchoolViewModel(
  private val repository: SchoolRepository = SchoolRepository(),
  private val database: AppDatabase? = null
) : ViewModel() {

  private val _databaseFlow = MutableStateFlow<AppDatabase?>(database)
  val databaseFlow: StateFlow<AppDatabase?> = _databaseFlow.asStateFlow()

  // App Theme Mode (System, Light, Dark)
  private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
  val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

  // Network Connectivity State
  private var networkMonitor: com.example.util.NetworkConnectivityMonitor? = null
  private val _networkState = MutableStateFlow<com.example.util.NetworkState>(com.example.util.NetworkState.Online("Connected"))
  val networkState: StateFlow<com.example.util.NetworkState> = _networkState.asStateFlow()

  private val _isSimulatedOffline = MutableStateFlow(false)
  val isSimulatedOffline: StateFlow<Boolean> = _isSimulatedOffline.asStateFlow()

  private val _refreshFeedbackMessage = MutableStateFlow<String?>(null)
  val refreshFeedbackMessage: StateFlow<String?> = _refreshFeedbackMessage.asStateFlow()

  fun clearRefreshFeedbackMessage() {
    _refreshFeedbackMessage.value = null
  }

  // Pull To Refresh State
  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

  fun setThemeMode(mode: AppThemeMode) {
    _themeMode.value = mode
  }

  fun setDatabase(db: AppDatabase) {
    _databaseFlow.value = db
  }

  fun setSimulatedOffline(offline: Boolean) {
    _isSimulatedOffline.value = offline
    networkMonitor?.setSimulatedOffline(offline)
    if (offline) {
      _networkState.value = com.example.util.NetworkState.Offline("Simulated Offline Mode (Room Database cache active)")
    } else {
      _networkState.value = networkMonitor?.getCurrentNetworkState() ?: com.example.util.NetworkState.Online("Connected")
    }
  }

  // Context reference for system notifications
  private var appContext: Context? = null

  fun retryNetworkConnection() {
    viewModelScope.launch {
      _networkState.value = com.example.util.NetworkState.Reconnecting
      kotlinx.coroutines.delay(600)
      _networkState.value = networkMonitor?.getCurrentNetworkState() ?: com.example.util.NetworkState.Online("Connected")
    }
  }

  fun initializeWithContext(context: Context) {
    appContext = context.applicationContext
    if (networkMonitor == null) {
      val monitor = com.example.util.NetworkConnectivityMonitor(context.applicationContext)
      networkMonitor = monitor
      viewModelScope.launch {
        monitor.networkState.collect { state ->
          _networkState.value = state
        }
      }
    }

    if (_databaseFlow.value == null) {
      val db = AppDatabase.getDatabase(context.applicationContext)
      _databaseFlow.value = db
      viewModelScope.launch(Dispatchers.IO) {
        // Ensure database has records populated
        val records = db.attendanceDao().getRecordById("att_1201") ?: db.attendanceDao().getRecordById("att_1")
        if (records == null) {
          AppDatabase.populateInitialData(db)
        }
      }
    }
  }

  init {
    // Default role
    repository.loginAsRole(UserRole.STUDENT)
  }

  // Current User & Profiles
  val currentUser: StateFlow<User> = repository.currentUser
    .filterNotNull()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = User(
        id = "usr_student_01",
        username = "student01",
        fullName = "Keerthivasan",
        email = "keerthivasan.s@stjosephs.edu",
        role = UserRole.STUDENT,
        designation = "Grade 12 - Section A"
      )
    )

  val studentProfile: StateFlow<StudentProfile?> = repository.currentStudentProfile
  val teacherProfile: StateFlow<TeacherProfile?> = repository.currentTeacherProfile
  val staffProfile: StateFlow<StaffProfile?> = repository.currentStaffProfile
  val adminProfile: StateFlow<AdminProfile?> = repository.currentAdminProfile
  val developerProfile: StateFlow<DeveloperProfile?> = repository.currentDeveloperProfile
  val systemUsers: StateFlow<List<SystemUserRecord>> = repository.systemUsers

  // Developer HUD & Secret Console State
  private val _isDeveloperConsoleOpen = MutableStateFlow(false)
  val isDeveloperConsoleOpen: StateFlow<Boolean> = _isDeveloperConsoleOpen.asStateFlow()

  private val _secretTerminalUnlocked = MutableStateFlow(false)
  val secretTerminalUnlocked: StateFlow<Boolean> = _secretTerminalUnlocked.asStateFlow()

  fun openDeveloperConsole() {
    _isDeveloperConsoleOpen.value = true
  }

  fun closeDeveloperConsole() {
    _isDeveloperConsoleOpen.value = false
  }

  fun unlockSecretTerminal() {
    _secretTerminalUnlocked.value = true
  }

  // Data streams
  val notices: StateFlow<List<Notice>> = repository.notices
  val schoolEvents: StateFlow<List<SchoolEvent>> = repository.events
  val homeworks: StateFlow<List<Homework>> = repository.homeworks
  val timetables: StateFlow<List<TimetableEntry>> = repository.timetables
  val schoolClasses: StateFlow<List<SchoolClass>> = repository.classes
  val staffDuties: StateFlow<List<DutyTask>> = repository.duties
  val attendanceRecords: StateFlow<List<AttendanceRecord>> = repository.attendanceRecords

  // Notifications Stream
  val notifications: StateFlow<List<AppNotification>> = repository.notifications
  val unreadNotificationsCount: StateFlow<Int> = repository.notifications
    .map { list -> list.count { !it.isRead } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

  // UI Filters / Selections
  private val _selectedDay = MutableStateFlow(DayOfWeek.MONDAY)
  val selectedDay: StateFlow<DayOfWeek> = _selectedDay.asStateFlow()

  private val _selectedClassForAttendance = MutableStateFlow("Class 12-A")
  val selectedClassForAttendance: StateFlow<String> = _selectedClassForAttendance.asStateFlow()

  private val _selectedNoticeCategory = MutableStateFlow<NoticeCategory?>(null)
  val selectedNoticeCategory: StateFlow<NoticeCategory?> = _selectedNoticeCategory.asStateFlow()

  val pendingHomeworkCount: StateFlow<Int> = homeworks
    .map { list -> list.count { it.status == HomeworkStatus.PENDING } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

  // Room DB live attendance records stream with fallback to in-memory repository
  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val roomAttendanceRecords: StateFlow<List<AttendanceEntity>> = _databaseFlow
    .flatMapLatest { db ->
      if (db != null) {
        db.attendanceDao().getAllAttendanceRecords()
      } else {
        attendanceRecords.map { list ->
          list.map {
            AttendanceEntity(
              id = it.id,
              studentId = it.studentId,
              studentName = it.studentName,
              rollNo = it.rollNo,
              className = it.className,
              date = it.date,
              status = it.status,
              markedBy = it.markedBy,
              notes = it.notes
            )
          }
        }
      }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Actions
  fun login(username: String, password: String): Result<User> {
    return repository.login(username, password)
  }

  fun logout() {
    repository.logout()
  }

  fun switchRole(role: UserRole) {
    repository.loginAsRole(role)
  }

  fun setSelectedDay(day: DayOfWeek) {
    _selectedDay.value = day
  }

  fun setSelectedClass(className: String) {
    _selectedClassForAttendance.value = className
  }

  fun setSelectedCategory(category: NoticeCategory?) {
    _selectedNoticeCategory.value = category
  }

  fun submitHomework(homeworkId: String, note: String) {
    repository.submitHomework(homeworkId, note)
  }

  fun createHomework(
    title: String,
    description: String,
    subjectName: String,
    className: String,
    dueDate: String,
    maxMarks: Int,
    context: Context? = null
  ) {
    repository.assignHomework(title, description, subjectName, className, dueDate, maxMarks)
    val ctx = context ?: appContext
    ctx?.let { c ->
      com.example.util.SystemNotificationHelper.showSystemNotification(
        context = c,
        title = "📚 New Assignment: $subjectName ($className)",
        message = "$title. Due: $dueDate",
        type = com.example.model.NotificationType.HOMEWORK,
        actionRoute = "homework",
        isUrgent = false
      )
    }
    _refreshFeedbackMessage.value = "Homework assigned and notification sent to students."
  }

  fun publishNotice(
    title: String,
    content: String,
    category: NoticeCategory,
    isUrgent: Boolean,
    context: Context? = null
  ) {
    val notice = repository.addNotice(title, content, category, isUrgent)
    val ctx = context ?: appContext
    ctx?.let { c ->
      com.example.util.SystemNotificationHelper.showSystemNotification(
        context = c,
        title = if (isUrgent) "🚨 Urgent Announcement: $title" else "📢 New Circular: $title",
        message = content,
        type = when (category) {
          NoticeCategory.ACADEMIC -> com.example.model.NotificationType.ACADEMIC
          NoticeCategory.EVENT -> com.example.model.NotificationType.EVENT
          NoticeCategory.SPORTS -> com.example.model.NotificationType.EVENT
          else -> com.example.model.NotificationType.NOTICE
        },
        actionRoute = "notices",
        isUrgent = isUrgent
      )
    }
    _refreshFeedbackMessage.value = if (isUrgent) {
      "🚨 Urgent circular published! Priority heads-up alert broadcasted."
    } else {
      "📢 Circular published! Heads-Up notification broadcasted to all students & faculty."
    }
  }

  fun updateDutyStatus(dutyId: String, newStatus: DutyStatus) {
    repository.updateDutyStatus(dutyId, newStatus)
  }

  fun addDutyTask(title: String, area: String, time: String, priority: DutyPriority = DutyPriority.MEDIUM) {
    repository.addDuty(title, area, time, priority)
  }

  /**
   * Updates student daily attendance status (Full-day, Half-day, On-duty, Absent)
   * simultaneously syncing in-memory state flow and persisting in Room SQLite database.
   */
  fun updateStudentAttendanceStatus(
    studentId: String,
    status: AttendanceStatus,
    notes: String = "",
    markedBy: String = "Prof. Sarah Jenkins (Class Teacher)"
  ): Job {
    // 1. Update in-memory repository for instant UI reactivity
    if (notes.isNotBlank()) {
      repository.updateAttendanceRecordWithNotes(studentId, status, notes, markedBy)
    } else {
      repository.updateAttendanceRecord(studentId, status, markedBy)
    }

    // 2. Persist in Room Database
    return viewModelScope.launch {
      _databaseFlow.value?.let { db ->
        withContext(Dispatchers.IO) {
          // Check if record exists in Room DB
          val currentRecords = db.attendanceDao().getRecordById("att_$studentId")
          if (currentRecords != null) {
            if (notes.isNotBlank()) {
              db.attendanceDao().updateStudentAttendanceStatusWithNotes(
                studentId = studentId,
                status = status,
                notes = notes,
                markedBy = markedBy
              )
            } else {
              db.attendanceDao().updateStudentAttendanceStatus(
                studentId = studentId,
                status = status,
                markedBy = markedBy
              )
            }
          } else {
            // Find in repository to get student details
            val repoRecord = repository.attendanceRecords.value.find { it.studentId == studentId }
            if (repoRecord != null) {
              db.attendanceDao().insertRecord(
                AttendanceEntity(
                  id = repoRecord.id,
                  studentId = studentId,
                  studentName = repoRecord.studentName,
                  rollNo = repoRecord.rollNo,
                  className = repoRecord.className,
                  date = repoRecord.date,
                  status = status,
                  markedBy = markedBy,
                  notes = notes.ifBlank { repoRecord.notes }
                )
              )
            }
          }
        }
      }
    }
  }

  /**
   * Fast 1-tap roll-call action to mark all students in the given class as Full-Day (FD).
   */
  fun markAllFullDay(
    className: String = _selectedClassForAttendance.value,
    markedBy: String = "Prof. Sarah Jenkins (Class Teacher)"
  ): Job {
    // 1. Update in-memory repository
    repository.markAllAttendance(AttendanceStatus.FULL_DAY, className, markedBy)

    // 2. Persist in Room Database
    return viewModelScope.launch {
      _databaseFlow.value?.let { db ->
        withContext(Dispatchers.IO) {
          db.attendanceDao().markAllClassAttendance(
            className = className,
            status = AttendanceStatus.FULL_DAY,
            markedBy = markedBy
          )
        }
      }
    }
  }

  fun markAllPresent() {
    markAllFullDay(_selectedClassForAttendance.value)
  }

  // Deep link navigation triggered by external system notification
  private val _deepLinkRoute = MutableStateFlow<String?>(null)
  val deepLinkRoute: StateFlow<String?> = _deepLinkRoute.asStateFlow()

  fun setDeepLinkRoute(route: String?) {
    _deepLinkRoute.value = route
  }

  fun clearDeepLinkRoute() {
    _deepLinkRoute.value = null
  }

  // Pull-To-Refresh Implementation
  fun refreshData(onComplete: (() -> Unit)? = null) {
    viewModelScope.launch {
      _isRefreshing.value = true
      kotlinx.coroutines.delay(800) // Smooth refresh animation
      val isOffline = _networkState.value is com.example.util.NetworkState.Offline
      _databaseFlow.value?.let { db ->
        withContext(Dispatchers.IO) {
          // Re-sync / verify Room database
          val count = db.attendanceDao().getAttendanceCount()
          if (count == 0) {
            AppDatabase.populateInitialData(db)
          }
        }
      }
      if (isOffline) {
        _refreshFeedbackMessage.value = "Offline Mode: Loaded cached records from Room Database (Live sync paused)."
      } else {
        _refreshFeedbackMessage.value = "Data refreshed successfully."
      }
      _isRefreshing.value = false
      onComplete?.invoke()
    }
  }

  // Notification actions
  fun markNotificationAsRead(id: String) {
    repository.markNotificationAsRead(id)
  }

  fun markAllNotificationsAsRead() {
    repository.markAllNotificationsAsRead()
  }

  fun deleteNotification(id: String) {
    repository.deleteNotification(id)
  }

  fun sendTestNotification(
    context: android.content.Context? = null,
    title: String = "Campus Alert (Grade 12-A)",
    message: String = "Physics laboratory experiment rescheduled to 11:00 AM in Science Block Floor 2.",
    type: NotificationType = NotificationType.ACADEMIC,
    actionRoute: String? = "timetable",
    isUrgent: Boolean = true,
    showSystemPopUp: Boolean = true
  ) {
    val newNotification = AppNotification(
      id = "notif_${System.currentTimeMillis()}",
      title = title,
      message = message,
      timeAgo = "Just now",
      type = type,
      isRead = false,
      actionRoute = actionRoute,
      isUrgent = isUrgent
    )
    repository.addNotification(newNotification)

    if (showSystemPopUp && context != null) {
      com.example.util.SystemNotificationHelper.showSystemNotification(
        context = context,
        title = title,
        message = message,
        type = type,
        actionRoute = actionRoute,
        isUrgent = isUrgent
      )
    }
  }

  fun triggerDelayedSystemPopUp(
    context: android.content.Context,
    delaySeconds: Long = 5L,
    title: String = "Urgent: Science & AI Expo 2026 Registration",
    message: String = "All Class 12 exhibit submissions are due by 4:00 PM today at STEM cell.",
    type: NotificationType = NotificationType.NOTICE,
    actionRoute: String = "notices"
  ) {
    val newNotification = AppNotification(
      id = "notif_${System.currentTimeMillis()}",
      title = title,
      message = message,
      timeAgo = "Just now",
      type = type,
      isRead = false,
      actionRoute = actionRoute,
      isUrgent = true
    )
    repository.addNotification(newNotification)

    com.example.util.SystemNotificationHelper.scheduleDelayedSystemNotification(
      context = context,
      delaySeconds = delaySeconds,
      title = title,
      message = message,
      type = type,
      actionRoute = actionRoute
    )
  }

  fun resetDatabaseToDefaults() {
    viewModelScope.launch(Dispatchers.IO) {
      _databaseFlow.value?.let { db ->
        AppDatabase.populateInitialData(db)
      }
    }
  }

  // Developer God Mode Mutators
  fun updateSystemUser(user: SystemUserRecord) {
    repository.updateSystemUser(user)
  }

  fun addSystemUser(user: SystemUserRecord) {
    repository.addSystemUser(user)
  }

  fun deleteSystemUser(userId: String) {
    repository.deleteSystemUser(userId)
  }

  fun updateNotice(notice: Notice) {
    repository.updateNotice(notice)
  }

  fun deleteNotice(noticeId: String) {
    repository.deleteNotice(noticeId)
  }

  fun updateHomework(homework: Homework) {
    repository.updateHomework(homework)
  }

  fun deleteHomework(hwId: String) {
    repository.deleteHomework(hwId)
  }

  fun updateAttendanceRecordDirect(recordId: String, studentName: String, status: AttendanceStatus, notes: String) {
    repository.updateAttendanceRecordDirect(recordId, studentName, status, notes)
  }

  fun updateTimetableEntry(entry: TimetableEntry) {
    repository.updateTimetableEntry(entry)
  }

  fun updateDuty(duty: DutyTask) {
    repository.updateDuty(duty)
  }

  fun deleteDuty(dutyId: String) {
    repository.deleteDuty(dutyId)
  }

  fun updateClass(schoolClass: SchoolClass) {
    repository.updateClass(schoolClass)
  }

  fun broadcastDeveloperNotice(title: String, content: String, isUrgent: Boolean = true) {
    repository.broadcastDeveloperNotice(title, content, isUrgent)
  }

  fun resetAllSystemDefaults() {
    repository.resetToDefaults()
    resetDatabaseToDefaults()
  }
}
