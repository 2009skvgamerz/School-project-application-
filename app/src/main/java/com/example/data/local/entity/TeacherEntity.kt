package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity representing the 'teachers' table.
 * Stores faculty credentials, department, assigned classes, subject mappings,
 * and homeroom Class Teacher designations.
 */
@Entity(tableName = "teachers")
data class TeacherEntity(
  @PrimaryKey
  @ColumnInfo(name = "id")
  val id: String,

  @ColumnInfo(name = "employee_id")
  val employeeId: String,

  @ColumnInfo(name = "full_name")
  val fullName: String,

  @ColumnInfo(name = "email")
  val email: String,

  @ColumnInfo(name = "phone")
  val phone: String = "+91 98765 43210",

  @ColumnInfo(name = "department")
  val department: String,

  @ColumnInfo(name = "assigned_classes")
  val assignedClasses: List<String>,

  @ColumnInfo(name = "subjects_taught")
  val subjectsTaught: List<String>,

  @ColumnInfo(name = "qualification")
  val qualification: String,

  @ColumnInfo(name = "is_class_teacher")
  val isClassTeacher: Boolean = true,

  @ColumnInfo(name = "class_teacher_of")
  val classTeacherOf: String? = "Class 10-A",

  @ColumnInfo(name = "room_no")
  val roomNo: String = "Staff Room 2B",

  @ColumnInfo(name = "joining_date")
  val joiningDate: String = "15 July 2018"
)
