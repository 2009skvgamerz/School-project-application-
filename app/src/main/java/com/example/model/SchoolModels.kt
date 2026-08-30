package com.example.model

enum class UserRole(val displayName: String, val badgeColor: Long) {
  STUDENT("Student", 0xFF2563EB),
  TEACHER("Teacher", 0xFF059669),
  STAFF("Staff", 0xFF7C3AED),
  DRIVER("Bus Driver", 0xFFEA580C),
  ADMIN("Administrator", 0xFFD97706),
  DEVELOPER("Developer / Root", 0xFF10B981);

  val label: String get() = displayName
}

data class User(
  val id: String,
  val username: String,
  val fullName: String,
  val email: String,
  val role: UserRole,
  val phone: String = "+91 98765 43210",
  val avatarUrl: String = "",
  val designation: String = ""
) {
  val name: String get() = fullName
  val avatarInitials: String
    get() = fullName.split(" ")
      .filter { it.isNotBlank() }
      .mapNotNull { it.firstOrNull()?.uppercase() }
      .take(2)
      .joinToString("")
      .ifEmpty { "SJ" }
}

data class StudentProfile(
  val user: User,
  val admissionNo: String,
  val grade: String,
  val section: String,
  val rollNo: Int,
  val parentName: String,
  val parentPhone: String,
  val bloodGroup: String,
  val attendancePercentage: Double,
  val houseName: String = "St. Francis House",
  val busRoute: String = "Route #12 (Main Gate)",
  val academicYear: String = "2026-2027",
  val emergencyContact: String = "+91 98450 78912"
)

data class TeacherProfile(
  val user: User,
  val employeeId: String,
  val department: String,
  val assignedClasses: List<String>,
  val subjectsTaught: List<String>,
  val qualification: String,
  val isClassTeacher: Boolean = true,
  val classTeacherOf: String? = "Class 10-A",
  val roomNo: String = "Staff Room 2B",
  val joiningDate: String = "15 July 2018"
) {
  val teachingSubjects: List<String> get() = subjectsTaught
}

data class StaffProfile(
  val user: User,
  val employeeId: String,
  val department: String,
  val duties: List<String>,
  val shiftTiming: String,
  val emergencyRole: String = "Campus Safety Warden",
  val locationArea: String = "Campus Ground & Labs"
) {
  val staffId: String get() = employeeId
}

data class DriverProfile(
  val user: User,
  val driverId: String = "DRV-102",
  val licenseNo: String = "KA-01-2015-DL99482",
  val assignedBusNo: String = "Bus #12",
  val busRegistration: String = "KA-04-SJ-1012",
  val assignedRouteId: String = "route_12",
  val shift: String = "Morning Shift (06:45 AM - 09:30 AM)",
  val experienceYears: Int = 12,
  val attendantName: String = "Mrs. Sunita Devi",
  val attendantPhone: String = "+91 98451 99881",
  val isTripActive: Boolean = true
)

data class AdminProfile(
  val user: User,
  val employeeId: String,
  val adminRole: String,
  val officeLocation: String = "Principal's Office, Main Block",
  val systemPermissions: List<String> = listOf("Academic Roster", "Staff Dispatch", "Broadcasts", "Audit Reports")
) {
  val adminId: String get() = employeeId
}

data class DeveloperProfile(
  val user: User,
  val devId: String = "DEV-ROOT-007",
  val accessLevel: String = "Level 5 - God Mode (Full Master Write)",
  val terminalStatus: String = "ROOT ACTIVE",
  val environment: String = "St. Joseph's Cloud Core Engine v4.2.0-PRO",
  val systemPermissions: List<String> = listOf(
    "Live User & Profile Override",
    "Universal Entity Mutation",
    "Global Heads-Up Broadcaster",
    "Attendance & Grade Override",
    "Timetable & Roster Master",
    "Database Reset & Seeding"
  )
) {
  val developerId: String get() = devId
}

