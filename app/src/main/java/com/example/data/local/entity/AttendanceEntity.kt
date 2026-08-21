package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AttendanceStatus

@Entity(tableName = "attendance_records")
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentName: String,
    val rollNo: String,
    val className: String,
    val date: String,
    val status: AttendanceStatus,
    val remarks: String = "",
    val markedByTeacherId: String = ""
)
