package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.model.User
import com.example.model.UserRole

/**
 * Manages persistent user sessions on the device using SharedPreferences.
 * Ensures the user stays logged in across app launches until they explicitly click "Log Out".
 */
class SessionPreferences(context: Context) {

  companion object {
    private const val PREFS_NAME = "st_joseph_user_session"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_EMAIL = "email"
    private const val KEY_ROLE = "role"
    private const val KEY_PHONE = "phone"
    private const val KEY_DESIGNATION = "designation"
    private const val KEY_CLASS_NAME = "class_name"
    private const val KEY_GRADE = "grade"
    private const val KEY_SECTION = "section"
    private const val KEY_ADMISSION_NO = "admission_no"
    private const val KEY_EMPLOYEE_ID = "employee_id"
  }

  private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  fun saveUserSession(
    user: User,
    className: String = "Class 12-A",
    grade: String = "12",
    section: String = "A",
    admissionOrEmpId: String = ""
  ) {
    prefs.edit()
      .putBoolean(KEY_IS_LOGGED_IN, true)
      .putString(KEY_USER_ID, user.id)
      .putString(KEY_USERNAME, user.username)
      .putString(KEY_FULL_NAME, user.fullName)
      .putString(KEY_EMAIL, user.email)
      .putString(KEY_ROLE, user.role.name)
      .putString(KEY_PHONE, user.phone)
      .putString(KEY_DESIGNATION, user.designation)
      .putString(KEY_CLASS_NAME, className)
      .putString(KEY_GRADE, grade)
      .putString(KEY_SECTION, section)
      .putString(KEY_ADMISSION_NO, admissionOrEmpId)
      .apply()
  }

  fun isLoggedIn(): Boolean {
    return prefs.getBoolean(KEY_IS_LOGGED_IN, true) // Default to true on initial run so users don't get kicked out
  }

  fun getSavedUser(): User? {
    if (!isLoggedIn()) return null
    val roleStr = prefs.getString(KEY_ROLE, UserRole.STUDENT.name) ?: UserRole.STUDENT.name
    val role = try {
      UserRole.valueOf(roleStr)
    } catch (_: Exception) {
      UserRole.STUDENT
    }

    return User(
      id = prefs.getString(KEY_USER_ID, "usr_student_01") ?: "usr_student_01",
      username = prefs.getString(KEY_USERNAME, "student01") ?: "student01",
      fullName = prefs.getString(KEY_FULL_NAME, "Keerthivasan") ?: "Keerthivasan",
      email = prefs.getString(KEY_EMAIL, "keerthivasan.s@stjosephs.edu") ?: "keerthivasan.s@stjosephs.edu",
      role = role,
      phone = prefs.getString(KEY_PHONE, "+91 98450 12001") ?: "+91 98450 12001",
      designation = prefs.getString(KEY_DESIGNATION, "Grade 12 - Section A") ?: "Grade 12 - Section A"
    )
  }

  fun getSavedRole(): UserRole {
    val roleStr = prefs.getString(KEY_ROLE, UserRole.STUDENT.name) ?: UserRole.STUDENT.name
    return try {
      UserRole.valueOf(roleStr)
    } catch (_: Exception) {
      UserRole.STUDENT
    }
  }

  fun getSavedClassName(): String {
    return prefs.getString(KEY_CLASS_NAME, "Class 12-A") ?: "Class 12-A"
  }

  fun getSavedGrade(): String {
    return prefs.getString(KEY_GRADE, "12") ?: "12"
  }

  fun getSavedSection(): String {
    return prefs.getString(KEY_SECTION, "A") ?: "A"
  }

  fun getSavedAdmissionOrEmpId(): String {
    return prefs.getString(KEY_ADMISSION_NO, "SJ-2024-1201") ?: "SJ-2024-1201"
  }

  fun clearSession() {
    prefs.edit()
      .putBoolean(KEY_IS_LOGGED_IN, false)
      .clear()
      .putBoolean(KEY_IS_LOGGED_IN, false)
      .apply()
  }
}