data class SystemUserRecord(
  val id: String,
  val username: String,
  val fullName: String,
  val email: String,
  val role: UserRole,
  val phone: String = "+91 98450 00000",
  val designation: String = "",
  val identifier: String = "", // Admission No or Employee ID
  val departmentOrGrade: String = "",
  val sectionOrRoom: String = "",
  val extraNotes: String = ""
)

data class SchoolClass(
  val id: String,
  val name: String,
  val section: String,
  val roomNo: String,
  val classTeacherName: String,
  val totalStudents: Int,
  val averageAttendance: Double = 94.5
)

data class Subject(
  val id: String,
  val code: String,
  val name: String,
  val teacherName: String,
  val totalPeriodsPerWeek: Int,
  val colorHex: Long = 0xFF1E40AF
)

enum class DayOfWeek(val shortName: String, val fullName: String) {
  MONDAY("Mon", "Monday"),
  TUESDAY("Tue", "Tuesday"),
  WEDNESDAY("Wed", "Wednesday"),
  THURSDAY("Thu", "Thursday"),
  FRIDAY("Fri", "Friday"),
  SATURDAY("Sat", "Saturday")
}

data class TimetableEntry(
  val id: String,
  val day: DayOfWeek,
  val periodNumber: Int,
  val startTime: String,
  val endTime: String,
  val subjectName: String,
  val teacherName: String,
  val roomNo: String,
  val className: String
)

enum class AttendanceStatus(
  val label: String,
  val code: String,
  val colorHex: Long,
  val description: String,
  val weight: Double // 1.0 for Full Day and OD, 0.5 for Half Day, 0.0 for Absent
) {
  FULL_DAY("Full Day", "FD", 0xFF059669, "Full Day Present", 1.0),
  HALF_DAY("Half Day", "HD", 0xFFD97706, "Half Day Present", 0.5),
  ON_DUTY("On-Duty", "OD", 0xFF2563EB, "Official School On-Duty", 1.0),
  ABSENT("Absent", "AB", 0xFFDC2626, "Absent", 0.0)
}

data class AttendanceRecord(
  val id: String,
  val studentId: String,
  val studentName: String,
  val rollNo: Int,
  val className: String,
  val date: String,
  val status: AttendanceStatus,
  val markedBy: String = "Prof. Sarah Jenkins (Class Teacher)",
  val notes: String = ""
)

data class AttendanceSummary(
  val totalWorkingDays: Int,
  val fullDays: Int,
  val halfDays: Int,
  val onDutyDays: Int,
  val absentDays: Int,
  val percentage: Double,
  val subjectWiseAttendance: Map<String, Double>
)

enum class HomeworkStatus(val label: String) {
  PENDING("Pending"),
  SUBMITTED("Submitted"),
  EVALUATED("Evaluated")
}

data class Homework(
  val id: String,
  val title: String,
  val description: String,
  val subjectName: String,
  val className: String,
  val assignedDate: String,
  val dueDate: String,
  val teacherName: String,
  val status: HomeworkStatus = HomeworkStatus.PENDING,
  val maxMarks: Int = 20,
  val submissionNote: String = "",
  val submissionsCount: Int = 28,
  val totalStudents: Int = 32
)

enum class NoticeCategory(val label: String, val colorHex: Long) {
  ALL("All", 0xFF0F3875),
  ACADEMIC("Academic", 0xFF2563EB),
  GENERAL("General", 0xFF059669),
  SPORTS("Sports", 0xFFD97706),
  EVENT("Events", 0xFF7C3AED),
  URGENT("Urgent", 0xFFDC2626)
}

data class Notice(
  val id: String,
  val title: String,
  val content: String,
  val date: String,
  val category: NoticeCategory,
  val publisherRole: String,
  val publisherName: String,
  val isUrgent: Boolean = false,
  val attachmentName: String? = null
) {
  val authorName: String get() = publisherName
  val authorRole: UserRole
    get() = when {
      publisherRole.contains("Teacher", ignoreCase = true) || publisherRole.contains("Exam", ignoreCase = true) -> UserRole.TEACHER
      publisherRole.contains("Staff", ignoreCase = true) || publisherRole.contains("Sports", ignoreCase = true) -> UserRole.STAFF
      else -> UserRole.ADMIN
    }
}

