package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SchoolRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SchoolViewModel(
  private val repository: SchoolRepository = SchoolRepository()
) : ViewModel() {

  init {
    // Default logged in as Student to show rich interface immediately
    repository.loginAsRole(UserRole.STUDENT)
  }

  // Current User & Profiles
  val currentUser: StateFlow<User> = repository.currentUser
    .filterNotNull()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = User(
        id = "usr_student_01",
        username = "student01",
        fullName = "Alex Johnson",
        email = "alex.j@stjosephs.edu",
        role = UserRole.STUDENT,
        designation = "Grade 10 - Section A"
      )
    )

  val studentProfile: StateFlow<StudentProfile?> = repository.currentStudentProfile
  val teacherProfile: StateFlow<TeacherProfile?> = repository.currentTeacherProfile
  val staffProfile: StateFlow<StaffProfile?> = repository.currentStaffProfile
  val adminProfile: StateFlow<AdminProfile?> = repository.currentAdminProfile

  // Data streams
  val notices: StateFlow<List<Notice>> = repository.notices
  val schoolEvents: StateFlow<List<SchoolEvent>> = repository.events
  val homeworks: StateFlow<List<Homework>> = repository.homeworks
  val timetables: StateFlow<List<TimetableEntry>> = repository.timetables
  val schoolClasses: StateFlow<List<SchoolClass>> = repository.classes
  val staffDuties: StateFlow<List<DutyTask>> = repository.duties
  val attendanceRecords: StateFlow<List<AttendanceRecord>> = repository.attendanceRecords

  // UI Filters / Selections
  private val _selectedDay = MutableStateFlow(DayOfWeek.MONDAY)
  val selectedDay: StateFlow<DayOfWeek> = _selectedDay.asStateFlow()

  private val _selectedClassForAttendance = MutableStateFlow("Class 10-A")
  val selectedClassForAttendance: StateFlow<String> = _selectedClassForAttendance.asStateFlow()

  private val _selectedNoticeCategory = MutableStateFlow<NoticeCategory?>(null)
  val selectedNoticeCategory: StateFlow<NoticeCategory?> = _selectedNoticeCategory.asStateFlow()

  val pendingHomeworkCount: StateFlow<Int> = homeworks
    .map { list -> list.count { it.status == HomeworkStatus.PENDING } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

  // Actions
  fun switchRole(role: UserRole) {
    repository.loginAsRole(role)
  }

  fun setSelectedDay(day: DayOfWeek) {
    _selectedDay.value = day
  }

  fun setSelectedClass(className: String) {
    _selectedClassForAttendance.value = className
  }

  fun setSelectedCategory(category: NoticeCategory?) {
    _selectedNoticeCategory.value = category
  }

  fun submitHomework(homeworkId: String, note: String) {
    repository.submitHomework(homeworkId, note)
  }

  fun createHomework(
    title: String,
    description: String,
    subjectName: String,
    className: String,
    dueDate: String,
    maxMarks: Int
  ) {
    repository.assignHomework(title, description, subjectName, className, dueDate, maxMarks)
  }

  fun publishNotice(
    title: String,
    content: String,
    category: NoticeCategory,
    isUrgent: Boolean
  ) {
    repository.addNotice(title, content, category, isUrgent)
  }

  fun updateDutyStatus(dutyId: String, newStatus: DutyStatus) {
    repository.updateDutyStatus(dutyId, newStatus)
  }

  fun addDutyTask(title: String, area: String, time: String, priority: DutyPriority = DutyPriority.MEDIUM) {
    repository.addDuty(title, area, time, priority)
  }

  fun updateStudentAttendanceStatus(studentId: String, status: AttendanceStatus) {
    repository.updateAttendanceRecord(studentId, status)
  }

  fun markAllPresent() {
    repository.markAllAttendance(AttendanceStatus.PRESENT)
  }
}
