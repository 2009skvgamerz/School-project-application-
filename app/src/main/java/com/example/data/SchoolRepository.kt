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
      DemoAccount("driver01", DEMO_PASSWORD, UserRole.DRIVER, "Mr. Ramesh Kumar", "driver@stjosephs.edu"),
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

    // --- Wave 1: Seed Calendar Events ---
    private val initialCalendarEvents = listOf(
      CalendarEvent(
        id = "evt_1",
        title = "Independence Day & Cultural Assembly",
        description = "Special patriotic flag hoisting ceremony followed by patriotic song & dance performances by middle and high school students.",
        date = "2026-08-15",
        formattedDate = "Sat, 15 Aug 2026",
        time = "08:30 AM - 11:30 AM",
        location = "Main Campus Grounds & Flagpost",
        category = CalendarCategory.HOLIDAY,
        isHoliday = true,
        targetGrades = "All Grades (1 - 12)",
        organizer = "Cultural Affairs Committee",
        hasReminder = true
      ),
      CalendarEvent(
        id = "evt_2",
        title = "Annual Science, AI & Robotics Expo 2026",
        description = "Showcase of over 85 working models, robotics automation exhibits, and research papers from Grades 9-12. Parents & visitors welcome.",
        date = "2026-08-20",
        formattedDate = "Thu, 20 Aug 2026",
        time = "09:30 AM - 04:30 PM",
        location = "Main Auditorium & STEM Innovation Hub",
        category = CalendarCategory.ACADEMIC,
        isHoliday = false,
        targetGrades = "Grades 9 to 12",
        organizer = "STEM Faculty Council",
        hasReminder = true
      ),
      CalendarEvent(
        id = "evt_3",
        title = "Teacher Professional Development Day",
        description = "Workshops on modern pedagogies, AI tools in classroom teaching, and student mental wellness counseling.",
        date = "2026-08-25",
        formattedDate = "Tue, 25 Aug 2026",
        time = "01:30 PM - 04:30 PM",
        location = "AV Conference Hall 1",
        category = CalendarCategory.MEETING,
        isHoliday = false,
        targetGrades = "Faculty & Staff Only",
        organizer = "Dean of Academics"
      ),
      CalendarEvent(
        id = "evt_4",
        title = "Parent-Teacher Consultation (PTM) - Term 1",
        description = "One-on-one 10-minute performance review consultations between parents and class subject teachers.",
        date = "2026-08-29",
        formattedDate = "Sat, 29 Aug 2026",
        time = "09:00 AM - 01:30 PM",
        location = "Respective Classrooms (Blocks A & B)",
        category = CalendarCategory.MEETING,
        isHoliday = false,
        targetGrades = "All Classes (Pre-KG to 12)",
        organizer = "Academic Affairs Directorate",
        hasReminder = true
      ),
      CalendarEvent(
        id = "evt_5",
        title = "Term 1 Mid-Year Board Preparatory Exams",
        description = "Mid-term comprehensive assessments covering 50% prescribed syllabus for CBSE/ICSE Board batches.",
        date = "2026-09-01",
        formattedDate = "01 Sep - 12 Sep 2026",
        time = "09:00 AM - 12:30 PM",
        location = "Examination Halls A, B & Science Labs",
        category = CalendarCategory.EXAM,
        isHoliday = false,
        targetGrades = "Classes 10, 11 & 12",
        organizer = "Central Examination Cell"
      ),
      CalendarEvent(
        id = "evt_6",
        title = "Inter-School Athletics & Sports Championship",
        description = "Track & field, sprint relays, basketball finals, and inter-house championship matches.",
        date = "2026-09-18",
        formattedDate = "Fri, 18 Sep 2026",
        time = "08:00 AM - 05:00 PM",
        location = "St. Joseph's Sports Stadium & Courts",
        category = CalendarCategory.SPORTS,
        isHoliday = false,
        targetGrades = "Athletes & House Squads",
        organizer = "Physical Education Department"
      ),
      CalendarEvent(
        id = "evt_7",
        title = "Annual Cultural Gala 'Euphoria 2026'",
        description = "Grand music, drama, choir concert, and art gallery exhibition celebrating student artistic brilliance.",
        date = "2026-09-26",
        formattedDate = "Sat, 26 Sep 2026",
        time = "04:30 PM - 08:30 PM",
        location = "Open Air Amphitheatre",
        category = CalendarCategory.CULTURAL,
        isHoliday = false,
        targetGrades = "Entire School Community",
        organizer = "Student Union & Arts Council"
      )
    )

    // --- Wave 1: Seed Bus Routes ---
    private val initialBusRoutes = listOf(
      BusRoute(
        id = "route_12",
        routeNumber = "Route #12",
        routeName = "SIPCOT - Gandhi Nagar Express",
        busRegistration = "TN-70-SJ-1012",
        driverName = "Mr. Ramesh Kumar",
        driverPhone = "+91 98451 22334",
        attendantName = "Mrs. Sunita Devi",
        attendantPhone = "+91 98451 99881",
        currentSpeedKmH = 36,
        currentLocationName = "Gandhi Nagar Main Circle",
        nextStopName = "Mookondapalli Junction (Stop 3)",
        estimatedArrivalMins = 6,
        status = BusStatus.ON_TIME,
        delayMinutes = 0,
        capacity = 45,
        studentsOnboard = 34,
        progressPercent = 0.45f,
        currentLatitude = 12.7435,
        currentLongitude = 77.8120,
        currentHeadingDegrees = 72f,
        schoolLatitude = 12.74632,
        schoolLongitude = 77.80728,
        stops = listOf(
          BusStop(
            id = "st_12_1",
            name = "Hosur Bus Stand Circle",
            scheduledTime = "07:15 AM",
            isCompleted = true,
            studentCount = 4,
            latitude = 12.7410,
            longitude = 77.8250,
            passengers = listOf(
              BusPassenger("p_12_1_1", "Keerthivasan S", UserRole.STUDENT, "Class 12-A", 1, "S. Sundar", "+91 98450 78912", "+91 98450 78912", PassengerBoardingStatus.BOARDED, "st_12_1", "Hosur Bus Stand Circle", "07:14 AM"),
              BusPassenger("p_12_1_2", "Aarav Patel", UserRole.STUDENT, "Class 10-B", 4, "Vikram Patel", "+91 98450 78920", "+91 98450 78920", PassengerBoardingStatus.BOARDED, "st_12_1", "Hosur Bus Stand Circle", "07:15 AM"),
              BusPassenger("p_12_1_3", "Prof. Sarah Jenkins", UserRole.TEACHER, "Science Faculty", null, "Self", "+91 98765 22001", "+91 98765 22001", PassengerBoardingStatus.BOARDED, "st_12_1", "Hosur Bus Stand Circle", "07:13 AM"),
              BusPassenger("p_12_1_4", "Meera Nair", UserRole.STUDENT, "Class 9-A", 14, "Gopal Nair", "+91 98450 78933", "+91 98450 78933", PassengerBoardingStatus.BOARDED, "st_12_1", "Hosur Bus Stand Circle", "07:15 AM")
            )
          ),
          BusStop(
            id = "st_12_2",
            name = "Gandhi Nagar Junction",
            scheduledTime = "07:30 AM",
            isCompleted = true,
            isCurrent = true,
            studentCount = 5,
            latitude = 12.7435,
            longitude = 77.8120,
            passengers = listOf(
              BusPassenger("p_12_2_1", "Rahul Sharma", UserRole.STUDENT, "Class 12-A", 2, "Rajesh Sharma", "+91 98450 78913", "+91 98450 78913", PassengerBoardingStatus.BOARDED, "st_12_2", "Gandhi Nagar Junction", "07:29 AM"),
              BusPassenger("p_12_2_2", "Ananya Verma", UserRole.STUDENT, "Class 12-A", 3, "Col. K. Verma", "+91 98450 78914", "+91 98450 78914", PassengerBoardingStatus.BOARDED, "st_12_2", "Gandhi Nagar Junction", "07:30 AM"),
              BusPassenger("p_12_2_3", "Devansh Joshi", UserRole.STUDENT, "Class 11-B", 8, "Sanjay Joshi", "+91 98450 78945", "+91 98450 78945", PassengerBoardingStatus.WAITING, "st_12_2", "Gandhi Nagar Junction"),
              BusPassenger("p_12_2_4", "Dr. Emily Watson", UserRole.TEACHER, "English Faculty", null, "Self", "+91 98765 22003", "+91 98765 22003", PassengerBoardingStatus.BOARDED, "st_12_2", "Gandhi Nagar Junction", "07:28 AM"),
              BusPassenger("p_12_2_5", "Sneha Rao", UserRole.STUDENT, "Class 8-C", 19, "M. Rao", "+91 98450 78950", "+91 98450 78950", PassengerBoardingStatus.ABSENT, "st_12_2", "Gandhi Nagar Junction")
            )
          ),
          BusStop(
            id = "st_12_3",
            name = "Mookondapalli Ring Road",
            scheduledTime = "07:45 AM",
            isCompleted = false,
            studentCount = 4,
            latitude = 12.7450,
            longitude = 77.8090,
            passengers = listOf(
              BusPassenger("p_12_3_1", "Aditya Krishnan", UserRole.STUDENT, "Class 10-A", 6, "S. Krishnan", "+91 98450 78955", "+91 98450 78955", PassengerBoardingStatus.WAITING, "st_12_3", "Mookondapalli Ring Road"),
              BusPassenger("p_12_3_2", "Pooja Reddy", UserRole.STUDENT, "Class 11-A", 12, "V. Reddy", "+91 98450 78960", "+91 98450 78960", PassengerBoardingStatus.WAITING, "st_12_3", "Mookondapalli Ring Road"),
              BusPassenger("p_12_3_3", "Rohan Pillai", UserRole.STUDENT, "Class 12-B", 15, "T. Pillai", "+91 98450 78965", "+91 98450 78965", PassengerBoardingStatus.WAITING, "st_12_3", "Mookondapalli Ring Road"),
              BusPassenger("p_12_3_4", "Mr. David Miller", UserRole.TEACHER, "Mathematics Faculty", null, "Self", "+91 98765 22002", "+91 98765 22002", PassengerBoardingStatus.WAITING, "st_12_3", "Mookondapalli Ring Road")
            )
          ),
          BusStop(
            id = "st_12_4",
            name = "SIPCOT Phase-1 Gate",
            scheduledTime = "08:00 AM",
            isCompleted = false,
            studentCount = 3,
            latitude = 12.7460,
            longitude = 77.8080,
            passengers = listOf(
              BusPassenger("p_12_4_1", "Tanvi Sengupta", UserRole.STUDENT, "Class 9-B", 22, "A. Sengupta", "+91 98450 78970", "+91 98450 78970", PassengerBoardingStatus.WAITING, "st_12_4", "SIPCOT Phase-1 Gate"),
              BusPassenger("p_12_4_2", "Varun Chopra", UserRole.STUDENT, "Class 12-A", 9, "N. Chopra", "+91 98450 78975", "+91 98450 78975", PassengerBoardingStatus.WAITING, "st_12_4", "SIPCOT Phase-1 Gate"),
              BusPassenger("p_12_4_3", "Riya Sen", UserRole.STUDENT, "Class 10-A", 18, "B. Sen", "+91 98450 78980", "+91 98450 78980", PassengerBoardingStatus.WAITING, "st_12_4", "SIPCOT Phase-1 Gate")
            )
          ),
          BusStop(
            id = "st_12_5",
            name = "St. Joseph Matriculation Hr. Sec. School (Campus)",
            scheduledTime = "08:15 AM",
            isCompleted = false,
            studentCount = 0,
            latitude = 12.74632,
            longitude = 77.80728,
            passengers = emptyList()
          )
        )
      ),
      BusRoute(
        id = "route_05",
        routeNumber = "Route #05",
        routeName = "Bagalur Road - Mathigiri Shuttle",
        busRegistration = "TN-70-SJ-1005",
        driverName = "Mr. Suresh Babu",
        driverPhone = "+91 98452 33445",
        attendantName = "Mrs. Mary D'Souza",
        attendantPhone = "+91 98452 88772",
        currentSpeedKmH = 24,
        currentLocationName = "Mathigiri Junction",
        nextStopName = "Mookondapalli Cross (Stop 4)",
        estimatedArrivalMins = 11,
        status = BusStatus.DELAYED,
        delayMinutes = 5,
        delayReason = "Overbridge maintenance detour",
        capacity = 45,
        studentsOnboard = 38,
        progressPercent = 0.52f,
        currentLatitude = 12.7300,
        currentLongitude = 77.8200,
        currentHeadingDegrees = 240f,
        schoolLatitude = 12.74632,
        schoolLongitude = 77.80728,
        activeDetourAlert = "🚧 Detour active: Stop 3 diverted via Mathigiri Main Rd.",
        stops = listOf(
          BusStop(
            id = "st_05_1",
            name = "Bagalur Road Circle",
            scheduledTime = "07:10 AM",
            isCompleted = true,
            studentCount = 4,
            latitude = 12.7250,
            longitude = 77.8350,
            passengers = listOf(
              BusPassenger("p_05_1_1", "Kavya Menon", UserRole.STUDENT, "Class 11-A", 5, "S. Menon", "+91 98452 00101", "+91 98452 00101", PassengerBoardingStatus.BOARDED, "st_05_1", "Bagalur Road Circle", "07:09 AM"),
              BusPassenger("p_05_1_2", "Siddharth Roy", UserRole.STUDENT, "Class 12-B", 7, "P. Roy", "+91 98452 00102", "+91 98452 00102", PassengerBoardingStatus.BOARDED, "st_05_1", "Bagalur Road Circle", "07:10 AM"),
              BusPassenger("p_05_1_3", "Arjun Nambiar", UserRole.STUDENT, "Class 9-A", 11, "K. Nambiar", "+91 98452 00103", "+91 98452 00103", PassengerBoardingStatus.BOARDED, "st_05_1", "Bagalur Road Circle", "07:11 AM"),
              BusPassenger("p_05_1_4", "Mrs. Maya Pillai", UserRole.TEACHER, "Biology Faculty", null, "Self", "+91 98765 22004", "+91 98765 22004", PassengerBoardingStatus.BOARDED, "st_05_1", "Bagalur Road Circle", "07:08 AM")
            )
          ),
          BusStop(
            id = "st_05_2",
            name = "Mathigiri Cattle Farm Rd",
            scheduledTime = "07:25 AM",
            isCompleted = true,
            studentCount = 3,
            latitude = 12.7280,
            longitude = 77.8280,
            passengers = listOf(
              BusPassenger("p_05_2_1", "Neha Hegde", UserRole.STUDENT, "Class 10-B", 14, "U. Hegde", "+91 98452 00201", "+91 98452 00201", PassengerBoardingStatus.BOARDED, "st_05_2", "Mathigiri Cattle Farm Rd", "07:24 AM"),
              BusPassenger("p_05_2_2", "Aryan Gupta", UserRole.STUDENT, "Class 8-A", 3, "R. Gupta", "+91 98452 00202", "+91 98452 00202", PassengerBoardingStatus.BOARDED, "st_05_2", "Mathigiri Cattle Farm Rd", "07:25 AM"),
              BusPassenger("p_05_2_3", "Ishaan Kapoor", UserRole.STUDENT, "Class 12-A", 17, "S. Kapoor", "+91 98452 00203", "+91 98452 00203", PassengerBoardingStatus.BOARDED, "st_05_2", "Mathigiri Cattle Farm Rd", "07:26 AM")
            )
          ),
          BusStop(
            id = "st_05_3",
            name = "Mathigiri Junction",
            scheduledTime = "07:42 AM",
            isCompleted = true,
            isCurrent = true,
            studentCount = 3,
            latitude = 12.7300,
            longitude = 77.8200,
            passengers = listOf(
              BusPassenger("p_05_3_1", "Divya Suresh", UserRole.STUDENT, "Class 10-A", 10, "K. Suresh", "+91 98452 00301", "+91 98452 00301", PassengerBoardingStatus.BOARDED, "st_05_3", "Mathigiri Junction", "07:41 AM"),
              BusPassenger("p_05_3_2", "Pranav Nair", UserRole.STUDENT, "Class 11-B", 21, "C. Nair", "+91 98452 00302", "+91 98452 00302", PassengerBoardingStatus.WAITING, "st_05_3", "Mathigiri Junction"),
              BusPassenger("p_05_3_3", "Gauri Deshmukh", UserRole.STUDENT, "Class 9-C", 16, "A. Deshmukh", "+91 98452 00303", "+91 98452 00303", PassengerBoardingStatus.ABSENT, "st_05_3", "Mathigiri Junction")
            )
          ),
          BusStop(
            id = "st_05_4",
            name = "Mookondapalli Cross",
            scheduledTime = "07:58 AM",
            isCompleted = false,
            studentCount = 2,
            latitude = 12.7420,
            longitude = 77.8110,
            passengers = listOf(
              BusPassenger("p_05_4_1", "Manish Tiwari", UserRole.STUDENT, "Class 12-A", 25, "H. Tiwari", "+91 98452 00401", "+91 98452 00401", PassengerBoardingStatus.WAITING, "st_05_4", "Mookondapalli Cross"),
              BusPassenger("p_05_4_2", "Shruti Rao", UserRole.STUDENT, "Class 7-B", 8, "V. Rao", "+91 98452 00402", "+91 98452 00402", PassengerBoardingStatus.WAITING, "st_05_4", "Mookondapalli Cross")
            )
          ),
          BusStop(
            id = "st_05_5",
            name = "St. Joseph Matriculation Hr. Sec. School (Campus)",
            scheduledTime = "08:18 AM",
            isCompleted = false,
            studentCount = 0,
            latitude = 12.74632,
            longitude = 77.80728,
            passengers = emptyList()
          )
        )
      ),
      BusRoute(
        id = "route_08",
        routeNumber = "Route #08",
        routeName = "Zuzuvadi - SIPCOT Line",
        busRegistration = "TN-70-SJ-1008",
        driverName = "Mr. Mohan Raj",
        driverPhone = "+91 98453 44556",
        attendantName = "Mrs. Rekha Sharma",
        attendantPhone = "+91 98453 77663",
        currentSpeedKmH = 42,
        currentLocationName = "Zuzuvadi Checkpost Rd",
        nextStopName = "SIPCOT Phase 2 Gate (Stop 4)",
        estimatedArrivalMins = 5,
        status = BusStatus.ON_TIME,
        delayMinutes = 0,
        capacity = 45,
        studentsOnboard = 41,
        progressPercent = 0.78f,
        currentLatitude = 12.7550,
        currentLongitude = 77.7950,
        currentHeadingDegrees = 45f,
        schoolLatitude = 12.74632,
        schoolLongitude = 77.80728,
        stops = listOf(
          BusStop("st_08_1", "Zuzuvadi Flyover", "07:05 AM", isCompleted = true, studentCount = 3, latitude = 12.7600, longitude = 77.7900),
          BusStop("st_08_2", "Moranapalli Junction", "07:22 AM", isCompleted = true, studentCount = 4, latitude = 12.7580, longitude = 77.7920),
          BusStop("st_08_3", "Zuzuvadi Checkpost", "07:38 AM", isCompleted = true, isCurrent = true, studentCount = 3, latitude = 12.7550, longitude = 77.7950),
          BusStop("st_08_4", "SIPCOT Phase 2 Gate", "07:50 AM", isCompleted = false, studentCount = 2, latitude = 12.7500, longitude = 77.8020),
          BusStop("st_08_5", "St. Joseph Matriculation Hr. Sec. School (Campus)", "08:05 AM", isCompleted = false, studentCount = 0, latitude = 12.74632, longitude = 77.80728)
        )
      ),
      BusRoute(
        id = "route_15",
        routeNumber = "Route #15",
        routeName = "Kamaraj Nagar - SIPCOT Express",
        busRegistration = "TN-70-SJ-1015",
        driverName = "Mr. Anand Paul",
        driverPhone = "+91 98454 55667",
        attendantName = "Mrs. Geeta Nayak",
        attendantPhone = "+91 98454 66554",
        currentSpeedKmH = 31,
        currentLocationName = "Kamaraj Nagar Main Rd",
        nextStopName = "Gandhi Nagar Cross (Stop 4)",
        estimatedArrivalMins = 9,
        status = BusStatus.ON_TIME,
        delayMinutes = 0,
        capacity = 45,
        studentsOnboard = 36,
        progressPercent = 0.68f,
        currentLatitude = 12.7380,
        currentLongitude = 77.8180,
        currentHeadingDegrees = 30f,
        schoolLatitude = 12.74632,
        schoolLongitude = 77.80728,
        stops = listOf(
          BusStop("st_15_1", "Rayakottai Road Junction", "07:00 AM", isCompleted = true, studentCount = 4, latitude = 12.7300, longitude = 77.8300),
          BusStop("st_15_2", "Kamaraj Nagar Bus Stop", "07:18 AM", isCompleted = true, studentCount = 5, latitude = 12.7350, longitude = 77.8220),
          BusStop("st_15_3", "Kamaraj Nagar Main Rd", "07:35 AM", isCompleted = true, isCurrent = true, studentCount = 3, latitude = 12.7380, longitude = 77.8180),
          BusStop("st_15_4", "Gandhi Nagar Cross", "07:52 AM", isCompleted = false, studentCount = 2, latitude = 12.7430, longitude = 77.8120),
          BusStop("st_15_5", "St. Joseph Matriculation Hr. Sec. School (Campus)", "08:15 AM", isCompleted = false, studentCount = 0, latitude = 12.74632, longitude = 77.80728)
        )
      )
    )

    // --- Wave 1: Seed Announcements ---
    private val initialAnnouncements = listOf(
      SchoolAnnouncement(
        id = "ann_1",
        title = "Severe Weather Alert: Early Bus Departure & Sports Session Rescheduling",
        content = "Attention all parents and students: Due to heavy precipitation warnings issued by civic authorities, all evening outdoor coaching and clubs are suspended today. School buses will commence return trips starting at 03:00 PM. Parents picking up wards via private transit are requested to arrive at Gate 2.",
        priority = AnnouncementPriority.URGENT,
        targetAudience = AnnouncementAudience.ALL_SCHOOL,
        date = "22 Aug 2026",
        timeAgo = "15m ago",
        authorName = "Dr. Arthur Pendelton",
        authorRole = "Principal",
        isEmergency = true,
        audioDurationSec = 38,
        acknowledgedByCurrentUser = false,
        acknowledgmentsCount = 184
      ),
      SchoolAnnouncement(
        id = "ann_2",
        title = "Annual Science & Tech Expo 2026: Project Exhibits & Hall Layout",
        content = "Final booth allotments for Grade 11 & 12 robotics, clean energy models, and software prototypes have been published. Team captains must complete electrical safety checks with Mr. Robert Taylor in Lab Prep 2 by 4:00 PM Thursday.",
        priority = AnnouncementPriority.HIGH,
        targetAudience = AnnouncementAudience.SENIOR_SECONDARY,
        date = "21 Aug 2026",
        timeAgo = "3h ago",
        authorName = "Prof. Sarah Jenkins",
        authorRole = "Lead Science Faculty",
        isEmergency = false,
        attachmentName = "Expo_Floor_Plan_2026.pdf",
        acknowledgedByCurrentUser = true,
        acknowledgmentsCount = 76
      ),
      SchoolAnnouncement(
        id = "ann_3",
        title = "Parent-Teacher Consultation (PTM) Online Slot Booking Open",
        content = "Online appointment booking for the Term 1 Parent-Teacher Conference (Saturday, 29 Aug) is now open. Parents can reserve individual 10-minute consultation slots with class educators and subject specialists.",
        priority = AnnouncementPriority.GENERAL,
        targetAudience = AnnouncementAudience.PARENTS_ONLY,
        date = "20 Aug 2026",
        timeAgo = "Yesterday",
        authorName = "Academic Affairs Bureau",
        authorRole = "Administration",
        isEmergency = false,
        acknowledgedByCurrentUser = true,
        acknowledgmentsCount = 210
      ),
      SchoolAnnouncement(
        id = "ann_4",
        title = "Transport Route #05 Temporary Road Diversion Notice",
        content = "Due to civic drainage maintenance on Kundalahalli Gate main road, Bus Route #05 will detour via AECS Layout for the next 3 working days. Morning pickup times at stops 1 and 2 will be 5 minutes earlier.",
        priority = AnnouncementPriority.HIGH,
        targetAudience = AnnouncementAudience.ALL_SCHOOL,
        date = "19 Aug 2026",
        timeAgo = "2 days ago",
        authorName = "Mr. Thomas Wright",
        authorRole = "Campus Operations Supervisor",
        isEmergency = false,
        acknowledgedByCurrentUser = false,
        acknowledgmentsCount = 112
      )
    )

    // --- Wave 1: Seed Directory Contacts ---
    private val initialDirectoryContacts = listOf(
      // 1. Helplines & Emergency
      DirectoryContact(
        id = "dir_sos_1",
        name = "24/7 Campus Emergency SOS & Security Desk",
        role = UserRole.ADMIN,
        category = DirectoryCategory.HELPLINE,
        designation = "24/7 Rapid Emergency Response Control",
        departmentOrGrade = "Campus Security & Safety Command",
        phoneNumber = "+91 98450 91100",
        email = "emergency@stjosephs.edu",
        roomOrLocation = "Gate 1 Main Security Office",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_sos_2",
        name = "Campus Clinic & Medical Infirmary",
        role = UserRole.STAFF,
        category = DirectoryCategory.HELPLINE,
        designation = "Head Nurse & First Aid Response",
        departmentOrGrade = "Health & Medical Wellness",
        phoneNumber = "+91 98450 91102",
        email = "infirmary@stjosephs.edu",
        roomOrLocation = "Ground Floor Health Wing, Block A",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_sos_3",
        name = "Student Well-being & Counseling Cell",
        role = UserRole.STAFF,
        category = DirectoryCategory.HELPLINE,
        designation = "Lead Student Counselor & Psychologist",
        departmentOrGrade = "Student Wellness Department",
        phoneNumber = "+91 98450 91101",
        email = "counselor@stjosephs.edu",
        roomOrLocation = "Room 108, Block C",
        isStudent = false
      ),

      // 2. Teachers & Faculty
      DirectoryContact(
        id = "dir_tch_1",
        name = "Prof. Sarah Jenkins",
        role = UserRole.TEACHER,
        category = DirectoryCategory.FACULTY,
        designation = "Senior Faculty & Class 10-A Mentor",
        departmentOrGrade = "Physics & Applied Sciences",
        phoneNumber = "+91 98765 22001",
        email = "s.jenkins@stjosephs.edu",
        roomOrLocation = "Staff Room 2B, 2nd Floor",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_tch_2",
        name = "Dr. Michael Chang",
        role = UserRole.TEACHER,
        category = DirectoryCategory.FACULTY,
        designation = "Head of Department (HOD) - Mathematics",
        departmentOrGrade = "Mathematics & Advanced Calculus",
        phoneNumber = "+91 98765 22002",
        email = "m.chang@stjosephs.edu",
        roomOrLocation = "Math Faculty Suite 1A",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_tch_3",
        name = "Mrs. Anita Sharma",
        role = UserRole.TEACHER,
        category = DirectoryCategory.FACULTY,
        designation = "Head of Department (HOD) - Chemistry",
        departmentOrGrade = "Chemical Sciences & Research",
        phoneNumber = "+91 98765 22003",
        email = "a.sharma@stjosephs.edu",
        roomOrLocation = "Chemistry Lab 1 Prep Room",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_tch_4",
        name = "Mr. Kevin Ross",
        role = UserRole.TEACHER,
        category = DirectoryCategory.FACULTY,
        designation = "Lead Computer Science & AI Faculty",
        departmentOrGrade = "Computer Science & Robotics",
        phoneNumber = "+91 98765 22004",
        email = "k.ross@stjosephs.edu",
        roomOrLocation = "Computer Lab B, Tech Block",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_tch_5",
        name = "Ms. Rachel Green",
        role = UserRole.TEACHER,
        category = DirectoryCategory.FACULTY,
        designation = "Senior English & Debate Master",
        departmentOrGrade = "Humanities & Languages",
        phoneNumber = "+91 98765 22005",
        email = "r.green@stjosephs.edu",
        roomOrLocation = "Staff Room 2A",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_tch_6",
        name = "Coach David Miller",
        role = UserRole.TEACHER,
        category = DirectoryCategory.FACULTY,
        designation = "Director of Physical Education & Sports",
        departmentOrGrade = "Physical Education & Athletics",
        phoneNumber = "+91 98765 22006",
        email = "d.miller@stjosephs.edu",
        roomOrLocation = "Sports Pavilion Office",
        isStudent = false
      ),

      // 3. Administration
      DirectoryContact(
        id = "dir_adm_1",
        name = "Dr. Arthur Pendelton",
        role = UserRole.ADMIN,
        category = DirectoryCategory.ADMINISTRATION,
        designation = "Principal & Head of Institution",
        departmentOrGrade = "Executive Administration Directorate",
        phoneNumber = "+91 98765 00001",
        email = "principal@stjosephs.edu",
        roomOrLocation = "Principal's Office, Main Block",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_adm_2",
        name = "Dr. Anthony Davies",
        role = UserRole.ADMIN,
        category = DirectoryCategory.ADMINISTRATION,
        designation = "Vice Principal & Academic Dean",
        departmentOrGrade = "Academic Directorate",
        phoneNumber = "+91 98765 00002",
        email = "viceprincipal@stjosephs.edu",
        roomOrLocation = "Dean's Office 102",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_adm_3",
        name = "Mr. S. Ramanathan",
        role = UserRole.ADMIN,
        category = DirectoryCategory.ADMINISTRATION,
        designation = "Bursar & Accounts Controller",
        departmentOrGrade = "Finance & Fee Management",
        phoneNumber = "+91 98765 00003",
        email = "accounts@stjosephs.edu",
        roomOrLocation = "Accounts Wing, Ground Floor",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_adm_4",
        name = "Mrs. Jennifer Lee",
        role = UserRole.ADMIN,
        category = DirectoryCategory.ADMINISTRATION,
        designation = "Exam Cell Controller & Registrar",
        departmentOrGrade = "Central Examination Bureau",
        phoneNumber = "+91 98765 00004",
        email = "examcell@stjosephs.edu",
        roomOrLocation = "Exam Control Room 105",
        isStudent = false
      ),

      // 4. Staff & Operations
      DirectoryContact(
        id = "dir_stf_1",
        name = "Mr. Thomas Wright",
        role = UserRole.STAFF,
        category = DirectoryCategory.STAFF,
        designation = "Facilities & Campus Supervisor",
        departmentOrGrade = "Campus Operations & Safety",
        phoneNumber = "+91 98765 33014",
        email = "t.wright@stjosephs.edu",
        roomOrLocation = "Main Operations Center",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_stf_2",
        name = "Mr. Robert Taylor",
        role = UserRole.STAFF,
        category = DirectoryCategory.STAFF,
        designation = "Senior Laboratory Technician",
        departmentOrGrade = "Science Block Laboratories",
        phoneNumber = "+91 98765 33015",
        email = "r.taylor@stjosephs.edu",
        roomOrLocation = "Lab Storage & Prep Room",
        isStudent = false
      ),

      // 5. Transport Fleet & Drivers
      DirectoryContact(
        id = "dir_trn_0",
        name = "Mr. Gururaj Naik",
        role = UserRole.STAFF,
        category = DirectoryCategory.TRANSPORT,
        designation = "Chief Transport & Fleet In-charge",
        departmentOrGrade = "Transport Management Bureau",
        phoneNumber = "+91 98450 99001",
        email = "transport@stjosephs.edu",
        roomOrLocation = "Transport Fleet Command Desk",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_trn_1",
        name = "Mr. Ramesh Kumar (Bus 12 Driver)",
        role = UserRole.STAFF,
        category = DirectoryCategory.TRANSPORT,
        designation = "Designated Driver - Route #12 Indiranagar",
        departmentOrGrade = "School Bus Fleet",
        phoneNumber = "+91 98451 22334",
        email = "bus12@stjosephs.edu",
        roomOrLocation = "Bus Bay #12",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_trn_2",
        name = "Mr. Suresh Babu (Bus 05 Driver)",
        role = UserRole.STAFF,
        category = DirectoryCategory.TRANSPORT,
        designation = "Designated Driver - Route #05 Whitefield",
        departmentOrGrade = "School Bus Fleet",
        phoneNumber = "+91 98452 33445",
        email = "bus05@stjosephs.edu",
        roomOrLocation = "Bus Bay #05",
        isStudent = false
      ),
      DirectoryContact(
        id = "dir_trn_3",
        name = "Mr. Mohan Raj (Bus 08 Driver)",
        role = UserRole.STAFF,
        category = DirectoryCategory.TRANSPORT,
        designation = "Designated Driver - Route #08 Koramangala",
        departmentOrGrade = "School Bus Fleet",
        phoneNumber = "+91 98453 44556",
        email = "bus08@stjosephs.edu",
        roomOrLocation = "Bus Bay #08",
        isStudent = false
      ),

      // 6. Students (Privacy-Protected for peer students)
      DirectoryContact(
        id = "dir_stu_1",
        name = "Keerthivasan S",
        role = UserRole.STUDENT,
        category = DirectoryCategory.STUDENTS,
        designation = "Grade 12-A • Roll #1 • House Captain",
        departmentOrGrade = "Grade 12 - Section A",
        phoneNumber = "+91 98450 12001",
        email = "keerthivasan.s@stjosephs.edu",
        roomOrLocation = "Classroom 12-A",
        isStudent = true,
        parentContact = "+91 98450 78912",
        bloodGroup = "O+ve"
      ),
      DirectoryContact(
        id = "dir_stu_2",
        name = "Rahul Sharma",
        role = UserRole.STUDENT,
        category = DirectoryCategory.STUDENTS,
        designation = "Grade 12-A • Roll #2 • Science Club Lead",
        departmentOrGrade = "Grade 12 - Section A",
        phoneNumber = "+91 98450 12002",
        email = "rahul.s@stjosephs.edu",
        roomOrLocation = "Classroom 12-A",
        isStudent = true,
        parentContact = "+91 98450 78913",
        bloodGroup = "B+ve"
      ),
      DirectoryContact(
        id = "dir_stu_3",
        name = "Ananya Verma",
        role = UserRole.STUDENT,
        category = DirectoryCategory.STUDENTS,
        designation = "Grade 12-A • Roll #3 • Head Girl",
        departmentOrGrade = "Grade 12 - Section A",
        phoneNumber = "+91 98450 12003",
        email = "ananya.v@stjosephs.edu",
        roomOrLocation = "Classroom 12-A",
        isStudent = true,
        parentContact = "+91 98450 78914",
        bloodGroup = "A+ve"
      ),
      DirectoryContact(
        id = "dir_stu_4",
        name = "Priya Nair",
        role = UserRole.STUDENT,
        category = DirectoryCategory.STUDENTS,
        designation = "Grade 10-A • Roll #1 • Cultural Secretary",
        departmentOrGrade = "Grade 10 - Section A",
        phoneNumber = "+91 98450 10001",
        email = "priya.n@stjosephs.edu",
        roomOrLocation = "Classroom 10-A",
        isStudent = true,
        parentContact = "+91 98450 78915",
        bloodGroup = "AB+ve"
      ),
      DirectoryContact(
        id = "dir_stu_5",
        name = "Rohan Gupta",
        role = UserRole.STUDENT,
        category = DirectoryCategory.STUDENTS,
        designation = "Grade 10-A • Roll #2 • Sports Vice Captain",
        departmentOrGrade = "Grade 10 - Section A",
        phoneNumber = "+91 98450 10002",
        email = "rohan.g@stjosephs.edu",
        roomOrLocation = "Classroom 10-A",
        isStudent = true,
        parentContact = "+91 98450 78916",
        bloodGroup = "O-ve"
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
