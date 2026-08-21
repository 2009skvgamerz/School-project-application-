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

@Database(
    entities = [StudentEntity::class, TeacherEntity::class, AttendanceEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun teacherDao(): TeacherDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "st_josephs_school_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
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

        suspend fun populateInitialData(database: AppDatabase) {
            val studentDao = database.studentDao()
            val teacherDao = database.teacherDao()
            val attendanceDao = database.attendanceDao()

            val students = listOf(
                StudentEntity("std_01", "Alex Johnson", "01", "10", "A", "Male", 94.5f, "+1 555-0101", "St. Patrick"),
                StudentEntity("std_02", "Bella Martinez", "02", "10", "A", "Female", 88.0f, "+1 555-0102", "St. George"),
                StudentEntity("std_03", "Christopher Lee", "03", "10", "A", "Male", 91.2f, "+1 555-0103", "St. David"),
                StudentEntity("std_04", "Diana Prince", "04", "10", "A", "Female", 72.5f, "+1 555-0104", "St. Andrew"),
                StudentEntity("std_05", "Ethan Hunt", "05", "10", "A", "Male", 96.0f, "+1 555-0105", "St. Patrick"),
                StudentEntity("std_06", "Fiona Gallagher", "06", "10", "A", "Female", 83.4f, "+1 555-0106", "St. George"),
                StudentEntity("std_07", "Gabriel Silva", "07", "10", "A", "Male", 78.5f, "+1 555-0107", "St. David"),
                StudentEntity("std_08", "Hannah Abbott", "08", "10", "A", "Female", 92.1f, "+1 555-0108", "St. Andrew"),
                StudentEntity("std_09", "Ian Malcolm", "09", "10", "B", "Male", 85.0f, "+1 555-0109", "St. Patrick"),
                StudentEntity("std_10", "Julia Roberts", "10", "10", "B", "Female", 90.0f, "+1 555-0110", "St. George")
            )
            studentDao.insertStudents(students)

            val teachers = listOf(
                TeacherEntity("tch_01", "Prof. Sarah Jenkins", "s.jenkins@stjosephs.edu", "Physics & Science", "10-A", "+1 555-0201"),
                TeacherEntity("tch_02", "Mr. David Miller", "d.miller@stjosephs.edu", "Mathematics", "10-B", "+1 555-0202"),
                TeacherEntity("tch_03", "Dr. Emily Watson", "e.watson@stjosephs.edu", "English Literature", "11-A", "+1 555-0203"),
                TeacherEntity("tch_04", "Mr. Carlos Gomez", "c.gomez@stjosephs.edu", "Physical Education", "12-A", "+1 555-0204")
            )
            teacherDao.insertTeachers(teachers)

            val sampleAttendance = listOf(
                AttendanceEntity("att_01", "std_01", "Alex Johnson", "01", "10-A", "2026-08-21", AttendanceStatus.FULL_DAY, "Present in homeroom", "tch_01"),
                AttendanceEntity("att_02", "std_02", "Bella Martinez", "02", "10-A", "2026-08-21", AttendanceStatus.FULL_DAY, "", "tch_01"),
                AttendanceEntity("att_03", "std_03", "Christopher Lee", "03", "10-A", "2026-08-21", AttendanceStatus.ON_DUTY, "District Science Fair Delegate", "tch_01"),
                AttendanceEntity("att_04", "std_04", "Diana Prince", "04", "10-A", "2026-08-21", AttendanceStatus.ABSENT, "Medical sick leave approved", "tch_01"),
                AttendanceEntity("att_05", "std_05", "Ethan Hunt", "05", "10-A", "2026-08-21", AttendanceStatus.HALF_DAY, "Afternoon dental appointment", "tch_01"),
                AttendanceEntity("att_06", "std_06", "Fiona Gallagher", "06", "10-A", "2026-08-21", AttendanceStatus.FULL_DAY, "", "tch_01"),
                AttendanceEntity("att_07", "std_07", "Gabriel Silva", "07", "10-A", "2026-08-21", AttendanceStatus.FULL_DAY, "", "tch_01"),
                AttendanceEntity("att_08", "std_08", "Hannah Abbott", "08", "10-A", "2026-08-21", AttendanceStatus.FULL_DAY, "", "tch_01")
            )
            attendanceDao.insertAttendance(sampleAttendance)
        }
    }
}
