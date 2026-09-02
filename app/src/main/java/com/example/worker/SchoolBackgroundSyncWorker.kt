package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.model.NotificationType
import com.example.util.SystemNotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Background worker that runs when the phone is connected to the internet,
 * even when the app is completely closed (like WhatsApp background message delivery).
 *
 * It checks Firestore for new school announcements, calendar events, notices, and homework
 * and immediately triggers Heads-Up system notifications with sound, vibration, and deep linking.
 */
class SchoolBackgroundSyncWorker(
  private val appContext: Context,
  workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result {
    Log.d(TAG, "SchoolBackgroundSyncWorker executing in background (App closed/background state)...")

    val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val seenAnnouncementIds = prefs.getStringSet(KEY_SEEN_ANNOUNCEMENTS, emptySet())?.toMutableSet() ?: mutableSetOf()
    val seenEventIds = prefs.getStringSet(KEY_SEEN_EVENTS, emptySet())?.toMutableSet() ?: mutableSetOf()
    val seenNoticeIds = prefs.getStringSet(KEY_SEEN_NOTICES, emptySet())?.toMutableSet() ?: mutableSetOf()
    val seenHomeworkIds = prefs.getStringSet(KEY_SEEN_HOMEWORK, emptySet())?.toMutableSet() ?: mutableSetOf()
    val isFirstRun = prefs.getBoolean(KEY_IS_FIRST_RUN, true)

    try {
      if (FirebaseApp.getApps(appContext).isEmpty()) {
        FirebaseApp.initializeApp(appContext)
      }

      val firestore = FirebaseFirestore.getInstance()

      // 1. Check for New Announcements
      try {
        val announcementsSnapshot = firestore.collection("announcements")
          .orderBy("timestamp", Query.Direction.DESCENDING)
          .limit(10)
          .get()
          .await()

        for (doc in announcementsSnapshot.documents) {
          val id = doc.getString("id") ?: doc.id
          val title = doc.getString("title") ?: "Important School Announcement"
          val content = doc.getString("content") ?: ""
          val isEmergency = doc.getBoolean("isEmergency") ?: false

          if (!seenAnnouncementIds.contains(id)) {
            seenAnnouncementIds.add(id)
            Log.d(TAG, "New background announcement detected: $title")
            SystemNotificationHelper.showSystemNotification(
              context = appContext,
              title = if (isEmergency) "🚨 EMERGENCY: $title" else "📢 School Notice: $title",
              message = content,
              type = NotificationType.ANNOUNCEMENT,
              actionRoute = "announcements",
              isUrgent = isEmergency
            )
          }
        }
      } catch (e: Exception) {
        Log.d(TAG, "Worker announcement check note: ${e.message}")
      }

      // 2. Check for New Calendar Events & Exams
      try {
        val eventsSnapshot = firestore.collection("calendar_events")
          .limit(10)
          .get()
          .await()

        for (doc in eventsSnapshot.documents) {
          val id = doc.getString("id") ?: doc.id
          val title = doc.getString("title") ?: "Upcoming School Event"
          val date = doc.getString("formattedDate") ?: doc.getString("date") ?: ""
          val location = doc.getString("location") ?: "Campus"
          val time = doc.getString("time") ?: ""
          val isHoliday = doc.getBoolean("isHoliday") ?: false

          if (!seenEventIds.contains(id)) {
            seenEventIds.add(id)
            Log.d(TAG, "New background calendar event detected: $title")
            SystemNotificationHelper.showSystemNotification(
              context = appContext,
              title = if (isHoliday) "🎉 Holiday Alert: $title" else "📅 New Event: $title",
              message = "$date • $time at $location",
              type = NotificationType.EVENT,
              actionRoute = "calendar",
              isUrgent = false
            )
          }
        }
      } catch (e: Exception) {
        Log.d(TAG, "Worker event check note: ${e.message}")
      }

      // 3. Check for New Notices
      try {
        val noticesSnapshot = firestore.collection("notices")
          .orderBy("timestamp", Query.Direction.DESCENDING)
          .limit(10)
          .get()
          .await()

        for (doc in noticesSnapshot.documents) {
          val id = doc.getString("id") ?: doc.id
          val title = doc.getString("title") ?: "School Circular"
          val content = doc.getString("content") ?: ""

          if (!seenNoticeIds.contains(id)) {
            seenNoticeIds.add(id)
            Log.d(TAG, "New background notice detected: $title")
            SystemNotificationHelper.showSystemNotification(
              context = appContext,
              title = "📌 Circular: $title",
              message = content,
              type = NotificationType.NOTICE,
              actionRoute = "notices",
              isUrgent = false
            )
          }
        }
      } catch (e: Exception) {
        Log.d(TAG, "Worker notice check note: ${e.message}")
      }

      // 4. Check for New Homework Assignments
      try {
        val homeworkSnapshot = firestore.collection("homework")
          .limit(10)
          .get()
          .await()

        for (doc in homeworkSnapshot.documents) {
          val id = doc.getString("id") ?: doc.id
          val subject = doc.getString("subject") ?: "Homework"
          val title = doc.getString("title") ?: "New Assignment"
          val dueDate = doc.getString("dueDate") ?: ""

          if (!seenHomeworkIds.contains(id)) {
            seenHomeworkIds.add(id)
            Log.d(TAG, "New background homework detected: $subject - $title")
            SystemNotificationHelper.showSystemNotification(
              context = appContext,
              title = "📝 Homework Assigned: $subject",
              message = "$title (Due: $dueDate)",
              type = NotificationType.HOMEWORK,
              actionRoute = "homework",
              isUrgent = false
            )
          }
        }
      } catch (e: Exception) {
        Log.d(TAG, "Worker homework check note: ${e.message}")
      }

      // Save state
      prefs.edit()
        .putStringSet(KEY_SEEN_ANNOUNCEMENTS, seenAnnouncementIds)
        .putStringSet(KEY_SEEN_EVENTS, seenEventIds)
        .putStringSet(KEY_SEEN_NOTICES, seenNoticeIds)
        .putStringSet(KEY_SEEN_HOMEWORK, seenHomeworkIds)
        .putBoolean(KEY_IS_FIRST_RUN, false)
        .putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
        .apply()

      Log.d(TAG, "SchoolBackgroundSyncWorker successfully finished checking updates.")
      return Result.success()
    } catch (e: Exception) {
      Log.d(TAG, "SchoolBackgroundSyncWorker completed with note: ${e.message}")
      return Result.success()
    }
  }

  companion object {
    private const val TAG = "SchoolBgSyncWorker"
    const val PREFS_NAME = "school_bg_sync_prefs"
    const val KEY_SEEN_ANNOUNCEMENTS = "seen_announcements"
    const val KEY_SEEN_EVENTS = "seen_events"
    const val KEY_SEEN_NOTICES = "seen_notices"
    const val KEY_SEEN_HOMEWORK = "seen_homework"
    const val KEY_IS_FIRST_RUN = "is_first_sync_run"
    const val KEY_LAST_SYNC_TIME = "last_bg_sync_time"

    /**
     * Mark an item as seen locally so the worker doesn't duplicate notifications if created locally.
     */
    fun markItemSeen(context: Context, key: String, itemId: String) {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      val current = prefs.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()
      current.add(itemId)
      prefs.edit().putStringSet(key, current).apply()
    }
  }
}
