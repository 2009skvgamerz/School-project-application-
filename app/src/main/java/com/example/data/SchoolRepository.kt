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

  private val _currentDriverProfile = MutableStateFlow<DriverProfile?>(null)
  val currentDriverProfile: StateFlow<DriverProfile?> = _currentDriverProfile.asStateFlow()

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

  // ==================== WAVE 1 ERP STATE FLOWS ====================
  // 1. Calendar Events
  private val _calendarEvents = MutableStateFlow<List<CalendarEvent>>(initialCalendarEvents)
  val calendarEvents: StateFlow<List<CalendarEvent>> = _calendarEvents.asStateFlow()

  // 2. Bus Routes & Tracking
  private val _busRoutes = MutableStateFlow<List<BusRoute>>(initialBusRoutes)
  val busRoutes: StateFlow<List<BusRoute>> = _busRoutes.asStateFlow()

  private val _selectedBusRouteId = MutableStateFlow("route_12")
  val selectedBusRouteId: StateFlow<String> = _selectedBusRouteId.asStateFlow()

  // 3. School Announcements
  private val _announcements = MutableStateFlow<List<SchoolAnnouncement>>(initialAnnouncements)
  val announcements: StateFlow<List<SchoolAnnouncement>> = _announcements.asStateFlow()

  // 4. Directory Contacts
  private val _directoryContacts = MutableStateFlow<List<DirectoryContact>>(initialDirectoryContacts)
  val directoryContacts: StateFlow<List<DirectoryContact>> = _directoryContacts.asStateFlow()

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

  fun loginWithUser(user: User) {
    _currentUser.value = user
    when (user.role) {
      UserRole.STUDENT -> {
        _currentStudentProfile.value = StudentProfile(
          user = user,
          admissionNo = "SJ-${user.id.take(8).uppercase()}",
          grade = "12",
          section = "A",
          rollNo = 1,
          parentName = "Verified Parent / Guardian",
          parentPhone = user.phone,
          bloodGroup = "B+ve",
          attendancePercentage = 97.5,
          houseName = "St. Francis House (Blue)",
          busRoute = "Bus 12 - Main Gate"
        )
      }
      UserRole.TEACHER -> {
        _currentTeacherProfile.value = TeacherProfile(
          user = user,
          employeeId = "EMP-${user.id.take(6).uppercase()}",
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
          employeeId = "STF-${user.id.take(6).uppercase()}",
          department = "Facilities & Operations",
          duties = listOf("Campus Supervision", "Lab Support", "Safety Protocol Oversight"),
          shiftTiming = "08:00 AM - 04:30 PM"
        )
      }
      UserRole.DRIVER -> {
        _currentDriverProfile.value = DriverProfile(
          user = user,
          driverId = "DRV-${user.id.take(6).uppercase()}",
          assignedBusNo = "Bus #12",
          shift = "Morning Shift (06:45 AM - 09:30 AM)"
        )
      }
      UserRole.ADMIN -> {
        _currentAdminProfile.value = AdminProfile(
          user = user,
          employeeId = "ADM-${user.id.take(6).uppercase()}",
          adminRole = "Principal & System Administrator"
        )
      }
      UserRole.DEVELOPER -> {
        _currentDeveloperProfile.value = DeveloperProfile(
          user = user,
          devId = "DEV-${user.id.take(6).uppercase()}"
        )
      }
    }
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
        UserRole.DRIVER -> "+91 98451 22334"
        UserRole.ADMIN -> "+91 98765 00001"
        UserRole.DEVELOPER -> "+91 99999 00000"
      },
      designation = when(role) {
        UserRole.STUDENT -> "Grade 12 - Section A"
        UserRole.TEACHER -> "Senior Science Faculty"
        UserRole.STAFF -> "Facilities & Campus Supervisor"
        UserRole.DRIVER -> "Senior Fleet Pilot & Transit In-Charge"
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
      UserRole.DRIVER -> {
        _currentDriverProfile.value = DriverProfile(
          user = user,
          driverId = "DRV-102",
          licenseNo = "KA-01-2015-DL99482",
          assignedBusNo = "Bus #12",
          busRegistration = "KA-04-SJ-1012",
          assignedRouteId = "route_12",
          shift = "Morning Shift (06:45 AM - 09:30 AM)",
          experienceYears = 12,
          attendantName = "Mrs. Sunita Devi",
          attendantPhone = "+91 98451 99881",
          isTripActive = true
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
    _currentDriverProfile.value = null
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
      date = com.example.util.SchoolDateTimeUtils.getCurrentNoticeDateString(),
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

  fun publishNotice(notice: Notice) {
    _notices.update { list ->
      val index = list.indexOfFirst { it.id == notice.id }
      if (index != -1) {
        val updated = list.toMutableList()
        updated[index] = notice
        updated
      } else {
        listOf(notice) + list
      }
    }
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

  fun syncAnnouncement(announcement: SchoolAnnouncement) {
    _announcements.update { list ->
      val index = list.indexOfFirst { it.id == announcement.id }
      if (index != -1) {
        val updated = list.toMutableList()
        updated[index] = announcement
        updated
      } else {
        listOf(announcement) + list
      }
    }
  }

  fun syncAttendanceRecord(record: AttendanceRecord) {
    _attendanceRecords.update { list ->
      val index = list.indexOfFirst { it.id == record.id || it.studentId == record.studentId }
      if (index != -1) {
        val updated = list.toMutableList()
        updated[index] = record
        updated
      } else {
        list + record
      }
    }
  }

  fun syncHomework(hw: Homework) {
    _homeworks.update { list ->
      val index = list.indexOfFirst { it.id == hw.id }
      if (index != -1) {
        val updated = list.toMutableList()
        updated[index] = hw
        updated
      } else {
        listOf(hw) + list
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
          UserRole.DRIVER -> {
            _currentDriverProfile.update { old ->
              old?.copy(
                user = updatedUser,
                driverId = updated.identifier.ifBlank { old.driverId }
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

  // ==================== WAVE 1 ERP METHODS ====================

  // 1. Calendar
  fun syncCalendarEvent(event: CalendarEvent) {
    _calendarEvents.update { list ->
      val index = list.indexOfFirst { it.id == event.id }
      if (index != -1) {
        val updated = list.toMutableList()
        updated[index] = event
        updated
      } else {
        listOf(event) + list
      }
    }
  }

  fun addCalendarEvent(event: CalendarEvent) {
    _calendarEvents.update { listOf(event) + it }
    addNotification(
      AppNotification(
        id = "notif_evt_${System.currentTimeMillis()}",
        title = "📅 New Calendar Event: ${event.title}",
        message = "${event.formattedDate} • ${event.time} at ${event.location}",
        timeAgo = "Just now",
        type = NotificationType.EVENT,
        isRead = false,
        actionRoute = "calendar"
      )
    )
  }

  fun toggleCalendarEventReminder(eventId: String): Boolean {
    var newState = false
    _calendarEvents.update { list ->
      list.map {
        if (it.id == eventId) {
          newState = !it.hasReminder
          it.copy(hasReminder = newState)
        } else it
      }
    }
    return newState
  }

  fun deleteCalendarEvent(eventId: String) {
    _calendarEvents.update { list -> list.filterNot { it.id == eventId } }
  }

  // 2. Bus Tracking
  fun selectBusRoute(routeId: String) {
    _selectedBusRouteId.value = routeId
  }

  fun updateDriverVehicleAndRoute(driverId: String, busNo: String, busReg: String, routeId: String) {
    _currentDriverProfile.update { current ->
      current?.copy(
        assignedBusNo = busNo,
        busRegistration = busReg,
        assignedRouteId = routeId
      )
    }
    _busRoutes.update { list ->
      list.map { route ->
        if (route.id == routeId) {
          route.copy(
            routeNumber = busNo,
            busRegistration = busReg,
            driverName = _currentUser.value?.fullName ?: route.driverName,
            driverPhone = _currentUser.value?.phone ?: route.driverPhone
          )
        } else route
      }
    }
  }

  fun updatePassengerBoardingStatus(routeId: String, stopId: String, passengerId: String, status: PassengerBoardingStatus) {
    _busRoutes.update { list ->
      list.map { route ->
        if (route.id == routeId) {
          val updatedStops = route.stops.map { stop ->
            if (stop.id == stopId) {
              val updatedPassengers = stop.passengers.map { p ->
                if (p.id == passengerId) {
                  p.copy(
                    boardingStatus = status,
                    checkInTime = if (status == PassengerBoardingStatus.BOARDED) "Just now" else null
                  )
                } else p
              }
              stop.copy(
                passengers = updatedPassengers,
                studentCount = updatedPassengers.count { it.boardingStatus != PassengerBoardingStatus.ABSENT }
              )
            } else stop
          }
          val totalOnboard = updatedStops.flatMap { it.passengers }.count { it.boardingStatus == PassengerBoardingStatus.BOARDED }
          route.copy(stops = updatedStops, studentsOnboard = totalOnboard)
        } else route
      }
    }
  }

  fun markAllStopPassengersBoarded(routeId: String, stopId: String) {
    _busRoutes.update { list ->
      list.map { route ->
        if (route.id == routeId) {
          val updatedStops = route.stops.map { stop ->
            if (stop.id == stopId) {
              val updatedPassengers = stop.passengers.map { p ->
                if (p.boardingStatus == PassengerBoardingStatus.WAITING) {
                  p.copy(boardingStatus = PassengerBoardingStatus.BOARDED, checkInTime = "Just now")
                } else p
              }
              stop.copy(passengers = updatedPassengers)
            } else stop
          }
          val totalOnboard = updatedStops.flatMap { it.passengers }.count { it.boardingStatus == PassengerBoardingStatus.BOARDED }
          route.copy(stops = updatedStops, studentsOnboard = totalOnboard)
        } else route
      }
    }
  }

  fun addTemporaryDetourStop(routeId: String, stopName: String, scheduledTime: String, latitude: Double, longitude: Double, note: String? = null) {
    _busRoutes.update { list ->
      list.map { route ->
        if (route.id == routeId) {
          val newStop = BusStop(
            id = "st_detour_${System.currentTimeMillis()}",
            name = "$stopName (Detour)",
            scheduledTime = scheduledTime,
            isCompleted = false,
            isCurrent = false,
            isExtraDetourStop = true,
            skipReason = note,
            studentCount = 2,
            latitude = latitude,
            longitude = longitude,
            passengers = listOf(
              BusPassenger(
                id = "p_detour_${System.currentTimeMillis()}",
                name = "Added Reroute Passenger",
                role = UserRole.STUDENT,
                gradeAndSection = "Class 10-A",
                rollNo = 30,
                parentName = "Parent / Guardian",
                parentPhone = "+91 98450 78912",
                emergencyPhone = "+91 98450 78912",
                boardingStatus = PassengerBoardingStatus.WAITING,
                stopId = "st_detour_${System.currentTimeMillis()}",
                stopName = stopName
              )
            )
          )
          val insertIndex = (route.stops.size - 1).coerceAtLeast(1)
          val updatedStops = route.stops.toMutableList().apply { add(insertIndex, newStop) }
          route.copy(
            stops = updatedStops,
            activeDetourAlert = "🚧 Driver added temporary stop: $stopName" + (if (!note.isNullOrBlank()) " ($note)" else "")
          )
        } else route
      }
    }
    addNotification(
      AppNotification(
        id = "notif_bus_detour_${System.currentTimeMillis()}",
        title = "🚧 Route Detour Added ($stopName)",
        message = "Bus driver added extra stop at $stopName due to road conditions.",
        timeAgo = "Just now",
        type = NotificationType.NOTICE,
        isRead = false,
        actionRoute = "bus",
        isUrgent = true
      )
    )
  }

  fun skipStopWithReason(routeId: String, stopId: String, reason: String) {
    _busRoutes.update { list ->
      list.map { route ->
        if (route.id == routeId) {
          val updatedStops = route.stops.map { stop ->
            if (stop.id == stopId) {
              stop.copy(isSkipped = true, skipReason = reason, isCompleted = true)
            } else stop
          }
          val skippedStopName = route.stops.find { it.id == stopId }?.name ?: "Stop"
          route.copy(
            stops = updatedStops,
            activeDetourAlert = "⚠️ $skippedStopName SKIPPED: $reason"
          )
        } else route
      }
    }
    addNotification(
      AppNotification(
        id = "notif_bus_skip_${System.currentTimeMillis()}",
        title = "⚠️ Bus Stop Skipped Notice",
        message = "Driver bypassed stop due to: $reason. Next stop active.",
        timeAgo = "Just now",
        type = NotificationType.NOTICE,
        isRead = false,
        actionRoute = "bus",
        isUrgent = true
      )
    )
  }

  fun broadcastDriverDelayAlert(routeId: String, delayMins: Int, reason: String) {
    _busRoutes.update { list ->
      list.map { route ->
        if (route.id == routeId) {
          route.copy(
            delayMinutes = delayMins,
            delayReason = reason,
            status = if (delayMins > 0) BusStatus.DELAYED else BusStatus.ON_TIME,
            activeDetourAlert = if (delayMins > 0) "⚠️ +$delayMins min delay: $reason" else null
          )
        } else route
      }
    }
    addNotification(
      AppNotification(
        id = "notif_driver_delay_${System.currentTimeMillis()}",
        title = "⏱️ Bus Delay Notice: +$delayMins Mins",
        message = "$reason. Live ETA has been recalculated for all stops.",
        timeAgo = "Just now",
        type = NotificationType.NOTICE,
        isRead = false,
        actionRoute = "bus",
        isUrgent = true
      )
    )
  }

  fun advanceBusToNextStop(routeId: String) {
    _busRoutes.update { list ->
      list.map { route ->
        if (route.id == routeId) {
          val currentIndex = route.stops.indexOfFirst { it.isCurrent }.let { if (it == -1) 0 else it }
          val nextIndex = (currentIndex + 1).coerceAtMost(route.stops.size - 1)
          val nextStop = route.stops[nextIndex]
          val updatedStops = route.stops.mapIndexed { idx, stop ->
            stop.copy(
              isCompleted = idx < nextIndex,
              isCurrent = idx == nextIndex
            )
          }
          val newProgress = (nextIndex.toFloat() / (route.stops.size - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
          val newStatus = if (nextIndex == route.stops.size - 1) BusStatus.ARRIVED else route.status
          route.copy(
            stops = updatedStops,
            progressPercent = newProgress,
            currentLocationName = nextStop.name,
            nextStopName = if (nextIndex < route.stops.size - 1) route.stops[nextIndex + 1].name else "Campus Destination",
            currentLatitude = nextStop.latitude,
            currentLongitude = nextStop.longitude,
            status = newStatus,
            estimatedArrivalMins = ((1f - newProgress) * 20).toInt().coerceAtLeast(1)
          )
        } else route
      }
    }
  }

  fun simulateBusMovement(routeId: String) {
    _busRoutes.update { list ->
      list.map { route ->
        if (route.id == routeId) {
          val nextProgress = (route.progressPercent + 0.10f).let { if (it > 1.0f) 0.05f else it }
          val stopIndex = (nextProgress * route.stops.size).toInt().coerceIn(0, route.stops.size - 1)
          val updatedStops = route.stops.mapIndexed { idx, stop ->
            stop.copy(
              isCompleted = idx < stopIndex,
              isCurrent = idx == stopIndex
            )
          }
          val currentStopName = updatedStops.getOrNull(stopIndex)?.name ?: route.currentLocationName
          val nextStop = updatedStops.getOrNull(stopIndex + 1)?.name ?: "Campus Main Gate (Final)"
          val newSpeed = if (nextProgress >= 0.95f) 0 else (28..48).random()
          val newStatus = when {
            nextProgress >= 0.95f -> BusStatus.ARRIVED
            route.delayMinutes > 0 -> BusStatus.DELAYED
            else -> BusStatus.ON_TIME
          }
          val remainingMins = ((1.0f - nextProgress) * 25).toInt().coerceAtLeast(1)

          // Interpolate GPS coordinates between stops
          val totalSegments = (route.stops.size - 1).coerceAtLeast(1)
          val scaled = nextProgress * totalSegments
          val segIdx = scaled.toInt().coerceIn(0, totalSegments - 1)
          val fraction = (scaled - segIdx).coerceIn(0f, 1f)

          val p1 = route.stops[segIdx]
          val p2 = route.stops[(segIdx + 1).coerceAtMost(route.stops.size - 1)]

          val newLat = p1.latitude + (p2.latitude - p1.latitude) * fraction
          val newLng = p1.longitude + (p2.longitude - p1.longitude) * fraction

          val dLat = p2.latitude - p1.latitude
          val dLng = p2.longitude - p1.longitude
          val newHeading = ((Math.toDegrees(Math.atan2(dLng, dLat)).toFloat() + 360f) % 360f)

          route.copy(
            progressPercent = nextProgress,
            currentLocationName = currentStopName,
            nextStopName = nextStop,
            currentSpeedKmH = newSpeed,
            status = newStatus,
            stops = updatedStops,
            estimatedArrivalMins = remainingMins,
            currentLatitude = newLat,
            currentLongitude = newLng,
            currentHeadingDegrees = if (newSpeed > 0) newHeading else route.currentHeadingDegrees
          )
        } else route
      }
    }
  }

  // 3. Announcements
  fun addAnnouncement(announcement: SchoolAnnouncement) {
    _announcements.update { listOf(announcement) + it }
    addNotification(
      AppNotification(
        id = "notif_ann_${System.currentTimeMillis()}",
        title = if (announcement.isEmergency) "🚨 URGENT: ${announcement.title}" else "📢 Announcement: ${announcement.title}",
        message = announcement.content,
        timeAgo = "Just now",
        type = NotificationType.ANNOUNCEMENT,
        isRead = false,
        actionRoute = "announcements",
        isUrgent = announcement.isEmergency
      )
    )
  }

  fun acknowledgeAnnouncement(announcementId: String) {
    _announcements.update { list ->
      list.map {
        if (it.id == announcementId) {
          it.copy(
            acknowledgedByCurrentUser = true,
            acknowledgmentsCount = it.acknowledgmentsCount + 1
          )
        } else it
      }
    }
  }

  fun deleteAnnouncement(announcementId: String) {
    _announcements.update { list -> list.filterNot { it.id == announcementId } }
  }

  // 4. Role-Gated School Directory
  fun getSanitizedDirectoryContacts(viewingRole: UserRole): List<DirectoryContact> {
    val master = _directoryContacts.value
    return if (viewingRole == UserRole.STUDENT) {
      // STRICT STUDENT PRIVACY: Redact peer students' personal phone numbers and parent phones!
      // Teachers, Staff, Administration, Bus Drivers, and Helplines remain fully visible and clickable.
      master.map { contact ->
        if (contact.isStudent) {
          contact.copy(
            phoneNumber = "🔒 Hidden for Student Privacy",
            parentContact = "🔒 Hidden for Student Privacy"
          )
        } else {
          contact
        }
      }
    } else {
      master
    }
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
    _calendarEvents.value = initialCalendarEvents
    _busRoutes.value = initialBusRoutes
    _announcements.value = initialAnnouncements
    _directoryContacts.value = initialDirectoryContacts
  }

  companion object {
    const val DEMO_PASSWORD = com.example.data.seed.UsersAndDirectorySeedData.DEMO_PASSWORD
    val demoUsers = com.example.data.seed.UsersAndDirectorySeedData.demoUsers

    private val initialNotifications = com.example.data.seed.AcademicSeedData.initialNotifications
    private val initialNotices = com.example.data.seed.AcademicSeedData.initialNotices
    private val initialHomeworks = com.example.data.seed.AcademicSeedData.initialHomeworks
    private val initialTimetable = com.example.data.seed.AcademicSeedData.initialTimetable
    private val initialAttendanceRecords = com.example.data.seed.AcademicSeedData.initialAttendanceRecords
    private val initialDuties = com.example.data.seed.AcademicSeedData.initialDuties
    private val initialEvents = com.example.data.seed.AcademicSeedData.initialEvents
    private val initialClasses = com.example.data.seed.AcademicSeedData.initialClasses
    private val initialSystemUsers = com.example.data.seed.UsersAndDirectorySeedData.initialSystemUsers
    private val initialCalendarEvents = com.example.data.seed.TransportAndEventsSeedData.initialCalendarEvents
    private val initialBusRoutes = com.example.data.seed.TransportAndEventsSeedData.initialBusRoutes
    private val initialAnnouncements = com.example.data.seed.TransportAndEventsSeedData.initialAnnouncements
    private val initialDirectoryContacts = com.example.data.seed.UsersAndDirectorySeedData.initialDirectoryContacts
  }
}

data class DemoAccount(
  val username: String,
  val password: String,
  val role: UserRole,
  val fullName: String,
  val email: String
)
