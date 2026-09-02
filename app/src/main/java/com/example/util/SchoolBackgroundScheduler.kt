package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.receiver.SchoolAlarmReceiver

/**
 * SchoolBackgroundScheduler
 *
 * Ensures continuous, autonomous background school notifications are dispatched
 * reliably by the Android OS AlarmManager, EVEN IF the application is closed,
 * killed from recent tasks, or running in low-power Doze mode.
 */
object SchoolBackgroundScheduler {

  private const val TAG = "SchoolBgScheduler"
  const val ACTION_SCHOOL_ALERT = "com.example.schoolapp.ACTION_SCHOOL_ALERT"
  private const val ALARM_REQUEST_CODE = 4401

  // Default interval between continuous autonomous background alerts (2 minutes)
  private const val DEFAULT_INTERVAL_SECONDS = 120L

  /**
   * Starts the continuous autonomous background alert cycle.
   * Dispatches the initial alert shortly after startup (45 seconds),
   * then chains subsequent alerts every 2 minutes.
   */
  fun startContinuousBackgroundAlerts(context: Context, initialDelaySeconds: Long = 45L) {
    scheduleNextBackgroundAlert(context, delaySeconds = initialDelaySeconds)
    Log.d(TAG, "Autonomous continuous background notifications scheduled (initial in ${initialDelaySeconds}s).")
  }

  /**
   * Schedules the next background alarm using Android's system AlarmManager.
   * Uses setExactAndAllowWhileIdle / setAndAllowWhileIdle to guarantee execution
   * even when the app is completely terminated or the device is idling in Doze mode.
   */
  fun scheduleNextBackgroundAlert(context: Context, delaySeconds: Long = DEFAULT_INTERVAL_SECONDS) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val triggerAtMillis = System.currentTimeMillis() + (delaySeconds * 1000L)

      val intent = Intent(context, SchoolAlarmReceiver::class.java).apply {
        action = ACTION_SCHOOL_ALERT
      }

      val pendingIntent = PendingIntent.getBroadcast(
        context,
        ALARM_REQUEST_CODE,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
          } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
          }
        } else {
          alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
      } else {
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
      }

      Log.d(TAG, "Queued next autonomous background school notification at +${delaySeconds}s ($triggerAtMillis)")
    } catch (e: Exception) {
      Log.e(TAG, "Error scheduling background school notification alarm: ${e.message}")
    }
  }

  /**
   * Cancels any scheduled background alarms.
   */
  fun cancelBackgroundAlerts(context: Context) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val intent = Intent(context, SchoolAlarmReceiver::class.java).apply {
        action = ACTION_SCHOOL_ALERT
      }
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        ALARM_REQUEST_CODE,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
      )
      if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Cancelled autonomous background school notification alarms.")
      }
    } catch (e: Exception) {
      Log.w(TAG, "Note cancelling background alarms: ${e.message}")
    }
  }
}
