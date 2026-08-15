package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.TeacherEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for 'teachers' table.
 */
@Dao
interface TeacherDao {

  @Query("SELECT * FROM teachers ORDER BY full_name ASC")
  fun getAllTeachers(): Flow<List<TeacherEntity>>

  @Query("SELECT * FROM teachers WHERE id = :id LIMIT 1")
  suspend fun getTeacherById(id: String): TeacherEntity?

  @Query("SELECT * FROM teachers WHERE email = :email LIMIT 1")
  suspend fun getTeacherByEmail(email: String): TeacherEntity?

  @Query("SELECT * FROM teachers WHERE id = :query OR email = :query OR employee_id = :query LIMIT 1")
  suspend fun findTeacherByIdentifier(query: String): TeacherEntity?

  @Query("SELECT * FROM teachers WHERE class_teacher_of = :className AND is_class_teacher = 1 LIMIT 1")
  suspend fun getClassTeacherForClass(className: String): TeacherEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTeacher(teacher: TeacherEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTeachers(teachers: List<TeacherEntity>)

  @Update
  suspend fun updateTeacher(teacher: TeacherEntity)

  @Delete
  suspend fun deleteTeacher(teacher: TeacherEntity)

  @Query("SELECT COUNT(*) FROM teachers")
  suspend fun getTeacherCount(): Int
}
