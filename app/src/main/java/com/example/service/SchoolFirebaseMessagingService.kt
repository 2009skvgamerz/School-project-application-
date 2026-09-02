package com.example.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.model.NotificationType
import com.example.util.SystemNotificationHelper
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SchoolFirebaseMessagingService : FirebaseMessagingService() {

  companion object {
    private const val TAG = "SchoolFCMService"
    private const val PREFS_NAME = "fcm_prefs"
    private const val KEY_DEVICE_TOKEN = "fcm_device_token"
    private const val KEY_SUBSCRIBED_TOPICS = "fcm_subscribed_topics"

    // In-memory cache for latest FCM token for quick UI inspection
    var latestToken: String? = null
      private set

    // Topic names for Firebase Cloud Messaging
    const val TOPIC_ALL_SCHOOL = "all_school"
    const val TOPIC_ANNOUNCEMENTS = "announcements"
    const val TOPIC_EVENTS = "events"
    const val TOPIC_EXAMS = "exams"
    const val TOPIC_SPORTS = "sports"
    const val TOPIC_CULTURAL = "cultural"
    const val TOPIC_ACADEMIC = "academic"
    const val TOPIC_STUDENTS = "role_students"
    const val TOPIC_TEACHERS = "role_teachers"
    const val TOPIC_STAFF = "role_staff"
    const val TOPIC_ADMIN = "role_admin"

    val DEFAULT_TOPICS = setOf(
      TOPIC_ALL_SCHOOL,
      TOPIC_ANNOUNCEMENTS,
      TOPIC_EVENTS,
      TOPIC_EXAMS
    )

    /**
     * Subscribe app client to standard Firebase Cloud Messaging topics safely.
     */
    fun subscribeToDefaultTopics(context: Context? = null) {
      try {
        context?.let { ctx ->
          val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
          val current = prefs.getStringSet(KEY_SUBSCRIBED_TOPICS, emptySet())?.toMutableSet() ?: mutableSetOf()
          current.addAll(DEFAULT_TOPICS)
          prefs.edit().putStringSet(KEY_SUBSCRIBED_TOPICS, current).apply()
        }

        // Check if FCM token is available before calling remote topic registration
        fetchFcmToken { token ->
          if (token != null) {
            try {
              val fcm = FirebaseMessaging.getInstance()
              DEFAULT_TOPICS.forEach { topic ->
                fcm.subscribeToTopic(topic).addOnFailureListener { e ->
                  Log.d(TAG, "Topic registration deferred for #$topic: ${e.message}")
                }
              }
              Log.d(TAG, "Subscribed to FCM default topics: $DEFAULT_TOPICS")
            } catch (e: Exception) {
              Log.d(TAG, "FCM topic subscription note: ${e.message}")
            }
          } else {
            Log.d(TAG, "FCM token unavailable; running in offline/local notification mode for topics: $DEFAULT_TOPICS")
          }
        }
      } catch (e: Exception) {
        Log.d(TAG, "Note initializing default topics: ${e.message}")
      }
    }

    /**
     * Subscribe to a specific topic with persistence.
     */
    fun subscribeToTopic(topic: String, context: Context? = null, onComplete: ((Boolean) -> Unit)? = null) {
      context?.let { ctx ->
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_SUBSCRIBED_TOPICS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(topic)
        prefs.edit().putStringSet(KEY_SUBSCRIBED_TOPICS, current).apply()
      }
      try {
        if (latestToken != null) {
          FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
              val success = task.isSuccessful
              if (success) {
                Log.d(TAG, "Successfully subscribed to topic: $topic")
              } else {
                Log.d(TAG, "Topic registration deferred: $topic")
              }
              onComplete?.invoke(success)
            }
        } else {
          onComplete?.invoke(true)
        }
      } catch (e: Exception) {
        Log.d(TAG, "Topic registration note for $topic: ${e.message}")
        onComplete?.invoke(true)
      }
    }

    /**
     * Unsubscribe from a specific topic.
     */
    fun unsubscribeFromTopic(topic: String, context: Context? = null, onComplete: ((Boolean) -> Unit)? = null) {
      context?.let { ctx ->
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_SUBSCRIBED_TOPICS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(topic)
        prefs.edit().putStringSet(KEY_SUBSCRIBED_TOPICS, current).apply()
      }
      try {
        if (latestToken != null) {
          FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
              val success = task.isSuccessful
              if (success) {
                Log.d(TAG, "Successfully unsubscribed from topic: $topic")
              } else {
                Log.d(TAG, "Topic unsubscription deferred: $topic")
              }
              onComplete?.invoke(success)
            }
        } else {
          onComplete?.invoke(true)
        }
      } catch (e: Exception) {
        Log.d(TAG, "Topic unsubscription note for $topic: ${e.message}")
        onComplete?.invoke(true)
      }
    }

    /**
     * Retrieve list of subscribed topics from preferences.
     */
    fun getSubscribedTopics(context: Context): Set<String> {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      val saved = prefs.getStringSet(KEY_SUBSCRIBED_TOPICS, null)
      return saved ?: DEFAULT_TOPICS
    }

    /**
     * Subscribe to role-specific FCM notification topic.
     */
    fun subscribeToRoleTopic(roleName: String, context: Context? = null) {
      val topic = "role_${roleName.lowercase()}"
      subscribeToTopic(topic, context)
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
              Log.d(TAG, "FCM registration token not available on current environment.")
              onTokenRetrieved(null)
            }
          }
      } catch (e: Exception) {
        Log.d(TAG, "FCM token retrieval note: ${e.message}")
        onTokenRetrieved(null)
      }
    }
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    latestToken = token
    Log.d(TAG, "Refreshed FCM Device Token: $token")
    saveFcmTokenToPreferences(token)

    // Sync refreshed token to Firestore
    try {
      val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
      val tokenDoc = hashMapOf(
        "token" to token,
        "updatedAt" to com.google.firebase.Timestamp.now(),
        "platform" to "Android",
        "appVersion" to "1.0.0"
      )
      firestore.collection("fcm_tokens").document(token.takeLast(20))
        .set(tokenDoc)
        .addOnSuccessListener { Log.d(TAG, "FCM token registered in Firestore") }
        .addOnFailureListener { e -> Log.d(TAG, "FCM token Firestore registration note: ${e.message}") }
    } catch (e: Exception) {
      Log.d(TAG, "Firestore token sync note: ${e.message}")
    }
  }

  /**
   * Called when an incoming Firebase Cloud Message arrives while the app is in the background,
   * foreground, or when background data messages are delivered by the Firebase SDK.
   *
   * Ensures every message is parsed and posted directly to the Android System Notification Tray
   * as a high-priority Heads-Up notification banner with sound, vibration, and deep-link routing.
   */
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Log.d(TAG, "FCM Message Received from: ${remoteMessage.from}, messageId: ${remoteMessage.messageId}")

    try {
      // 1. Extract Title with comprehensive fallbacks across notification and data payloads
      val title = remoteMessage.notification?.title
        ?: remoteMessage.data["title"]
        ?: remoteMessage.data["subject"]
        ?: remoteMessage.data["headline"]
        ?: "School Announcement"

      // 2. Extract Body / Message with fallbacks
      val message = remoteMessage.notification?.body
        ?: remoteMessage.data["message"]
        ?: remoteMessage.data["body"]
        ?: remoteMessage.data["description"]
        ?: remoteMessage.data["content"]
        ?: "New update from St. Joseph's Higher Secondary School."

      // 3. Extract Category and Route
      val typeStr = remoteMessage.data["type"]
        ?: remoteMessage.data["category"]
        ?: remoteMessage.data["tag"]
        ?: "announcement"

      val defaultRoute = when {
        typeStr.contains("event", ignoreCase = true) || typeStr.contains("calendar", ignoreCase = true) -> "calendar"
        typeStr.contains("homework", ignoreCase = true) || typeStr.contains("assignment", ignoreCase = true) -> "homework"
        typeStr.contains("attendance", ignoreCase = true) -> "attendance"
        typeStr.contains("exam", ignoreCase = true) -> "exams"
        typeStr.contains("fee", ignoreCase = true) -> "fees"
        typeStr.contains("bus", ignoreCase = true) || typeStr.contains("transport", ignoreCase = true) -> "bus_tracking"
        typeStr.contains("notice", ignoreCase = true) -> "notices"
        else -> "announcements"
      }

      val actionRoute = remoteMessage.data["action_route"]
        ?: remoteMessage.data["route"]
        ?: remoteMessage.data["target_screen"]
        ?: remoteMessage.data["click_action"]
        ?: defaultRoute

      val isUrgent = remoteMessage.data["urgent"]?.toBoolean()
        ?: remoteMessage.data["isEmergency"]?.toBoolean()
        ?: remoteMessage.data["priority"]?.equals("URGENT", ignoreCase = true)
        ?: (remoteMessage.priority == RemoteMessage.PRIORITY_HIGH)

      val notificationType = when (typeStr.lowercase()) {
        "notice", "circular" -> NotificationType.NOTICE
        "announcement", "broadcast" -> NotificationType.ANNOUNCEMENT
        "event", "events", "calendar" -> NotificationType.EVENT
        "homework", "assignment" -> NotificationType.HOMEWORK
        "attendance" -> NotificationType.ATTENDANCE
        "exam", "test" -> NotificationType.EXAM
        "fee", "payment" -> NotificationType.FEE
        "bus", "transport" -> NotificationType.BUS
        else -> NotificationType.ACADEMIC
      }

      val targetId = remoteMessage.data["target_id"]
        ?: remoteMessage.data["targetId"]
        ?: remoteMessage.data["id"]
        ?: remoteMessage.data["announcement_id"]
        ?: remoteMessage.data["event_id"]
        ?: remoteMessage.data["homework_id"]

      // Generate distinct notification ID based on messageId or timestamp to prevent collisions
      val notificationId = remoteMessage.messageId?.hashCode()
        ?: (System.currentTimeMillis() % 100000).toInt()

      Log.d(TAG, "Dispatching System Tray Notification: id=$notificationId, title='$title', type=$notificationType, route=$actionRoute, targetId=$targetId, urgent=$isUrgent")

      // 4. Display System Tray Notification (Heads-Up Pop-Up Banner) & Persist to Room
      SystemNotificationHelper.showSystemNotification(
        context = applicationContext,
        id = notificationId,
        title = title,
        message = message,
        type = notificationType,
        actionRoute = actionRoute,
        targetId = targetId,
        isUrgent = isUrgent
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error processing incoming FCM background message: ${e.message}", e)
    }
  }

  private fun saveFcmTokenToPreferences(token: String) {
    try {
      val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
      prefs.edit().putString(KEY_DEVICE_TOKEN, token).apply()
    } catch (e: Exception) {
      Log.e(TAG, "Error saving FCM token to preferences: ${e.message}")
    }
  }
}

