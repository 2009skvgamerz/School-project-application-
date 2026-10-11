package com.example.model

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
