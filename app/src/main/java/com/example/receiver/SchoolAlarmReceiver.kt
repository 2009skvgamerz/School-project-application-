package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.model.NotificationType
import com.example.util.SchoolBackgroundScheduler
import com.example.util.SystemNotificationHelper

/**
 * SchoolAlarmReceiver
 *
 * Broadcast receiver triggered by AlarmManager even when the app is completely
 * closed or killed. Dispatches realistic autonomous school notifications and
 * chains the next background alert to run continuously without interruption.
 */
class SchoolAlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    Log.d(TAG, "SchoolAlarmReceiver triggered in background (App closed/killed state).")

    try {
      // 1. Pick and show the next scheduled autonomous school alert
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      val currentIndex = prefs.getInt(KEY_ALERT_INDEX, 0)
      val alert = AUTONOMOUS_ALERTS[currentIndex % AUTONOMOUS_ALERTS.size]

      // Increment index for next time
      prefs.edit().putInt(KEY_ALERT_INDEX, currentIndex + 1).apply()

      // 2. Dispatch the system notification banner (Heads-Up + Room DB sync)
      SystemNotificationHelper.showSystemNotification(
        context = context,
        title = alert.title,
        message = alert.message,
        type = alert.type,
        actionRoute = alert.route,
        isUrgent = alert.isUrgent
      )

      Log.d(TAG, "Autonomous background notification posted: ${alert.title}")
    } catch (e: Exception) {
      Log.e(TAG, "Error posting autonomous background alert: ${e.message}", e)
    } finally {
      // 3. Chain the next background alarm (continuous autonomous delivery every 2 minutes)
      try {
        SchoolBackgroundScheduler.scheduleNextBackgroundAlert(context, delaySeconds = 120L)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to re-arm next background alert: ${e.message}")
      }
    }
  }

  data class AutonomousSchoolAlert(
    val title: String,
    val message: String,
    val type: NotificationType,
    val route: String,
    val isUrgent: Boolean = false
  )

  companion object {
    private const val TAG = "SchoolAlarmReceiver"
    private const val PREFS_NAME = "autonomous_school_alerts_prefs"
    private const val KEY_ALERT_INDEX = "current_alert_index"

    private val AUTONOMOUS_ALERTS = listOf(
      AutonomousSchoolAlert(
        title = "🚌 School Bus #04 Approaching",
        message = "Live Bus GPS: 500m from your stop (South Gate). Estimated arrival in 3 mins.",
        type = NotificationType.NOTICE,
        route = "bus",
        isUrgent = true
      ),
      AutonomousSchoolAlert(
        title = "📝 New Homework: Physics (Class 12-A)",
        message = "Electromagnetic induction problems assigned by Prof. Sarah Jenkins. Due tomorrow at 9:00 AM.",
        type = NotificationType.HOMEWORK,
        route = "homework",
        isUrgent = false
      ),
      AutonomousSchoolAlert(
        title = "🔔 Period Alert: Computer Lab 2",
        message = "Period 3 Advanced Python & AI starting in 10 mins. Science & Technology Wing, Room 204.",
        type = NotificationType.ACADEMIC,
        route = "timetable",
        isUrgent = false
      ),
      AutonomousSchoolAlert(
        title = "✅ Daily Attendance Recorded",
        message = "Attendance marked: Present for Class 12-A morning roll call (Roll #14).",
        type = NotificationType.ATTENDANCE,
        route = "attendance",
        isUrgent = false
      ),
      AutonomousSchoolAlert(
        title = "📢 Official Circular: Science & AI Expo 2026",
        message = "Annual Inter-School Robotics & AI prototype registration closes this Friday at 4:00 PM.",
        type = NotificationType.NOTICE,
        route = "announcements",
        isUrgent = true
      ),
      AutonomousSchoolAlert(
        title = "📅 Upcoming Exam: Mathematics Mid-Term",
        message = "Differential Calculus & Vectors revision session tomorrow at 8:30 AM in Main Hall.",
        type = NotificationType.EXAM,
        route = "exams",
        isUrgent = false
      ),
      AutonomousSchoolAlert(
        title = "🏆 Campus Sports Day Trials",
        message = "Track & Field selections begin at 3:30 PM on the Athletics Ground. Bring sports kit.",
        type = NotificationType.EVENT,
        route = "calendar",
        isUrgent = false
      ),
      AutonomousSchoolAlert(
        title = "💳 Tuition Fee Clearance Update",
        message = "Term 2 tuition fee receipt #STJ-2026-881 verified. Zero outstanding dues.",
        type = NotificationType.FEE,
        route = "fees",
        isUrgent = false
      )
    )
  }
}
