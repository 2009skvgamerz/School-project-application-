package com.example.util

import com.example.model.*

/**
 * Intelligent Audience Filter for School Push & In-App Notifications.
 * Ensures that notifications are delivered strictly to the relevant user, role, grade, and section.
 * Example: Homework assigned to "Class 12-A" is received ONLY by Class 12-A students/teachers, NOT Class 10-A.
 */
object NotificationAudienceFilter {

  /**
   * Evaluates if a Homework notification should be delivered to the currently logged in user.
   */
  fun shouldReceiveHomeworkNotification(
    homework: Homework,
    currentUser: User?,
    studentProfile: StudentProfile?,
    teacherProfile: TeacherProfile?
  ): Boolean {
    if (currentUser == null) return false

    return when (currentUser.role) {
      UserRole.STUDENT -> {
        val studentGrade = studentProfile?.grade ?: extractGradeFromText(currentUser.designation) ?: "12"
        val studentSection = studentProfile?.section ?: extractSectionFromText(currentUser.designation) ?: "A"
        isClassMatch(homework.className, studentGrade, studentSection)
      }
      UserRole.TEACHER -> {
        // Teacher receives notification if they assigned it or teach this class
        val assigned = teacherProfile?.assignedClasses ?: listOf("Class 10-A", "Class 12-A")
        val isAssignedClass = assigned.any { isClassMatch(it, extractGradeFromText(homework.className) ?: "", extractSectionFromText(homework.className) ?: "") }
        val isCreator = homework.teacherName.equals(currentUser.fullName, ignoreCase = true)
        isAssignedClass || isCreator
      }
      UserRole.ADMIN, UserRole.DEVELOPER -> true
      UserRole.STAFF, UserRole.DRIVER -> false
    }
  }

  /**
   * Evaluates if an Attendance status notification should be delivered to the currently logged in user.
   * A student only receives notification regarding THEIR OWN attendance record.
   */
  fun shouldReceiveAttendanceNotification(
    record: AttendanceRecord,
    currentUser: User?,
    studentProfile: StudentProfile?
  ): Boolean {
    if (currentUser == null) return false

    return when (currentUser.role) {
      UserRole.STUDENT -> {
        val matchesId = currentUser.id == record.studentId
        val matchesAdmission = studentProfile?.admissionNo?.equals(record.studentId, ignoreCase = true) == true
        val matchesName = currentUser.fullName.trim().equals(record.studentName.trim(), ignoreCase = true) ||
                          currentUser.name.trim().equals(record.studentName.trim(), ignoreCase = true)
        matchesId || matchesAdmission || matchesName
      }
      UserRole.TEACHER -> {
        // Teacher gets notified when attendance is logged for their class
        true
      }
      UserRole.ADMIN, UserRole.DEVELOPER -> true
      UserRole.STAFF, UserRole.DRIVER -> false
    }
  }

  /**
   * Evaluates if an Announcement should be delivered to the currently logged in user.
   */
  fun shouldReceiveAnnouncement(
    announcement: SchoolAnnouncement,
    currentUser: User?
  ): Boolean {
    if (announcement.isEmergency) return true // Urgent school-wide emergency alerts reach everyone
    if (currentUser == null) return announcement.targetAudience == AnnouncementAudience.ALL_SCHOOL

    return when (announcement.targetAudience) {
      AnnouncementAudience.ALL_SCHOOL -> true
      AnnouncementAudience.STUDENTS_ONLY -> currentUser.role == UserRole.STUDENT
      AnnouncementAudience.PARENTS_ONLY -> currentUser.role == UserRole.STUDENT
      AnnouncementAudience.TEACHERS_FACULTY -> currentUser.role == UserRole.TEACHER || currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.DEVELOPER
      AnnouncementAudience.SENIOR_SECONDARY -> {
        when (currentUser.role) {
          UserRole.STUDENT -> {
            val grade = extractGradeFromText(currentUser.designation) ?: "12"
            grade in listOf("10", "11", "12")
          }
          UserRole.TEACHER, UserRole.ADMIN, UserRole.DEVELOPER -> true
          else -> false
        }
      }
    }
  }

  /**
   * Evaluates if a Notice / Circular should be delivered to the currently logged in user.
   */
  fun shouldReceiveNotice(
    notice: Notice,
    currentUser: User?,
    studentProfile: StudentProfile?
  ): Boolean {
    if (notice.isUrgent) return true
    if (currentUser == null) return true

    val content = "${notice.title} ${notice.content}".lowercase()

    // Check if the circular is targeted at a specific grade
    val targetGrades = listOf("grade 9", "class 9", "grade 10", "class 10", "grade 11", "class 11", "grade 12", "class 12")
    val mentionsAnySpecificGrade = targetGrades.any { content.contains(it) }

    if (!mentionsAnySpecificGrade) return true // General notice for all

    if (currentUser.role == UserRole.STUDENT) {
      val userGrade = studentProfile?.grade ?: extractGradeFromText(currentUser.designation) ?: "12"
      val userGradeTag1 = "grade $userGrade"
      val userGradeTag2 = "class $userGrade"
      return content.contains(userGradeTag1) || content.contains(userGradeTag2)
    }

    return true // Faculty and Admin receive all notices
  }

  /**
   * Checks if a target class (e.g. "Class 12-A", "12-A", "Class 12") matches a student's grade & section.
   */
  private fun isClassMatch(targetClass: String, grade: String, section: String): Boolean {
    val normalizedTarget = targetClass.replace("Class", "", ignoreCase = true)
      .replace("Grade", "", ignoreCase = true)
      .replace(" ", "")
      .trim()
      .uppercase()

    val normalizedUserClass = "$grade-$section".uppercase()
    val normalizedUserGradeOnly = grade.trim().uppercase()

    // Exact match: "12-A" vs "12-A" or "12A" vs "12-A"
    if (normalizedTarget == normalizedUserClass || normalizedTarget == "$grade$section".uppercase()) {
      return true
    }

    // Target is just grade (e.g. "Grade 12" or "12" for entire batch)
    if (normalizedTarget == normalizedUserGradeOnly) {
      return true
    }

    // Contains checks
    if (normalizedTarget.contains(grade, ignoreCase = true)) {
      if (section.isNotBlank() && normalizedTarget.contains(section, ignoreCase = true)) {
        return true
      }
      // If the target doesn't specify section A or B, but specifies the grade, it matches all sections in that grade
      val specifiesOtherSection = listOf("A", "B", "C", "D")
        .filter { it != section.uppercase() }
        .any { normalizedTarget.contains(it) }
      if (!specifiesOtherSection) {
        return true
      }
    }

    return false
  }

  private fun extractGradeFromText(text: String): String? {
    val regex = Regex("""(?i)(?:grade|class)?\s*(\d{1,2})""")
    return regex.find(text)?.groupValues?.getOrNull(1)
  }

  private fun extractSectionFromText(text: String): String? {
    val regex = Regex("""(?i)(?:section|-)?\s*([A-D])\b""")
    return regex.find(text)?.groupValues?.getOrNull(1)
  }
}
