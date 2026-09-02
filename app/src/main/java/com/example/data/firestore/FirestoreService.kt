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
}

