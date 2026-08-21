package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE date = :date AND className = :className ORDER BY rollNo ASC")
    fun getAttendanceForClass(date: String, className: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(records: List<AttendanceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleAttendance(record: AttendanceEntity)

    @Query("DELETE FROM attendance_records WHERE date = :date AND className = :className")
    suspend fun deleteForDateAndClass(date: String, className: String)
}
