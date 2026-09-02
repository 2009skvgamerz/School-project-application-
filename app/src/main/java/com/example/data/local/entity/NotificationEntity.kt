package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.NotificationType

/**
 * Room Database Entity for local offline persistence of received FCM push messages,
 * announcements, and school alerts.
 */
@Entity(tableName = "received_notifications")
data class NotificationEntity(
  @PrimaryKey
  val id: String,
  val title: String,
  val message: String,
  val timeAgo: String,
  val timestamp: Long = System.currentTimeMillis(),
  val type: NotificationType = NotificationType.ACADEMIC,
  val isRead: Boolean = false,
  val actionRoute: String? = null,
  val targetId: String? = null,
  val isUrgent: Boolean = false,
  val channelId: String? = null
)
