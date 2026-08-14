package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.SchoolRepository
import com.example.data.local.SchoolDatabase
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TeacherEntity
import com.example.model.AttendanceStatus
import com.example.model.HomeworkStatus
import com.example.model.NoticeCategory
import com.example.model.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: SchoolDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, SchoolDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("St. Joseph's School", appName)
  }

  @Test
  fun `verify room entities and daos for Student, Teacher, and Attendance`() = runBlocking {
    val studentDao = db.studentDao()
    val teacherDao = db.teacherDao()
    val attendanceDao = db.attendanceDao()

    // 1. Student Entity & DAO test
    val student = StudentEntity(
      id = "std_101",
      admissionNo = "SJ-2024-1001",
      fullName = "Alex Johnson",
      email = "alex.j@stjosephs.edu",
      grade = "Class 10",
      section = "A",
      rollNo = 1,
      parentName = "Robert Johnson",
      parentPhone = "+91 98450 11223",
      bloodGroup = "O+",
      attendancePercentage = 96.4
    )
    studentDao.insertStudent(student)
    val fetchedStudent = studentDao.getStudentById("std_101")
    assertNotNull(fetchedStudent)
    assertEquals("Alex Johnson", fetchedStudent?.fullName)
    assertEquals("Class 10-A", fetchedStudent?.className)

    // 2. Teacher Entity & DAO test
    val teacher = TeacherEntity(
      id = "tch_201",
      employeeId = "EMP-0412",
      fullName = "Prof. Sarah Jenkins",
      email = "s.jenkins@stjosephs.edu",
      phone = "+91 98765 43210",
      department = "Physical & Chemical Sciences",
      assignedClasses = listOf("Class 10-A", "Class 10-B"),
      subjectsTaught = listOf("Physics", "General Science"),
      qualification = "M.Sc. Physics, B.Ed",
      isClassTeacher = true,
      classTeacherOf = "Class 10-A"
    )
    teacherDao.insertTeacher(teacher)
    val fetchedTeacher = teacherDao.getClassTeacherForClass("Class 10-A")
    assertNotNull(fetchedTeacher)
    assertEquals("Prof. Sarah Jenkins", fetchedTeacher?.fullName)
    assertTrue(fetchedTeacher?.isClassTeacher == true)

    // 3. Attendance Entity & DAO test
    val attendance = AttendanceEntity(
      id = "att_1",
      studentId = "std_101",
      studentName = "Alex Johnson",
      rollNo = 1,
      className = "Class 10-A",
      date = "2026-08-14",
      status = AttendanceStatus.FULL_DAY,
      markedBy = "Prof. Sarah Jenkins (Class Teacher)",
      notes = "Full day attendance"
    )
    attendanceDao.insertRecord(attendance)
    val fetchedAttendance = attendanceDao.getAttendanceByClassAndDate("Class 10-A", "2026-08-14").first()
    assertEquals(1, fetchedAttendance.size)
    assertEquals(AttendanceStatus.FULL_DAY, fetchedAttendance[0].status)

    // Update status to ON_DUTY
    attendanceDao.updateStudentAttendanceStatus("std_101", AttendanceStatus.ON_DUTY, "Prof. Sarah Jenkins (Class Teacher)")
    val updatedRec = attendanceDao.getRecordById("att_1")
    assertEquals(AttendanceStatus.ON_DUTY, updatedRec?.status)
  }

  @Test
  fun `verify role switching in repository`() {
    val repository = SchoolRepository()
    repository.loginAsRole(UserRole.STUDENT)
    assertEquals(UserRole.STUDENT, repository.currentUser.value?.role)
    assertNotNull(repository.currentStudentProfile.value)

    repository.loginAsRole(UserRole.TEACHER)
    assertEquals(UserRole.TEACHER, repository.currentUser.value?.role)
    assertNotNull(repository.currentTeacherProfile.value)
    assertEquals("Class 10-A", repository.currentTeacherProfile.value?.classTeacherOf)
    assertTrue(repository.currentTeacherProfile.value?.isClassTeacher == true)

    repository.loginAsRole(UserRole.STAFF)
    assertEquals(UserRole.STAFF, repository.currentUser.value?.role)
    assertNotNull(repository.currentStaffProfile.value)

    repository.loginAsRole(UserRole.ADMIN)
    assertEquals(UserRole.ADMIN, repository.currentUser.value?.role)
    assertNotNull(repository.currentAdminProfile.value)
  }

  @Test
  fun `verify daily attendance statuses and class teacher updates`() {
    val repository = SchoolRepository()
    repository.loginAsRole(UserRole.TEACHER)

    // Verify 4 daily attendance statuses exist with correct weights & codes
    val statuses = AttendanceStatus.values()
    assertEquals(4, statuses.size)
    assertTrue(statuses.contains(AttendanceStatus.FULL_DAY))
    assertTrue(statuses.contains(AttendanceStatus.HALF_DAY))
    assertTrue(statuses.contains(AttendanceStatus.ON_DUTY))
    assertTrue(statuses.contains(AttendanceStatus.ABSENT))

    assertEquals("FD", AttendanceStatus.FULL_DAY.code)
    assertEquals("HD", AttendanceStatus.HALF_DAY.code)
    assertEquals("OD", AttendanceStatus.ON_DUTY.code)
    assertEquals("AB", AttendanceStatus.ABSENT.code)

    // Update attendance record for a student
    repository.updateAttendanceRecord("std_101", AttendanceStatus.ON_DUTY, "Prof. Sarah Jenkins (Class Teacher)")
    val rec = repository.attendanceRecords.value.first { it.studentId == "std_101" }
    assertEquals(AttendanceStatus.ON_DUTY, rec.status)
    assertEquals("Prof. Sarah Jenkins (Class Teacher)", rec.markedBy)

    // Mark all full day
    repository.markAllAttendance(AttendanceStatus.FULL_DAY, "Class 10-A")
    val records10A = repository.attendanceRecords.value.filter { it.className == "Class 10-A" }
    assertTrue(records10A.all { it.status == AttendanceStatus.FULL_DAY })
  }

  @Test
  fun `verify homework submission and notices`() {
    val repository = SchoolRepository()
    repository.loginAsRole(UserRole.STUDENT)

    val hw = repository.homeworks.value.first()
    repository.submitHomework(hw.id, "Submission note test")
    val updatedHw = repository.homeworks.value.first { it.id == hw.id }
    assertEquals(HomeworkStatus.SUBMITTED, updatedHw.status)

    repository.addNotice("Test Notice", "Content here", NoticeCategory.ACADEMIC, false)
    val notice = repository.notices.value.first()
    assertEquals("Test Notice", notice.title)
  }
}
