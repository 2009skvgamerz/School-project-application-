package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SchoolRepository
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AttendanceEntity
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SchoolViewModel(
  private val repository: SchoolRepository = SchoolRepository(),
  private val database: AppDatabase? = null
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

  // Room DB live attendance records stream
  val roomAttendanceRecords: StateFlow<List<AttendanceEntity>> = flow {
    val db = runtimeDatabase
    if (db != null) {
      emitAll(db.attendanceDao().getAllAttendanceRecords())
    } else {
      // Fallback map from repository
      emitAll(attendanceRecords.map { list ->
        list.map {
          AttendanceEntity(
            id = it.id,
            studentId = it.studentId,
            studentName = it.studentName,
            rollNo = it.rollNo,
            className = it.className,
            date = it.date,
            status = it.status,
            markedBy = it.markedBy,
            notes = it.notes
          )
        }
      })
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

  fun updateStudentAttendanceStatus(
    studentId: String,
    status: AttendanceStatus,
    notes: String = "",
    markedBy: String = "Prof. Sarah Jenkins (Class Teacher)"
  ) {
    // 1. Update in-memory repository
    if (notes.isNotBlank()) {
      repository.updateAttendanceRecordWithNotes(studentId, status, notes, markedBy)
    } else {
      repository.updateAttendanceRecord(studentId, status, markedBy)
    }

    // 2. Persist in Room Database
    viewModelScope.launch {
      runtimeDatabase?.let { db ->
        withContext(Dispatchers.IO) {
          if (notes.isNotBlank()) {
            db.attendanceDao().updateStudentAttendanceStatusWithNotes(
              studentId = studentId,
              status = status,
              notes = notes,
              markedBy = markedBy
            )
          } else {
            db.attendanceDao().updateStudentAttendanceStatus(
              studentId = studentId,
              status = status,
              markedBy = markedBy
            )
          }
        }
      }
    }
  }

  fun markAllFullDay(
    className: String = _selectedClassForAttendance.value,
    markedBy: String = "Prof. Sarah Jenkins (Class Teacher)"
  ) {
    // 1. Update in-memory repository
    repository.markAllAttendance(AttendanceStatus.FULL_DAY, className, markedBy)

    // 2. Persist in Room Database
    viewModelScope.launch {
      runtimeDatabase?.let { db ->
        withContext(Dispatchers.IO) {
          db.attendanceDao().markAllClassAttendance(
            className = className,
            status = AttendanceStatus.FULL_DAY,
            markedBy = markedBy
          )
        }
      }
    }
  }

  fun markAllPresent() {
    markAllFullDay(_selectedClassForAttendance.value)
  }
}
