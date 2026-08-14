package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.StudentDao
import com.example.data.local.dao.TeacherDao
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TeacherEntity
import com.example.model.AttendanceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room Database for St. Joseph's School Management System.
 * Persists tables:
 * - 'students'
 * - 'teachers'
 * - 'attendance_records'
 */
@Database(
  entities = [
    StudentEntity::class,
    TeacherEntity::class,
    AttendanceEntity::class
  ],
  version = 1,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SchoolDatabase : RoomDatabase() {

  abstract fun studentDao(): StudentDao
  abstract fun teacherDao(): TeacherDao
  abstract fun attendanceDao(): AttendanceDao

  companion object {
    @Volatile
    private var INSTANCE: SchoolDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): SchoolDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          SchoolDatabase::class.java,
          "st_josephs_school.db"
        )
          .fallbackToDestructiveMigration()
          .addCallback(SchoolDatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class SchoolDatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database)
          }
        }
      }
    }

    suspend fun populateInitialData(database: SchoolDatabase) {
      val studentDao = database.studentDao()
      val teacherDao = database.teacherDao()
      val attendanceDao = database.attendanceDao()

      // Initial Students
      val initialStudents = listOf(
        StudentEntity("std_101", "SJ-2024-1001", "Alex Johnson", "alex.j@stjosephs.edu", "Class 10", "A", 1, "Robert Johnson", "+91 98450 11223", "O+", 96.4),
        StudentEntity("std_102", "SJ-2024-1002", "Bella Collins", "b.collins@stjosephs.edu", "Class 10", "A", 2, "Arthur Collins", "+91 98450 11224", "A+", 92.0),
        StudentEntity("std_103", "SJ-2024-1003", "Christian Davies", "c.davies@stjosephs.edu", "Class 10", "A", 3, "Marcus Davies", "+91 98450 11225", "B+", 88.5),
        StudentEntity("std_104", "SJ-2024-1004", "Daniel Evans", "d.evans@stjosephs.edu", "Class 10", "A", 4, "George Evans", "+91 98450 11226", "AB+", 94.0),
        StudentEntity("std_105", "SJ-2024-1005", "Emma Foster", "e.foster@stjosephs.edu", "Class 10", "A", 5, "Henry Foster", "+91 98450 11227", "O-", 85.0),
        StudentEntity("std_106", "SJ-2024-1006", "Felix Gomez", "f.gomez@stjosephs.edu", "Class 10", "A", 6, "Carlos Gomez", "+91 98450 11228", "A-", 98.0),
        StudentEntity("std_107", "SJ-2024-1007", "Grace Howard", "g.howard@stjosephs.edu", "Class 10", "A", 7, "Edward Howard", "+91 98450 11229", "O+", 95.5),
        StudentEntity("std_108", "SJ-2024-1008", "Henry Irwin", "h.irwin@stjosephs.edu", "Class 10", "A", 8, "Thomas Irwin", "+91 98450 11230", "B-", 91.0),
        StudentEntity("std_109", "SJ-2024-1009", "Isabella Jackson", "i.jackson@stjosephs.edu", "Class 10", "A", 9, "Philip Jackson", "+91 98450 11231", "A+", 97.2),
        StudentEntity("std_110", "SJ-2024-1010", "Jacob Klein", "j.klein@stjosephs.edu", "Class 10", "A", 10, "Samuel Klein", "+91 98450 11232", "AB-", 89.0),
        StudentEntity("std_111", "SJ-2024-1011", "Lily Morris", "l.morris@stjosephs.edu", "Class 10", "A", 11, "Charles Morris", "+91 98450 11233", "O+", 93.4),
        StudentEntity("std_112", "SJ-2024-1012", "Noah Parker", "n.parker@stjosephs.edu", "Class 10", "A", 12, "David Parker", "+91 98450 11234", "B+", 90.0)
      )
      studentDao.insertStudents(initialStudents)

      // Initial Teachers
      val initialTeachers = listOf(
        TeacherEntity(
          id = "tch_201",
          employeeId = "EMP-0412",
          fullName = "Prof. Sarah Jenkins",
          email = "s.jenkins@stjosephs.edu",
          phone = "+91 98765 43210",
          department = "Physical & Chemical Sciences",
          assignedClasses = listOf("Class 10-A", "Class 10-B", "Class 9-A", "Class 11-Science"),
          subjectsTaught = listOf("Physics", "Science Lab", "General Science"),
          qualification = "M.Sc. Physics, B.Ed (Gold Medalist)",
          isClassTeacher = true,
          classTeacherOf = "Class 10-A"
        ),
        TeacherEntity(
          id = "tch_202",
          employeeId = "EMP-0388",
          fullName = "Mr. David Miller",
          email = "d.miller@stjosephs.edu",
          phone = "+91 98765 43211",
          department = "Mathematical Sciences",
          assignedClasses = listOf("Class 10-A", "Class 10-B", "Class 12-Science"),
          subjectsTaught = listOf("Mathematics", "Applied Calculus"),
          qualification = "M.Sc. Mathematics, B.Ed",
          isClassTeacher = true,
          classTeacherOf = "Class 10-B"
        ),
        TeacherEntity(
          id = "tch_203",
          employeeId = "EMP-0504",
          fullName = "Mrs. Clara Higgins",
          email = "c.higgins@stjosephs.edu",
          phone = "+91 98765 43212",
          department = "Humanities & Languages",
          assignedClasses = listOf("Class 9-A", "Class 10-A"),
          subjectsTaught = listOf("English Literature", "Creative Writing"),
          qualification = "M.A. English Literature",
          isClassTeacher = true,
          classTeacherOf = "Class 9-A"
        )
      )
      teacherDao.insertTeachers(initialTeachers)

      // Initial Attendance Records
      val initialAttendance = listOf(
        AttendanceEntity("att_1", "std_101", "Alex Johnson", 1, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
        AttendanceEntity("att_2", "std_102", "Bella Collins", 2, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
        AttendanceEntity("att_3", "std_103", "Christian Davies", 3, "Class 10-A", "Today", AttendanceStatus.HALF_DAY, "Prof. Sarah Jenkins (Class Teacher)", "Departed 12:30 PM (Medical appointment)"),
        AttendanceEntity("att_4", "std_104", "Daniel Evans", 4, "Class 10-A", "Today", AttendanceStatus.ON_DUTY, "Prof. Sarah Jenkins (Class Teacher)", "Inter-School Science Olympiad (OD Approved)"),
        AttendanceEntity("att_5", "std_105", "Emma Foster", 5, "Class 10-A", "Today", AttendanceStatus.ABSENT, "Prof. Sarah Jenkins (Class Teacher)", "Sick leave (Parent letter received)"),
        AttendanceEntity("att_6", "std_106", "Felix Gomez", 6, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
        AttendanceEntity("att_7", "std_107", "Grace Howard", 7, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
        AttendanceEntity("att_8", "std_108", "Henry Irwin", 8, "Class 10-A", "Today", AttendanceStatus.ON_DUTY, "Prof. Sarah Jenkins (Class Teacher)", "State Basketball Championship (OD Approved)"),
        AttendanceEntity("att_9", "std_109", "Isabella Jackson", 9, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
        AttendanceEntity("att_10", "std_110", "Jacob Klein", 10, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
        AttendanceEntity("att_11", "std_111", "Lily Morris", 11, "Class 10-A", "Today", AttendanceStatus.HALF_DAY, "Prof. Sarah Jenkins (Class Teacher)", "Morning Session Only (Family event)"),
        AttendanceEntity("att_12", "std_112", "Noah Parker", 12, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)")
      )
      attendanceDao.insertRecords(initialAttendance)
    }
  }
}
