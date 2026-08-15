package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.SchoolRepository
import com.example.data.local.AppDatabase
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

  private lateinit var appDb: AppDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    appDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun closeDb() {
    appDb.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("St. Joseph's School", appName)
  }

  @Test
  fun `verify AppDatabase entities and daos for Student, Teacher, and Attendance`() = runBlocking {
    val studentDao = appDb.studentDao()
    val teacherDao = appDb.teacherDao()
    val attendanceDao = appDb.attendanceDao()

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
  fun `verify AppDatabase pre-population and migration configuration`() = runBlocking {
    // Populate initial data in AppDatabase
    AppDatabase.populateInitialData(appDb)

    val studentCount = appDb.studentDao().getStudentCount()
    val teacherCount = appDb.teacherDao().getTeacherCount()
    val attendanceRecords = appDb.attendanceDao().getAllAttendanceRecords().first()

    assertTrue("Students should be seeded", studentCount >= 12)
    assertTrue("Teachers should be seeded", teacherCount >= 3)
    assertTrue("Attendance records should be seeded", attendanceRecords.isNotEmpty())

    assertNotNull(AppDatabase.MIGRATION_1_2)
    assertEquals(1, AppDatabase.MIGRATION_1_2.startVersion)
    assertEquals(2, AppDatabase.MIGRATION_1_2.endVersion)
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

  @Test
  fun `verify AuthenticationViewModel validates credentials against Room database`() = runBlocking {
    // Populate DB
    AppDatabase.populateInitialData(appDb)

    val authViewModel = com.example.viewmodel.AuthenticationViewModel(
      database = appDb
    )

    // 1. Authenticate with Student admission number
    val studentAuthSuccess = authViewModel.authenticateUser(identifier = "SJ-2024-1001", secret = "password123")
    assertTrue(studentAuthSuccess)
    assertEquals(UserRole.STUDENT, authViewModel.currentRole.value)
    assertEquals("Alex Johnson", authViewModel.currentUser.value?.fullName)
    assertEquals("SJ-2024-1001", authViewModel.studentProfile.value?.admissionNo)
    assertTrue(authViewModel.authState.value is com.example.viewmodel.AuthState.Authenticated)

    // 2. Authenticate with Teacher Employee ID
    val teacherAuthSuccess = authViewModel.authenticateUser(identifier = "EMP-0412", secret = "pass123")
    assertTrue(teacherAuthSuccess)
    assertEquals(UserRole.TEACHER, authViewModel.currentRole.value)
    assertEquals("Prof. Sarah Jenkins", authViewModel.currentUser.value?.fullName)
    assertEquals("Class 10-A", authViewModel.teacherProfile.value?.classTeacherOf)
    assertTrue(authViewModel.teacherProfile.value?.isClassTeacher == true)

    // 3. Authenticate with Administrator identifier
    val adminAuthSuccess = authViewModel.authenticateUser(identifier = "principal@stjosephs.edu", secret = "admin123")
    assertTrue(adminAuthSuccess)
    assertEquals(UserRole.ADMIN, authViewModel.currentRole.value)
    assertNotNull(authViewModel.adminProfile.value)

    // 4. Logout test
    authViewModel.logout()
    assertTrue(authViewModel.authState.value is com.example.viewmodel.AuthState.Unauthenticated)
    assertEquals(null, authViewModel.currentUser.value)
  }

  @Test
  fun `verify Teacher Attendance with daily status options integrates with Room database`() = runBlocking {
    // Populate DB
    AppDatabase.populateInitialData(appDb)

    val schoolViewModel = com.example.viewmodel.SchoolViewModel(
      database = appDb
    )

    // Verify initial records in Class 10-A
    val initialRecords = appDb.attendanceDao().getAttendanceByClassAndDate("Class 10-A", "Today").first()
    assertTrue(initialRecords.isNotEmpty())
    val firstStudent = initialRecords.first()

    // 1. Teacher marks student as Half-Day (HD) with a remark note
    schoolViewModel.updateStudentAttendanceStatus(
      studentId = firstStudent.studentId,
      status = AttendanceStatus.HALF_DAY,
      notes = "Doctor appointment after lunch",
      markedBy = "Prof. Sarah Jenkins (Class Teacher)"
    )

    // Allow coroutine to persist to Room
    val updatedRecord = appDb.attendanceDao().getAttendanceForStudent(firstStudent.studentId).first().first()
    assertEquals(AttendanceStatus.HALF_DAY, updatedRecord.status)
    assertEquals("Doctor appointment after lunch", updatedRecord.notes)
    assertEquals("Prof. Sarah Jenkins (Class Teacher)", updatedRecord.markedBy)

    // 2. Teacher marks student as On-Duty (OD) for Sports Olympiad
    schoolViewModel.updateStudentAttendanceStatus(
      studentId = firstStudent.studentId,
      status = AttendanceStatus.ON_DUTY,
      notes = "State Athletics Championship",
      markedBy = "Prof. Sarah Jenkins (Class Teacher)"
    )
    val odRecord = appDb.attendanceDao().getAttendanceForStudent(firstStudent.studentId).first().first()
    assertEquals(AttendanceStatus.ON_DUTY, odRecord.status)
    assertEquals("State Athletics Championship", odRecord.notes)

    // 3. Teacher marks student as Absent (AB)
    schoolViewModel.updateStudentAttendanceStatus(
      studentId = firstStudent.studentId,
      status = AttendanceStatus.ABSENT,
      notes = "Uninformed absence",
      markedBy = "Prof. Sarah Jenkins (Class Teacher)"
    )
    val abRecord = appDb.attendanceDao().getAttendanceForStudent(firstStudent.studentId).first().first()
    assertEquals(AttendanceStatus.ABSENT, abRecord.status)
    assertEquals("Uninformed absence", abRecord.notes)

    // 4. Mark All Full-day in Class 10-A
    schoolViewModel.markAllFullDay(
      className = "Class 10-A",
      markedBy = "Prof. Sarah Jenkins (Class Teacher)"
    )
    val allFullDayRecords = appDb.attendanceDao().getAttendanceByClassAndDate("Class 10-A", "Today").first()
    assertTrue(allFullDayRecords.all { it.status == AttendanceStatus.FULL_DAY })
  }
}
