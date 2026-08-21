package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SchoolRepository
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.StudentEntity
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SchoolViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SchoolRepository(application, viewModelScope)

    private val _selectedClass = MutableStateFlow("10-A")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    private val _currentDate = MutableStateFlow("2026-08-21")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val studentsInSelectedClass: StateFlow<List<StudentEntity>> = _selectedClass
        .flatMapLatest { className ->
            val parts = className.split("-")
            val grade = parts.getOrNull(0) ?: "10"
            val section = parts.getOrNull(1) ?: "A"
            repository.getStudentsForClass(grade, section)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceForCurrentClass: StateFlow<List<AttendanceEntity>> = combine(
        _currentDate,
        _selectedClass
    ) { date, className ->
        Pair(date, className)
    }.flatMapLatest { (date, className) ->
        repository.getAttendanceForClass(date, className)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<StudentEntity>> = repository.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeachers = repository.getAllTeachers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedClass(className: String) {
        _selectedClass.value = className
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun markAttendance(studentId: String, studentName: String, rollNo: String, status: AttendanceStatus, remarks: String = "", teacherId: String = "") {
        viewModelScope.launch {
            val record = AttendanceEntity(
                id = "${_currentDate.value}_${studentId}",
                studentId = studentId,
                studentName = studentName,
                rollNo = rollNo,
                className = _selectedClass.value,
                date = _currentDate.value,
                status = status,
                remarks = remarks,
                markedByTeacherId = teacherId
            )
            repository.saveAttendanceRecord(record)
        }
    }

    fun markAllFullDay(students: List<StudentEntity>, teacherId: String) {
        viewModelScope.launch {
            val records = students.map { student ->
                AttendanceEntity(
                    id = "${_currentDate.value}_${student.id}",
                    studentId = student.id,
                    studentName = student.name,
                    rollNo = student.rollNo,
                    className = _selectedClass.value,
                    date = _currentDate.value,
                    status = AttendanceStatus.FULL_DAY,
                    remarks = "Marked Full Day present",
                    markedByTeacherId = teacherId
                )
            }
            repository.saveBatchAttendance(records)
        }
    }

    fun getTimetable() = repository.getTimetableForClass(_selectedClass.value)
    fun getHomeworkList() = repository.getHomeworkList()
    fun getNotices() = repository.getNotices()
    fun getFeeRecords() = repository.getFeeRecords()
    fun getCampusDuties() = repository.getCampusDuties()
    fun getStudentSubjectAttendance() = repository.getStudentSubjectAttendance()

    fun resetDatabase() {
        viewModelScope.launch {
            repository.resetDatabase()
        }
    }
}
