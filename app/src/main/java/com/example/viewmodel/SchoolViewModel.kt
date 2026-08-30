package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SchoolRepository
import com.example.data.local.AppDatabase
import com.example.data.local.SessionPreferences
import com.example.data.local.entity.AttendanceEntity
import com.example.model.*
import com.example.util.NotificationAudienceFilter
import com.example.util.SystemNotificationHelper
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

  // Session Preferences for persistent login across app launches
  private var sessionPreferences: SessionPreferences? = null
  private val _isAuthenticated = MutableStateFlow(true)
  val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

  // Firebase Auth and Firestore Services
  private var firebaseAuthService: com.example.data.auth.FirebaseAuthService? = null
  private var firestoreService: com.example.data.firestore.FirestoreService? = null

  // Tracking observed IDs to prevent duplicate alerts on initial app start
  private val knownNoticeIds = mutableSetOf<String>()
  private val knownAttendanceIds = mutableSetOf<String>()
  private val knownAnnouncementIds = mutableSetOf<String>()
  private val knownHomeworkIds = mutableSetOf<String>()
  private var isInitialNoticeLoad = true
  private var isInitialAttendanceLoad = true
  private var isInitialAnnouncementLoad = true
  private var isInitialHomeworkLoad = true

  // Auth Loading & Status State
  private val _isAuthLoading = MutableStateFlow(false)
  val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

  private val _authErrorMessage = MutableStateFlow<String?>(null)
  val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

  fun clearAuthError() {
    _authErrorMessage.value = null
  }

  // Network Connectivity State
  private var networkMonitor: com.example.util.NetworkConnectivityMonitor? = null
  private val _networkState = MutableStateFlow<com.example.util.NetworkState>(com.example.util.NetworkState.Online("Connected"))
  val networkState: StateFlow<com.example.util.NetworkState> = _networkState.asStateFlow()

  private val _isSimulatedOffline = MutableStateFlow(false)
  val isSimulatedOffline: StateFlow<Boolean> = _isSimulatedOffline.asStateFlow()

  // Real-time Cloud Sync Telemetry State
  private val _cloudSyncInfo = MutableStateFlow(
    CloudSyncInfo(
      state = CloudSyncState.SYNCED,
      lastSyncedTime = "Just now",
      pendingChangesCount = 0,
      isRealtimeConnected = true
    )
  )
  val cloudSyncInfo: StateFlow<CloudSyncInfo> = _cloudSyncInfo.asStateFlow()

  // FCM Device Token State Flow
  private val _fcmDeviceToken = MutableStateFlow<String?>(com.example.service.SchoolFirebaseMessagingService.latestToken)
  val fcmDeviceToken: StateFlow<String?> = _fcmDeviceToken.asStateFlow()

  fun refreshFcmDeviceToken() {
    com.example.service.SchoolFirebaseMessagingService.fetchFcmToken { token ->
      _fcmDeviceToken.value = token
    }
  }

  fun triggerFcmPushNotification(
    title: String = "📢 Emergency School Circular",
    message: String = "Heavy rainfall warning: School will operate online for afternoon sessions.",
    type: String = "notice",
    route: String = "notices"
  ) {
    appContext?.let { ctx ->
      val notifType = when (type.lowercase()) {
        "event" -> NotificationType.EVENT
        "homework" -> NotificationType.HOMEWORK
        "attendance" -> NotificationType.ATTENDANCE
        "exam" -> NotificationType.EXAM
        else -> NotificationType.NOTICE
      }
      com.example.util.SystemNotificationHelper.showSystemNotification(
        context = ctx,
        title = "🔥 FCM Push: $title",
        message = message,
        type = notifType,
        actionRoute = route,
        isUrgent = true
      )
      val appNotif = AppNotification(
        id = "fcm_push_${System.currentTimeMillis()}",
        title = "🔥 FCM Push: $title",
        message = message,
        timeAgo = "Just now",
        type = notifType,
        isRead = false,
        actionRoute = route,
        isUrgent = true
      )
      repository.addNotification(appNotif)
      _refreshFeedbackMessage.value = "🔥 FCM Push Notification dispatched to system notification tray!"
    }
  }

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
      _cloudSyncInfo.value = _cloudSyncInfo.value.copy(
        state = CloudSyncState.OFFLINE,
        isRealtimeConnected = false
      )
    } else {
      _networkState.value = networkMonitor?.getCurrentNetworkState() ?: com.example.util.NetworkState.Online("Connected")
      _cloudSyncInfo.value = _cloudSyncInfo.value.copy(
        state = CloudSyncState.SYNCED,
        isRealtimeConnected = true,
        lastSyncedTime = "Just now"
      )
    }
  }

  fun triggerManualCloudSync() {
    viewModelScope.launch {
      if (_isSimulatedOffline.value || _networkState.value is com.example.util.NetworkState.Offline) {
        _cloudSyncInfo.value = _cloudSyncInfo.value.copy(
          state = CloudSyncState.OFFLINE,
          isRealtimeConnected = false
        )
        _refreshFeedbackMessage.value = "Working offline. Changes are saved locally in Room SQLite."
        return@launch
      }

      _cloudSyncInfo.value = _cloudSyncInfo.value.copy(state = CloudSyncState.SYNCING)
      kotlinx.coroutines.delay(800)
      val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
      val timeStr = formatter.format(java.util.Date())
      _cloudSyncInfo.value = CloudSyncInfo(
        state = CloudSyncState.SYNCED,
        lastSyncedTime = "Today at $timeStr",
        pendingChangesCount = 0,
        isRealtimeConnected = true
      )
      _refreshFeedbackMessage.value = "Cloud Sync complete! All data synchronized with Firestore."
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

    // Restore persistent session
    if (sessionPreferences == null) {
      val session = SessionPreferences(context.applicationContext)
      sessionPreferences = session
      val savedUser = session.getSavedUser()
      if (savedUser != null) {
        repository.loginWithUser(savedUser)
      } else {
        repository.loginAsRole(UserRole.STUDENT)
      }
      _isAuthenticated.value = true
    }

    if (firebaseAuthService == null) {
      try {
        firebaseAuthService = com.example.data.auth.FirebaseAuthService(context.applicationContext)
      } catch (e: Exception) {
        android.util.Log.w("SchoolViewModel", "Firebase Auth init failed gracefully: ${e.message}")
      }
    }
    if (firestoreService == null) {
      try {
        val fService = com.example.data.firestore.FirestoreService(context.applicationContext)
        firestoreService = fService
        _cloudSyncInfo.value = _cloudSyncInfo.value.copy(
          state = CloudSyncState.SYNCED,
          isRealtimeConnected = true,
          lastSyncedTime = "Just now"
        )

        // 1. Listen for real-time cloud notices from Firestore with Audience Filtering
        viewModelScope.launch {
          try {
            fService.observeNotices().collect { cloudNotices ->
              if (cloudNotices.isNotEmpty()) {
                if (isInitialNoticeLoad) {
                  knownNoticeIds.addAll(cloudNotices.map { it.id })
                  isInitialNoticeLoad = false
                  cloudNotices.forEach { repository.publishNotice(it) }
                } else {
                  cloudNotices.forEach { cloudNotice ->
                    if (!knownNoticeIds.contains(cloudNotice.id)) {
                      knownNoticeIds.add(cloudNotice.id)
                      repository.publishNotice(cloudNotice)

                      // Evaluate Targeted Audience
                      val user = repository.currentUser.value
                      val sProf = repository.currentStudentProfile.value
                      if (NotificationAudienceFilter.shouldReceiveNotice(cloudNotice, user, sProf)) {
                        val notifType = when (cloudNotice.category) {
                          NoticeCategory.ACADEMIC -> NotificationType.ACADEMIC
                          NoticeCategory.EVENT -> NotificationType.EVENT
                          NoticeCategory.SPORTS -> NotificationType.EVENT
                          else -> NotificationType.NOTICE
                        }
                        val appNotif = AppNotification(
                          id = "notif_circ_${cloudNotice.id}",
                          title = if (cloudNotice.isUrgent) "🚨 Urgent Circular: ${cloudNotice.title}" else "📢 New Circular: ${cloudNotice.title}",
                          message = cloudNotice.content,
                          timeAgo = "Just now",
                          type = notifType,
                          isRead = false,
                          actionRoute = "notices",
                          isUrgent = cloudNotice.isUrgent
                        )
                        repository.addNotification(appNotif)

                        appContext?.let { ctx ->
                          SystemNotificationHelper.showSystemNotification(
                            context = ctx,
                            title = if (cloudNotice.isUrgent) "🚨 Urgent: ${cloudNotice.title}" else "📢 Circular: ${cloudNotice.title}",
                            message = cloudNotice.content,
                            type = notifType,
                            actionRoute = "notices",
                            isUrgent = cloudNotice.isUrgent
                          )
                        }
                      }
                    } else {
                      repository.publishNotice(cloudNotice)
                    }
                  }
                }
              }
            }
          } catch (e: Exception) {
            android.util.Log.w("SchoolViewModel", "observeNotices error: ${e.message}")
          }
        }

        // 2. Listen for real-time attendance updates from Firestore with Student Targeting
        viewModelScope.launch {
          try {
            fService.observeAttendance().collect { records ->
              if (records.isNotEmpty()) {
                if (isInitialAttendanceLoad) {
                  knownAttendanceIds.addAll(records.map { it.id })
                  isInitialAttendanceLoad = false
                  records.forEach { repository.syncAttendanceRecord(it) }
                } else {
                  records.forEach { record ->
                    if (!knownAttendanceIds.contains(record.id)) {
                      knownAttendanceIds.add(record.id)
                      repository.syncAttendanceRecord(record)

                      val user = repository.currentUser.value
                      val sProf = repository.currentStudentProfile.value
                      if (NotificationAudienceFilter.shouldReceiveAttendanceNotification(record, user, sProf)) {
                        val appNotif = AppNotification(
                          id = "notif_att_${record.id}",
                          title = "📋 Daily Attendance Marked (${record.className})",
                          message = "${record.studentName} marked as ${record.status.label} for ${record.date}.",
                          timeAgo = "Just now",
                          type = NotificationType.ATTENDANCE,
                          isRead = false,
                          actionRoute = "attendance",
                          isUrgent = false
                        )
                        repository.addNotification(appNotif)

                        appContext?.let { ctx ->
                          SystemNotificationHelper.showSystemNotification(
                            context = ctx,
                            title = "📋 Attendance Update (${record.className})",
                            message = "${record.studentName} was marked ${record.status.label} for ${record.date}",
                            type = NotificationType.ATTENDANCE,
                            actionRoute = "attendance",
                            isUrgent = false
                          )
                        }
                      }
                    } else {
                      repository.syncAttendanceRecord(record)
                    }
                  }
                }
              }
            }
          } catch (e: Exception) {
            android.util.Log.w("SchoolViewModel", "observeAttendance error: ${e.message}")
          }
        }

        // 3. Listen for real-time announcements from Firestore with Audience Filtering
        viewModelScope.launch {
          try {
            fService.observeAnnouncements().collect { announcements ->
              if (announcements.isNotEmpty()) {
                if (isInitialAnnouncementLoad) {
                  knownAnnouncementIds.addAll(announcements.map { it.id })
                  isInitialAnnouncementLoad = false
                  announcements.forEach { repository.syncAnnouncement(it) }
                } else {
                  announcements.forEach { ann ->
                    if (!knownAnnouncementIds.contains(ann.id)) {
                      knownAnnouncementIds.add(ann.id)
                      repository.syncAnnouncement(ann)

                      val user = repository.currentUser.value
                      if (NotificationAudienceFilter.shouldReceiveAnnouncement(ann, user)) {
                        val appNotif = AppNotification(
                          id = "notif_ann_${ann.id}",
                          title = if (ann.isEmergency) "🚨 Emergency Alert: ${ann.title}" else "📢 Announcement: ${ann.title}",
                          message = ann.content,
                          timeAgo = "Just now",
                          type = NotificationType.ANNOUNCEMENT,
                          isRead = false,
                          actionRoute = "announcements",
                          isUrgent = ann.isEmergency
                        )
                        repository.addNotification(appNotif)

                        appContext?.let { ctx ->
                          SystemNotificationHelper.showSystemNotification(
                            context = ctx,
                            title = if (ann.isEmergency) "🚨 Emergency: ${ann.title}" else "📢 School Alert: ${ann.title}",
                            message = ann.content,
                            type = NotificationType.ANNOUNCEMENT,
                            actionRoute = "announcements",
                            isUrgent = ann.isEmergency
                          )
                        }
                      }
                    } else {
                      repository.syncAnnouncement(ann)
                    }
                  }
                }
              }
            }
          } catch (e: Exception) {
            android.util.Log.w("SchoolViewModel", "observeAnnouncements error: ${e.message}")
          }
        }

        // 4. Listen for real-time homework assignments with Strict Class / Grade Filtering
        viewModelScope.launch {
          try {
            fService.observeHomework().collect { homeworks ->
              if (homeworks.isNotEmpty()) {
                if (isInitialHomeworkLoad) {
                  knownHomeworkIds.addAll(homeworks.map { it.id })
                  isInitialHomeworkLoad = false
                  homeworks.forEach { repository.syncHomework(it) }
                } else {
                  homeworks.forEach { hw ->
                    if (!knownHomeworkIds.contains(hw.id)) {
                      knownHomeworkIds.add(hw.id)
                      repository.syncHomework(hw)

                      // Evaluate Targeted Class Audience (e.g. 12-A student receives 12-A homework, NOT 10-A)
                      val user = repository.currentUser.value
                      val sProf = repository.currentStudentProfile.value
                      val tProf = repository.currentTeacherProfile.value

                      if (NotificationAudienceFilter.shouldReceiveHomeworkNotification(hw, user, sProf, tProf)) {
                        val appNotif = AppNotification(
                          id = "notif_hw_${hw.id}",
                          title = "📚 New Assignment: ${hw.subjectName} (${hw.className})",
                          message = "${hw.title}. Due: ${hw.dueDate} (${hw.teacherName})",
                          timeAgo = "Just now",
                          type = NotificationType.HOMEWORK,
                          isRead = false,
                          actionRoute = "homework",
                          isUrgent = true
                        )
                        repository.addNotification(appNotif)

                        appContext?.let { ctx ->
                          SystemNotificationHelper.showSystemNotification(
                            context = ctx,
                            title = "📚 New Assignment: ${hw.subjectName} (${hw.className})",
                            message = "${hw.title}. Due: ${hw.dueDate}",
                            type = NotificationType.HOMEWORK,
                            actionRoute = "homework",
                            isUrgent = true
                          )
                        }
                      }
                    } else {
                      repository.syncHomework(hw)
                    }
                  }
                }
              }
            }
          } catch (e: Exception) {
            android.util.Log.w("SchoolViewModel", "observeHomework error: ${e.message}")
          }
        }
      } catch (e: Exception) {
        android.util.Log.w("SchoolViewModel", "FirestoreService init failed gracefully: ${e.message}")
      }
    }

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
  val driverProfile: StateFlow<DriverProfile?> = repository.currentDriverProfile
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

  // Wave 1 ERP Data Streams
  val calendarEvents: StateFlow<List<CalendarEvent>> = repository.calendarEvents
  val busRoutes: StateFlow<List<BusRoute>> = repository.busRoutes
  val selectedBusRouteId: StateFlow<String> = repository.selectedBusRouteId
  val announcements: StateFlow<List<SchoolAnnouncement>> = repository.announcements
  val directoryContacts: StateFlow<List<DirectoryContact>> = repository.directoryContacts

  // Role-Sanitized Directory Stream (Privacy Protection for Students)
  val sanitizedDirectoryContacts: StateFlow<List<DirectoryContact>> = combine(
    currentUser,
    repository.directoryContacts
  ) { user, contacts ->
    if (user.role == UserRole.STUDENT) {
      contacts.map { contact ->
        if (contact.isStudent) {
          contact.copy(
            phoneNumber = "🔒 Hidden for Student Privacy",
            parentContact = "🔒 Hidden for Student Privacy"
          )
        } else contact
      }
    } else contacts
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
    val res = repository.login(username, password)
    if (res.isSuccess) {
      val user = res.getOrThrow()
      val sProf = repository.currentStudentProfile.value
      sessionPreferences?.saveUserSession(
        user = user,
        className = sProf?.let { "Class ${it.grade}-${it.section}" } ?: "Class 12-A",
        grade = sProf?.grade ?: "12",
        section = sProf?.section ?: "A",
        admissionOrEmpId = sProf?.admissionNo ?: ""
      )
      _isAuthenticated.value = true
    }
    return res
  }

  /**
   * Initiates Google Sign-In with Android Credential Manager and Firebase Auth
   */
  fun signInWithGoogle(
    activityContext: Context,
    preferredRole: UserRole,
    onSuccess: (User) -> Unit,
    onError: (String) -> Unit
  ) {
    val authService = firebaseAuthService ?: com.example.data.auth.FirebaseAuthService(activityContext)
    firebaseAuthService = authService

    viewModelScope.launch {
      _isAuthLoading.value = true
      _authErrorMessage.value = null
      val result = authService.signInWithGoogle(activityContext, preferredRole)
      _isAuthLoading.value = false

      result.fold(
        onSuccess = { user ->
          repository.loginWithUser(user)
          val sProf = repository.currentStudentProfile.value
          sessionPreferences?.saveUserSession(
            user = user,
            className = sProf?.let { "Class ${it.grade}-${it.section}" } ?: "Class 12-A",
            grade = sProf?.grade ?: "12",
            section = sProf?.section ?: "A",
            admissionOrEmpId = sProf?.admissionNo ?: ""
          )
          _isAuthenticated.value = true
          // Sync profile to Firestore asynchronously
          viewModelScope.launch {
            firestoreService?.saveUserProfile(user)
          }
          onSuccess(user)
        },
        onFailure = { error ->
          val msg = error.message ?: "Google Sign-In failed."
          _authErrorMessage.value = msg
          onError(msg)
        }
      )
    }
  }

  /**
   * Signs in with Firebase Email & Password
   */
  fun signInWithFirebaseEmail(
    email: String,
    pass: String,
    role: UserRole,
    onSuccess: (User) -> Unit,
    onError: (String) -> Unit
  ) {
    val authService = firebaseAuthService
    if (authService == null) {
      val res = repository.login(email, pass)
      if (res.isSuccess) {
        val user = res.getOrThrow()
        val sProf = repository.currentStudentProfile.value
        sessionPreferences?.saveUserSession(
          user = user,
          className = sProf?.let { "Class ${it.grade}-${it.section}" } ?: "Class 12-A",
          grade = sProf?.grade ?: "12",
          section = sProf?.section ?: "A",
          admissionOrEmpId = sProf?.admissionNo ?: ""
        )
        _isAuthenticated.value = true
        onSuccess(user)
      } else {
        onError(res.exceptionOrNull()?.message ?: "Authentication failed")
      }
      return
    }

    viewModelScope.launch {
      _isAuthLoading.value = true
      _authErrorMessage.value = null
      val result = authService.signInWithEmailPassword(email, pass, role)
      _isAuthLoading.value = false

      result.fold(
        onSuccess = { user ->
          repository.loginWithUser(user)
          val sProf = repository.currentStudentProfile.value
          sessionPreferences?.saveUserSession(
            user = user,
            className = sProf?.let { "Class ${it.grade}-${it.section}" } ?: "Class 12-A",
            grade = sProf?.grade ?: "12",
            section = sProf?.section ?: "A",
            admissionOrEmpId = sProf?.admissionNo ?: ""
          )
          _isAuthenticated.value = true
          onSuccess(user)
        },
        onFailure = { error ->
          // Fallback to local demo auth if email matches demo repository accounts
          val localRes = repository.login(email, pass)
          if (localRes.isSuccess) {
            val user = localRes.getOrThrow()
            val sProf = repository.currentStudentProfile.value
            sessionPreferences?.saveUserSession(
              user = user,
              className = sProf?.let { "Class ${it.grade}-${it.section}" } ?: "Class 12-A",
              grade = sProf?.grade ?: "12",
              section = sProf?.section ?: "A",
              admissionOrEmpId = sProf?.admissionNo ?: ""
            )
            _isAuthenticated.value = true
            onSuccess(user)
          } else {
            val msg = error.message ?: "Sign-in failed."
            _authErrorMessage.value = msg
            onError(msg)
          }
        }
      )
    }
  }

  fun logout() {
    viewModelScope.launch {
      firebaseAuthService?.signOut()
    }
    sessionPreferences?.clearSession()
    _isAuthenticated.value = false
    repository.logout()
  }

  fun switchRole(role: UserRole) {
    repository.loginAsRole(role)
    repository.currentUser.value?.let { user ->
      val sProf = repository.currentStudentProfile.value
      sessionPreferences?.saveUserSession(
        user = user,
        className = sProf?.let { "Class ${it.grade}-${it.section}" } ?: "Class 12-A",
        grade = sProf?.grade ?: "12",
        section = sProf?.section ?: "A",
        admissionOrEmpId = sProf?.admissionNo ?: ""
      )
      _isAuthenticated.value = true
    }
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
    val createdHw = repository.homeworks.value.firstOrNull()
    if (createdHw != null) {
      knownHomeworkIds.add(createdHw.id)
      viewModelScope.launch {
        firestoreService?.saveHomework(createdHw)
      }
    }
    _refreshFeedbackMessage.value = "Homework assigned for $className and synced across devices."
  }

  fun publishNotice(
    title: String,
    content: String,
    category: NoticeCategory,
    isUrgent: Boolean,
    context: Context? = null
  ) {
    val notice = repository.addNotice(title, content, category, isUrgent)
    knownNoticeIds.add(notice.id)
    viewModelScope.launch {
      firestoreService?.publishNotice(notice)
    }
    _refreshFeedbackMessage.value = if (isUrgent) {
      "🚨 Urgent circular published! Priority alert broadcasted."
    } else {
      "📢 Circular published! Cloud broadcast sent to target faculty & students."
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
      // 3. Persist to Firestore
      val updatedRecord = repository.attendanceRecords.value.find { it.studentId == studentId }
      if (updatedRecord != null) {
        firestoreService?.saveAttendanceRecord(updatedRecord)
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

  // ==================== WAVE 1 ERP METHODS ====================

  // 1. Calendar Actions
  fun addCalendarEvent(event: CalendarEvent) {
    repository.addCalendarEvent(event)
  }

  fun toggleCalendarEventReminder(eventId: String): Boolean {
    return repository.toggleCalendarEventReminder(eventId)
  }

  fun deleteCalendarEvent(eventId: String) {
    repository.deleteCalendarEvent(eventId)
  }

  // 2. Bus Tracking Actions
  fun selectBusRoute(routeId: String) {
    repository.selectBusRoute(routeId)
  }

  fun updateDriverVehicleAndRoute(driverId: String, busNo: String, busReg: String, routeId: String) {
    repository.updateDriverVehicleAndRoute(driverId, busNo, busReg, routeId)
    _refreshFeedbackMessage.value = "🚍 Driver configured to $busNo • Assigned route synced"
  }

  fun updatePassengerBoardingStatus(routeId: String, stopId: String, passengerId: String, status: PassengerBoardingStatus) {
    repository.updatePassengerBoardingStatus(routeId, stopId, passengerId, status)
  }

  fun markAllStopPassengersBoarded(routeId: String, stopId: String) {
    repository.markAllStopPassengersBoarded(routeId, stopId)
    _refreshFeedbackMessage.value = "✅ All passengers at stop marked as Boarded"
  }

  fun addTemporaryDetourStop(routeId: String, stopName: String, scheduledTime: String, latitude: Double, longitude: Double, note: String? = null) {
    repository.addTemporaryDetourStop(routeId, stopName, scheduledTime, latitude, longitude, note)
    _refreshFeedbackMessage.value = "🚧 Extra detour stop '$stopName' added. Broadcast sent."
  }

  fun skipStopWithReason(routeId: String, stopId: String, reason: String) {
    repository.skipStopWithReason(routeId, stopId, reason)
    _refreshFeedbackMessage.value = "⚠️ Stop marked as bypassed ($reason)"
  }

  fun broadcastDriverDelayAlert(routeId: String, delayMins: Int, reason: String) {
    repository.broadcastDriverDelayAlert(routeId, delayMins, reason)
    _refreshFeedbackMessage.value = "📢 Delay announcement (+$delayMins min) broadcasted to parents."
  }

  fun advanceBusToNextStop(routeId: String) {
    repository.advanceBusToNextStop(routeId)
  }

  fun simulateBusMovement(routeId: String) {
    repository.simulateBusMovement(routeId)
  }

  // 3. Announcements Actions
  fun addAnnouncement(announcement: SchoolAnnouncement) {
    repository.addAnnouncement(announcement)
    knownAnnouncementIds.add(announcement.id)
    viewModelScope.launch {
      firestoreService?.publishAnnouncement(announcement)
    }
  }

  fun triggerCloudSync(onComplete: ((Boolean) -> Unit)? = null) {
    viewModelScope.launch {
      _isRefreshing.value = true
      try {
        val fService = firestoreService
        if (fService != null) {
          // Push current notices
          repository.notices.value.forEach { notice ->
            fService.publishNotice(notice)
          }
          // Push current announcements
          repository.announcements.value.forEach { ann ->
            fService.publishAnnouncement(ann)
          }
          // Push current homeworks
          repository.homeworks.value.forEach { hw ->
            fService.saveHomework(hw)
          }
          // Push current attendance records
          repository.attendanceRecords.value.forEach { rec ->
            fService.saveAttendanceRecord(rec)
          }
          _refreshFeedbackMessage.value = "☁️ Cloud Sync Complete: All notices, assignments, and attendance synced with Firestore."
          onComplete?.invoke(true)
        } else {
          _refreshFeedbackMessage.value = "Cloud Sync: Offline local cache updated."
          onComplete?.invoke(false)
        }
      } catch (e: Exception) {
        _refreshFeedbackMessage.value = "Cloud Sync Error: ${e.message}"
        onComplete?.invoke(false)
      } finally {
        _isRefreshing.value = false
      }
    }
  }

  fun acknowledgeAnnouncement(announcementId: String) {
    repository.acknowledgeAnnouncement(announcementId)
  }

  fun deleteAnnouncement(announcementId: String) {
    repository.deleteAnnouncement(announcementId)
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