data class SchoolEvent(
  val id: String,
  val title: String,
  val description: String,
  val date: String,
  val time: String,
  val location: String,
  val category: String,
  val iconName: String = "event"
)

enum class DutyStatus(val label: String, val colorHex: Long) {
  PENDING("Pending", 0xFFD97706),
  IN_PROGRESS("In Progress", 0xFF2563EB),
  COMPLETED("Completed", 0xFF059669)
}

enum class DutyPriority(val label: String) {
  HIGH("High"),
  MEDIUM("Medium"),
  LOW("Low")
}

data class DutyTask(
  val id: String,
  val title: String,
  val area: String,
  val scheduledTime: String,
  val status: DutyStatus,
  val assignedTo: String,
  val priority: DutyPriority = DutyPriority.MEDIUM
)

enum class NotificationType(val label: String, val colorHex: Long) {
  ALL("All", 0xFF0F3875),
  ACADEMIC("Academic", 0xFF2563EB),
  ATTENDANCE("Attendance", 0xFF059669),
  HOMEWORK("Homework", 0xFFD97706),
  NOTICE("Notice", 0xFFDC2626),
  EXAM("Exam", 0xFF7C3AED),
  FEE("Fee & Admin", 0xFF0891B2),
  EVENT("Event", 0xFFE11D48),
  BUS("Bus & Transport", 0xFFD97706),
  ANNOUNCEMENT("Announcement", 0xFFDC2626)
}

data class AppNotification(
  val id: String,
  val title: String,
  val message: String,
  val timeAgo: String,
  val type: NotificationType,
  val isRead: Boolean = false,
  val actionRoute: String? = null,
  val isUrgent: Boolean = false
)

// ==================== WAVE 1 ERP MODELS ====================

// --- 1. Calendar ---
enum class CalendarCategory(val label: String, val colorHex: Long) {
  ALL("All Events", 0xFF0F3875),
  ACADEMIC("Academic", 0xFF2563EB),
  EXAM("Exams", 0xFF7C3AED),
  HOLIDAY("Holidays", 0xFF059669),
  SPORTS("Sports & Games", 0xFFD97706),
  CULTURAL("Cultural & Arts", 0xFFE11D48),
  MEETING("PTM & Meetings", 0xFF0891B2)
}

data class CalendarEvent(
  val id: String,
  val title: String,
  val description: String,
  val date: String, // YYYY-MM-DD or readable
  val formattedDate: String,
  val time: String,
  val location: String,
  val category: CalendarCategory,
  val isHoliday: Boolean = false,
  val targetGrades: String = "All Classes",
  val organizer: String = "St. Joseph's Academic Council",
  val hasReminder: Boolean = false
)

// --- 2. Live Bus Tracking ---
enum class BusStatus(val label: String, val colorHex: Long) {
  ON_TIME("On Time", 0xFF059669),
  DELAYED("Delayed", 0xFFD97706),
  ARRIVED("Arrived at Campus", 0xFF2563EB),
  HALTED("Halted", 0xFFDC2626)
}

enum class PassengerBoardingStatus(val label: String, val colorHex: Long) {
  WAITING("Waiting at Stop", 0xFFD97706),
  BOARDED("Boarded Bus", 0xFF059669),
  ABSENT("Absent / On Leave", 0xFFDC2626),
  DROPPED_OFF("Dropped Off", 0xFF2563EB)
}

data class BusPassenger(
  val id: String,
  val name: String,
  val role: UserRole = UserRole.STUDENT,
  val gradeAndSection: String = "Class 12-A",
  val rollNo: Int? = 1,
  val parentName: String = "Guardian",
  val parentPhone: String = "+91 98450 78912",
  val emergencyPhone: String = "+91 98450 78912",
  val boardingStatus: PassengerBoardingStatus = PassengerBoardingStatus.WAITING,
  val stopId: String,
  val stopName: String,
  val checkInTime: String? = null
)

