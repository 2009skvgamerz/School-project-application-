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

  // Demo Users Directory
  val demoUsers = listOf(
    DemoAccount("student01", "password123", UserRole.STUDENT, "Alex Johnson", "alex.j@stjosephs.edu"),
    DemoAccount("teacher01", "password123", UserRole.TEACHER, "Prof. Sarah Jenkins", "s.jenkins@stjosephs.edu"),
    DemoAccount("staff01", "password123", UserRole.STAFF, "Robert Taylor", "r.taylor@stjosephs.edu"),
    DemoAccount("admin01", "password123", UserRole.ADMIN, "Dr. Anthony Davies", "principal@stjosephs.edu")
  )

  fun login(username: String, password: String):Result<User> {
    val matched = demoUsers.find { 
      it.username.equals(username.trim(), ignoreCase = true) && 
      (it.password == password || password == "demo123" || password == "password123")
    }

    return if (matched != null) {
      setUserByRole(matched.role, matched.username, matched.fullName, matched.email)
      Result.success(_currentUser.value!!)
    } else {
      // If user typed custom username, fallback or check role prefix
      val fallbackRole = when {
        username.contains("teacher", ignoreCase = true) -> UserRole.TEACHER
        username.contains("staff", ignoreCase = true) -> UserRole.STAFF
        username.contains("admin", ignoreCase = true) -> UserRole.ADMIN
        else -> UserRole.STUDENT
      }
      setUserByRole(fallbackRole, username, "$username (Demo)", "$username@stjosephs.edu")
      Result.success(_currentUser.value!!)
    }
  }

  fun loginAsRole(role: UserRole) {
    val demo = demoUsers.first { it.role == role }
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
        UserRole.STUDENT -> "+91 98765 11042"
        UserRole.TEACHER -> "+91 98765 22001"
        UserRole.STAFF -> "+91 98765 33014"
        UserRole.ADMIN -> "+91 98765 00001"
      },
      designation = when(role) {
        UserRole.STUDENT -> "Grade 10 - Section A"
        UserRole.TEACHER -> "Senior Science Faculty"
        UserRole.STAFF -> "Facilities & Campus Supervisor"
        UserRole.ADMIN -> "Principal & Head of Institution"
      }
    )
    _currentUser.value = user

    when (role) {
      UserRole.STUDENT -> {
        _currentStudentProfile.value = StudentProfile(
          user = user,
          admissionNo = "SJ-2024-1042",
          grade = "10",
          section = "A",
          rollNo = 18,
          parentName = "Marcus & Elena Johnson",
          parentPhone = "+91 98450 78912",
          bloodGroup = "O+ve",
          attendancePercentage = 94.8,
          houseName = "St. Francis House (Blue)",
          busRoute = "Bus 14 - St. Mary's Junction"
        )
      }
      UserRole.TEACHER -> {
        _currentTeacherProfile.value = TeacherProfile(
          user = user,
          employeeId = "EMP-T-2018",
          department = "Physical & Chemical Sciences",
          assignedClasses = listOf("Class 10-A", "Class 10-B", "Class 9-A", "Class 11-Science"),
          subjectsTaught = listOf("Physics", "Science Lab", "General Science"),
          qualification = "M.Sc. Physics, B.Ed (Gold Medalist)"
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
    }
  }

  fun logout() {
    _currentUser.value = null
    _currentStudentProfile.value = null
    _currentTeacherProfile.value = null
    _currentStaffProfile.value = null
    _currentAdminProfile.value = null
  }

  // Interactive Actions
  fun addNotice(title: String, content: String, category: NoticeCategory, isUrgent: Boolean) {
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
  }

  fun updateAttendanceRecord(studentId: String, newStatus: AttendanceStatus) {
    _attendanceRecords.update { list ->
      list.map { rec ->
        if (rec.studentId == studentId) rec.copy(status = newStatus) else rec
      }
    }
  }

  fun markAllAttendance(status: AttendanceStatus) {
    _attendanceRecords.update { list ->
      list.map { it.copy(status = status) }
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

  companion object {
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
        className = "Class 10-A",
        assignedDate = "Yesterday",
        dueDate = "Tomorrow, 09:00 AM",
        teacherName = "Prof. Sarah Jenkins",
        status = HomeworkStatus.PENDING,
        maxMarks = 20,
        submissionsCount = 22,
        totalStudents = 32
      ),
      Homework(
        id = "hw_2",
        title = "Quadratic Equations Problem Set",
        description = "Solve quadratic formula questions on page 84 (Q1 to Q15). Show all factoring steps clearly.",
        subjectName = "Mathematics",
        className = "Class 10-A",
        assignedDate = "12 Aug",
        dueDate = "16 Aug, 10:00 AM",
        teacherName = "Mr. David Miller",
        status = HomeworkStatus.PENDING,
        maxMarks = 25,
        submissionsCount = 18,
        totalStudents = 32
      ),
      Homework(
        id = "hw_3",
        title = "Chemical Bonding Lab Report",
        description = "Write down experiment observation, ionic vs covalent bonding test results with diagrams.",
        subjectName = "Chemistry",
        className = "Class 10-A",
        assignedDate = "10 Aug",
        dueDate = "14 Aug, 02:00 PM",
        teacherName = "Dr. Anita Sharma",
        status = HomeworkStatus.SUBMITTED,
        maxMarks = 15,
        submissionNote = "Lab report with observations attached",
        submissionsCount = 31,
        totalStudents = 32
      ),
      Homework(
        id = "hw_4",
        title = "Shakespeare 'Julius Caesar' Essay",
        description = "Write a 400-word analysis on the rhetorical strategies of Mark Antony's funeral speech in Act 3.",
        subjectName = "English Lit",
        className = "Class 10-A",
        assignedDate = "08 Aug",
        dueDate = "11 Aug",
        teacherName = "Mrs. Clara Higgins",
        status = HomeworkStatus.EVALUATED,
        maxMarks = 20,
        submissionNote = "Score: 18/20 - Excellent character analysis!",
        submissionsCount = 32,
        totalStudents = 32
      )
    )

    private val initialTimetable = listOf(
      TimetableEntry("tt_1", DayOfWeek.MONDAY, 1, "08:30 AM", "09:15 AM", "Physics", "Prof. Sarah Jenkins", "Room 204", "Class 10-A"),
      TimetableEntry("tt_2", DayOfWeek.MONDAY, 2, "09:15 AM", "10:00 AM", "Mathematics", "Mr. David Miller", "Room 204", "Class 10-A"),
      TimetableEntry("tt_3", DayOfWeek.MONDAY, 3, "10:15 AM", "11:00 AM", "Chemistry", "Dr. Anita Sharma", "Science Lab 1", "Class 10-A"),
      TimetableEntry("tt_4", DayOfWeek.MONDAY, 4, "11:00 AM", "11:45 AM", "English Lit", "Mrs. Clara Higgins", "Room 204", "Class 10-A"),
      TimetableEntry("tt_5", DayOfWeek.MONDAY, 5, "12:30 PM", "01:15 PM", "Computer Science", "Mr. Kevin Ross", "Comp Lab B", "Class 10-A"),
      TimetableEntry("tt_6", DayOfWeek.MONDAY, 6, "01:15 PM", "02:00 PM", "Physical Education", "Coach Raymond", "Ground 1", "Class 10-A"),

      TimetableEntry("tt_7", DayOfWeek.TUESDAY, 1, "08:30 AM", "09:15 AM", "Mathematics", "Mr. David Miller", "Room 204", "Class 10-A"),
      TimetableEntry("tt_8", DayOfWeek.TUESDAY, 2, "09:15 AM", "10:00 AM", "Physics Lab", "Prof. Sarah Jenkins", "Physics Lab", "Class 10-A"),
      TimetableEntry("tt_9", DayOfWeek.TUESDAY, 3, "10:15 AM", "11:00 AM", "Biology", "Dr. Rachel Green", "Bio Lab", "Class 10-A"),
      TimetableEntry("tt_10", DayOfWeek.TUESDAY, 4, "11:00 AM", "11:45 AM", "Social Studies", "Mr. Alan Paul", "Room 204", "Class 10-A"),
      TimetableEntry("tt_11", DayOfWeek.TUESDAY, 5, "12:30 PM", "01:15 PM", "Moral Science", "Fr. Joseph Mathew", "Chapel Hall", "Class 10-A"),

      TimetableEntry("tt_12", DayOfWeek.WEDNESDAY, 1, "08:30 AM", "09:15 AM", "Chemistry", "Dr. Anita Sharma", "Science Lab 1", "Class 10-A"),
      TimetableEntry("tt_13", DayOfWeek.WEDNESDAY, 2, "09:15 AM", "10:00 AM", "Physics", "Prof. Sarah Jenkins", "Room 204", "Class 10-A"),
      TimetableEntry("tt_14", DayOfWeek.WEDNESDAY, 3, "10:15 AM", "11:00 AM", "Mathematics", "Mr. David Miller", "Room 204", "Class 10-A"),
      TimetableEntry("tt_15", DayOfWeek.WEDNESDAY, 4, "11:00 AM", "11:45 AM", "Library & Reading", "Ms. Beatrice Vance", "Main Library", "Class 10-A"),

      TimetableEntry("tt_16", DayOfWeek.THURSDAY, 1, "08:30 AM", "09:15 AM", "Computer Science", "Mr. Kevin Ross", "Comp Lab B", "Class 10-A"),
      TimetableEntry("tt_17", DayOfWeek.THURSDAY, 2, "09:15 AM", "10:00 AM", "English Lit", "Mrs. Clara Higgins", "Room 204", "Class 10-A"),
      TimetableEntry("tt_18", DayOfWeek.THURSDAY, 3, "10:15 AM", "11:00 AM", "Physics", "Prof. Sarah Jenkins", "Room 204", "Class 10-A"),
      TimetableEntry("tt_19", DayOfWeek.THURSDAY, 4, "11:00 AM", "11:45 AM", "History & Civics", "Mr. Alan Paul", "Room 204", "Class 10-A"),

      TimetableEntry("tt_20", DayOfWeek.FRIDAY, 1, "08:30 AM", "09:15 AM", "Robotics & AI Expo Prep", "Prof. Sarah Jenkins", "STEM Lab", "Class 10-A"),
      TimetableEntry("tt_21", DayOfWeek.FRIDAY, 2, "09:15 AM", "10:00 AM", "Mathematics", "Mr. David Miller", "Room 204", "Class 10-A"),
      TimetableEntry("tt_22", DayOfWeek.FRIDAY, 3, "10:15 AM", "11:00 AM", "Art & Design", "Ms. Laura Bennett", "Art Studio", "Class 10-A"),
      TimetableEntry("tt_23", DayOfWeek.FRIDAY, 4, "11:00 AM", "11:45 AM", "Sports & Games", "Coach Raymond", "School Field", "Class 10-A"),

      TimetableEntry("tt_24", DayOfWeek.SATURDAY, 1, "08:30 AM", "09:30 AM", "Science Club Activity", "Prof. Sarah Jenkins", "Auditorium", "Class 10-A"),
      TimetableEntry("tt_25", DayOfWeek.SATURDAY, 2, "09:45 AM", "11:00 AM", "House Assembly & Drill", "Fr. Joseph Mathew", "Campus Grounds", "Class 10-A")
    )

    private val initialAttendanceRecords = listOf(
      AttendanceRecord("att_1", "std_101", "Alex Johnson", 1, "Class 10-A", "Today", AttendanceStatus.PRESENT),
      AttendanceRecord("att_2", "std_102", "Bella Collins", 2, "Class 10-A", "Today", AttendanceStatus.PRESENT),
      AttendanceRecord("att_3", "std_103", "Christian Davies", 3, "Class 10-A", "Today", AttendanceStatus.LATE, "Bus delay"),
      AttendanceRecord("att_4", "std_104", "Daniel Evans", 4, "Class 10-A", "Today", AttendanceStatus.PRESENT),
      AttendanceRecord("att_5", "std_105", "Emma Foster", 5, "Class 10-A", "Today", AttendanceStatus.ABSENT, "Medical leave"),
      AttendanceRecord("att_6", "std_106", "Felix Gomez", 6, "Class 10-A", "Today", AttendanceStatus.PRESENT),
      AttendanceRecord("att_7", "std_107", "Grace Howard", 7, "Class 10-A", "Today", AttendanceStatus.PRESENT),
      AttendanceRecord("att_8", "std_108", "Henry Irwin", 8, "Class 10-A", "Today", AttendanceStatus.PRESENT),
      AttendanceRecord("att_9", "std_109", "Isabella Jackson", 9, "Class 10-A", "Today", AttendanceStatus.PRESENT),
      AttendanceRecord("att_10", "std_110", "Jacob Klein", 10, "Class 10-A", "Today", AttendanceStatus.PRESENT),
      AttendanceRecord("att_11", "std_111", "Lily Morris", 11, "Class 10-A", "Today", AttendanceStatus.PRESENT),
      AttendanceRecord("att_12", "std_112", "Noah Parker", 12, "Class 10-A", "Today", AttendanceStatus.PRESENT)
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
      SchoolClass("cls_10a", "Class 10", "A", "Room 204", "Prof. Sarah Jenkins", 32, 95.2),
      SchoolClass("cls_10b", "Class 10", "B", "Room 205", "Mr. David Miller", 30, 93.8),
      SchoolClass("cls_9a", "Class 9", "A", "Room 102", "Mrs. Clara Higgins", 34, 96.0),
      SchoolClass("cls_9b", "Class 9", "B", "Room 103", "Dr. Anita Sharma", 33, 94.1),
      SchoolClass("cls_11sci", "Class 11", "Science", "Room 301", "Dr. Rachel Green", 28, 92.5),
      SchoolClass("cls_12sci", "Class 12", "Science", "Room 302", "Mr. Kevin Ross", 26, 96.8)
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
