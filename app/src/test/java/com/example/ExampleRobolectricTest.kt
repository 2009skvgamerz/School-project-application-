package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SchoolRepository
import com.example.model.AttendanceStatus
import com.example.model.HomeworkStatus
import com.example.model.NoticeCategory
import com.example.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("St. Joseph's School", appName)
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

    repository.loginAsRole(UserRole.STAFF)
    assertEquals(UserRole.STAFF, repository.currentUser.value?.role)
    assertNotNull(repository.currentStaffProfile.value)

    repository.loginAsRole(UserRole.ADMIN)
    assertEquals(UserRole.ADMIN, repository.currentUser.value?.role)
    assertNotNull(repository.currentAdminProfile.value)
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

