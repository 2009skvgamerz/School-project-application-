package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.AttendanceStatus

/**
 * Room Database Entity representing the 'attendance_records' table.
 * Stores daily roll-call attendance entries recorded by the designated Class Teacher.
 *
 * Supports status values: FULL_DAY, HALF_DAY, ON_DUTY, ABSENT.
 */
@Entity(
  tableName = "attendance_records",
  foreignKeys = [
    ForeignKey(
      entity = StudentEntity::class,
      parentColumns = ["id"],
      childColumns = ["student_id"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [
    Index(value = ["student_id"]),
    Index(value = ["class_name", "date"]),
    Index(value = ["date"])
  ]
)
data class AttendanceEntity(
  @PrimaryKey
  @ColumnInfo(name = "id")
  val id: String,

  @ColumnInfo(name = "student_id")
  val studentId: String,

  @ColumnInfo(name = "student_name")
  val studentName: String,

  @ColumnInfo(name = "roll_no")
  val rollNo: Int,

  @ColumnInfo(name = "class_name")
  val className: String,

  @ColumnInfo(name = "date")
  val date: String,

  @ColumnInfo(name = "status")
  val status: AttendanceStatus,

  @ColumnInfo(name = "marked_by")
  val markedBy: String = "Prof. Sarah Jenkins (Class Teacher)",

  @ColumnInfo(name = "notes")
  val notes: String = "",

  @ColumnInfo(name = "recorded_at")
  val recordedAt: Long = System.currentTimeMillis()
)
