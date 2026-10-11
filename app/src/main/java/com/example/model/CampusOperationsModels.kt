package com.example.model

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
  val firestoreProject: String = "st-joseph-school-app"
)
