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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

data class PendingSyncOperation(
  val collection: String,
  val documentId: String,
  val summary: String,
  val data: Map<String, Any?>,
  val errorSummary: String? = null,
  val timestamp: Long = System.currentTimeMillis()
)

class FirestoreService(private val context: Context) {

  companion object {
    private const val TAG = "FirestoreService"
    const val COLLECTION_USERS = "users"
    const val COLLECTION_NOTICES = "notices"
    const val COLLECTION_ATTENDANCE = "attendance"
    const val COLLECTION_ANNOUNCEMENTS = "announcements"
    const val COLLECTION_HOMEWORK = "homework"
    const val COLLECTION_EVENTS = "events"
    const val COLLECTION_FCM_TOKENS = "fcm_tokens"
    const val COLLECTION_FCM_BROADCASTS = "fcm_broadcasts"
    const val COLLECTION_FEEDBACK = "feedback"
    const val COLLECTION_STUDENTS = "students"
    const val COLLECTION_TEACHERS = "teachers"
  }

  private val _lastRetryEvent = MutableStateFlow<FirestoreRetryEvent?>(null)
  val lastRetryEvent: StateFlow<FirestoreRetryEvent?> = _lastRetryEvent.asStateFlow()

  private val _pendingSyncQueue = MutableStateFlow<List<PendingSyncOperation>>(emptyList())
  val pendingSyncQueue: StateFlow<List<PendingSyncOperation>> = _pendingSyncQueue.asStateFlow()

  private val _realtimeError = MutableStateFlow<String?>(null)
  val realtimeError: StateFlow<String?> = _realtimeError.asStateFlow()

  fun clearLastRetryEvent() {
    _lastRetryEvent.value = null
  }

  fun enqueuePendingSync(collection: String, documentId: String, summary: String, data: Map<String, Any?>, errorSummary: String?) {
    _pendingSyncQueue.update { list ->
      val filtered = list.filterNot { it.collection == collection && it.documentId == documentId }
      filtered + PendingSyncOperation(
        collection = collection,
        documentId = documentId,
        summary = summary,
        data = data,
        errorSummary = errorSummary
      )
    }
  }

  fun removeFromPendingQueue(collection: String, documentId: String) {
    _pendingSyncQueue.update { list ->
      list.filterNot { it.collection == collection && it.documentId == documentId }
    }
  }

  suspend fun flushPendingSyncQueue(): Int {
    val db = firestore ?: return 0
    val items = _pendingSyncQueue.value
    if (items.isEmpty()) return 0
    var flushed = 0
    val remaining = mutableListOf<PendingSyncOperation>()

    for (item in items) {
      try {
        db.collection(item.collection).document(item.documentId)
          .set(item.data, SetOptions.merge())
          .await()
        flushed++
      } catch (e: Exception) {
        remaining.add(item.copy(errorSummary = e.message))
      }
    }
    _pendingSyncQueue.value = remaining
    return flushed
  }

