package com.example.service

import android.util.Log
import com.example.model.NotificationType
import com.example.util.SystemNotificationHelper
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SchoolFirebaseMessagingService : FirebaseMessagingService() {

  companion object {
    private const val TAG = "SchoolFCMService"

    // In-memory cache for latest FCM token for quick UI inspection
    var latestToken: String? = null
      private set

    // Topic names for Firebase Cloud Messaging
    const val TOPIC_ALL_SCHOOL = "all_school"
    const val TOPIC_ANNOUNCEMENTS = "announcements"
    const val TOPIC_EVENTS = "events"
    const val TOPIC_EXAMS = "exams"
    const val TOPIC_STUDENTS = "role_students"
    const val TOPIC_TEACHERS = "role_teachers"

    /**
     * Subscribe app client to standard Firebase Cloud Messaging topics.
     */
    fun subscribeToDefaultTopics() {
      try {
        val fcm = FirebaseMessaging.getInstance()
        fcm.subscribeToTopic(TOPIC_ALL_SCHOOL)
        fcm.subscribeToTopic(TOPIC_ANNOUNCEMENTS)
        fcm.subscribeToTopic(TOPIC_EVENTS)
        Log.d(TAG, "Subscribed to FCM default topics: $TOPIC_ALL_SCHOOL, $TOPIC_ANNOUNCEMENTS, $TOPIC_EVENTS")
      } catch (e: Exception) {
        Log.e(TAG, "Error subscribing to FCM default topics: ${e.message}")
      }
    }

    /**
     * Subscribe to role-specific FCM notification topic.
     */
    fun subscribeToRoleTopic(roleName: String) {
      try {
        val topic = "role_${roleName.lowercase()}"
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
        Log.d(TAG, "Subscribed to FCM role topic: $topic")
      } catch (e: Exception) {
        Log.e(TAG, "Error subscribing to role topic: ${e.message}")
      }
    }

    /**
     * Helper to retrieve FCM registration token.
     */
    fun fetchFcmToken(onTokenRetrieved: (String?) -> Unit) {
      if (latestToken != null) {
        onTokenRetrieved(latestToken)
        return
      }
      try {
        FirebaseMessaging.getInstance().token
          .addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
              latestToken = task.result
              onTokenRetrieved(latestToken)
            } else {
              Log.w(TAG, "Fetching FCM registration token failed", task.exception)
              onTokenRetrieved(null)
            }
          }
      } catch (e: Exception) {
        Log.e(TAG, "FCM Token retrieval exception: ${e.message}")
        onTokenRetrieved(null)
      }
    }
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    latestToken = token
    Log.d(TAG, "Refreshed FCM Device Token: $token")
    // Store token or register to backend server if needed
    saveFcmTokenToPreferences(token)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Log.d(TAG, "FCM Message Received from: ${remoteMessage.from}")

    // 1. Extract Title & Body from notification object or data map
    val title = remoteMessage.notification?.title
      ?: remoteMessage.data["title"]
      ?: "School Announcement"

    val message = remoteMessage.notification?.body
      ?: remoteMessage.data["message"]
      ?: remoteMessage.data["body"]
      ?: "New update from St. Joseph's Higher Secondary School."

    val typeStr = remoteMessage.data["type"] ?: "notice"
    val actionRoute = remoteMessage.data["action_route"] ?: remoteMessage.data["route"] ?: "notices"
    val isUrgent = remoteMessage.data["urgent"]?.toBoolean() ?: true

    val notificationType = when (typeStr.lowercase()) {
      "notice", "announcement" -> NotificationType.NOTICE
      "event", "events" -> NotificationType.EVENT
      "homework", "assignment" -> NotificationType.HOMEWORK
      "attendance" -> NotificationType.ATTENDANCE
      "exam", "test" -> NotificationType.EXAM
      "fee", "payment" -> NotificationType.FEE
      else -> NotificationType.ACADEMIC
    }

    Log.d(TAG, "Dispatching Push Notification: title='$title', type=$notificationType, route=$actionRoute")

    // 2. Display System Heads-Up Notification Banner
    SystemNotificationHelper.showSystemNotification(
      context = applicationContext,
      title = title,
      message = message,
      type = notificationType,
      actionRoute = actionRoute,
      isUrgent = isUrgent
    )
  }

  private fun saveFcmTokenToPreferences(token: String) {
    try {
      val prefs = getSharedPreferences("fcm_prefs", MODE_PRIVATE)
      prefs.edit().putString("fcm_device_token", token).apply()
    } catch (e: Exception) {
      Log.e(TAG, "Error saving FCM token to preferences: ${e.message}")
    }
  }
}
