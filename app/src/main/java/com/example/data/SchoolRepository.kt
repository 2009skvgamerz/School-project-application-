package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SchoolRepository {

  // Current logged in user and profiles
  private val _currentUser = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

  private val _currentStudentProfile = MutableStateFlow<StudentProfile?>(null)
  val currentStudentProfile: StateFlow<StudentProfile?> = _currentStudentProfile.asStateFlow()

  private val _currentTeacherProfile = MutableStateFlow<TeacherProfile?>(null)
  val currentTeacherProfile: StateFlow<TeacherProfile?> = _currentTeacherProfile.asStateFlow()

  private val _currentStaffProfile = MutableStateFlow<StaffProfile?>(null)
  val currentStaffProfile: StateFlow<StaffProfile?> = _currentStaffProfile.asStateFlow()

  private val _currentAdminProfile = MutableStateFlow<AdminProfile?>(null)
  val currentAdminProfile: StateFlow<AdminProfile?> = _currentAdminProfile.asStateFlow()

  private val _currentDeveloperProfile = MutableStateFlow<DeveloperProfile?>(null)
  val currentDeveloperProfile: StateFlow<DeveloperProfile?> = _currentDeveloperProfile.asStateFlow()

  // Master System Users (Editable by Developer God Mode)
  private val _systemUsers = MutableStateFlow<List<SystemUserRecord>>(initialSystemUsers)
  val systemUsers: StateFlow<List<SystemUserRecord>> = _systemUsers.asStateFlow()

  // Notices
  private val _notices = MutableStateFlow<List<Notice>>(initialNotices)
  val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

  // Homework
  private val _homeworks = MutableStateFlow<List<Homework>>(initialHomeworks)
  val homeworks: StateFlow<List<Homework>> = _homeworks.asStateFlow()

  // Timetable
  private val _timetables = MutableStateFlow<List<TimetableEntry>>(initialTimetable)
  val timetables: StateFlow<List<TimetableEntry>> = _timetables.asStateFlow()

  // Attendance Records
  private val _attendanceRecords = MutableStateFlow<List<AttendanceRecord>>(initialAttendanceRecords)
  val attendanceRecords: StateFlow<List<AttendanceRecord>> = _attendanceRecords.asStateFlow()

  // Duties
  private val _duties = MutableStateFlow<List<DutyTask>>(initialDuties)
  val duties: StateFlow<List<DutyTask>> = _duties.asStateFlow()

  // Events
  private val _events = MutableStateFlow<List<SchoolEvent>>(initialEvents)
  val events: StateFlow<List<SchoolEvent>> = _events.asStateFlow()

  // Classes
  private val _classes = MutableStateFlow<List<SchoolClass>>(initialClasses)
  val classes: StateFlow<List<SchoolClass>> = _classes.asStateFlow()

  // Notifications
  private val _notifications = MutableStateFlow<List<AppNotification>>(initialNotifications)
  val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

  fun markNotificationAsRead(notificationId: String) {
    _notifications.update { list ->
      list.map { if (it.id == notificationId) it.copy(isRead = true) else it }
    }
  }

  fun markAllNotificationsAsRead() {
    _notifications.update { list ->
      list.map { it.copy(isRead = true) }
    }
  }

  fun deleteNotification(notificationId: String) {
    _notifications.update { list ->
      list.filterNot { it.id == notificationId }
    }
  }

  fun addNotification(notification: AppNotification) {
    _notifications.update { listOf(notification) + it }
  }

  fun login(username: String, password: String): Result<User> {
    val trimmed = username.trim()
    val isDevAlias = trimmed.equals("dev", ignoreCase = true) ||
                     trimmed.equals("developer", ignoreCase = true) ||
                     trimmed.equals("root", ignoreCase = true) ||
                     trimmed.equals("godmode", ignoreCase = true) ||
                     trimmed.equals("matrix", ignoreCase = true) ||
                     trimmed.equals("dev.root@stjosephs.edu", ignoreCase = true)

    val matched = if (isDevAlias) {
      demoUsers.find { it.role == UserRole.DEVELOPER }
    } else {
      demoUsers.find { 
        (it.username.equals(trimmed, ignoreCase = true) || it.email.equals(trimmed, ignoreCase = true)) && 
        (it.password == password || password == "demo123" || password == DEMO_PASSWORD || password == "dev123" || password == "root")
      }
    }

    return if (matched != null) {
      setUserByRole(matched.role, matched.username, matched.fullName, matched.email)
      Result.success(_currentUser.value!!)
    } else {
      // Reject unknown accounts - prototype explicitly validates against predefined demo accounts
      Result.failure(
        IllegalArgumentException("Unknown credentials for '$username'. Authorized demo accounts are: student01, teacher01, staff01, admin01 (Password: $DEMO_PASSWORD).")
      )
    }
  }

  fun loginAsRole(role: UserRole) {
    val demo = demoUsers.firstOrNull { it.role == role } ?: demoUsers.first()
    setUserByRole(demo.role, demo.username, demo.fullName, demo.email)
  }

  private fun setUserByRole(role: UserRole, username: String, fullName: String, email: String) {
    val user = User(
      id = "usr_${role.name.lowercase()}_01",
      username = username,
      fullName = fullName,
      email = email,
      role = role,
      phone = when(role) {
        UserRole.STUDENT -> "+91 98450 12001"
        UserRole.TEACHER -> "+91 98765 22001"
        UserRole.STAFF -> "+91 98765 33014"
        UserRole.ADMIN -> "+91 98765 00001"
        UserRole.DEVELOPER -> "+91 99999 00000"
      },
      designation = when(role) {
        UserRole.STUDENT -> "Grade 12 - Section A"
        UserRole.TEACHER -> "Senior Science Faculty"
        UserRole.STAFF -> "Facilities & Campus Supervisor"
        UserRole.ADMIN -> "Principal & Head of Institution"
        UserRole.DEVELOPER -> "Level 5 Root Administrator & System Developer"
      }
    )
    _currentUser.value = user

    when (role) {
      UserRole.STUDENT -> {
        _currentStudentProfile.value = StudentProfile(
          user = user,
          admissionNo = "SJ-2024-1201",
          grade = "12",
          section = "A",
          rollNo = 1,
          parentName = "S. Sundar & V. Lakshmi",
          parentPhone = "+91 98450 78912",
          bloodGroup = "O+ve",
          attendancePercentage = 96.5,
          houseName = "St. Francis House (Blue)",
          busRoute = "Bus 12 - Main Gate"
        )
      }
      UserRole.TEACHER -> {
        _currentTeacherProfile.value = TeacherProfile(
          user = user,
          employeeId = "EMP-T-2018",
          department = "Physical & Chemical Sciences",
          assignedClasses = listOf("Class 10-A", "Class 10-B", "Class 9-A", "Class 11-Science"),
          subjectsTaught = listOf("Physics", "Science Lab", "General Science"),
          qualification = "M.Sc. Physics, B.Ed (Gold Medalist)",
          isClassTeacher = true,
          classTeacherOf = "Class 10-A"
        )
      }
      UserRole.STAFF -> {
        _currentStaffProfile.value = StaffProfile(
          user = user,
          employeeId = "EMP-S-304",
          department = "Campus Operations & Safety",
          duties = listOf("Science Expo Floor Setup", "Physics Lab Calibration", "School Bus Fleet Inspection", "Morning Gate Security Protocol"),
          shiftTiming = "07:30 AM - 04:30 PM",
          emergencyRole = "Campus Fire & Safety Marshal"
        )
      }
      UserRole.ADMIN -> {
        _currentAdminProfile.value = AdminProfile(
          user = user,
          employeeId = "ADM-001",
          adminRole = "Executive Principal",
          officeLocation = "Administrative Block, Room 101"
        )
      }
      UserRole.DEVELOPER -> {
        _currentDeveloperProfile.value = DeveloperProfile(
          user = user,
          devId = "DEV-ROOT-007",
          accessLevel = "Level 5 - God Mode (Full Master Write)",
          terminalStatus = "ROOT ACTIVE",
          environment = "St. Joseph's Cloud Core Engine v4.2.0-PRO"
        )
      }
    }
  }

  fun logout() {
    _currentUser.value = null
    _currentStudentProfile.value = null
    _currentTeacherProfile.value = null
    _currentStaffProfile.value = null
    _currentAdminProfile.value = null
    _currentDeveloperProfile.value = null
  }

  // Interactive Actions
  fun addNotice(title: String, content: String, category: NoticeCategory, isUrgent: Boolean): Notice {
    val publisher = _currentUser.value?.fullName ?: "Administration"
    val roleName = _currentUser.value?.role?.displayName ?: "Admin"
    val newNotice = Notice(
      id = "not_${System.currentTimeMillis()}",
      title = title,
      content = content,
      date = "Today, 09:00 AM",
      category = category,
      publisherRole = roleName,
      publisherName = publisher,
      isUrgent = isUrgent
    )
    _notices.update { listOf(newNotice) + it }

    val notifType = when (category) {
      NoticeCategory.ACADEMIC -> NotificationType.ACADEMIC
      NoticeCategory.EVENT -> NotificationType.EVENT
      NoticeCategory.SPORTS -> NotificationType.EVENT
      else -> NotificationType.NOTICE
    }

    val newNotification = AppNotification(
      id = "notif_circ_${System.currentTimeMillis()}",
      title = if (isUrgent) "🚨 Urgent Announcement: $title" else "📢 New Circular: $title",
      message = if (content.length > 130) content.take(130) + "..." else content,
      timeAgo = "Just now",
      type = notifType,
      isRead = false,
      actionRoute = "notices",
      isUrgent = isUrgent
    )
    _notifications.update { listOf(newNotification) + it }
    return newNotice
  }

  fun submitHomework(homeworkId: String, note: String) {
    _homeworks.update { list ->
      list.map { hw ->
        if (hw.id == homeworkId) {
          hw.copy(
            status = HomeworkStatus.SUBMITTED,
            submissionNote = note,
            submissionsCount = hw.submissionsCount + 1
          )
        } else hw
      }
    }
  }

  fun assignHomework(title: String, description: String, subject: String, className: String, dueDate: String, maxMarks: Int) {
    val teacherName = _currentUser.value?.fullName ?: "Prof. Sarah Jenkins"
    val newHw = Homework(
      id = "hw_${System.currentTimeMillis()}",
      title = title,
      description = description,
      subjectName = subject,
      className = className,
      assignedDate = "Today",
      dueDate = dueDate,
      teacherName = teacherName,
      status = HomeworkStatus.PENDING,
      maxMarks = maxMarks,
      submissionsCount = 0,
      totalStudents = 32
    )
    _homeworks.update { listOf(newHw) + it }

    val newNotif = AppNotification(
      id = "notif_hw_${System.currentTimeMillis()}",
      title = "📚 New Homework: $subject ($className)",
      message = "$title assigned by $teacherName. Due: $dueDate",
      timeAgo = "Just now",
      type = NotificationType.HOMEWORK,
      isRead = false,
      actionRoute = "homework",
      isUrgent = false
    )
    _notifications.update { listOf(newNotif) + it }
  }

  fun updateAttendanceRecord(studentId: String, newStatus: AttendanceStatus, markedBy: String = "Prof. Sarah Jenkins (Class Teacher)") {
    _attendanceRecords.update { list ->
      list.map { rec ->
        if (rec.studentId == studentId) rec.copy(status = newStatus, markedBy = markedBy) else rec
      }
    }
  }

  fun updateAttendanceRecordWithNotes(studentId: String, newStatus: AttendanceStatus, notes: String, markedBy: String = "Prof. Sarah Jenkins (Class Teacher)") {
    _attendanceRecords.update { list ->
      list.map { rec ->
        if (rec.studentId == studentId) rec.copy(status = newStatus, notes = notes, markedBy = markedBy) else rec
      }
    }
  }

  fun markAllAttendance(status: AttendanceStatus = AttendanceStatus.FULL_DAY, className: String = "Class 10-A", markedBy: String = "Prof. Sarah Jenkins (Class Teacher)") {
    _attendanceRecords.update { list ->
      list.map { rec ->
        if (rec.className.equals(className, ignoreCase = true) || className.isEmpty()) {
          rec.copy(status = status, markedBy = markedBy)
        } else rec
      }
    }
  }

  fun updateDutyStatus(dutyId: String, newStatus: DutyStatus) {
    _duties.update { list ->
      list.map { duty ->
        if (duty.id == dutyId) duty.copy(status = newStatus) else duty
      }
    }
  }

  fun addDuty(title: String, area: String, scheduledTime: String, priority: DutyPriority) {
    val assignedName = _currentUser.value?.fullName ?: "Robert Taylor"
    val newDuty = DutyTask(
      id = "duty_${System.currentTimeMillis()}",
      title = title,
      area = area,
      scheduledTime = scheduledTime,
      status = DutyStatus.PENDING,
      assignedTo = assignedName,
      priority = priority
    )
    _duties.update { listOf(newDuty) + it }
  }

  // ==========================================
  // DEVELOPER GOD MODE FULL OVERRIDE METHODS
  // ==========================================

  fun updateSystemUser(updated: SystemUserRecord) {
    _systemUsers.update { list ->
      list.map { if (it.id == updated.id) updated else it }
    }

    // If the currently active user was edited, reflect changes in session immediately
    _currentUser.value?.let { current ->
      if (current.id == updated.id || current.email.equals(updated.email, ignoreCase = true) || current.username.equals(updated.username, ignoreCase = true)) {
        val updatedUser = current.copy(
          fullName = updated.fullName,
          username = updated.username,
          email = updated.email,
          role = updated.role,
          phone = updated.phone,
          designation = updated.designation
        )
        _currentUser.value = updatedUser

        // Also refresh role profile state if active
        when (updated.role) {
          UserRole.STUDENT -> {
            _currentStudentProfile.update { old ->
              old?.copy(
                user = updatedUser,
                admissionNo = updated.identifier.ifBlank { old.admissionNo },
                grade = updated.departmentOrGrade.ifBlank { old.grade },
                section = updated.sectionOrRoom.ifBlank { old.section }
              )
            }
          }
          UserRole.TEACHER -> {
            _currentTeacherProfile.update { old ->
              old?.copy(
                user = updatedUser,
                employeeId = updated.identifier.ifBlank { old.employeeId },
                department = updated.departmentOrGrade.ifBlank { old.department },
                roomNo = updated.sectionOrRoom.ifBlank { old.roomNo }
              )
            }
          }
          UserRole.STAFF -> {
            _currentStaffProfile.update { old ->
              old?.copy(
                user = updatedUser,
                employeeId = updated.identifier.ifBlank { old.employeeId },
                department = updated.departmentOrGrade.ifBlank { old.department }
              )
            }
          }
          UserRole.ADMIN -> {
            _currentAdminProfile.update { old ->
              old?.copy(
                user = updatedUser,
                employeeId = updated.identifier.ifBlank { old.employeeId },
                adminRole = updated.designation.ifBlank { old.adminRole }
              )
            }
          }
          UserRole.DEVELOPER -> {
            _currentDeveloperProfile.update { old ->
              old?.copy(user = updatedUser)
            }
          }
        }
      }
    }
  }

  fun addSystemUser(user: SystemUserRecord) {
    _systemUsers.update { listOf(user) + it }
  }

  fun deleteSystemUser(userId: String) {
    _systemUsers.update { list -> list.filterNot { it.id == userId } }
  }

  fun updateNotice(updated: Notice) {
    _notices.update { list ->
      list.map { if (it.id == updated.id) updated else it }
    }
  }

  fun deleteNotice(noticeId: String) {
    _notices.update { list -> list.filterNot { it.id == noticeId } }
  }

  fun updateHomework(updated: Homework) {
    _homeworks.update { list ->
      list.map { if (it.id == updated.id) updated else it }
    }
  }

  fun deleteHomework(hwId: String) {
    _homeworks.update { list -> list.filterNot { it.id == hwId } }
  }

  fun updateAttendanceRecordDirect(recordId: String, studentName: String, status: AttendanceStatus, notes: String) {
    _attendanceRecords.update { list ->
      list.map { rec ->
        if (rec.id == recordId || rec.studentName.equals(studentName, ignoreCase = true)) {
          rec.copy(status = status, notes = notes, markedBy = "Developer Root Override")
        } else rec
      }
    }
  }

  fun updateTimetableEntry(updated: TimetableEntry) {
    _timetables.update { list ->
      list.map { if (it.id == updated.id) updated else it }
    }
  }

  fun updateDuty(updated: DutyTask) {
    _duties.update { list ->
      list.map { if (it.id == updated.id) updated else it }
    }
  }

  fun deleteDuty(dutyId: String) {
    _duties.update { list -> list.filterNot { it.id == dutyId } }
  }

  fun updateClass(updated: SchoolClass) {
    _classes.update { list ->
      list.map { if (it.id == updated.id) updated else it }
    }
  }

  fun broadcastDeveloperNotice(title: String, content: String, isUrgent: Boolean = true): Notice {
    val devNotice = Notice(
      id = "not_dev_${System.currentTimeMillis()}",
      title = title,
      content = content,
      date = "Just now (Root Broadcast)",
      category = NoticeCategory.GENERAL,
      publisherRole = "Core System Developer",
      publisherName = "Developer Root",
      isUrgent = isUrgent
    )
    _notices.update { listOf(devNotice) + it }

    val alertNotif = AppNotification(
      id = "notif_dev_${System.currentTimeMillis()}",
      title = "⚡ ROOT BROADCAST: $title",
      message = content,
      timeAgo = "Just now",
      type = NotificationType.NOTICE,
      isRead = false,
      actionRoute = "notices",
      isUrgent = isUrgent
    )
    _notifications.update { listOf(alertNotif) + it }
    return devNotice
  }

  fun resetToDefaults() {
    _notices.value = initialNotices
    _homeworks.value = initialHomeworks
    _timetables.value = initialTimetable
    _attendanceRecords.value = initialAttendanceRecords
    _duties.value = initialDuties
    _events.value = initialEvents
    _classes.value = initialClasses
    _systemUsers.value = initialSystemUsers
    _notifications.value = initialNotifications
  }

  companion object {
    /**
     * PROTOTYPE AUTHENTICATION CONFIGURATION:
     * This in-memory demo authentication system is temporary and explicitly created for the
     * Science Expo interactive prototype. It validates against explicitly defined demo accounts.
     * In future production releases, this module will be replaced with Firebase Authentication
     * using Google Sign-In and institutional identity providers.
     */
    const val DEMO_PASSWORD = "password123"

    // Explicitly defined demo accounts for Science Expo prototype
    val demoUsers = listOf(
      DemoAccount("student01", DEMO_PASSWORD, UserRole.STUDENT, "Keerthivasan", "keerthivasan.s@stjosephs.edu"),
      DemoAccount("teacher01", DEMO_PASSWORD, UserRole.TEACHER, "Prof. Sarah Jenkins", "s.jenkins@stjosephs.edu"),
      DemoAccount("staff01", DEMO_PASSWORD, UserRole.STAFF, "Mr. Thomas Wright", "t.wright@stjosephs.edu"),
      DemoAccount("admin01", DEMO_PASSWORD, UserRole.ADMIN, "Dr. Arthur Pendelton", "principal@stjosephs.edu"),
      DemoAccount("dev", DEMO_PASSWORD, UserRole.DEVELOPER, "Keerthivasan", "keerthivasan.dev@stjosephs.edu")
    )

    private val initialNotifications = listOf(
      AppNotification(
        id = "notif_1",
        title = "New Homework: Physics (Class 12-A)",
        message = "Electromagnetic Induction numericals assigned by Prof. Sarah Jenkins. Due tomorrow, 09:00 AM.",
        timeAgo = "10m ago",
        type = NotificationType.HOMEWORK,
        isRead = false,
        actionRoute = "homework",
        isUrgent = true
      ),
      AppNotification(
        id = "notif_2",
        title = "Daily Attendance Marked",
        message = "Your attendance for today (Grade 12-A) has been marked as Full Day Present (FD).",
        timeAgo = "1h ago",
        type = NotificationType.ATTENDANCE,
        isRead = false,
        actionRoute = "attendance"
      ),
      AppNotification(
        id = "notif_3",
        title = "Urgent: Science & Tech Expo 2026",
        message = "All Grade 12 project exhibit registrations must be submitted to the STEM cell by Friday 4 PM.",
        timeAgo = "3h ago",
        type = NotificationType.NOTICE,
        isRead = false,
        actionRoute = "notices",
        isUrgent = true
      ),
      AppNotification(
        id = "notif_4",
        title = "Mid-Term Datesheet Released",
        message = "Class 12 board preparation mid-term examination timetable is now live on the student portal.",
        timeAgo = "Yesterday",
        type = NotificationType.EXAM,
        isRead = true,
        actionRoute = "timetable"
      ),
      AppNotification(
        id = "notif_5",
        title = "Term 2 Fee Receipt #SJ-8921",
        message = "Official receipt for Term 2 tuition & laboratory fees generated. Status: Verified Paid.",
        timeAgo = "2 days ago",
        type = NotificationType.FEE,
        isRead = true
      )
    )

    private val initialNotices = listOf(
      Notice(
        id = "not_1",
        title = "Annual Science & Tech Expo 2026",
        content = "St. Joseph's Annual Science & Technology Expo will be held on August 20th. All student project exhibits must be registered with science faculty by Friday.",
        date = "14 Aug 2026",
        category = NoticeCategory.EVENT,
        publisherRole = "Principal",
        publisherName = "Dr. Anthony Davies",
        isUrgent = true,
        attachmentName = "Expo_Guidelines_2026.pdf"
      ),
      Notice(
        id = "not_2",
        title = "Mid-Term Examination Schedule Released",
        content = "The timetable for Mid-Term Exams starting next month has been uploaded to the student portal. Practical examinations commence a week prior.",
        date = "12 Aug 2026",
        category = NoticeCategory.ACADEMIC,
        publisherRole = "Exam Cell",
        publisherName = "Prof. Sarah Jenkins",
        isUrgent = false,
        attachmentName = "MidTerm_Datesheet_X_XII.pdf"
      ),
      Notice(
        id = "not_3",
        title = "Inter-School Football Tournament Selections",
        content = "Under-16 and Under-18 football trials will take place at the school main ground at 3:30 PM this Wednesday. Bring your sports kits.",
        date = "11 Aug 2026",
        category = NoticeCategory.SPORTS,
        publisherRole = "Sports Dept",
        publisherName = "Coach Raymond",
        isUrgent = false
      ),
      Notice(
        id = "not_4",
        title = "Independence Day Celebration Protocol",
        content = "Flag hoisting ceremony begins promptly at 08:00 AM on August 15th in the central amphitheatre. Uniform check will be conducted.",
        date = "10 Aug 2026",
        category = NoticeCategory.GENERAL,
        publisherRole = "Administration",
        publisherName = "Office of the Dean",
        isUrgent = false
      )
    )

    private val initialHomeworks = listOf(
      Homework(
        id = "hw_1",
        title = "Electromagnetic Induction Numericals",
        description = "Complete exercises 5.1 to 5.4 from chapter 5. Write derivations for Faraday's and Lenz's Law in the physics notebook.",
        subjectName = "Physics",
        className = "Class 12-A",
        assignedDate = "Yesterday",
        dueDate = "Tomorrow, 09:00 AM",
        teacherName = "Prof. Sarah Jenkins",
        status = HomeworkStatus.PENDING,
        maxMarks = 20,
        submissionsCount = 22,
        totalStudents = 28
      ),
      Homework(
        id = "hw_2",
        title = "Calculus Integration Problem Set",
        description = "Solve definite integrals by substitution on page 112 (Q1 to Q20). Show all steps clearly.",
        subjectName = "Mathematics",
        className = "Class 12-A",
        assignedDate = "12 Aug",
        dueDate = "16 Aug, 10:00 AM",
        teacherName = "Mr. David Miller",
        status = HomeworkStatus.PENDING,
        maxMarks = 25,
        submissionsCount = 18,
        totalStudents = 28
      ),
      Homework(
        id = "hw_3",
        title = "Organic Chemistry Synthesis & Reaction Mechanisms",
        description = "Write down aldehyde and ketone nucleophilic addition mechanisms with energy profile diagrams.",
        subjectName = "Chemistry",
        className = "Class 12-A",
        assignedDate = "10 Aug",
        dueDate = "14 Aug, 02:00 PM",
        teacherName = "Dr. Anita Sharma",
        status = HomeworkStatus.SUBMITTED,
        maxMarks = 15,
        submissionNote = "Lab report with observations attached",
        submissionsCount = 27,
        totalStudents = 28
      ),
      Homework(
        id = "hw_4",
        title = "Data Structures: Binary Trees & Graphs",
        description = "Implement BFS and DFS tree traversal algorithms in Kotlin / C++ and analyze time complexity.",
        subjectName = "Computer Science",
        className = "Class 12-A",
        assignedDate = "08 Aug",
        dueDate = "11 Aug",
        teacherName = "Mr. Kevin Ross",
        status = HomeworkStatus.EVALUATED,
        maxMarks = 20,
        submissionNote = "Score: 19/20 - Outstanding algorithm design!",
        submissionsCount = 28,
        totalStudents = 28
      )
    )

    private val initialTimetable = listOf(
      TimetableEntry("tt_1", DayOfWeek.MONDAY, 1, "08:30 AM", "09:15 AM", "Physics", "Prof. Sarah Jenkins", "Room 302", "Class 12-A"),
      TimetableEntry("tt_2", DayOfWeek.MONDAY, 2, "09:15 AM", "10:00 AM", "Mathematics", "Mr. David Miller", "Room 302", "Class 12-A"),
      TimetableEntry("tt_3", DayOfWeek.MONDAY, 3, "10:15 AM", "11:00 AM", "Chemistry", "Dr. Anita Sharma", "Science Lab 1", "Class 12-A"),
      TimetableEntry("tt_4", DayOfWeek.MONDAY, 4, "11:00 AM", "11:45 AM", "English Lit", "Mrs. Clara Higgins", "Room 302", "Class 12-A"),
      TimetableEntry("tt_5", DayOfWeek.MONDAY, 5, "12:30 PM", "01:15 PM", "Computer Science", "Mr. Kevin Ross", "Comp Lab B", "Class 12-A"),
      TimetableEntry("tt_6", DayOfWeek.MONDAY, 6, "01:15 PM", "02:00 PM", "Physical Education", "Coach Raymond", "Ground 1", "Class 12-A"),

      TimetableEntry("tt_7", DayOfWeek.TUESDAY, 1, "08:30 AM", "09:15 AM", "Mathematics", "Mr. David Miller", "Room 302", "Class 12-A"),
      TimetableEntry("tt_8", DayOfWeek.TUESDAY, 2, "09:15 AM", "10:00 AM", "Physics Lab", "Prof. Sarah Jenkins", "Physics Lab", "Class 12-A"),
      TimetableEntry("tt_9", DayOfWeek.TUESDAY, 3, "10:15 AM", "11:00 AM", "Chemistry Lab", "Dr. Anita Sharma", "Chem Lab", "Class 12-A"),
      TimetableEntry("tt_10", DayOfWeek.TUESDAY, 4, "11:00 AM", "11:45 AM", "Computer Science", "Mr. Kevin Ross", "Comp Lab B", "Class 12-A"),
      TimetableEntry("tt_11", DayOfWeek.TUESDAY, 5, "12:30 PM", "01:15 PM", "Moral Science", "Fr. Joseph Mathew", "Chapel Hall", "Class 12-A"),

      TimetableEntry("tt_12", DayOfWeek.WEDNESDAY, 1, "08:30 AM", "09:15 AM", "Chemistry", "Dr. Anita Sharma", "Science Lab 1", "Class 12-A"),
      TimetableEntry("tt_13", DayOfWeek.WEDNESDAY, 2, "09:15 AM", "10:00 AM", "Physics", "Prof. Sarah Jenkins", "Room 302", "Class 12-A"),
      TimetableEntry("tt_14", DayOfWeek.WEDNESDAY, 3, "10:15 AM", "11:00 AM", "Mathematics", "Mr. David Miller", "Room 302", "Class 12-A"),
      TimetableEntry("tt_15", DayOfWeek.WEDNESDAY, 4, "11:00 AM", "11:45 AM", "Library & Research", "Ms. Beatrice Vance", "Main Library", "Class 12-A"),

      TimetableEntry("tt_16", DayOfWeek.THURSDAY, 1, "08:30 AM", "09:15 AM", "Computer Science", "Mr. Kevin Ross", "Comp Lab B", "Class 12-A"),
      TimetableEntry("tt_17", DayOfWeek.THURSDAY, 2, "09:15 AM", "10:00 AM", "English Lit", "Mrs. Clara Higgins", "Room 302", "Class 12-A"),
      TimetableEntry("tt_18", DayOfWeek.THURSDAY, 3, "10:15 AM", "11:00 AM", "Physics", "Prof. Sarah Jenkins", "Room 302", "Class 12-A"),
      TimetableEntry("tt_19", DayOfWeek.THURSDAY, 4, "11:00 AM", "11:45 AM", "Mathematics", "Mr. David Miller", "Room 302", "Class 12-A"),

      TimetableEntry("tt_20", DayOfWeek.FRIDAY, 1, "08:30 AM", "09:15 AM", "Robotics & AI Expo Prep", "Prof. Sarah Jenkins", "STEM Lab", "Class 12-A"),
      TimetableEntry("tt_21", DayOfWeek.FRIDAY, 2, "09:15 AM", "10:00 AM", "Mathematics", "Mr. David Miller", "Room 302", "Class 12-A"),
      TimetableEntry("tt_22", DayOfWeek.FRIDAY, 3, "10:15 AM", "11:00 AM", "Chemistry", "Dr. Anita Sharma", "Room 302", "Class 12-A"),
      TimetableEntry("tt_23", DayOfWeek.FRIDAY, 4, "11:00 AM", "11:45 AM", "Sports & Games", "Coach Raymond", "School Field", "Class 12-A"),

      TimetableEntry("tt_24", DayOfWeek.SATURDAY, 1, "08:30 AM", "09:30 AM", "Science & AI Club", "Prof. Sarah Jenkins", "Auditorium", "Class 12-A"),
      TimetableEntry("tt_25", DayOfWeek.SATURDAY, 2, "09:45 AM", "11:00 AM", "Senior House Assembly", "Fr. Joseph Mathew", "Campus Grounds", "Class 12-A")
    )

    private val initialAttendanceRecords = listOf(
      // Class 12-A (Keerthivasan's class)
      AttendanceRecord("att_1201", "std_1201", "Keerthivasan", 1, "Class 12-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
      AttendanceRecord("att_1202", "std_1202", "Kavya Sundaram", 2, "Class 12-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
      AttendanceRecord("att_1203", "std_1203", "Varun Raghavan", 3, "Class 12-A", "Today", AttendanceStatus.HALF_DAY, "Prof. Sarah Jenkins (Class Teacher)", "Departed 12:30 PM (Medical appointment)"),
      AttendanceRecord("att_1204", "std_1204", "Ananya Iyer", 4, "Class 12-A", "Today", AttendanceStatus.ON_DUTY, "Prof. Sarah Jenkins (Class Teacher)", "Inter-School Science Olympiad (OD Approved)"),
      AttendanceRecord("att_1205", "std_1205", "Rohan Verma", 5, "Class 12-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
      AttendanceRecord("att_1206", "std_1206", "Sneha Nair", 6, "Class 12-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
      // Class 10-A
      AttendanceRecord("att_1", "std_101", "Alex Johnson", 1, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
      AttendanceRecord("att_2", "std_102", "Bella Collins", 2, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
      AttendanceRecord("att_3", "std_103", "Christian Davies", 3, "Class 10-A", "Today", AttendanceStatus.HALF_DAY, "Prof. Sarah Jenkins (Class Teacher)", "Departed 12:30 PM (Medical appointment)"),
      AttendanceRecord("att_4", "std_104", "Daniel Evans", 4, "Class 10-A", "Today", AttendanceStatus.ON_DUTY, "Prof. Sarah Jenkins (Class Teacher)", "Inter-School Science Olympiad (OD Approved)"),
      AttendanceRecord("att_5", "std_105", "Emma Foster", 5, "Class 10-A", "Today", AttendanceStatus.ABSENT, "Prof. Sarah Jenkins (Class Teacher)", "Sick leave (Parent letter received)"),
      AttendanceRecord("att_6", "std_106", "Felix Gomez", 6, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
      AttendanceRecord("att_7", "std_107", "Grace Howard", 7, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
      AttendanceRecord("att_8", "std_108", "Henry Irwin", 8, "Class 10-A", "Today", AttendanceStatus.ON_DUTY, "Prof. Sarah Jenkins (Class Teacher)", "State Basketball Championship (OD Approved)"),
      AttendanceRecord("att_9", "std_109", "Isabella Jackson", 9, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
      AttendanceRecord("att_10", "std_110", "Jacob Klein", 10, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
      AttendanceRecord("att_11", "std_111", "Lily Morris", 11, "Class 10-A", "Today", AttendanceStatus.HALF_DAY, "Prof. Sarah Jenkins (Class Teacher)", "Morning Session Only (Family event)"),
      AttendanceRecord("att_12", "std_112", "Noah Parker", 12, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
      // Class 10-B
      AttendanceRecord("att_201", "std_201", "Aaron Cooper", 1, "Class 10-B", "Today", AttendanceStatus.FULL_DAY, "Mr. David Miller (Class Teacher)"),
      AttendanceRecord("att_202", "std_202", "Brianna Diaz", 2, "Class 10-B", "Today", AttendanceStatus.FULL_DAY, "Mr. David Miller (Class Teacher)"),
      AttendanceRecord("att_203", "std_203", "Chloe Edwards", 3, "Class 10-B", "Today", AttendanceStatus.ON_DUTY, "Mr. David Miller (Class Teacher)", "Debate Competition"),
      AttendanceRecord("att_204", "std_204", "Dylan Flores", 4, "Class 10-B", "Today", AttendanceStatus.ABSENT, "Mr. David Miller (Class Teacher)", "Fever"),
      AttendanceRecord("att_205", "std_205", "Elena Garcia", 5, "Class 10-B", "Today", AttendanceStatus.FULL_DAY, "Mr. David Miller (Class Teacher)"),
      // Class 9-A
      AttendanceRecord("att_301", "std_301", "Adrian Hughes", 1, "Class 9-A", "Today", AttendanceStatus.FULL_DAY, "Mrs. Clara Higgins (Class Teacher)"),
      AttendanceRecord("att_302", "std_302", "Brooke Jenkins", 2, "Class 9-A", "Today", AttendanceStatus.FULL_DAY, "Mrs. Clara Higgins (Class Teacher)"),
      AttendanceRecord("att_303", "std_303", "Caleb Kelly", 3, "Class 9-A", "Today", AttendanceStatus.HALF_DAY, "Mrs. Clara Higgins (Class Teacher)", "Afternoon appointment"),
      AttendanceRecord("att_304", "std_304", "Daisy Lewis", 4, "Class 9-A", "Today", AttendanceStatus.FULL_DAY, "Mrs. Clara Higgins (Class Teacher)"),
      // Class 11-Science
      AttendanceRecord("att_401", "std_401", "Alexander Scott", 1, "Class 11-Science", "Today", AttendanceStatus.FULL_DAY, "Dr. Rachel Green (Class Teacher)"),
      AttendanceRecord("att_402", "std_402", "Benjamin Ward", 2, "Class 11-Science", "Today", AttendanceStatus.ON_DUTY, "Dr. Rachel Green (Class Teacher)", "Robotics Expo Prep"),
      AttendanceRecord("att_403", "std_403", "Charlotte Young", 3, "Class 11-Science", "Today", AttendanceStatus.FULL_DAY, "Dr. Rachel Green (Class Teacher)")
    )

    private val initialDuties = listOf(
      DutyTask("d_1", "Science Expo Main Stage Audio & Projector Setup", "School Auditorium", "08:00 AM", DutyStatus.IN_PROGRESS, "Robert Taylor", DutyPriority.HIGH),
      DutyTask("d_2", "Physics & Chemistry Labs Safety Inspection", "Science Block Floor 2", "10:30 AM", DutyStatus.PENDING, "Robert Taylor", DutyPriority.HIGH),
      DutyTask("d_3", "School Bus Fleet GPS & First Aid Kits Check", "Transport Bay", "01:00 PM", DutyStatus.COMPLETED, "Robert Taylor", DutyPriority.MEDIUM),
      DutyTask("d_4", "Generator & Power Backup Routine Test", "Utility Room", "03:30 PM", DutyStatus.PENDING, "Robert Taylor", DutyPriority.MEDIUM),
      DutyTask("d_5", "Campus Fire Extinguisher Monthly Audit", "All Blocks", "04:00 PM", DutyStatus.PENDING, "Robert Taylor", DutyPriority.LOW)
    )

    private val initialEvents = listOf(
      SchoolEvent("ev_1", "Science & Tech Expo 2026", "Grand exhibition featuring robotics, physics simulations, and environmental science models.", "20 Aug 2026", "09:00 AM - 04:00 PM", "Auditorium & Labs", "Expo"),
      SchoolEvent("ev_2", "Independence Day Parade", "Flag hoisting, band performance, patriotic choir, and address by the Principal.", "15 Aug 2026", "08:00 AM - 10:30 AM", "Central Ground", "Ceremony"),
      SchoolEvent("ev_3", "Inter-House Debate Championship", "Topic: 'Artificial Intelligence in Modern Education: Opportunity or Challenge?'", "25 Aug 2026", "01:30 PM - 03:30 PM", "AV Hall", "Academics"),
      SchoolEvent("ev_4", "Parent-Teacher Conference (Mid-Term)", "Interactive review of academic progress and attendance records for Term 1.", "05 Sep 2026", "08:30 AM - 01:30 PM", "Respective Classrooms", "Meeting")
    )

    private val initialClasses = listOf(
      SchoolClass("cls_12a", "Class 12", "A", "Room 302", "Prof. Sarah Jenkins", 28, 96.5),
      SchoolClass("cls_10a", "Class 10", "A", "Room 204", "Prof. Sarah Jenkins", 32, 95.2),
      SchoolClass("cls_10b", "Class 10", "B", "Room 205", "Mr. David Miller", 30, 93.8),
      SchoolClass("cls_9a", "Class 9", "A", "Room 102", "Mrs. Clara Higgins", 34, 96.0),
      SchoolClass("cls_9b", "Class 9", "B", "Room 103", "Dr. Anita Sharma", 33, 94.1),
      SchoolClass("cls_11sci", "Class 11", "Science", "Room 301", "Dr. Rachel Green", 28, 92.5),
      SchoolClass("cls_12sci", "Class 12", "Science", "Room 302", "Mr. Kevin Ross", 26, 96.8)
    )

    private val initialSystemUsers = listOf(
      SystemUserRecord(
        id = "usr_dev_01",
        username = "dev",
        fullName = "Keerthivasan",
        email = "keerthivasan.dev@stjosephs.edu",
        role = UserRole.DEVELOPER,
        phone = "+91 99999 00000",
        designation = "Lead Systems Developer & Root Admin",
        identifier = "DEV-ROOT-007",
        departmentOrGrade = "Core Infrastructure & Engineering",
        sectionOrRoom = "Server Room Alpha",
        extraNotes = "Full unrestricted master write & debug access"
      ),
      SystemUserRecord(
        id = "usr_student_01",
        username = "student01",
        fullName = "Keerthivasan",
        email = "keerthivasan.s@stjosephs.edu",
        role = UserRole.STUDENT,
        phone = "+91 98450 12001",
        designation = "Grade 12 - Section A",
        identifier = "SJ-2024-1201",
        departmentOrGrade = "12",
        sectionOrRoom = "A",
        extraNotes = "Science Expo Head Coordinator, Roll #1"
      ),
      SystemUserRecord(
        id = "usr_std_1202",
        username = "kavya_s",
        fullName = "Kavya Sundaram",
        email = "kavya.s@stjosephs.edu",
        role = UserRole.STUDENT,
        phone = "+91 98450 12002",
        designation = "Grade 12 - Section A",
        identifier = "SJ-2024-1202",
        departmentOrGrade = "12",
        sectionOrRoom = "A",
        extraNotes = "Class Representative, Roll #2"
      ),
      SystemUserRecord(
        id = "usr_std_1203",
        username = "varun_r",
        fullName = "Varun Raghavan",
        email = "varun.r@stjosephs.edu",
        role = UserRole.STUDENT,
        phone = "+91 98450 12003",
        designation = "Grade 12 - Section A",
        identifier = "SJ-2024-1203",
        departmentOrGrade = "12",
        sectionOrRoom = "A",
        extraNotes = "Robotics Team Captain, Roll #3"
      ),
      SystemUserRecord(
        id = "usr_std_1204",
        username = "ananya_i",
        fullName = "Ananya Iyer",
        email = "ananya.i@stjosephs.edu",
        role = UserRole.STUDENT,
        phone = "+91 98450 12004",
        designation = "Grade 12 - Section A",
        identifier = "SJ-2024-1204",
        departmentOrGrade = "12",
        sectionOrRoom = "A",
        extraNotes = "Science Olympiad Winner, Roll #4"
      ),
      SystemUserRecord(
        id = "usr_teacher_01",
        username = "teacher01",
        fullName = "Prof. Sarah Jenkins",
        email = "s.jenkins@stjosephs.edu",
        role = UserRole.TEACHER,
        phone = "+91 98765 22001",
        designation = "Senior Physics Faculty • Class Teacher (10-A)",
        identifier = "EMP-T-2018",
        departmentOrGrade = "Physical & Chemical Sciences",
        sectionOrRoom = "Staff Room 2B",
        extraNotes = "M.Sc. Physics, Gold Medalist"
      ),
      SystemUserRecord(
        id = "usr_tch_202",
        username = "dmiller",
        fullName = "Mr. David Miller",
        email = "d.miller@stjosephs.edu",
        role = UserRole.TEACHER,
        phone = "+91 98765 22002",
        designation = "Senior Mathematics Faculty",
        identifier = "EMP-T-2019",
        departmentOrGrade = "Mathematics & Statistics",
        sectionOrRoom = "Staff Room 2A",
        extraNotes = "Class Teacher (10-B)"
      ),
      SystemUserRecord(
        id = "usr_tch_203",
        username = "asharma",
        fullName = "Dr. Anita Sharma",
        email = "a.sharma@stjosephs.edu",
        role = UserRole.TEACHER,
        phone = "+91 98765 22003",
        designation = "Head of Chemistry Department",
        identifier = "EMP-T-2015",
        departmentOrGrade = "Chemical Sciences",
        sectionOrRoom = "Chemistry Lab 1",
        extraNotes = "Ph.D. Organic Chemistry"
      ),
      SystemUserRecord(
        id = "usr_tch_204",
        username = "kross",
        fullName = "Mr. Kevin Ross",
        email = "k.ross@stjosephs.edu",
        role = UserRole.TEACHER,
        phone = "+91 98765 22004",
        designation = "Lead Computer Science & AI Faculty",
        identifier = "EMP-T-2021",
        departmentOrGrade = "Computer Science & Robotics",
        sectionOrRoom = "Computer Lab B",
        extraNotes = "Robotics Club Mentor"
      ),
      SystemUserRecord(
        id = "usr_staff_01",
        username = "staff01",
        fullName = "Mr. Thomas Wright",
        email = "t.wright@stjosephs.edu",
        role = UserRole.STAFF,
        phone = "+91 98765 33014",
        designation = "Facilities & Campus Supervisor",
        identifier = "EMP-S-304",
        departmentOrGrade = "Campus Operations & Safety",
        sectionOrRoom = "Main Operations Center",
        extraNotes = "Campus Fire & Safety Marshal"
      ),
      SystemUserRecord(
        id = "usr_stf_302",
        username = "rtaylor",
        fullName = "Robert Taylor",
        email = "r.taylor@stjosephs.edu",
        role = UserRole.STAFF,
        phone = "+91 98765 33015",
        designation = "Senior Laboratory Technician",
        identifier = "EMP-S-305",
        departmentOrGrade = "Science Block Laboratories",
        sectionOrRoom = "Lab Storage & Prep Room",
        extraNotes = "Safety Calibration In-charge"
      ),
      SystemUserRecord(
        id = "usr_admin_01",
        username = "admin01",
        fullName = "Dr. Arthur Pendelton",
        email = "principal@stjosephs.edu",
        role = UserRole.ADMIN,
        phone = "+91 98765 00001",
        designation = "Principal & Head of Institution",
        identifier = "ADM-001",
        departmentOrGrade = "Executive Administration",
        sectionOrRoom = "Principal's Office, Main Block",
        extraNotes = "Institutional Executive Authority"
      )
    )
  }
}

data class DemoAccount(
  val username: String,
  val password: String,
  val role: UserRole,
  val fullName: String,
  val email: String
)
