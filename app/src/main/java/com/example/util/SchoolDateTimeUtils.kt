package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility for formatting real-time school dates and timestamps dynamically.
 * Eliminates static hardcoded strings so notices, homework, announcements,
 * and circulars reflect actual live device time.
 */
object SchoolDateTimeUtils {

  private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
  private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
  private val fullDateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

  /**
   * Returns a dynamic notice date string based on current time (e.g., "Today, 07:35 PM").
   */
  fun getCurrentNoticeDateString(): String {
    val currentTime = timeFormat.format(Date())
    return "Today, $currentTime"
  }

  /**
   * Formats a timestamp into a human-readable notice date (e.g., "Today, 07:35 PM", "Yesterday, 04:10 PM", or "12 Sep 2026, 07:35 PM").
   */
  fun formatNoticeDate(timestamp: Long): String {
    if (timestamp <= 0L) return getCurrentNoticeDateString()

    val now = Calendar.getInstance()
    val eventCal = Calendar.getInstance().apply { timeInMillis = timestamp }

    val isSameDay = now.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == eventCal.get(Calendar.DAY_OF_YEAR)

    if (isSameDay) {
      return "Today, ${timeFormat.format(Date(timestamp))}"
    }

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
        yesterday.get(Calendar.DAY_OF_YEAR) == eventCal.get(Calendar.DAY_OF_YEAR)

    if (isYesterday) {
      return "Yesterday, ${timeFormat.format(Date(timestamp))}"
    }

    return fullDateTimeFormat.format(Date(timestamp))
  }

  /**
   * Returns formatted date for announcements (e.g., "12 Sep 2026").
   */
  fun getCurrentAnnouncementDateString(): String {
    return dateFormat.format(Date())
  }

  /**
   * Formats a timestamp into an announcement date (e.g., "12 Sep 2026").
   */
  fun formatAnnouncementDate(timestamp: Long): String {
    if (timestamp <= 0L) return getCurrentAnnouncementDateString()
    return dateFormat.format(Date(timestamp))
  }

  /**
   * Returns relative time ago string (e.g., "Just now", "5m ago", "2h ago", "Yesterday", "3d ago").
   */
  fun formatTimeAgo(timestamp: Long): String {
    if (timestamp <= 0L) return "Just now"

    val diffMillis = System.currentTimeMillis() - timestamp
    if (diffMillis < 45_000L) {
      return "Just now"
    }

    val diffMinutes = diffMillis / (60 * 1000L)
    if (diffMinutes < 60) {
      return "${diffMinutes}m ago"
    }

    val diffHours = diffMinutes / 60
    if (diffHours < 24) {
      return "${diffHours}h ago"
    }

    val diffDays = diffHours / 24
    if (diffDays == 1L) {
      return "Yesterday"
    }
    if (diffDays < 7) {
      return "${diffDays}d ago"
    }

    return dateFormat.format(Date(timestamp))
  }

  /**
   * Returns current formatted time (e.g., "07:35 PM").
   */
  fun getCurrentTime(): String {
    return timeFormat.format(Date())
  }

  /**
   * Returns default homework due date (Tomorrow, 09:00 AM).
   */
  fun getDefaultHomeworkDueDate(): String {
    return "Tomorrow, 09:00 AM"
  }
}
