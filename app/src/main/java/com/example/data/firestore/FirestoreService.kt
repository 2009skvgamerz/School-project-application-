package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreService(private val context: Context) {

  companion object {
    private const val TAG = "FirestoreService"
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_NOTICES = "notices"
    private const val COLLECTION_ATTENDANCE = "attendance"
    private const val COLLECTION_ANNOUNCEMENTS = "announcements"
    private const val COLLECTION_HOMEWORK = "homework"
    private const val COLLECTION_EVENTS = "events"
    private const val COLLECTION_FCM_TOKENS = "fcm_tokens"
    private const val COLLECTION_FCM_BROADCASTS = "fcm_broadcasts"
    private const val COLLECTION_FEEDBACK = "feedback"
  }

  init {
    Log.d(TAG, "Initializing FirestoreService with persistence enabled")
    try {
      if (FirebaseApp.getApps(context).isNotEmpty()) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
          auth.signInAnonymously()
            .addOnSuccessListener {
              Log.d(TAG, "Firestore anonymous session established for user: ${it.user?.uid}")
            }
            .addOnFailureListener { e ->
              Log.d(TAG, "Firestore anonymous auth fallback note: ${e.message}")
            }
        }
      }
    } catch (e: Exception) {
      Log.d(TAG, "Auth init note: ${e.message}")
    }
  }

  private val firestore: FirebaseFirestore? by lazy {
    try {
      if (FirebaseApp.getApps(context).isNotEmpty()) {
        FirebaseFirestore.getInstance().apply {
          firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        }
      } else {
        null
      }
    } catch (e: Exception) {
      Log.w(TAG, "Firestore initialization error: ${e.message}")
      null
    }
  }

  /**
   * Saves or updates a user profile in Firestore
   */
  suspend fun saveUserProfile(user: User): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      val data = hashMapOf(
        "id" to user.id,
        "username" to user.username,
        "fullName" to user.fullName,
        "email" to user.email,
        "role" to user.role.name,
        "phone" to user.phone,
        "avatarUrl" to user.avatarUrl,
        "designation" to user.designation,
        "updatedAt" to System.currentTimeMillis()
      )
      db.collection(COLLECTION_USERS).document(user.id)
        .set(data, SetOptions.merge())
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save user profile: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Publishes or updates a Notice on Cloud Firestore
   */
  suspend fun publishNotice(notice: Notice): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      val data = hashMapOf(
        "id" to notice.id,
        "title" to notice.title,
        "content" to notice.content,
        "date" to notice.date,
        "category" to notice.category.name,
        "publisherRole" to notice.publisherRole,
        "publisherName" to notice.publisherName,
        "isUrgent" to notice.isUrgent,
        "attachmentName" to (notice.attachmentName ?: ""),
        "timestamp" to System.currentTimeMillis()
      )
      db.collection(COLLECTION_NOTICES).document(notice.id)
        .set(data, SetOptions.merge())
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to publish notice to Firestore: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Listens for real-time Notice updates from Cloud Firestore
   */
  fun observeNotices(): Flow<List<Notice>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val subscription = db.collection(COLLECTION_NOTICES)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "Listen error on notices: ${error.message}")
          return@addSnapshotListener
        }

        if (snapshot != null && !snapshot.isEmpty) {
          val noticesList = snapshot.documents.mapNotNull { doc ->
            try {
              val id = doc.getString("id") ?: doc.id
              val title = doc.getString("title") ?: ""
              val content = doc.getString("content") ?: ""
              val date = doc.getString("date") ?: "Today"
              val categoryStr = doc.getString("category") ?: NoticeCategory.GENERAL.name
              val category = try {
                NoticeCategory.valueOf(categoryStr)
              } catch (_: Exception) {
                NoticeCategory.GENERAL
              }
              val publisherRole = doc.getString("publisherRole") ?: "Admin"
              val publisherName = doc.getString("publisherName") ?: "School Admin"
              val isUrgent = doc.getBoolean("isUrgent") ?: false
              val attachmentName = doc.getString("attachmentName")

              Notice(
                id = id,
                title = title,
                content = content,
                date = date,
                category = category,
                publisherRole = publisherRole,
                publisherName = publisherName,
                isUrgent = isUrgent,
                attachmentName = attachmentName
              )
            } catch (e: Exception) {
              Log.e(TAG, "Error mapping notice document: ${e.message}")
              null
            }
          }
          trySend(noticesList)
        }
      }

    awaitClose {
      subscription.remove()
    }
  }

  /**
   * Saves attendance record to Firestore
   */
  suspend fun saveAttendanceRecord(record: AttendanceRecord): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      val data = hashMapOf(
        "id" to record.id,
        "studentId" to record.studentId,
        "studentName" to record.studentName,
        "rollNo" to record.rollNo,
        "className" to record.className,
        "date" to record.date,
        "status" to record.status.name,
        "markedBy" to record.markedBy,
        "notes" to record.notes,
        "timestamp" to System.currentTimeMillis()
      )
      db.collection(COLLECTION_ATTENDANCE).document(record.id)
        .set(data, SetOptions.merge())
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save attendance record: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Listens for real-time Attendance updates from Cloud Firestore
   */
  fun observeAttendance(): Flow<List<AttendanceRecord>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val subscription = db.collection(COLLECTION_ATTENDANCE)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "Listen error on attendance: ${error.message}")
          return@addSnapshotListener
        }

        if (snapshot != null && !snapshot.isEmpty) {
          val records = snapshot.documents.mapNotNull { doc ->
            try {
              val id = doc.getString("id") ?: doc.id
              val studentId = doc.getString("studentId") ?: ""
              val studentName = doc.getString("studentName") ?: ""
              val rollNo = (doc.getLong("rollNo") ?: 1L).toInt()
              val className = doc.getString("className") ?: "Class 10-A"
              val date = doc.getString("date") ?: ""
              val statusStr = doc.getString("status") ?: AttendanceStatus.FULL_DAY.name
              val status = try {
                AttendanceStatus.valueOf(statusStr)
              } catch (_: Exception) {
                AttendanceStatus.FULL_DAY
              }
              val markedBy = doc.getString("markedBy") ?: ""
              val notes = doc.getString("notes") ?: ""

              AttendanceRecord(
                id = id,
                studentId = studentId,
                studentName = studentName,
                rollNo = rollNo,
                className = className,
                date = date,
                status = status,
                markedBy = markedBy,
                notes = notes
              )
            } catch (e: Exception) {
              Log.e(TAG, "Error mapping attendance document: ${e.message}")
              null
            }
          }
          trySend(records)
        }
      }

    awaitClose {
      subscription.remove()
    }
  }

  /**
   * Publishes a School Announcement to Firestore
   */
  suspend fun publishAnnouncement(announcement: SchoolAnnouncement): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      val data = hashMapOf(
        "id" to announcement.id,
        "title" to announcement.title,
        "content" to announcement.content,
        "priority" to announcement.priority.name,
        "targetAudience" to announcement.targetAudience.name,
        "date" to announcement.date,
        "timeAgo" to announcement.timeAgo,
        "authorName" to announcement.authorName,
        "authorRole" to announcement.authorRole,
        "isEmergency" to announcement.isEmergency,
        "audioDurationSec" to (announcement.audioDurationSec ?: 0),
        "acknowledgedByCurrentUser" to announcement.acknowledgedByCurrentUser,
        "acknowledgmentsCount" to announcement.acknowledgmentsCount,
        "attachmentName" to (announcement.attachmentName ?: ""),
        "timestamp" to System.currentTimeMillis()
      )
      db.collection(COLLECTION_ANNOUNCEMENTS).document(announcement.id)
        .set(data, SetOptions.merge())
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to publish announcement: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Listens for real-time announcements
   */
  fun observeAnnouncements(): Flow<List<SchoolAnnouncement>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val subscription = db.collection(COLLECTION_ANNOUNCEMENTS)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "Listen error on announcements: ${error.message}")
          return@addSnapshotListener
        }

        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { doc ->
            try {
              val id = doc.getString("id") ?: doc.id
              val title = doc.getString("title") ?: ""
              val content = doc.getString("content") ?: ""
              val date = doc.getString("date") ?: ""
              val timeAgo = doc.getString("timeAgo") ?: "Just now"
              val authorName = doc.getString("authorName") ?: "Administration"
              val authorRole = doc.getString("authorRole") ?: "Principal's Desk"
              val priorityStr = doc.getString("priority") ?: AnnouncementPriority.GENERAL.name
              val priority = try {
                AnnouncementPriority.valueOf(priorityStr)
              } catch (_: Exception) {
                AnnouncementPriority.GENERAL
              }
              val targetAudienceStr = doc.getString("targetAudience") ?: AnnouncementAudience.ALL_SCHOOL.name
              val targetAudience = try {
                AnnouncementAudience.valueOf(targetAudienceStr)
              } catch (_: Exception) {
                AnnouncementAudience.ALL_SCHOOL
              }
              val isEmergency = doc.getBoolean("isEmergency") ?: false
              val audioDurationSec = doc.getLong("audioDurationSec")?.toInt()
              val acknowledged = doc.getBoolean("acknowledgedByCurrentUser") ?: false
              val acknowledgmentsCount = (doc.getLong("acknowledgmentsCount") ?: 128L).toInt()
              val attachmentName = doc.getString("attachmentName")

              SchoolAnnouncement(
                id = id,
                title = title,
                content = content,
                priority = priority,
                targetAudience = targetAudience,
                date = date,
                timeAgo = timeAgo,
                authorName = authorName,
                authorRole = authorRole,
                isEmergency = isEmergency,
                audioDurationSec = audioDurationSec,
                acknowledgedByCurrentUser = acknowledged,
                acknowledgmentsCount = acknowledgmentsCount,
                attachmentName = attachmentName
              )
            } catch (e: Exception) {
              null
            }
          }
          trySend(list)
        }
      }

    awaitClose {
      subscription.remove()
    }
  }

  /**
   * Saves Homework to Firestore
   */
  suspend fun saveHomework(hw: Homework): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      val data = hashMapOf(
        "id" to hw.id,
        "title" to hw.title,
        "description" to hw.description,
        "subjectName" to hw.subjectName,
        "className" to hw.className,
        "assignedDate" to hw.assignedDate,
        "dueDate" to hw.dueDate,
        "teacherName" to hw.teacherName,
        "status" to hw.status.name,
        "maxMarks" to hw.maxMarks,
        "submissionNote" to hw.submissionNote,
        "submissionsCount" to hw.submissionsCount,
        "totalStudents" to hw.totalStudents,
        "timestamp" to System.currentTimeMillis()
      )
      db.collection(COLLECTION_HOMEWORK).document(hw.id)
        .set(data, SetOptions.merge())
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save homework: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Listens for real-time Homework updates
   */
  fun observeHomework(): Flow<List<Homework>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val subscription = db.collection(COLLECTION_HOMEWORK)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "Listen error on homework: ${error.message}")
          return@addSnapshotListener
        }

        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { doc ->
            try {
              val id = doc.getString("id") ?: doc.id
              val title = doc.getString("title") ?: ""
              val description = doc.getString("description") ?: ""
              val subjectName = doc.getString("subjectName") ?: ""
              val className = doc.getString("className") ?: "Class 10-A"
              val assignedDate = doc.getString("assignedDate") ?: ""
              val dueDate = doc.getString("dueDate") ?: ""
              val teacherName = doc.getString("teacherName") ?: "Prof. Sarah Jenkins"
              val statusStr = doc.getString("status") ?: HomeworkStatus.PENDING.name
              val status = try {
                HomeworkStatus.valueOf(statusStr)
              } catch (_: Exception) {
                HomeworkStatus.PENDING
              }
              val maxMarks = (doc.getLong("maxMarks") ?: 100L).toInt()
              val submissionNote = doc.getString("submissionNote") ?: ""
              val submissionsCount = (doc.getLong("submissionsCount") ?: 0L).toInt()
              val totalStudents = (doc.getLong("totalStudents") ?: 32L).toInt()

              Homework(
                id = id,
                title = title,
                description = description,
                subjectName = subjectName,
                className = className,
                assignedDate = assignedDate,
                dueDate = dueDate,
                teacherName = teacherName,
                status = status,
                maxMarks = maxMarks,
                submissionNote = submissionNote,
                submissionsCount = submissionsCount,
                totalStudents = totalStudents
              )
            } catch (e: Exception) {
              null
            }
          }
          trySend(list)
        }
      }

    awaitClose {
      subscription.remove()
    }
  }

  /**
   * Publishes or updates a School Calendar Event in Firestore
   */
  suspend fun saveCalendarEvent(event: CalendarEvent): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      val data = hashMapOf(
        "id" to event.id,
        "title" to event.title,
        "description" to event.description,
        "date" to event.date,
        "formattedDate" to event.formattedDate,
        "time" to event.time,
        "location" to event.location,
        "category" to event.category.name,
        "isHoliday" to event.isHoliday,
        "targetGrades" to event.targetGrades,
        "organizer" to event.organizer,
        "hasReminder" to event.hasReminder,
        "timestamp" to System.currentTimeMillis()
      )
      db.collection(COLLECTION_EVENTS).document(event.id)
        .set(data, SetOptions.merge())
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.w(TAG, "Calendar event save note (${event.id}): ${e.message}")
      Result.failure(e)
    }
  }

  /**
   * Listens for real-time School Calendar Events updates from Cloud Firestore
   */
  fun observeCalendarEvents(): Flow<List<CalendarEvent>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val subscription = db.collection(COLLECTION_EVENTS)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "Listen error on calendar events: ${error.message}")
          return@addSnapshotListener
        }

        if (snapshot != null && !snapshot.isEmpty) {
          val list = snapshot.documents.mapNotNull { doc ->
            try {
              val id = doc.getString("id") ?: doc.id
              val title = doc.getString("title") ?: ""
              val description = doc.getString("description") ?: ""
              val date = doc.getString("date") ?: ""
              val formattedDate = doc.getString("formattedDate") ?: date
              val time = doc.getString("time") ?: "09:00 AM"
              val location = doc.getString("location") ?: "Main Campus"
              val categoryStr = doc.getString("category") ?: CalendarCategory.ACADEMIC.name
              val category = try {
                CalendarCategory.valueOf(categoryStr)
              } catch (_: Exception) {
                CalendarCategory.ACADEMIC
              }
              val isHoliday = doc.getBoolean("isHoliday") ?: false
              val targetGrades = doc.getString("targetGrades") ?: "All Classes"
              val organizer = doc.getString("organizer") ?: "Academic Council"
              val hasReminder = doc.getBoolean("hasReminder") ?: false

              CalendarEvent(
                id = id,
                title = title,
                description = description,
                date = date,
                formattedDate = formattedDate,
                time = time,
                location = location,
                category = category,
                isHoliday = isHoliday,
                targetGrades = targetGrades,
                organizer = organizer,
                hasReminder = hasReminder
              )
            } catch (e: Exception) {
              null
            }
          }
          trySend(list)
        }
      }

    awaitClose {
      subscription.remove()
    }
  }

  /**
   * Registers or updates an FCM device token in Cloud Firestore for targeted push routing
   */
  suspend fun registerDeviceToken(userId: String, token: String, role: String, topics: List<String>): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      val data = hashMapOf(
        "userId" to userId,
        "fcmToken" to token,
        "role" to role,
        "subscribedTopics" to topics,
        "platform" to "Android",
        "lastSeen" to System.currentTimeMillis()
      )
      db.collection(COLLECTION_FCM_TOKENS).document(token.take(64))
        .set(data, SetOptions.merge())
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.w(TAG, "FCM device token registration note: ${e.message}")
      Result.failure(e)
    }
  }

  /**
   * Records a dispatched FCM push broadcast message in Firestore for auditing and cross-client telemetry
   */
  suspend fun recordFcmBroadcast(
    broadcastId: String = "fcm_${System.currentTimeMillis()}",
    title: String,
    body: String,
    type: String,
    topic: String,
    targetRoute: String,
    isUrgent: Boolean
  ): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      val data = hashMapOf(
        "broadcastId" to broadcastId,
        "title" to title,
        "body" to body,
        "type" to type,
        "topic" to topic,
        "targetRoute" to targetRoute,
        "isUrgent" to isUrgent,
        "timestamp" to System.currentTimeMillis()
      )
      db.collection(COLLECTION_FCM_BROADCASTS).document(broadcastId)
        .set(data, SetOptions.merge())
        .await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.w(TAG, "FCM broadcast record note: ${e.message}")
      Result.failure(e)
    }
  }

  /**
   * Saves user suggestions and feedback. First attempts direct Firestore sync.
   * If Firestore rules deny permission or network is offline, securely saves to local
   * feedback repository and sync queue so user submissions are never rejected or lost.
   */
  suspend fun submitFeedback(
    feedbackId: String = "fb_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}",
    userId: String? = null,
    userName: String? = null,
    userRole: String? = null,
    category: String,
    suggestion: String,
    rating: Int = 5
  ): Result<FeedbackSubmissionItem> {
    var isCloudSynced = false

    // 1. Attempt anonymous authentication if current user is unauthenticated
    try {
      if (FirebaseApp.getApps(context).isNotEmpty()) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
          auth.signInAnonymously().await()
          Log.d(TAG, "Anonymous auth established for feedback submitter: ${auth.currentUser?.uid}")
        }
      }
    } catch (authEx: Exception) {
      Log.w(TAG, "Auth prep note: ${authEx.message}")
    }

    // 2. Attempt Firestore Cloud write
    try {
      val db = firestore
      if (db != null) {
        val data = hashMapOf(
          "id" to feedbackId,
          "userId" to (userId ?: "anonymous"),
          "userName" to (userName ?: "Anonymous User"),
          "userRole" to (userRole ?: "Student"),
          "category" to category,
          "suggestion" to suggestion,
          "rating" to rating,
          "status" to "submitted",
          "createdAt" to System.currentTimeMillis()
        )
        db.collection(COLLECTION_FEEDBACK).document(feedbackId)
          .set(data, SetOptions.merge())
          .await()
        isCloudSynced = true
        Log.d(TAG, "Feedback saved directly to Firestore collection '$COLLECTION_FEEDBACK': $feedbackId")
      }
    } catch (e: Exception) {
      Log.w(TAG, "Firestore write note (${e.javaClass.simpleName}): ${e.message}. Saving securely to local submission queue.")
    }

    // 3. Always persist to local feedback storage
    val submission = FeedbackSubmissionItem(
      id = feedbackId,
      userId = userId ?: "anonymous",
      userName = userName ?: "Anonymous User",
      userRole = userRole ?: "Student",
      category = category,
      suggestion = suggestion,
      rating = rating,
      isCloudSynced = isCloudSynced,
      createdAt = System.currentTimeMillis()
    )
    saveFeedbackLocally(submission)

    return Result.success(submission)
  }

  private fun saveFeedbackLocally(item: FeedbackSubmissionItem) {
    try {
      val prefs = context.getSharedPreferences("st_joseph_feedback_vault", Context.MODE_PRIVATE)
      val existingJson = prefs.getString("feedback_history", "[]") ?: "[]"
      val array = org.json.JSONArray(existingJson)

      val obj = org.json.JSONObject().apply {
        put("id", item.id)
        put("userId", item.userId)
        put("userName", item.userName)
        put("userRole", item.userRole)
        put("category", item.category)
        put("suggestion", item.suggestion)
        put("rating", item.rating)
        put("isCloudSynced", item.isCloudSynced)
        put("createdAt", item.createdAt)
      }
      array.put(obj)

      prefs.edit().putString("feedback_history", array.toString()).apply()
      Log.d(TAG, "Feedback safely stored in local vault. Total: ${array.length()}")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save feedback locally: ${e.message}")
    }
  }

  fun getLocalFeedbackList(): List<FeedbackSubmissionItem> {
    val result = mutableListOf<FeedbackSubmissionItem>()
    try {
      val prefs = context.getSharedPreferences("st_joseph_feedback_vault", Context.MODE_PRIVATE)
      val existingJson = prefs.getString("feedback_history", "[]") ?: "[]"
      val array = org.json.JSONArray(existingJson)

      for (i in (array.length() - 1) downTo 0) {
        val obj = array.getJSONObject(i)
        result.add(
          FeedbackSubmissionItem(
            id = obj.optString("id", "fb_$i"),
            userId = obj.optString("userId", "anonymous"),
            userName = obj.optString("userName", "Anonymous User"),
            userRole = obj.optString("userRole", "Student"),
            category = obj.optString("category", "Suggestion"),
            suggestion = obj.optString("suggestion", ""),
            rating = obj.optInt("rating", 5),
            isCloudSynced = obj.optBoolean("isCloudSynced", false),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
          )
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to read local feedback: ${e.message}")
    }
    return result
  }

  /**
   * Deletes a feedback item from both Cloud Firestore and local vault storage
   */
  suspend fun deleteFeedbackItem(feedbackId: String): Result<Unit> {
    // 1. Delete from Firestore if available
    try {
      firestore?.collection(COLLECTION_FEEDBACK)?.document(feedbackId)?.delete()?.await()
      Log.d(TAG, "Deleted feedback doc from Firestore: $feedbackId")
    } catch (e: Exception) {
      Log.w(TAG, "Firestore feedback delete note: ${e.message}")
    }

    // 2. Delete from local storage
    try {
      val prefs = context.getSharedPreferences("st_joseph_feedback_vault", Context.MODE_PRIVATE)
      val existingJson = prefs.getString("feedback_history", "[]") ?: "[]"
      val array = org.json.JSONArray(existingJson)
      val newArray = org.json.JSONArray()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        if (obj.optString("id") != feedbackId) {
          newArray.put(obj)
        }
      }
      prefs.edit().putString("feedback_history", newArray.toString()).apply()
      Log.d(TAG, "Deleted feedback $feedbackId from local vault")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to delete feedback locally: ${e.message}")
    }
    return Result.success(Unit)
  }

  /**
   * Clears all feedback from both Cloud Firestore and local storage
   */
  suspend fun clearAllFeedback(): Result<Int> {
    var deletedCount = 0
    // 1. Purge from Firestore
    try {
      val res = purgeCollection(COLLECTION_FEEDBACK)
      deletedCount = res.getOrDefault(0)
    } catch (e: Exception) {
      Log.w(TAG, "Failed to purge Firestore feedback collection: ${e.message}")
    }

    // 2. Clear local vault
    try {
      val prefs = context.getSharedPreferences("st_joseph_feedback_vault", Context.MODE_PRIVATE)
      val count = org.json.JSONArray(prefs.getString("feedback_history", "[]") ?: "[]").length()
      if (deletedCount == 0) deletedCount = count
      prefs.edit().remove("feedback_history").apply()
      Log.d(TAG, "Cleared all local feedback vault entries")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to clear local feedback: ${e.message}")
    }

    return Result.success(deletedCount)
  }

  // ==========================================
  // GOD MODE DIRECT FIRESTORE DATA CONTROLS
  // ==========================================

  data class FirestoreRawDoc(
    val id: String,
    val collection: String,
    val summary: String,
    val timestamp: Long,
    val fields: Map<String, Any?>
  )

  /**
   * Returns list of primary known Firestore collections
   */
  fun getKnownCollections(): List<String> = listOf(
    COLLECTION_FEEDBACK,
    COLLECTION_NOTICES,
    COLLECTION_ANNOUNCEMENTS,
    COLLECTION_HOMEWORK,
    COLLECTION_ATTENDANCE,
    COLLECTION_EVENTS,
    COLLECTION_USERS,
    COLLECTION_FCM_BROADCASTS,
    COLLECTION_FCM_TOKENS
  )

  /**
   * Deletes a specific document from any Firestore collection
   */
  suspend fun deleteFirestoreDocument(collection: String, documentId: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      db.collection(collection).document(documentId).delete().await()
      Log.d(TAG, "God Mode deleted Firestore document: $collection/$documentId")
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to delete Firestore document $collection/$documentId: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Fetches raw documents from a given collection for live developer inspection and surgical deletion
   */
  suspend fun getCollectionDocuments(collection: String, limit: Long = 100): Result<List<FirestoreRawDoc>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      val snapshot = db.collection(collection).limit(limit).get().await()
      val list = snapshot.documents.map { doc ->
        val summary = doc.getString("title")
          ?: doc.getString("suggestion")
          ?: doc.getString("fullName")
          ?: doc.getString("name")
          ?: doc.getString("studentName")
          ?: doc.getString("body")
          ?: doc.getString("content")?.take(40)
          ?: doc.id
        val ts = doc.getLong("timestamp")
          ?: doc.getLong("createdAt")
          ?: doc.getLong("updatedAt")
          ?: 0L
        FirestoreRawDoc(
          id = doc.id,
          collection = collection,
          summary = summary,
          timestamp = ts,
          fields = doc.data ?: emptyMap()
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to get docs from Firestore collection $collection: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Purges / deletes all documents from a specific Firestore collection
   */
  suspend fun purgeCollection(collection: String): Result<Int> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      val snapshot = db.collection(collection).get().await()
      val total = snapshot.size()
      if (total == 0) return Result.success(0)

      val batch = db.batch()
      var count = 0
      for (doc in snapshot.documents) {
        batch.delete(doc.reference)
        count++
        if (count % 400 == 0) {
          batch.commit().await()
        }
      }
      if (count % 400 != 0 && count > 0) {
        batch.commit().await()
      }
      Log.d(TAG, "God Mode purged $total documents from Firestore collection: $collection")
      Result.success(total)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to purge collection $collection: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Nuclear Wipe: deletes all documents across all known Firestore collections
   */
  suspend fun wipeAllFirestoreData(): Result<Map<String, Int>> {
    val collections = getKnownCollections()
    val results = mutableMapOf<String, Int>()
    var lastError: Exception? = null

    for (col in collections) {
      val res = purgeCollection(col)
      res.fold(
        onSuccess = { results[col] = it },
        onFailure = {
          results[col] = 0
          lastError = it as? Exception
        }
      )
    }

    return if (results.values.sum() > 0 || lastError == null) {
      Result.success(results)
    } else {
      Result.failure(lastError ?: IllegalStateException("Failed to wipe Firestore"))
    }
  }

  /**
   * Pushes / seeds current system data into Cloud Firestore
   */
  suspend fun pushSeedDataToFirestore(
    notices: List<Notice>,
    homeworks: List<Homework>,
    announcements: List<SchoolAnnouncement>,
    events: List<CalendarEvent>,
    attendance: List<AttendanceRecord>
  ): Result<Int> {
    var count = 0
    try {
      notices.forEach { publishNotice(it); count++ }
      homeworks.forEach { saveHomework(it); count++ }
      announcements.forEach { publishAnnouncement(it); count++ }
      events.forEach { saveCalendarEvent(it); count++ }
      attendance.take(20).forEach { saveAttendanceRecord(it); count++ }
      return Result.success(count)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to push seed data to Firestore: ${e.message}", e)
      return Result.failure(e)
    }
  }
}

data class FeedbackSubmissionItem(
  val id: String,
  val userId: String,
  val userName: String,
  val userRole: String,
  val category: String,
  val suggestion: String,
  val rating: Int,
  val isCloudSynced: Boolean,
  val createdAt: Long
)

