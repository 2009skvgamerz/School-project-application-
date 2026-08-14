package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.AttendanceStatus

/**
 * Type converters for Room database to persist complex data types
 * like lists and enums.
 */
class Converters {

  @TypeConverter
  fun fromStringList(value: List<String>?): String {
    return value?.joinToString(separator = "|||") ?: ""
  }

  @TypeConverter
  fun toStringList(value: String?): List<String> {
    if (value.isNullOrBlank()) return emptyList()
    return value.split("|||").filter { it.isNotBlank() }
  }

  @TypeConverter
  fun fromAttendanceStatus(status: AttendanceStatus?): String {
    return status?.name ?: AttendanceStatus.FULL_DAY.name
  }

  @TypeConverter
  fun toAttendanceStatus(value: String?): AttendanceStatus {
    return try {
      if (value != null) AttendanceStatus.valueOf(value) else AttendanceStatus.FULL_DAY
    } catch (e: Exception) {
      AttendanceStatus.FULL_DAY
    }
  }
}
