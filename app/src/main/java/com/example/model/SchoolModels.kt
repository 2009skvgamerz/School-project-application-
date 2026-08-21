package com.example.model

enum class UserRole(val displayName: String) {
    STUDENT("Student"),
    TEACHER("Teacher"),
    STAFF("Operations Staff"),
    ADMIN("Administrator / Principal")
}

enum class AttendanceStatus(val code: String, val label: String, val weight: Float) {
    FULL_DAY("FD", "Full Day", 1.0f),
    HALF_DAY("HD", "Half Day", 0.5f),
    ON_DUTY("OD", "On Duty", 1.0f),
    ABSENT("AB", "Absent", 0.0f)
}

data class UserSession(
    val id: String,
    val username: String,
    val fullName: String,
    val role: UserRole,
    val email: String,
    val avatarUrl: String = "",
    val grade: String = "",
    val section: String = "",
    val rollNumber: String = "",
    val house: String = "St. Patrick",
    val bloodGroup: String = "O+",
    val homeroomClass: String = "", // e.g. "10-A" if homeroom teacher
    val subjects: List<String> = emptyList()
)

data class StudentItem(
    val id: String,
    val name: String,
    val rollNo: String,
    val grade: String,
    val section: String,
    val gender: String,
    val attendanceRate: Float,
    val parentContact: String,
    val house: String = "St. Patrick"
)

data class TeacherItem(
    val id: String,
    val name: String,
    val email: String,
    val department: String,
    val homeroomClass: String,
    val phone: String
)

data class AttendanceRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val rollNo: String,
    val className: String,
    val date: String,
    val status: AttendanceStatus,
    val remarks: String = "",
    val markedByTeacherId: String = ""
)

data class SubjectAttendance(
    val subject: String,
    val attended: Int,
    val total: Int,
    val percentage: Float
)

data class TimetableSlot(
    val period: Int,
    val time: String,
    val subject: String,
    val teacher: String,
    val room: String,
    val dayOfWeek: String = "Monday"
)

data class HomeworkItem(
    val id: String,
    val subject: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val className: String,
    val teacherName: String,
    val isCompleted: Boolean = false
)

data class NoticeItem(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val category: String,
    val isUrgent: Boolean = false,
    val author: String = "Administration"
)

data class FeeRecord(
    val term: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val dueDate: String,
    val status: String // "Paid", "Pending", "Overdue"
)

data class CampusDuty(
    val id: String,
    val title: String,
    val location: String,
    val timeSlot: String,
    val staffAssigned: String,
    val date: String,
    val isCompleted: Boolean = false
)
