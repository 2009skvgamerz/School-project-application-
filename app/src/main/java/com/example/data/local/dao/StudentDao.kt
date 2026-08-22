package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for 'students' table.
 */
@Dao
interface StudentDao {

  @Query("SELECT * FROM students ORDER BY roll_no ASC")
  fun getAllStudents(): Flow<List<StudentEntity>>

  @Query("SELECT * FROM students WHERE grade || '-' || section = :className OR grade = :className ORDER BY roll_no ASC")
  fun getStudentsByClass(className: String): Flow<List<StudentEntity>>

  @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
  suspend fun getStudentById(id: String): StudentEntity?

  @Query("SELECT * FROM students WHERE email = :email LIMIT 1")
  suspend fun getStudentByEmail(email: String): StudentEntity?

  @Query("SELECT * FROM students WHERE id = :query OR email = :query OR admission_no = :query LIMIT 1")
  suspend fun findStudentByIdentifier(query: String): StudentEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStudent(student: StudentEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStudents(students: List<StudentEntity>)

  @Update
  suspend fun updateStudent(student: StudentEntity)

  @Query("UPDATE students SET attendance_percentage = :newPercentage WHERE id = :studentId")
  suspend fun updateAttendancePercentage(studentId: String, newPercentage: Double)

  @Delete
  suspend fun deleteStudent(student: StudentEntity)

  @Query("DELETE FROM students WHERE id = :id")
  suspend fun deleteStudentById(id: String)

  @Query("SELECT COUNT(*) FROM students")
  suspend fun getStudentCount(): Int
}
