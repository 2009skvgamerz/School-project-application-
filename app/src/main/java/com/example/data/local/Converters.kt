package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.AttendanceStatus

class Converters {
    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus): String {
        return status.name
    }

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus {
        return try {
            AttendanceStatus.valueOf(value)
        } catch (e: Exception) {
            AttendanceStatus.FULL_DAY
        }
    }
}
