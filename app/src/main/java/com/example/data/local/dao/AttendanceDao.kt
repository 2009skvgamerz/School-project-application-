package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.AttendanceEntity
import com.example.model.AttendanceStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for 'attendance_records' table.
 */
@Dao
interface AttendanceDao {

  @Query("SELECT * FROM attendance_records ORDER BY roll_no ASC")
  fun getAllAttendanceRecords(): Flow<List<AttendanceEntity>>

  @Query("SELECT * FROM attendance_records WHERE class_name = :className AND date = :date ORDER BY roll_no ASC")
  fun getAttendanceByClassAndDate(className: String, date: String): Flow<List<AttendanceEntity>>

  @Query("SELECT * FROM attendance_records WHERE student_id = :studentId ORDER BY recorded_at DESC")
  fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceEntity>>

  @Query("SELECT * FROM attendance_records WHERE id = :id LIMIT 1")
  suspend fun getRecordById(id: String): AttendanceEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecord(record: AttendanceEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecords(records: List<AttendanceEntity>)

  @Update
  suspend fun updateRecord(record: AttendanceEntity)

  @Query("UPDATE attendance_records SET status = :status, marked_by = :markedBy, recorded_at = :timestamp WHERE student_id = :studentId")
  suspend fun updateStudentAttendanceStatus(
    studentId: String,
    status: AttendanceStatus,
    markedBy: String,
    timestamp: Long = System.currentTimeMillis()
  )

  @Query("UPDATE attendance_records SET status = :status, notes = :notes, marked_by = :markedBy, recorded_at = :timestamp WHERE student_id = :studentId")
  suspend fun updateStudentAttendanceStatusWithNotes(
    studentId: String,
    status: AttendanceStatus,
    notes: String,
    markedBy: String,
    timestamp: Long = System.currentTimeMillis()
  )

  @Query("UPDATE attendance_records SET status = :status, marked_by = :markedBy, recorded_at = :timestamp WHERE class_name = :className")
  suspend fun markAllClassAttendance(
    className: String,
    status: AttendanceStatus,
    markedBy: String,
    timestamp: Long = System.currentTimeMillis()
  )

  @Query("DELETE FROM attendance_records WHERE id = :id")
  suspend fun deleteRecordById(id: String)

  @Query("DELETE FROM attendance_records WHERE class_name = :className AND date = :date")
  suspend fun deleteRecordsByClassAndDate(className: String, date: String)

  @Query("SELECT COUNT(*) FROM attendance_records")
  suspend fun getAttendanceCount(): Int
}
