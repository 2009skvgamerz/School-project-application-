package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.TeacherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers WHERE id = :id")
    suspend fun getTeacherById(id: String): TeacherEntity?

    @Query("SELECT * FROM teachers WHERE homeroomClass = :homeroomClass LIMIT 1")
    suspend fun getTeacherForClass(homeroomClass: String): TeacherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<TeacherEntity>)
}
