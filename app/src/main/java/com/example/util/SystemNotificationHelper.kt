package com.example.util

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.local.entity.NotificationEntity
import com.example.model.NotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * System Notification Manager handling Heads-Up Pop-Up alerts, category-specific
 * Notification Channels, deep linking into specific announcements/events,
 * and offline Room database synchronization.
 */
object SystemNotificationHelper {

  // Notification Channel Constants
  const val CHANNEL_EMERGENCY = "channel_emergency_notices"
  const val CHANNEL_EMERGENCY_NAME = "Emergency Notices & Circulars"
  const val CHANNEL_EMERGENCY_DESC = "Critical safety alerts, campus closures, weather emergencies, and urgent headmaster circulars."

  const val CHANNEL_ACADEMIC = "channel_academic_updates"
  const val CHANNEL_ACADEMIC_NAME = "Academic Updates & Homework"
  const val CHANNEL_ACADEMIC_DESC = "Daily homework assignments, attendance notices, exam schedules, and grade reports."

  const val CHANNEL_EVENTS = "channel_school_events"
  const val CHANNEL_EVENTS_NAME = "School Events & Calendar"
  const val CHANNEL_EVENTS_DESC = "Sports days, science exhibitions, cultural galas, parent-teacher meetings, and holidays."

  const val CHANNEL_GENERAL = "school_heads_up_channel"
  const val CHANNEL_GENERAL_NAME = "General School Bulletins"
  const val CHANNEL_GENERAL_DESC = "General announcements, bus transport alerts, and everyday campus news."

  // Intent Extra Keys for Deep-Linking
  const val EXTRA_TARGET_ROUTE = "extra_target_route"
  const val EXTRA_TARGET_ID = "extra_target_id"

