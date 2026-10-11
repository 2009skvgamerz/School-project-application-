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
