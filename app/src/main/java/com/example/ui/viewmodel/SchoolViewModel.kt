package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SchoolRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AuthState {
  object Splash : AuthState
  object Unauthenticated : AuthState
  object Loading : AuthState
  data class Authenticated(val user: User) : AuthState
  data class Error(val message: String) : AuthState
}

class SchoolViewModel(
  val repository: SchoolRepository = SchoolRepository()
) : ViewModel() {

  private val _authState = MutableStateFlow<AuthState>(AuthState.Splash)
  val authState: StateFlow<AuthState> = _authState.asStateFlow()

  val currentUser = repository.currentUser
  val studentProfile = repository.currentStudentProfile
  val teacherProfile = repository.currentTeacherProfile
  val staffProfile = repository.currentStaffProfile
  val adminProfile = repository.currentAdminProfile

  val notices = repository.notices
  val homeworks = repository.homeworks
  val timetables = repository.timetables
  val attendanceRecords = repository.attendanceRecords
  val duties = repository.duties
  val events = repository.events
  val classes = repository.classes

  // Selected filters/states
  private val _selectedNoticeCategory = MutableStateFlow(NoticeCategory.ALL)
  val selectedNoticeCategory: StateFlow<NoticeCategory> = _selectedNoticeCategory.asStateFlow()

  private val _selectedTimetableDay = MutableStateFlow(DayOfWeek.MONDAY)
  val selectedTimetableDay: StateFlow<DayOfWeek> = _selectedTimetableDay.asStateFlow()

  private val _selectedAttendanceClass = MutableStateFlow("Class 10-A")
  val selectedAttendanceClass: StateFlow<String> = _selectedAttendanceClass.asStateFlow()

  // Computed metrics
  val studentPendingHomeworkCount = homeworks.map { list ->
    list.count { it.status == HomeworkStatus.PENDING }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

  val filteredNotices = combine(notices, _selectedNoticeCategory) { list, category ->
    if (category == NoticeCategory.ALL) list else list.filter { it.category == category }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val dayTimetable = combine(timetables, _selectedTimetableDay) { list, day ->
    list.filter { it.day == day }.sortedBy { it.periodNumber }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    // Show splash for quick moment then move to Unauthenticated
    viewModelScope.launch {
      kotlinx.coroutines.delay(1200)
      if (_authState.value is AuthState.Splash) {
        _authState.value = AuthState.Unauthenticated
      }
    }
  }

  fun setNoticeCategory(category: NoticeCategory) {
    _selectedNoticeCategory.value = category
  }

  fun setTimetableDay(day: DayOfWeek) {
    _selectedTimetableDay.value = day
  }

  fun setAttendanceClass(className: String) {
    _selectedAttendanceClass.value = className
  }

  fun login(username: String, password: String) {
    if (username.isBlank()) {
      _authState.value = AuthState.Error("Please enter your username")
      return
    }
    _authState.value = AuthState.Loading
    val result = repository.login(username, password)
    result.onSuccess { user ->
      _authState.value = AuthState.Authenticated(user)
    }.onFailure { error ->
      _authState.value = AuthState.Error(error.message ?: "Authentication failed")
    }
  }

  fun loginAsRole(role: UserRole) {
    _authState.value = AuthState.Loading
    repository.loginAsRole(role)
    val user = repository.currentUser.value
    if (user != null) {
      _authState.value = AuthState.Authenticated(user)
    } else {
      _authState.value = AuthState.Error("Could not log in as ${role.displayName}")
    }
  }

  fun logout() {
    repository.logout()
    _authState.value = AuthState.Unauthenticated
  }

  // Interactive repository mutations
  fun submitHomework(homeworkId: String, note: String) {
    repository.submitHomework(homeworkId, note)
  }

  fun assignHomework(title: String, description: String, subject: String, className: String, dueDate: String, maxMarks: Int) {
    repository.assignHomework(title, description, subject, className, dueDate, maxMarks)
  }

  fun addNotice(title: String, content: String, category: NoticeCategory, isUrgent: Boolean) {
    repository.addNotice(title, content, category, isUrgent)
  }

  fun updateAttendanceRecord(studentId: String, newStatus: AttendanceStatus) {
    repository.updateAttendanceRecord(studentId, newStatus)
  }

  fun markAllAttendance(status: AttendanceStatus) {
    repository.markAllAttendance(status)
  }

  fun updateDutyStatus(dutyId: String, newStatus: DutyStatus) {
    repository.updateDutyStatus(dutyId, newStatus)
  }

  fun addDuty(title: String, area: String, scheduledTime: String, priority: DutyPriority) {
    repository.addDuty(title, area, scheduledTime, priority)
  }
}