  /**
   * Initializes all distinct Notification Channels on Android 8.0 (API 26) and higher
   * so users can configure granular notification preferences for each category.
   */
  fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      // 1. Emergency Notices Channel (High Importance, RED light, Urgent Vibration, Public Lockscreen)
      val emergencyChannel = NotificationChannel(
        CHANNEL_EMERGENCY,
        CHANNEL_EMERGENCY_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = CHANNEL_EMERGENCY_DESC
        enableLights(true)
        lightColor = Color.RED
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 600)
        setShowBadge(true)
        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          setAllowBubbles(true)
        }
      }

      // 2. Academic Updates Channel (High Importance, Blue Light, Distinct Vibration)
      val academicChannel = NotificationChannel(
        CHANNEL_ACADEMIC,
        CHANNEL_ACADEMIC_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = CHANNEL_ACADEMIC_DESC
        enableLights(true)
        lightColor = Color.BLUE
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 300, 150, 300)
        setShowBadge(true)
        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
      }

      // 3. Events Channel (Default Importance, Green Light)
      val eventsChannel = NotificationChannel(
        CHANNEL_EVENTS,
        CHANNEL_EVENTS_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = CHANNEL_EVENTS_DESC
        enableLights(true)
        lightColor = Color.GREEN
        enableVibration(true)
        setShowBadge(true)
        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
      }

      // 4. General Bulletins Channel
      val generalChannel = NotificationChannel(
        CHANNEL_GENERAL,
        CHANNEL_GENERAL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = CHANNEL_GENERAL_DESC
        enableLights(true)
        lightColor = Color.CYAN
        enableVibration(true)
        setShowBadge(true)
      }

      notificationManager.createNotificationChannels(
        listOf(emergencyChannel, academicChannel, eventsChannel, generalChannel)
      )
    }
  }

  /**
   * Compatibility alias for single channel creation.
   */
  fun createNotificationChannel(context: Context) {
    createNotificationChannels(context)
  }

  /**
   * Resolves the appropriate Notification Channel ID based on category and urgency.
   */
  fun resolveChannelId(type: NotificationType, isUrgent: Boolean): String {
    return when {
      isUrgent || type == NotificationType.NOTICE -> CHANNEL_EMERGENCY
      type == NotificationType.EVENT -> CHANNEL_EVENTS
      type == NotificationType.HOMEWORK || type == NotificationType.ATTENDANCE || type == NotificationType.EXAM -> CHANNEL_ACADEMIC
      else -> CHANNEL_GENERAL
    }
  }

  /**
   * Checks if notification permission is granted (Android 13+).
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
   * Triggers an immediate system notification banner outside the app (Heads-Up Display)
   * with deep-link intent handling and Room database history persistence.
   */
  fun showSystemNotification(
    context: Context,
    id: Int = (System.currentTimeMillis() % 100000).toInt(),
    title: String,
    message: String,
    type: NotificationType = NotificationType.ACADEMIC,
    actionRoute: String? = null,
    targetId: String? = null,
    isUrgent: Boolean = true
  ) {
    createNotificationChannels(context)

    val channelId = resolveChannelId(type, isUrgent)

    // Build Launch Intent with deep-link payload for direct route & target item
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
      putExtra(EXTRA_TARGET_ROUTE, actionRoute ?: "dashboard")
      targetId?.let { putExtra(EXTRA_TARGET_ID, it) }
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

    val builder = NotificationCompat.Builder(context, channelId)
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
      .setColor(if (isUrgent) 0xFFDC2626.toInt() else 0xFF0F3875.toInt())

    // Add contextual action button in notification banner
    val actionTitle = when (actionRoute?.lowercase()) {
      "announcements", "broadcast" -> "Read Announcement"
      "calendar", "events" -> "View Event"
      "homework" -> "View Homework"
      "attendance" -> "Check Attendance"
      "timetable" -> "View Schedule"
      "notices" -> "Read Notice"
      else -> "Open School App"
    }
    builder.addAction(iconRes, actionTitle, pendingIntent)

    try {
      val notificationManager = NotificationManagerCompat.from(context)
      if (hasNotificationPermission(context)) {
        notificationManager.notify(id, builder.build())
      }
    } catch (e: SecurityException) {
      // Gracefully handle runtime permission denial
    }

    // Persist to local Room database for offline notification history
    persistNotificationToRoom(
      context = context,
      id = "notif_${id}_${System.currentTimeMillis()}",
      title = title,
      message = message,
      type = type,
      actionRoute = actionRoute,
      targetId = targetId,
      isUrgent = isUrgent,
      channelId = channelId
    )
  }

  /**
   * Persists a received notification into the local Room database asynchronously.
   */
  fun persistNotificationToRoom(
    context: Context,
    id: String,
    title: String,
    message: String,
    type: NotificationType,
    actionRoute: String? = null,
    targetId: String? = null,
    isUrgent: Boolean = false,
    channelId: String? = null
  ) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val db = AppDatabase.getDatabase(context)
        val entity = NotificationEntity(
          id = id,
          title = title,
          message = message,
          timeAgo = "Just now",
          timestamp = System.currentTimeMillis(),
          type = type,
          isRead = false,
          actionRoute = actionRoute,
          targetId = targetId,
          isUrgent = isUrgent,
          channelId = channelId ?: resolveChannelId(type, isUrgent)
        )
        db.notificationDao().insertNotification(entity)
      } catch (e: Exception) {
        android.util.Log.w("SystemNotificationHelper", "Room notification insert note: ${e.message}")
      }
    }
  }

  /**
   * Schedules a delayed pop-up notification for testing background behavior.
   */
  fun scheduleDelayedSystemNotification(
    context: Context,
    delaySeconds: Long = 5L,
    title: String,
    message: String,
    type: NotificationType = NotificationType.ACADEMIC,
    actionRoute: String? = null,
    targetId: String? = null
  ) {
    Handler(Looper.getMainLooper()).postDelayed({
      showSystemNotification(
        context = context,
        title = title,
        message = message,
        type = type,
        actionRoute = actionRoute,
        targetId = targetId,
        isUrgent = true
      )
    }, delaySeconds * 1000L)
  }

  /**
   * Opens Android System Notification Settings for the app or a specific channel
   * so users can customize sound, vibration, and banner pop-up preferences.
   */
  fun openNotificationSettings(context: Context, channelId: String? = null) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channelId != null) {
      Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
      }
    } else {
      Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = android.net.Uri.fromParts("package", context.packageName, null)
      }
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
      context.startActivity(intent)
    } catch (e: Exception) {
      android.util.Log.w("SystemNotificationHelper", "Failed to launch notification settings: ${e.message}")
    }
  }
}