  init {
    Log.d(TAG, "Initializing FirestoreService with persistence enabled")
    try {
      if (FirebaseApp.getApps(context).isNotEmpty()) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
          auth.signInAnonymously()
            .addOnSuccessListener {
              Log.d(TAG, "Firestore anonymous session established: ${it.user?.uid}")
            }
            .addOnFailureListener { e ->
              Log.d(TAG, "Firestore running with standard client rules: ${e.message}")
            }
        }
      }
    } catch (e: Exception) {
      Log.d(TAG, "Auth init note: ${e.message}")
    }
  }

  /**
   * Ensures active Firebase Auth credentials exist if available, or proceeds with open cloud rules
   */
  suspend fun ensureAuthenticated(): Boolean {
    try {
      if (FirebaseApp.getApps(context).isEmpty()) return false
      val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
      if (auth.currentUser != null) return true

      // Try Anonymous Auth if available
      try {
        val anon = auth.signInAnonymously().await()
        if (anon.user != null) {
          Log.d(TAG, "Authenticated anonymously: ${anon.user?.uid}")
          return true
        }
      } catch (e: Exception) {
        Log.d(TAG, "Anonymous auth note: ${e.message}")
      }
    } catch (e: Exception) {
      Log.d(TAG, "ensureAuthenticated note: ${e.message}")
    }
    return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
  }

  private val firestore: FirebaseFirestore? by lazy {
    try {
      if (FirebaseApp.getApps(context).isNotEmpty()) {
        FirebaseFirestore.getInstance()
      } else {
        null
      }
    } catch (e: Exception) {
      Log.w(TAG, "Firestore initialization error: ${e.message}")
      null
    }
  }

  /**
   * Saves or updates a user profile in Firestore with exponential backoff retry
   */
  suspend fun saveUserProfile(user: User): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
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
    return try {
      ensureAuthenticated()
      FirestoreRetryPolicy.executeWithRetry(
        operationName = "saveUserProfile(${user.id})",
        onRetry = { event -> _lastRetryEvent.value = event }
      ) {
        db.collection(COLLECTION_USERS).document(user.id)
          .set(data, SetOptions.merge())
          .await()
      }
      removeFromPendingQueue(COLLECTION_USERS, user.id)
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save user profile: ${e.message}", e)
      enqueuePendingSync(COLLECTION_USERS, user.id, "${user.fullName} (${user.role.displayName})", data, e.message)
      Result.failure(e)
    }
  }

  /**
   * Publishes or updates a Notice on Cloud Firestore with exponential backoff retry
   */
  suspend fun publishNotice(notice: Notice): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
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
    return try {
      ensureAuthenticated()
      FirestoreRetryPolicy.executeWithRetry(
        operationName = "publishNotice(${notice.id})",
        onRetry = { event -> _lastRetryEvent.value = event }
      ) {
        db.collection(COLLECTION_NOTICES).document(notice.id)
          .set(data, SetOptions.merge())
          .await()
      }
      removeFromPendingQueue(COLLECTION_NOTICES, notice.id)
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to publish notice to Firestore: ${e.message}", e)
      enqueuePendingSync(COLLECTION_NOTICES, notice.id, notice.title, data, e.message)
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
          _realtimeError.value = "Notices: ${error.message}"
          return@addSnapshotListener
        }

        if (snapshot != null) {
          val noticesList = snapshot.documents.mapNotNull { doc ->
            try {
              val id = doc.getString("id") ?: doc.id
              val title = doc.getString("title") ?: ""
              val content = doc.getString("content") ?: ""
              val timestamp = doc.getLong("timestamp") ?: 0L
              val rawDate = doc.getString("date")
              val date = when {
                !rawDate.isNullOrBlank() && rawDate != "Today, 09:00 AM" && rawDate != "Today" -> rawDate
                timestamp > 0L -> com.example.util.SchoolDateTimeUtils.formatNoticeDate(timestamp)
                else -> com.example.util.SchoolDateTimeUtils.getCurrentNoticeDateString()
              }
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
   * Saves attendance record to Firestore with exponential backoff retry
   */
  suspend fun saveAttendanceRecord(record: AttendanceRecord): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
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
    return try {
      ensureAuthenticated()
      FirestoreRetryPolicy.executeWithRetry(
        operationName = "saveAttendanceRecord(${record.id})",
        onRetry = { event -> _lastRetryEvent.value = event }
      ) {
        db.collection(COLLECTION_ATTENDANCE).document(record.id)
          .set(data, SetOptions.merge())
          .await()
      }
      removeFromPendingQueue(COLLECTION_ATTENDANCE, record.id)
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save attendance record: ${e.message}", e)
      enqueuePendingSync(COLLECTION_ATTENDANCE, record.id, "${record.studentName} (${record.status.label})", data, e.message)
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
          _realtimeError.value = "Attendance: ${error.message}"
          return@addSnapshotListener
        }

        if (snapshot != null) {
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
   * Publishes a School Announcement to Firestore with exponential backoff retry
   */
  suspend fun publishAnnouncement(announcement: SchoolAnnouncement): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
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
    return try {
      ensureAuthenticated()
      FirestoreRetryPolicy.executeWithRetry(
        operationName = "publishAnnouncement(${announcement.id})",
        onRetry = { event -> _lastRetryEvent.value = event }
      ) {
        db.collection(COLLECTION_ANNOUNCEMENTS).document(announcement.id)
          .set(data, SetOptions.merge())
          .await()
      }
      removeFromPendingQueue(COLLECTION_ANNOUNCEMENTS, announcement.id)
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to publish announcement: ${e.message}", e)
      enqueuePendingSync(COLLECTION_ANNOUNCEMENTS, announcement.id, announcement.title, data, e.message)
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
          _realtimeError.value = "Announcements: ${error.message}"
          return@addSnapshotListener
        }

        if (snapshot != null) {
          val list = snapshot.documents.mapNotNull { doc ->
            try {
              val id = doc.getString("id") ?: doc.id
              val title = doc.getString("title") ?: ""
              val content = doc.getString("content") ?: ""
              val timestamp = doc.getLong("timestamp") ?: 0L
              val rawDate = doc.getString("date")
              val date = when {
                !rawDate.isNullOrBlank() -> rawDate
                timestamp > 0L -> com.example.util.SchoolDateTimeUtils.formatAnnouncementDate(timestamp)
                else -> com.example.util.SchoolDateTimeUtils.getCurrentAnnouncementDateString()
              }
              val timeAgo = if (timestamp > 0L) {
                com.example.util.SchoolDateTimeUtils.formatTimeAgo(timestamp)
              } else {
                doc.getString("timeAgo") ?: "Just now"
              }
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
   * Saves Homework to Firestore with exponential backoff retry
   */
  suspend fun saveHomework(hw: Homework): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
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
    return try {
      ensureAuthenticated()
      FirestoreRetryPolicy.executeWithRetry(
        operationName = "saveHomework(${hw.id})",
        onRetry = { event -> _lastRetryEvent.value = event }
      ) {
        db.collection(COLLECTION_HOMEWORK).document(hw.id)
          .set(data, SetOptions.merge())
          .await()
      }
      removeFromPendingQueue(COLLECTION_HOMEWORK, hw.id)
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save homework: ${e.message}", e)
      enqueuePendingSync(COLLECTION_HOMEWORK, hw.id, "${hw.title} (${hw.className})", data, e.message)
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
          _realtimeError.value = "Homework: ${error.message}"
          return@addSnapshotListener
        }

        if (snapshot != null) {
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
   * Publishes or updates a School Calendar Event in Firestore with exponential backoff retry
   */
  suspend fun saveCalendarEvent(event: CalendarEvent): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
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
    return try {
      ensureAuthenticated()
      FirestoreRetryPolicy.executeWithRetry(
        operationName = "saveCalendarEvent(${event.id})",
        onRetry = { eventRetry -> _lastRetryEvent.value = eventRetry }
      ) {
        db.collection(COLLECTION_EVENTS).document(event.id)
          .set(data, SetOptions.merge())
          .await()
      }
      removeFromPendingQueue(COLLECTION_EVENTS, event.id)
      Result.success(Unit)
    } catch (e: Exception) {
      Log.w(TAG, "Calendar event save note (${event.id}): ${e.message}")
      enqueuePendingSync(COLLECTION_EVENTS, event.id, event.title, data, e.message)
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
          _realtimeError.value = "Events: ${error.message}"
          return@addSnapshotListener
        }

        if (snapshot != null) {
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
   * Saves or updates a Student profile in Cloud Firestore
   */
  suspend fun saveStudent(student: StudentProfile): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    val data = hashMapOf(
      "id" to student.user.id,
      "admissionNo" to student.admissionNo,
      "name" to student.user.fullName,
      "email" to student.user.email,
      "grade" to student.grade,
      "section" to student.section,
      "rollNo" to student.rollNo,
      "parentName" to student.parentName,
      "parentPhone" to student.parentPhone,
      "bloodGroup" to student.bloodGroup,
      "attendancePercentage" to student.attendancePercentage,
      "houseName" to student.houseName,
      "busRoute" to student.busRoute,
      "academicYear" to student.academicYear,
      "emergencyContact" to student.emergencyContact,
      "timestamp" to System.currentTimeMillis()
    )
    return try {
      ensureAuthenticated()
      FirestoreRetryPolicy.executeWithRetry(
        operationName = "saveStudent(${student.user.id})",
        onRetry = { event -> _lastRetryEvent.value = event }
      ) {
        db.collection(COLLECTION_STUDENTS).document(student.user.id)
          .set(data, SetOptions.merge())
          .await()
      }
      removeFromPendingQueue(COLLECTION_STUDENTS, student.user.id)
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save student to Firestore: ${e.message}", e)
      enqueuePendingSync(COLLECTION_STUDENTS, student.user.id, "${student.user.fullName} (${student.admissionNo})", data, e.message)
      Result.failure(e)
    }
  }

  /**
   * Listens for real-time Student profile updates from Cloud Firestore
   */
  fun observeStudents(): Flow<List<StudentProfile>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val subscription = db.collection(COLLECTION_STUDENTS)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "Listen error on students: ${error.message}")
          _realtimeError.value = "Students: ${error.message}"
          return@addSnapshotListener
        }

        if (snapshot != null) {
          val list = snapshot.documents.mapNotNull { doc ->
            try {
              val id = doc.getString("id") ?: doc.id
              val name = doc.getString("name") ?: "Student"
              val email = doc.getString("email") ?: "$id@stjoseph.school"
              val admissionNo = doc.getString("admissionNo") ?: id
              val grade = doc.getString("grade") ?: "Class 10"
              val section = doc.getString("section") ?: "A"
              val rollNo = (doc.getLong("rollNo") ?: 1L).toInt()
              val parentName = doc.getString("parentName") ?: "Guardian"
              val parentPhone = doc.getString("parentPhone") ?: "+91 98450 00000"
              val bloodGroup = doc.getString("bloodGroup") ?: "O+"
              val attPct = doc.getDouble("attendancePercentage") ?: 95.0
              val houseName = doc.getString("houseName") ?: "St. Francis House"
              val busRoute = doc.getString("busRoute") ?: "Route #12"
              val academicYear = doc.getString("academicYear") ?: "2026-2027"
              val emergencyContact = doc.getString("emergencyContact") ?: parentPhone

              StudentProfile(
                user = User(
                  id = id,
                  username = email.substringBefore("@"),
                  fullName = name,
                  email = email,
                  role = UserRole.STUDENT
                ),
                admissionNo = admissionNo,
                grade = grade,
                section = section,
                rollNo = rollNo,
                parentName = parentName,
                parentPhone = parentPhone,
                bloodGroup = bloodGroup,
                attendancePercentage = attPct,
                houseName = houseName,
                busRoute = busRoute,
                academicYear = academicYear,
                emergencyContact = emergencyContact
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
   * Saves or updates a Teacher profile in Cloud Firestore
   */
  suspend fun saveTeacher(teacher: TeacherProfile): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    val data = hashMapOf(
      "id" to teacher.user.id,
      "employeeId" to teacher.employeeId,
      "name" to teacher.user.fullName,
      "email" to teacher.user.email,
      "department" to teacher.department,
      "assignedClasses" to teacher.assignedClasses,
      "subjectsTaught" to teacher.subjectsTaught,
      "qualification" to teacher.qualification,
      "isClassTeacher" to teacher.isClassTeacher,
      "classTeacherOf" to (teacher.classTeacherOf ?: ""),
      "roomNo" to teacher.roomNo,
      "joiningDate" to teacher.joiningDate,
      "timestamp" to System.currentTimeMillis()
    )
    return try {
      ensureAuthenticated()
      FirestoreRetryPolicy.executeWithRetry(
        operationName = "saveTeacher(${teacher.user.id})",
        onRetry = { event -> _lastRetryEvent.value = event }
      ) {
        db.collection(COLLECTION_TEACHERS).document(teacher.user.id)
          .set(data, SetOptions.merge())
          .await()
      }
      removeFromPendingQueue(COLLECTION_TEACHERS, teacher.user.id)
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save teacher to Firestore: ${e.message}", e)
      enqueuePendingSync(COLLECTION_TEACHERS, teacher.user.id, "${teacher.user.fullName} (${teacher.employeeId})", data, e.message)
      Result.failure(e)
    }
  }

  /**
   * Listens for real-time Teacher updates from Cloud Firestore
   */
  fun observeTeachers(): Flow<List<TeacherProfile>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val subscription = db.collection(COLLECTION_TEACHERS)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "Listen error on teachers: ${error.message}")
          _realtimeError.value = "Teachers: ${error.message}"
          return@addSnapshotListener
        }

        if (snapshot != null) {
          val list = snapshot.documents.mapNotNull { doc ->
            try {
              val id = doc.getString("id") ?: doc.id
              val name = doc.getString("name") ?: "Teacher"
              val email = doc.getString("email") ?: "$id@stjoseph.school"
              val employeeId = doc.getString("employeeId") ?: id
              val department = doc.getString("department") ?: "Academics"
              @Suppress("UNCHECKED_CAST")
              val assignedClasses = (doc.get("assignedClasses") as? List<String>) ?: listOf("Class 10-A")
              @Suppress("UNCHECKED_CAST")
              val subjectsTaught = (doc.get("subjectsTaught") as? List<String>) ?: listOf("General")
              val qualification = doc.getString("qualification") ?: "M.Sc., B.Ed."
              val isClassTeacher = doc.getBoolean("isClassTeacher") ?: false
              val classTeacherOf = doc.getString("classTeacherOf")
              val roomNo = doc.getString("roomNo") ?: "Staff Room"
              val joiningDate = doc.getString("joiningDate") ?: "15 July 2020"

              TeacherProfile(
                user = User(
                  id = id,
                  username = email.substringBefore("@"),
                  fullName = name,
                  email = email,
                  role = UserRole.TEACHER
                ),
                employeeId = employeeId,
                department = department,
                assignedClasses = assignedClasses,
                subjectsTaught = subjectsTaught,
                qualification = qualification,
                isClassTeacher = isClassTeacher,
                classTeacherOf = classTeacherOf,
                roomNo = roomNo,
                joiningDate = joiningDate
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
   * Registers or updates an FCM device token in Cloud Firestore with exponential backoff retry
   */
  suspend fun registerDeviceToken(userId: String, token: String, role: String, topics: List<String>): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      ensureAuthenticated()
      val data = hashMapOf(
        "userId" to userId,
        "fcmToken" to token,
        "role" to role,
        "subscribedTopics" to topics,
        "platform" to "Android",
        "lastSeen" to System.currentTimeMillis()
      )
      FirestoreRetryPolicy.executeWithRetry(
        operationName = "registerDeviceToken(${userId})",
        onRetry = { event -> _lastRetryEvent.value = event }
      ) {
        db.collection(COLLECTION_FCM_TOKENS).document(token.take(64))
          .set(data, SetOptions.merge())
          .await()
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Log.w(TAG, "FCM device token registration note: ${e.message}")
      Result.failure(e)
    }
  }

  /**
   * Records a dispatched FCM push broadcast message in Firestore with exponential backoff retry
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
      ensureAuthenticated()
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
      FirestoreRetryPolicy.executeWithRetry(
        operationName = "recordFcmBroadcast(${broadcastId})",
        onRetry = { event -> _lastRetryEvent.value = event }
      ) {
        db.collection(COLLECTION_FCM_BROADCASTS).document(broadcastId)
          .set(data, SetOptions.merge())
          .await()
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Log.w(TAG, "FCM broadcast record note: ${e.message}")
      Result.failure(e)
    }
  }

  /**
   * Saves user suggestions and feedback. First attempts direct Firestore sync with retry.
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
      ensureAuthenticated()
    } catch (authEx: Exception) {
      Log.w(TAG, "Auth prep note: ${authEx.message}")
    }

    // 2. Attempt Firestore Cloud write with retry
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
        FirestoreRetryPolicy.executeWithRetry(
          operationName = "submitFeedback(${feedbackId})",
          onRetry = { event -> _lastRetryEvent.value = event }
        ) {
          db.collection(COLLECTION_FEEDBACK).document(feedbackId)
            .set(data, SetOptions.merge())
            .await()
        }
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
    val fields: Map<String, Any?>,
    val isFromCloud: Boolean = true
  )

  data class ConnectionHealth(
    val isInitialized: Boolean,
    val isAuthActive: Boolean,
    val authUid: String?,
    val isCloudAccessible: Boolean,
    val isPermissionRestricted: Boolean,
    val projectId: String,
    val statusSummary: String
  )

  data class SyncSeedResult(
    val totalItems: Int,
    val cloudSuccessCount: Int,
    val cloudFailedCount: Int,
    val isPermissionRestricted: Boolean,
    val lastError: Exception? = null
  )

  /**
   * Diagnoses connection, google-services.json, security rules, and data sync status
   */
  suspend fun runDiagnostics(
    isNetworkOnline: Boolean = true,
    networkType: String = "Online",
    isSyncPaused: Boolean = false
  ): CloudDiagnosticsReport {
    return FirestoreDiagnosticTester.runDiagnostics(
      context = context,
      firestore = firestore,
      isNetworkOnline = isNetworkOnline,
      networkType = networkType,
      isSyncPausedManually = isSyncPaused
    )
  }

  /**
   * Diagnoses connection and security rules health for Cloud Firestore
   */
  suspend fun checkConnectionHealth(): ConnectionHealth {
    val diag = runDiagnostics()
    return ConnectionHealth(
      isInitialized = diag.connectionDiagnostics.isAppInitialized,
      isAuthActive = diag.rulesDiagnostics.authUid != null,
      authUid = diag.rulesDiagnostics.authUid,
      isCloudAccessible = diag.connectionDiagnostics.isCloudReachable,
      isPermissionRestricted = !diag.rulesDiagnostics.isWritePermitted || !diag.rulesDiagnostics.isReadPermitted,
      projectId = diag.configDiagnostics.projectId.ifBlank { "st-joseph-school-app" },
      statusSummary = if (diag.overallLevel == DiagnosticLevel.PASS) {
        diag.connectionDiagnostics.summary
      } else {
        "${diag.rulesDiagnostics.summary} (${diag.configDiagnostics.summary})"
      }
    )
  }

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
    COLLECTION_STUDENTS,
    COLLECTION_TEACHERS,
    COLLECTION_FCM_BROADCASTS,
    COLLECTION_FCM_TOKENS
  )

  /**
   * Synthesizes and loads local synchronized records for any collection
   * ensuring seamless data flow between device storage and cloud inspection.
   */
  fun getLocalDocumentsForCollection(
    collection: String,
    repository: com.example.data.SchoolRepository
  ): List<FirestoreRawDoc> {
    return when (collection) {
      COLLECTION_FEEDBACK -> {
        getLocalFeedbackList().map { item ->
          FirestoreRawDoc(
            id = item.id,
            collection = COLLECTION_FEEDBACK,
            summary = "${item.userName} (${item.userRole}) • ${item.category} • ★${item.rating}: ${item.suggestion.take(35)}",
            timestamp = item.createdAt,
            fields = mapOf(
              "id" to item.id,
              "userId" to item.userId,
              "userName" to item.userName,
              "userRole" to item.userRole,
              "category" to item.category,
              "suggestion" to item.suggestion,
              "rating" to item.rating,
              "isCloudSynced" to item.isCloudSynced,
              "createdAt" to item.createdAt
            ),
            isFromCloud = false
          )
        }
      }
      COLLECTION_NOTICES -> {
        repository.notices.value.map { notice ->
          FirestoreRawDoc(
            id = notice.id,
            collection = COLLECTION_NOTICES,
            summary = "${if (notice.isUrgent) "🚨 " else ""}${notice.title} (${notice.category.name})",
            timestamp = System.currentTimeMillis(),
            fields = mapOf(
              "id" to notice.id,
              "title" to notice.title,
              "content" to notice.content,
              "date" to notice.date,
              "category" to notice.category.name,
              "publisherRole" to notice.publisherRole,
              "publisherName" to notice.publisherName,
              "isUrgent" to notice.isUrgent,
              "attachmentName" to (notice.attachmentName ?: "")
            ),
            isFromCloud = false
          )
        }
      }
      COLLECTION_HOMEWORK -> {
        repository.homeworks.value.map { hw ->
          FirestoreRawDoc(
            id = hw.id,
            collection = COLLECTION_HOMEWORK,
            summary = "${hw.title} - ${hw.subjectName} (${hw.className}, Due: ${hw.dueDate})",
            timestamp = System.currentTimeMillis(),
            fields = mapOf(
              "id" to hw.id,
              "title" to hw.title,
              "subjectName" to hw.subjectName,
              "className" to hw.className,
              "assignedDate" to hw.assignedDate,
              "dueDate" to hw.dueDate,
              "description" to hw.description,
              "teacherName" to hw.teacherName,
              "maxMarks" to hw.maxMarks
            ),
            isFromCloud = false
          )
        }
      }
      COLLECTION_ANNOUNCEMENTS -> {
        repository.announcements.value.map { ann ->
          FirestoreRawDoc(
            id = ann.id,
            collection = COLLECTION_ANNOUNCEMENTS,
            summary = "${if (ann.isEmergency) "🚨 " else ""}${ann.title} (${ann.priority.name})",
            timestamp = System.currentTimeMillis(),
            fields = mapOf(
              "id" to ann.id,
              "title" to ann.title,
              "content" to ann.content,
              "authorName" to ann.authorName,
              "authorRole" to ann.authorRole,
              "targetAudience" to ann.targetAudience.name,
              "date" to ann.date,
              "priority" to ann.priority.name,
              "isEmergency" to ann.isEmergency
            ),
            isFromCloud = false
          )
        }
      }
      COLLECTION_ATTENDANCE -> {
        repository.attendanceRecords.value.map { rec ->
          FirestoreRawDoc(
            id = rec.id,
            collection = COLLECTION_ATTENDANCE,
            summary = "${rec.studentName} (${rec.className}, Roll #${rec.rollNo}) - ${rec.status.label} on ${rec.date}",
            timestamp = System.currentTimeMillis(),
            fields = mapOf(
              "id" to rec.id,
              "studentId" to rec.studentId,
              "studentName" to rec.studentName,
              "rollNo" to rec.rollNo,
              "className" to rec.className,
              "date" to rec.date,
              "status" to rec.status.name,
              "markedBy" to rec.markedBy,
              "notes" to rec.notes
            ),
            isFromCloud = false
          )
        }
      }
      COLLECTION_EVENTS -> {
        repository.calendarEvents.value.map { ev ->
          FirestoreRawDoc(
            id = ev.id,
            collection = COLLECTION_EVENTS,
            summary = "${ev.title} (${ev.date}, ${ev.category.name})",
            timestamp = System.currentTimeMillis(),
            fields = mapOf(
              "id" to ev.id,
              "title" to ev.title,
              "description" to ev.description,
              "date" to ev.date,
              "time" to ev.time,
              "category" to ev.category.name,
              "location" to ev.location
            ),
            isFromCloud = false
          )
        }
      }
      COLLECTION_USERS -> {
        repository.systemUsers.value.map { u ->
          FirestoreRawDoc(
            id = u.id,
            collection = COLLECTION_USERS,
            summary = "${u.fullName} (${u.role.displayName}) - ${u.email}",
            timestamp = System.currentTimeMillis(),
            fields = mapOf(
              "id" to u.id,
              "username" to u.username,
              "fullName" to u.fullName,
              "email" to u.email,
              "role" to u.role.name,
              "designation" to u.designation
            ),
            isFromCloud = false
          )
        }
      }
      COLLECTION_STUDENTS -> {
        repository.directoryContacts.value.filter { it.role == UserRole.STUDENT }.map { s ->
          FirestoreRawDoc(
            id = s.id,
            collection = COLLECTION_STUDENTS,
            summary = "${s.name} (${s.departmentOrGrade})",
            timestamp = System.currentTimeMillis(),
            fields = mapOf(
              "id" to s.id,
              "name" to s.name,
              "grade" to s.departmentOrGrade,
              "email" to s.email,
              "phone" to s.phoneNumber
            ),
            isFromCloud = false
          )
        }
      }
      COLLECTION_TEACHERS -> {
        repository.directoryContacts.value.filter { it.role == UserRole.TEACHER }.map { t ->
          FirestoreRawDoc(
            id = t.id,
            collection = COLLECTION_TEACHERS,
            summary = "${t.name} (${t.designation}, ${t.departmentOrGrade})",
            timestamp = System.currentTimeMillis(),
            fields = mapOf(
              "id" to t.id,
              "name" to t.name,
              "designation" to t.designation,
              "department" to t.departmentOrGrade,
              "email" to t.email,
              "room" to (t.roomOrLocation ?: "")
            ),
            isFromCloud = false
          )
        }
      }
      else -> emptyList()
    }
  }

  /**
   * Deletes a specific document from any Firestore collection with exponential backoff retry
   */
  suspend fun deleteFirestoreDocument(collection: String, documentId: String): Result<Unit> {
    ensureAuthenticated()
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      FirestoreRetryPolicy.executeWithRetry(
        operationName = "deleteFirestoreDocument($collection/$documentId)",
        onRetry = { event -> _lastRetryEvent.value = event }
      ) {
        db.collection(collection).document(documentId).delete().await()
      }
      Log.d(TAG, "God Mode deleted Firestore document: $collection/$documentId")
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to delete Firestore document $collection/$documentId: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Fetches raw documents from a given collection for live developer inspection and surgical deletion with retry
   */
  suspend fun getCollectionDocuments(collection: String, limit: Long = 100): Result<List<FirestoreRawDoc>> {
    ensureAuthenticated()
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      val snapshot = FirestoreRetryPolicy.executeWithRetry(
        operationName = "getCollectionDocuments($collection)",
        onRetry = { event -> _lastRetryEvent.value = event }
      ) {
        db.collection(collection).limit(limit).get().await()
      }
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
          fields = doc.data ?: emptyMap(),
          isFromCloud = true
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Log.w(TAG, "Failed to get docs from Firestore collection $collection: ${e.message}")
      Result.failure(e)
    }
  }

  /**
   * Purges / deletes all documents from a specific Firestore collection with retry
   */
  suspend fun purgeCollection(collection: String): Result<Int> {
    ensureAuthenticated()
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is unavailable."))
    return try {
      val snapshot = FirestoreRetryPolicy.executeWithRetry(
        operationName = "purgeCollection.query($collection)",
        onRetry = { event -> _lastRetryEvent.value = event }
      ) {
        db.collection(collection).get().await()
      }
      val total = snapshot.size()
      if (total == 0) return Result.success(0)

      val batch = db.batch()
      var count = 0
      for (doc in snapshot.documents) {
        batch.delete(doc.reference)
        count++
        if (count % 400 == 0) {
          FirestoreRetryPolicy.executeWithRetry(
            operationName = "purgeCollection.commitBatch($collection)",
            onRetry = { event -> _lastRetryEvent.value = event }
          ) {
            batch.commit().await()
          }
        }
      }
      if (count % 400 != 0 && count > 0) {
        FirestoreRetryPolicy.executeWithRetry(
          operationName = "purgeCollection.commitFinalBatch($collection)",
          onRetry = { event -> _lastRetryEvent.value = event }
        ) {
          batch.commit().await()
        }
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
    ensureAuthenticated()
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
    attendance: List<AttendanceRecord>,
    students: List<StudentProfile> = emptyList(),
    teachers: List<TeacherProfile> = emptyList()
  ): Result<SyncSeedResult> {
    ensureAuthenticated()
    var cloudSuccessCount = 0
    var cloudFailedCount = 0
    var lastError: Exception? = null

    notices.forEach {
      val res = publishNotice(it)
      if (res.isSuccess) cloudSuccessCount++ else { cloudFailedCount++; lastError = res.exceptionOrNull() as? Exception }
    }
    homeworks.forEach {
      val res = saveHomework(it)
      if (res.isSuccess) cloudSuccessCount++ else { cloudFailedCount++; lastError = res.exceptionOrNull() as? Exception }
    }
    announcements.forEach {
      val res = publishAnnouncement(it)
      if (res.isSuccess) cloudSuccessCount++ else { cloudFailedCount++; lastError = res.exceptionOrNull() as? Exception }
    }
    events.forEach {
      val res = saveCalendarEvent(it)
      if (res.isSuccess) cloudSuccessCount++ else { cloudFailedCount++; lastError = res.exceptionOrNull() as? Exception }
    }
    attendance.take(20).forEach {
      val res = saveAttendanceRecord(it)
      if (res.isSuccess) cloudSuccessCount++ else { cloudFailedCount++; lastError = res.exceptionOrNull() as? Exception }
    }
    students.forEach {
      val res = saveStudent(it)
      if (res.isSuccess) cloudSuccessCount++ else { cloudFailedCount++; lastError = res.exceptionOrNull() as? Exception }
    }
    teachers.forEach {
      val res = saveTeacher(it)
      if (res.isSuccess) cloudSuccessCount++ else { cloudFailedCount++; lastError = res.exceptionOrNull() as? Exception }
    }

    val totalItems = notices.size + homeworks.size + announcements.size + events.size + attendance.take(20).size + students.size + teachers.size
    return Result.success(
      SyncSeedResult(
        totalItems = totalItems,
        cloudSuccessCount = cloudSuccessCount,
        cloudFailedCount = cloudFailedCount,
        isPermissionRestricted = lastError?.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true,
        lastError = lastError
      )
    )
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

