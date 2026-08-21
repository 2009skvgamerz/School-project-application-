package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TeacherEntity
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SchoolRepository(context: Context, scope: CoroutineScope) {
    private val database = AppDatabase.getDatabase(context, scope)
    private val studentDao = database.studentDao()
    private val teacherDao = database.teacherDao()
    private val attendanceDao = database.attendanceDao()

    init {
        scope.launch(Dispatchers.IO) {
            if (studentDao.getStudentCount() == 0) {
                AppDatabase.populateInitialData(database)
            }
        }
    }

    fun getStudentsForClass(grade: String, section: String): Flow<List<StudentEntity>> {
        return studentDao.getStudentsByClass(grade, section)
    }

    fun getAllStudents(): Flow<List<StudentEntity>> {
        return studentDao.getAllStudents()
    }

    fun getAllTeachers(): Flow<List<TeacherEntity>> {
        return teacherDao.getAllTeachers()
    }

    suspend fun getTeacherForClass(homeroomClass: String): TeacherEntity? {
        return teacherDao.getTeacherForClass(homeroomClass)
    }

    fun getAttendanceForClass(date: String, className: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceForClass(date, className)
    }

    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceForStudent(studentId)
    }

    suspend fun saveAttendanceRecord(record: AttendanceEntity) {
        attendanceDao.insertSingleAttendance(record)
    }

    suspend fun saveBatchAttendance(records: List<AttendanceEntity>) {
        attendanceDao.insertAttendance(records)
    }

    suspend fun resetDatabase() {
        AppDatabase.populateInitialData(database)
    }

    // Static School Operations Domain Data
    fun getTimetableForClass(className: String): List<TimetableSlot> {
        return listOf(
            TimetableSlot(1, "08:30 - 09:15", "Physics", "Prof. Sarah Jenkins", "Lab 2", "Monday"),
            TimetableSlot(2, "09:20 - 10:05", "Mathematics", "Mr. David Miller", "Room 101", "Monday"),
            TimetableSlot(3, "10:10 - 10:55", "English", "Dr. Emily Watson", "Room 101", "Monday"),
            TimetableSlot(4, "11:15 - 12:00", "Chemistry", "Dr. Robert Stark", "Lab 1", "Monday"),
            TimetableSlot(5, "12:05 - 12:50", "History", "Mrs. Angela Vance", "Room 204", "Monday"),
            TimetableSlot(6, "01:30 - 02:15", "Physical Education", "Mr. Carlos Gomez", "Main Grounds", "Monday"),
            TimetableSlot(7, "02:20 - 03:05", "Computer Science", "Ms. Lisa Ray", "IT Lab 3", "Monday")
        )
    }

    fun getHomeworkList(): List<HomeworkItem> {
        return listOf(
            HomeworkItem("hw_01", "Physics", "Thermodynamics Lab Report", "Complete analysis questions 1-5 from chapter 4.", "2026-08-23", "10-A", "Prof. Sarah Jenkins", false),
            HomeworkItem("hw_02", "Mathematics", "Quadratic Equations Exercise", "Solve problems 10 through 25 on page 142.", "2026-08-24", "10-A", "Mr. David Miller", true),
            HomeworkItem("hw_03", "English", "Essay on Shakespearean Tragedy", "Submit 500-word critical evaluation of Macbeth.", "2026-08-26", "10-A", "Dr. Emily Watson", false),
            HomeworkItem("hw_04", "Computer Science", "Kotlin Coroutines Project", "Implement the state machine repository pattern.", "2026-08-28", "10-A", "Ms. Lisa Ray", false)
        )
    }

    fun getNotices(): List<NoticeItem> {
        return listOf(
            NoticeItem("not_01", "Annual Science Olympiad Registration Open", "Students interested in representing St. Joseph's at the State Level Science Olympiad must submit entries by Friday.", "2026-08-20", "Academic", false, "Principal's Office"),
            NoticeItem("not_02", "Urgent: Parent-Teacher Assembly Scheduled", "Mandatory term evaluation review session will be held this Saturday from 9:00 AM to 1:00 PM.", "2026-08-19", "Administrative", true, "Administration"),
            NoticeItem("not_03", "Inter-House Football Championship", "Prelims begin Tuesday afternoon on the main turf. St. Patrick vs St. George.", "2026-08-18", "Sports", false, "Sports Department")
        )
    }

    fun getFeeRecords(): List<FeeRecord> {
        return listOf(
            FeeRecord("Term 1 (Fall 2026)", 1250.0, 1250.0, "2026-07-15", "Paid"),
            FeeRecord("Term 2 (Winter 2026)", 1250.0, 1250.0, "2026-10-15", "Paid"),
            FeeRecord("Term 3 (Spring 2027)", 1250.0, 0.0, "2027-01-15", "Pending"),
            FeeRecord("Annual Lab & Tech Fee", 300.0, 300.0, "2026-08-01", "Paid")
        )
    }

    fun getCampusDuties(): List<CampusDuty> {
        return listOf(
            CampusDuty("duty_01", "Morning Main Gate Screening", "Gate 1 - North Entrance", "07:45 - 08:30", "Mr. Thomas Wright", "2026-08-21", true),
            CampusDuty("duty_02", "Cafeteria & Recess Supervision", "Central Dining Hall", "12:00 - 12:45", "Mr. Thomas Wright", "2026-08-21", false),
            CampusDuty("duty_03", "Chemistry Lab Fire Safety Check", "Science Block Floor 2", "03:15 - 04:00", "Mr. Thomas Wright", "2026-08-21", false),
            CampusDuty("duty_04", "Evening Bus Departure Coordination", "Bus Bay 4", "03:30 - 04:15", "Mr. Carlos Gomez", "2026-08-21", false)
        )
    }

    fun getStudentSubjectAttendance(): List<SubjectAttendance> {
        return listOf(
            SubjectAttendance("Physics", 38, 40, 95.0f),
            SubjectAttendance("Mathematics", 42, 45, 93.3f),
            SubjectAttendance("English", 36, 40, 90.0f),
            SubjectAttendance("Chemistry", 35, 40, 87.5f),
            SubjectAttendance("Computer Science", 30, 30, 100.0f),
            SubjectAttendance("Physical Education", 18, 20, 90.0f)
        )
    }
}
