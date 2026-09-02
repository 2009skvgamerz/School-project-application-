package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for querying and persisting received notifications
 * and announcements in the local Room SQLite database for offline viewing.
 */
@Dao
interface NotificationDao {

  @Query("SELECT * FROM received_notifications ORDER BY timestamp DESC")
  fun getAllNotifications(): Flow<List<NotificationEntity>>

  @Query("SELECT * FROM received_notifications WHERE isRead = 0 ORDER BY timestamp DESC")
  fun getUnreadNotifications(): Flow<List<NotificationEntity>>

  @Query("SELECT COUNT(*) FROM received_notifications WHERE isRead = 0")
  fun getUnreadCount(): Flow<Int>

  @Query("SELECT * FROM received_notifications WHERE id = :id LIMIT 1")
  suspend fun getNotificationById(id: String): NotificationEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: NotificationEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotifications(notifications: List<NotificationEntity>)

  @Update
  suspend fun updateNotification(notification: NotificationEntity)

  @Query("UPDATE received_notifications SET isRead = 1 WHERE id = :id")
  suspend fun markAsRead(id: String)

  @Query("UPDATE received_notifications SET isRead = 1")
  suspend fun markAllAsRead()

  @Query("DELETE FROM received_notifications WHERE id = :id")
  suspend fun deleteNotification(id: String)

  @Query("DELETE FROM received_notifications")
  suspend fun clearAllNotifications()
}
