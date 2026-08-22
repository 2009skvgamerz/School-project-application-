package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity representing the 'students' table.
 * Stores student demographic, enrollment, academic, and guardian records.
 */
@Entity(tableName = "students")
data class StudentEntity(
  @PrimaryKey
  @ColumnInfo(name = "id")
  val id: String,

  @ColumnInfo(name = "admission_no")
  val admissionNo: String,

  @ColumnInfo(name = "full_name")
  val fullName: String,

  @ColumnInfo(name = "email")
  val email: String,

  @ColumnInfo(name = "grade")
  val grade: String,

  @ColumnInfo(name = "section")
  val section: String,

  @ColumnInfo(name = "roll_no")
  val rollNo: Int,

  @ColumnInfo(name = "parent_name")
  val parentName: String,

  @ColumnInfo(name = "parent_phone")
  val parentPhone: String,

  @ColumnInfo(name = "blood_group")
  val bloodGroup: String = "O+",

  @ColumnInfo(name = "attendance_percentage")
  val attendancePercentage: Double = 96.4,

  @ColumnInfo(name = "house_name")
  val houseName: String = "St. Francis House",

  @ColumnInfo(name = "bus_route")
  val busRoute: String = "Route #12 (Main Gate)",

  @ColumnInfo(name = "academic_year")
  val academicYear: String = "2026-2027",

  @ColumnInfo(name = "emergency_contact")
  val emergencyContact: String = "+91 98450 78912"
) {
  val className: String get() = "$grade-$section"
}
