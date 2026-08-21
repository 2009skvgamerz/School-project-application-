package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val rollNo: String,
    val grade: String,
    val section: String,
    val gender: String,
    val attendanceRate: Float,
    val parentContact: String,
    val house: String = "St. Patrick"
)