data class BusStop(
  val id: String,
  val name: String,
  val scheduledTime: String,
  val isCompleted: Boolean = false,
  val isCurrent: Boolean = false,
  val isSkipped: Boolean = false,
  val skipReason: String? = null,
  val isExtraDetourStop: Boolean = false,
  val studentCount: Int = 4,
  val latitude: Double = 12.7463,
  val longitude: Double = 77.8073,
  val passengers: List<BusPassenger> = emptyList()
)

data class BusRoute(
  val id: String,
  val routeNumber: String,
  val routeName: String,
  val busRegistration: String,
  val driverName: String,
  val driverPhone: String,
  val attendantName: String,
  val attendantPhone: String,
  val currentSpeedKmH: Int,
  val currentLocationName: String,
  val nextStopName: String,
  val estimatedArrivalMins: Int,
  val status: BusStatus,
  val delayMinutes: Int = 0,
  val delayReason: String? = null,
  val capacity: Int = 45,
  val studentsOnboard: Int = 34,
  val stops: List<BusStop>,
  val progressPercent: Float = 0.60f,
  val currentLatitude: Double = 12.7440,
  val currentLongitude: Double = 77.8010,
  val currentHeadingDegrees: Float = 65f,
  val schoolLatitude: Double = 12.74632,
  val schoolLongitude: Double = 77.80728,
  val morningTripTime: String = "07:15 AM - 08:20 AM",
  val eveningTripTime: String = "03:30 PM - 04:45 PM",
  val activeDetourAlert: String? = null,
  val isTripActive: Boolean = true
)

// --- 3. School Announcements ---
enum class AnnouncementPriority(val label: String, val colorHex: Long) {
  URGENT("Emergency Alert", 0xFFDC2626),
  HIGH("High Priority", 0xFFD97706),
  GENERAL("General Bulletin", 0xFF2563EB)
}

enum class AnnouncementAudience(val label: String) {
  ALL_SCHOOL("Entire School Community"),
  STUDENTS_ONLY("Students Only"),
  TEACHERS_FACULTY("Teachers & Faculty"),
  PARENTS_ONLY("Parents Only"),
  SENIOR_SECONDARY("Classes 10, 11 & 12")
}

data class SchoolAnnouncement(
  val id: String,
  val title: String,
  val content: String,
  val priority: AnnouncementPriority,
  val targetAudience: AnnouncementAudience,
  val date: String,
  val timeAgo: String,
  val authorName: String,
  val authorRole: String,
  val isEmergency: Boolean = false,
  val audioDurationSec: Int? = null,
  val acknowledgedByCurrentUser: Boolean = false,
  val acknowledgmentsCount: Int = 128,
  val attachmentName: String? = null
)

// --- 4. Role-Gated School Directory ---
enum class DirectoryCategory(val label: String) {
  ALL("All"),
  FACULTY("Teachers"),
  ADMINISTRATION("Administration"),
  STAFF("Support Staff"),
  TRANSPORT("Transport"),
  STUDENTS("Students"),
  HELPLINE("Helpline & Emergency")
}

data class DirectoryContact(
  val id: String,
  val name: String,
  val role: UserRole,
  val category: DirectoryCategory,
  val designation: String,
  val departmentOrGrade: String,
  val phoneNumber: String, // Redacted for students viewing other students!
  val email: String,
  val roomOrLocation: String,
  val isStudent: Boolean = false,
  val parentContact: String = "",
  val bloodGroup: String = ""
)

// --- 5. Cloud Sync Status & Telemetry ---
enum class CloudSyncState(val displayName: String) {
  SYNCED("Cloud Synced"),
  SYNCING("Syncing..."),
  OFFLINE("Working Offline"),
  ERROR("Sync Alert")
}

data class CloudSyncInfo(
  val state: CloudSyncState = CloudSyncState.SYNCED,
  val lastSyncedTime: String = "Just now",
  val pendingChangesCount: Int = 0,
  val isRealtimeConnected: Boolean = true,
  val firestoreProject: String = "st-josephs-erp-prod"
)
