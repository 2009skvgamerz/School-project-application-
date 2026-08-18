package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.model.NotificationType

object SystemNotificationHelper {

  const val CHANNEL_ID = "school_heads_up_channel"
  const val CHANNEL_NAME = "High-Priority School Alerts"
  const val CHANNEL_DESC = "Pop-up heads-up notifications for homework, attendance, emergency circulars, and exam dates."

  const val EXTRA_TARGET_ROUTE = "extra_target_route"

  /**
   * Initializes the high-priority notification channel required for Heads-Up Pop-Up banners.
   */
  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = CHANNEL_DESC
        enableLights(true)
        lightColor = Color.BLUE
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 350, 150, 350)
        setShowBadge(true)
        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
      }

      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  /**
   * Check if notification permission is granted (Android 13+).
   */
  fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
  }

  /**
   * Triggers an immediate system notification banner outside the app (Heads-Up Display).
   */
  fun showSystemNotification(
    context: Context,
    id: Int = (System.currentTimeMillis() % 100000).toInt(),
    title: String,
    message: String,
    type: NotificationType = NotificationType.ACADEMIC,
    actionRoute: String? = null,
    isUrgent: Boolean = true
  ) {
    createNotificationChannel(context)

    // Build Launch Intent with deep-link payload
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra(EXTRA_TARGET_ROUTE, actionRoute ?: "dashboard")
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      id,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val iconRes = when (type) {
      NotificationType.HOMEWORK -> android.R.drawable.ic_menu_edit
      NotificationType.ATTENDANCE -> android.R.drawable.checkbox_on_background
      NotificationType.NOTICE -> android.R.drawable.ic_dialog_alert
      NotificationType.EXAM -> android.R.drawable.ic_menu_agenda
      NotificationType.FEE -> android.R.drawable.ic_menu_save
      NotificationType.EVENT -> android.R.drawable.ic_menu_today
      else -> android.R.drawable.ic_dialog_info
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(iconRes)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText(message)
          .setBigContentTitle(title)
          .setSummaryText("St. Joseph's Higher Secondary School")
      )
      .setPriority(if (isUrgent) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_MESSAGE)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setColor(0xFF0F3875.toInt())

    // Add direct quick-action button in the notification pop-up
    if (actionRoute != null) {
      val actionTitle = when (actionRoute.lowercase()) {
        "homework" -> "View Homework"
        "attendance" -> "Check Attendance"
        "timetable" -> "View Schedule"
        "notices" -> "Read Notice"
        else -> "Open App"
      }
      builder.addAction(iconRes, actionTitle, pendingIntent)
    }

    try {
      val notificationManager = NotificationManagerCompat.from(context)
      if (hasNotificationPermission(context)) {
        notificationManager.notify(id, builder.build())
      }
    } catch (e: SecurityException) {
      // In case permission was revoked at runtime
    }
  }

  /**
   * Schedules a delayed pop-up notification. This allows the user to press the button,
   * minimize/exit the app, and observe the Heads-Up pop-up banner appear over the Android OS / Home Screen!
   */
  fun scheduleDelayedSystemNotification(
    context: Context,
    delaySeconds: Long = 5L,
    title: String,
    message: String,
    type: NotificationType = NotificationType.ACADEMIC,
    actionRoute: String? = null
  ) {
    Handler(Looper.getMainLooper()).postDelayed({
      showSystemNotification(
        context = context,
        title = title,
        message = message,
        type = type,
        actionRoute = actionRoute,
        isUrgent = true
      )
    }, delaySeconds * 1000L)
  }
}
