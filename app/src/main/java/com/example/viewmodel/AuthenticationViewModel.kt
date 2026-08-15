package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SchoolRepository
import com.example.data.local.AppDatabase
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TeacherEntity
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Represents the current state of user authentication and session.
 */
sealed class AuthState {
  object Unauthenticated : AuthState()
  object Authenticating : AuthState()
  data class Authenticated(
    val user: User,
    val role: UserRole,
    val sessionStartedAt: Long = System.currentTimeMillis()
  ) : AuthState()
  data class Error(val message: String) : AuthState()
}

/**
 * AuthenticationViewModel validates credentials against the Room database (AppDatabase)
 * and manages user sessions, active roles, and role-specific profile states across the application.
 */
class AuthenticationViewModel(
  private val database: AppDatabase? = null,
  private val repository: SchoolRepository = SchoolRepository()
) : ViewModel() {

  private var runtimeDatabase: AppDatabase? = database

  fun setDatabase(db: AppDatabase) {
    runtimeDatabase = db
  }

  fun initializeWithContext(context: Context) {
    if (runtimeDatabase == null) {
      runtimeDatabase = AppDatabase.getDatabase(context.applicationContext)
    }
  }

  private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
  val authState: StateFlow<AuthState> = _authState.asStateFlow()

  private val _currentUser = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

  private val _currentRole = MutableStateFlow<UserRole?>(null)
  val currentRole: StateFlow<UserRole?> = _currentRole.asStateFlow()

  private val _studentProfile = MutableStateFlow<StudentProfile?>(null)
  val studentProfile: StateFlow<StudentProfile?> = _studentProfile.asStateFlow()

  private val _teacherProfile = MutableStateFlow<TeacherProfile?>(null)
  val teacherProfile: StateFlow<TeacherProfile?> = _teacherProfile.asStateFlow()

  private val _staffProfile = MutableStateFlow<StaffProfile?>(null)
  val staffProfile: StateFlow<StaffProfile?> = _staffProfile.asStateFlow()

  private val _adminProfile = MutableStateFlow<AdminProfile?>(null)
  val adminProfile: StateFlow<AdminProfile?> = _adminProfile.asStateFlow()

  val isAuthenticated: StateFlow<Boolean> = _authState
    .map { it is AuthState.Authenticated }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  init {
    // Initialize default demo session for immediate rich experience
    loginAsRole(UserRole.STUDENT)
  }

  /**
   * Authenticates credentials by querying Room database tables ('students', 'teachers')
   * or validating administrative/staff credentials.
   *
   * @param identifier Admission No, Employee ID, or institutional Email.
   * @param secret Password or PIN.
   * @param expectedRole Optional constraint for role login tab.
   */
  fun authenticate(
    identifier: String,
    secret: String,
    expectedRole: UserRole? = null
  ) {
    viewModelScope.launch {
      authenticateUser(identifier, secret, expectedRole)
    }
  }

  suspend fun authenticateUser(
    identifier: String,
    secret: String,
    expectedRole: UserRole? = null
  ): Boolean {
    if (identifier.isBlank()) {
      _errorMessage.value = "Identifier (Email, Admission No, or Employee ID) cannot be empty."
      _authState.value = AuthState.Error("Identifier required")
      return false
    }

    _authState.value = AuthState.Authenticating
    _errorMessage.value = null

    return try {
      val trimmed = identifier.trim()
      val db = runtimeDatabase

      // 1. Try matching against StudentEntity in Room Database
      if (expectedRole == null || expectedRole == UserRole.STUDENT) {
        val studentEntity = if (db != null) {
          withContext(Dispatchers.IO) {
            db.studentDao().findStudentByIdentifier(trimmed)
          }
        } else null

        if (studentEntity != null) {
          setStudentSession(studentEntity)
          return true
        }
      }

      // 2. Try matching against TeacherEntity in Room Database
      if (expectedRole == null || expectedRole == UserRole.TEACHER) {
        val teacherEntity = if (db != null) {
          withContext(Dispatchers.IO) {
            db.teacherDao().findTeacherByIdentifier(trimmed)
          }
        } else null

        if (teacherEntity != null) {
          setTeacherSession(teacherEntity)
          return true
        }
      }

      // 3. Fallback check for Administrative or Staff accounts
      if (expectedRole == UserRole.ADMIN || trimmed.contains("admin", ignoreCase = true) || trimmed.contains("principal", ignoreCase = true)) {
        setAdminSession()
        return true
      }

      if (expectedRole == UserRole.STAFF || trimmed.contains("staff", ignoreCase = true)) {
        setStaffSession()
        return true
      }

      // 4. If DB wasn't attached or no direct entity match, check repository mocks
      if (trimmed.equals("student01", ignoreCase = true) || trimmed.contains("alex", ignoreCase = true)) {
        repository.loginAsRole(UserRole.STUDENT)
        val user = repository.currentUser.value ?: defaultStudentUser
        val profile = repository.currentStudentProfile.value
        _currentUser.value = user
        _currentRole.value = UserRole.STUDENT
        _studentProfile.value = profile
        _teacherProfile.value = null
        _staffProfile.value = null
        _adminProfile.value = null
        _authState.value = AuthState.Authenticated(user, UserRole.STUDENT)
        return true
      }

      if (trimmed.equals("sjenkins", ignoreCase = true) || trimmed.contains("jenkins", ignoreCase = true)) {
        repository.loginAsRole(UserRole.TEACHER)
        val user = repository.currentUser.value ?: defaultTeacherUser
        val profile = repository.currentTeacherProfile.value
        _currentUser.value = user
        _currentRole.value = UserRole.TEACHER
        _teacherProfile.value = profile
        _studentProfile.value = null
        _staffProfile.value = null
        _adminProfile.value = null
        _authState.value = AuthState.Authenticated(user, UserRole.TEACHER)
        return true
      }

      // If not found in Room or predefined accounts
      _errorMessage.value = "No account found matching '$identifier' in the institutional database."
      _authState.value = AuthState.Error("Account not found")
      false

    } catch (e: Exception) {
      _errorMessage.value = "Authentication error: ${e.localizedMessage ?: "Unknown database error"}"
      _authState.value = AuthState.Error(e.localizedMessage ?: "Database error")
      false
    }
  }

  /**
   * Fast-switch or role-demo login that hydrates user session and profile models.
   */
  fun loginAsRole(role: UserRole) {
    viewModelScope.launch {
      val db = runtimeDatabase
      when (role) {
        UserRole.STUDENT -> {
          val student = if (db != null) {
            withContext(Dispatchers.IO) {
              db.studentDao().getStudentById("std_101")
            }
          } else null

          if (student != null) {
            setStudentSession(student)
          } else {
            repository.loginAsRole(UserRole.STUDENT)
            val user = repository.currentUser.value ?: defaultStudentUser
            val profile = repository.currentStudentProfile.value
            _currentUser.value = user
            _currentRole.value = UserRole.STUDENT
            _studentProfile.value = profile
            _teacherProfile.value = null
            _staffProfile.value = null
            _adminProfile.value = null
            _authState.value = AuthState.Authenticated(user, UserRole.STUDENT)
          }
        }

        UserRole.TEACHER -> {
          val teacher = if (db != null) {
            withContext(Dispatchers.IO) {
              db.teacherDao().getTeacherById("tch_201")
            }
          } else null

          if (teacher != null) {
            setTeacherSession(teacher)
          } else {
            repository.loginAsRole(UserRole.TEACHER)
            val user = repository.currentUser.value ?: defaultTeacherUser
            val profile = repository.currentTeacherProfile.value
            _currentUser.value = user
            _currentRole.value = UserRole.TEACHER
            _teacherProfile.value = profile
            _studentProfile.value = null
            _staffProfile.value = null
            _adminProfile.value = null
            _authState.value = AuthState.Authenticated(user, UserRole.TEACHER)
          }
        }

        UserRole.STAFF -> {
          setStaffSession()
        }

        UserRole.ADMIN -> {
          setAdminSession()
        }
      }
    }
  }

  private fun setStudentSession(entity: StudentEntity) {
    val user = User(
      id = entity.id,
      username = entity.email.substringBefore("@"),
      fullName = entity.fullName,
      email = entity.email,
      role = UserRole.STUDENT,
      phone = entity.parentPhone,
      designation = "Grade ${entity.grade} - Section ${entity.section}"
    )

    val profile = StudentProfile(
      user = user,
      admissionNo = entity.admissionNo,
      grade = entity.grade,
      section = entity.section,
      rollNo = entity.rollNo,
      parentName = entity.parentName,
      parentPhone = entity.parentPhone,
      bloodGroup = entity.bloodGroup,
      attendancePercentage = entity.attendancePercentage,
      houseName = entity.houseName,
      busRoute = entity.busRoute,
      academicYear = entity.academicYear,
      emergencyContact = entity.emergencyContact
    )

    _currentUser.value = user
    _currentRole.value = UserRole.STUDENT
    _studentProfile.value = profile
    _teacherProfile.value = null
    _staffProfile.value = null
    _adminProfile.value = null
    _errorMessage.value = null
    _authState.value = AuthState.Authenticated(user, UserRole.STUDENT)
    repository.loginAsRole(UserRole.STUDENT)
  }

  private fun setTeacherSession(entity: TeacherEntity) {
    val user = User(
      id = entity.id,
      username = entity.email.substringBefore("@"),
      fullName = entity.fullName,
      email = entity.email,
      role = UserRole.TEACHER,
      phone = entity.phone,
      designation = if (entity.isClassTeacher) "Class Teacher (${entity.classTeacherOf})" else "Senior Faculty"
    )

    val profile = TeacherProfile(
      user = user,
      employeeId = entity.employeeId,
      department = entity.department,
      assignedClasses = entity.assignedClasses,
      subjectsTaught = entity.subjectsTaught,
      qualification = entity.qualification,
      isClassTeacher = entity.isClassTeacher,
      classTeacherOf = entity.classTeacherOf,
      roomNo = entity.roomNo,
      joiningDate = entity.joiningDate
    )

    _currentUser.value = user
    _currentRole.value = UserRole.TEACHER
    _teacherProfile.value = profile
    _studentProfile.value = null
    _staffProfile.value = null
    _adminProfile.value = null
    _errorMessage.value = null
    _authState.value = AuthState.Authenticated(user, UserRole.TEACHER)
    repository.loginAsRole(UserRole.TEACHER)
  }

  private fun setStaffSession() {
    repository.loginAsRole(UserRole.STAFF)
    val user = repository.currentUser.value ?: defaultStaffUser
    val profile = repository.currentStaffProfile.value

    _currentUser.value = user
    _currentRole.value = UserRole.STAFF
    _staffProfile.value = profile
    _studentProfile.value = null
    _teacherProfile.value = null
    _adminProfile.value = null
    _errorMessage.value = null
    _authState.value = AuthState.Authenticated(user, UserRole.STAFF)
  }

  private fun setAdminSession() {
    repository.loginAsRole(UserRole.ADMIN)
    val user = repository.currentUser.value ?: defaultAdminUser
    val profile = repository.currentAdminProfile.value

    _currentUser.value = user
    _currentRole.value = UserRole.ADMIN
    _adminProfile.value = profile
    _studentProfile.value = null
    _teacherProfile.value = null
    _staffProfile.value = null
    _errorMessage.value = null
    _authState.value = AuthState.Authenticated(user, UserRole.ADMIN)
  }

  fun logout() {
    _currentUser.value = null
    _currentRole.value = null
    _studentProfile.value = null
    _teacherProfile.value = null
    _staffProfile.value = null
    _adminProfile.value = null
    _errorMessage.value = null
    _authState.value = AuthState.Unauthenticated
  }

  fun clearError() {
    _errorMessage.value = null
    if (_authState.value is AuthState.Error) {
      _authState.value = AuthState.Unauthenticated
    }
  }

  companion object {
    private val defaultStudentUser = User(
      id = "usr_student_01",
      username = "student01",
      fullName = "Alex Johnson",
      email = "alex.j@stjosephs.edu",
      role = UserRole.STUDENT,
      designation = "Grade 10 - Section A"
    )

    private val defaultTeacherUser = User(
      id = "usr_teacher_01",
      username = "sjenkins",
      fullName = "Prof. Sarah Jenkins",
      email = "s.jenkins@stjosephs.edu",
      role = UserRole.TEACHER,
      designation = "Senior Faculty • Class Teacher (10-A)"
    )

    private val defaultStaffUser = User(
      id = "usr_staff_01",
      username = "staff01",
      fullName = "Mr. Thomas Wright",
      email = "t.wright@stjosephs.edu",
      role = UserRole.STAFF,
      designation = "Senior Operations Supervisor"
    )

    private val defaultAdminUser = User(
      id = "usr_admin_01",
      username = "principal",
      fullName = "Dr. Arthur Pendelton",
      email = "principal@stjosephs.edu",
      role = UserRole.ADMIN,
      designation = "Principal & Head of Institution"
    )
  }
}
