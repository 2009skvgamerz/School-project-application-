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
 * Main Room Database class for St. Joseph's School Management System.
 *
 * Persists tables:
 * - 'students' (StudentEntity)
 * - 'teachers' (TeacherEntity)
 * - 'attendance_records' (AttendanceEntity)
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
abstract class AppDatabase : RoomDatabase() {

  abstract fun studentDao(): StudentDao
  abstract fun teacherDao(): TeacherDao
  abstract fun attendanceDao(): AttendanceDao

  companion object {
    const val DATABASE_NAME = "st_josephs_school_app.db"

    @Volatile
    private var INSTANCE: AppDatabase? = null

    /**
     * Database initializer with singleton instance and pre-population callback.
     */
    fun getDatabase(
      context: Context,
      scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          DATABASE_NAME
        )
          .addCallback(AppDatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    /**
     * Database callback to seed initial Students, Teachers, and Attendance records upon creation.
     */
    private class AppDatabaseCallback(
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

    /**
     * Seeds initial records for Students, Teachers, and Daily Attendance into Room.
     */
    suspend fun populateInitialData(database: AppDatabase) {
      val studentDao = database.studentDao()
      val teacherDao = database.teacherDao()
      val attendanceDao = database.attendanceDao()

      // 1. Initial Student Entities across multiple classes
      val initialStudents = listOf(
        // Class 10-A
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
        StudentEntity("std_112", "SJ-2024-1012", "Noah Parker", "n.parker@stjosephs.edu", "Class 10", "A", 12, "David Parker", "+91 98450 11234", "B+", 90.0),
        // Class 10-B
        StudentEntity("std_201", "SJ-2024-2001", "Aaron Cooper", "a.cooper@stjosephs.edu", "Class 10", "B", 1, "Jason Cooper", "+91 98450 22001", "O+", 94.2),
        StudentEntity("std_202", "SJ-2024-2002", "Brianna Diaz", "b.diaz@stjosephs.edu", "Class 10", "B", 2, "Manuel Diaz", "+91 98450 22002", "A+", 91.8),
        StudentEntity("std_203", "SJ-2024-2003", "Chloe Edwards", "c.edwards@stjosephs.edu", "Class 10", "B", 3, "Victor Edwards", "+91 98450 22003", "B+", 95.0),
        StudentEntity("std_204", "SJ-2024-2004", "Dylan Flores", "d.flores@stjosephs.edu", "Class 10", "B", 4, "Oscar Flores", "+91 98450 22004", "AB+", 88.0),
        StudentEntity("std_205", "SJ-2024-2005", "Elena Garcia", "e.garcia@stjosephs.edu", "Class 10", "B", 5, "Rafael Garcia", "+91 98450 22005", "O-", 96.5),
        // Class 9-A
        StudentEntity("std_301", "SJ-2024-3001", "Adrian Hughes", "a.hughes@stjosephs.edu", "Class 9", "A", 1, "Paul Hughes", "+91 98450 33001", "A+", 95.0),
        StudentEntity("std_302", "SJ-2024-3002", "Brooke Jenkins", "b.jenkins@stjosephs.edu", "Class 9", "A", 2, "Timothy Jenkins", "+91 98450 33002", "B+", 93.5),
        StudentEntity("std_303", "SJ-2024-3003", "Caleb Kelly", "c.kelly@stjosephs.edu", "Class 9", "A", 3, "Steven Kelly", "+91 98450 33003", "O+", 97.0),
        StudentEntity("std_304", "SJ-2024-3004", "Daisy Lewis", "d.lewis@stjosephs.edu", "Class 9", "A", 4, "Frank Lewis", "+91 98450 33004", "A-", 90.0),
        // Class 11-Science
        StudentEntity("std_401", "SJ-2024-4001", "Alexander Scott", "a.scott@stjosephs.edu", "Class 11", "Science", 1, "Walter Scott", "+91 98450 44001", "O+", 98.0),
        StudentEntity("std_402", "SJ-2024-4002", "Benjamin Ward", "b.ward@stjosephs.edu", "Class 11", "Science", 2, "Kenneth Ward", "+91 98450 44002", "B+", 92.5),
        StudentEntity("std_403", "SJ-2024-4003", "Charlotte Young", "c.young@stjosephs.edu", "Class 11", "Science", 3, "Jonathan Young", "+91 98450 44003", "A+", 96.0)
      )
      studentDao.insertStudents(initialStudents)

      // 2. Initial Teacher Entities
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

      // 3. Initial Attendance Entities (Daily Roll Call across classes)
      val initialAttendance = listOf(
        // Class 10-A
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
        AttendanceEntity("att_12", "std_112", "Noah Parker", 12, "Class 10-A", "Today", AttendanceStatus.FULL_DAY, "Prof. Sarah Jenkins (Class Teacher)"),
        // Class 10-B
        AttendanceEntity("att_201", "std_201", "Aaron Cooper", 1, "Class 10-B", "Today", AttendanceStatus.FULL_DAY, "Mr. David Miller (Class Teacher)"),
        AttendanceEntity("att_202", "std_202", "Brianna Diaz", 2, "Class 10-B", "Today", AttendanceStatus.FULL_DAY, "Mr. David Miller (Class Teacher)"),
        AttendanceEntity("att_203", "std_203", "Chloe Edwards", 3, "Class 10-B", "Today", AttendanceStatus.ON_DUTY, "Mr. David Miller (Class Teacher)", "Debate Competition"),
        AttendanceEntity("att_204", "std_204", "Dylan Flores", 4, "Class 10-B", "Today", AttendanceStatus.ABSENT, "Mr. David Miller (Class Teacher)", "Fever"),
        AttendanceEntity("att_205", "std_205", "Elena Garcia", 5, "Class 10-B", "Today", AttendanceStatus.FULL_DAY, "Mr. David Miller (Class Teacher)"),
        // Class 9-A
        AttendanceEntity("att_301", "std_301", "Adrian Hughes", 1, "Class 9-A", "Today", AttendanceStatus.FULL_DAY, "Mrs. Clara Higgins (Class Teacher)"),
        AttendanceEntity("att_302", "std_302", "Brooke Jenkins", 2, "Class 9-A", "Today", AttendanceStatus.FULL_DAY, "Mrs. Clara Higgins (Class Teacher)"),
        AttendanceEntity("att_303", "std_303", "Caleb Kelly", 3, "Class 9-A", "Today", AttendanceStatus.HALF_DAY, "Mrs. Clara Higgins (Class Teacher)", "Afternoon appointment"),
        AttendanceEntity("att_304", "std_304", "Daisy Lewis", 4, "Class 9-A", "Today", AttendanceStatus.FULL_DAY, "Mrs. Clara Higgins (Class Teacher)"),
        // Class 11-Science
        AttendanceEntity("att_401", "std_401", "Alexander Scott", 1, "Class 11-Science", "Today", AttendanceStatus.FULL_DAY, "Dr. Rachel Green (Class Teacher)"),
        AttendanceEntity("att_402", "std_402", "Benjamin Ward", 2, "Class 11-Science", "Today", AttendanceStatus.ON_DUTY, "Dr. Rachel Green (Class Teacher)", "Robotics Expo Prep"),
        AttendanceEntity("att_403", "std_403", "Charlotte Young", 3, "Class 11-Science", "Today", AttendanceStatus.FULL_DAY, "Dr. Rachel Green (Class Teacher)")
      )
      attendanceDao.insertRecords(initialAttendance)
    }
  }
}
