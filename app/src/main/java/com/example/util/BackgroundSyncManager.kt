package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worker.SchoolBackgroundSyncWorker
import java.util.concurrent.TimeUnit

/**
 * Manages background network synchronization to provide WhatsApp-like notification delivery:
 * When the app is completely closed or killed, as soon as the phone connects to the internet (or periodically),
 * it queries the school cloud database and fires high-priority Heads-Up system notifications immediately!
 */
object BackgroundSyncManager {

  private const val TAG = "BackgroundSyncManager"
  private const val PERIODIC_WORK_TAG = "school_periodic_background_sync"
  private const val IMMEDIATE_WORK_TAG = "school_immediate_connectivity_sync"

  private var isNetworkCallbackRegistered = false

  /**
   * Initializes both periodic background synchronization (every 15 min with NetworkType.CONNECTED)
   * and live connectivity listener so that as soon as the device gains internet, it checks for notices.
   */
  fun initialize(context: Context) {
    try {
      schedulePeriodicBackgroundSync(context)
      registerLiveConnectivityListener(context)
      enqueueImmediateSyncOnConnectivity(context)
      Log.d(TAG, "BackgroundSyncManager initialized: background notifications ready on internet connect.")
    } catch (e: Exception) {
      Log.e(TAG, "Error initializing BackgroundSyncManager: ${e.message}")
    }
  }

  /**
   * Schedules a battery-friendly PeriodicWorkRequest that runs in the background
   * as long as the device is connected to the internet, even if the app process is terminated.
   */
  fun schedulePeriodicBackgroundSync(context: Context) {
    try {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val periodicRequest = PeriodicWorkRequestBuilder<SchoolBackgroundSyncWorker>(
        15, TimeUnit.MINUTES,
        5, TimeUnit.MINUTES // Flex interval
      )
        .setConstraints(constraints)
        .addTag(PERIODIC_WORK_TAG)
        .build()

      WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        PERIODIC_WORK_TAG,
        ExistingPeriodicWorkPolicy.KEEP,
        periodicRequest
      )

      Log.d(TAG, "Scheduled periodic background sync (every 15 min when connected to internet)")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to schedule periodic background sync: ${e.message}")
    }
  }

  /**
   * Enqueues an immediate one-time sync whenever internet is connected.
   */
  fun enqueueImmediateSyncOnConnectivity(context: Context) {
    try {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val oneTimeRequest = OneTimeWorkRequestBuilder<SchoolBackgroundSyncWorker>()
        .setConstraints(constraints)
        .addTag(IMMEDIATE_WORK_TAG)
        .build()

      WorkManager.getInstance(context).enqueueUniqueWork(
        IMMEDIATE_WORK_TAG,
        ExistingWorkPolicy.REPLACE,
        oneTimeRequest
      )

      Log.d(TAG, "Enqueued immediate connectivity sync with WorkManager.")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to enqueue immediate connectivity sync: ${e.message}")
    }
  }

  /**
   * Registers a network callback with the Android OS ConnectivityManager.
   * Whenever internet connectivity is regained (e.g. WiFi turned on, Cellular enabled),
   * this immediately triggers the background worker to fetch any pending school notices!
   */
  private fun registerLiveConnectivityListener(context: Context) {
    if (isNetworkCallbackRegistered) return

    try {
      val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
      if (connectivityManager != null) {
        val networkRequest = NetworkRequest.Builder()
          .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
          .build()

        connectivityManager.registerNetworkCallback(
          networkRequest,
          object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
              super.onAvailable(network)
              Log.d(TAG, "Internet connection detected on device! Dispatching immediate background sync...")
              enqueueImmediateSyncOnConnectivity(context)
            }

            override fun onLost(network: Network) {
              super.onLost(network)
              Log.d(TAG, "Internet connection lost.")
            }
          }
        )
        isNetworkCallbackRegistered = true
        Log.d(TAG, "Registered system ConnectivityManager NetworkCallback for instant internet trigger.")
      }
    } catch (e: Exception) {
      Log.w(TAG, "Note registering NetworkCallback: ${e.message}")
    }
  }

  /**
   * Manually trigger a background sync check right now.
   */
  fun triggerManualSync(context: Context) {
    try {
      val oneTimeRequest = OneTimeWorkRequestBuilder<SchoolBackgroundSyncWorker>()
        .addTag("school_manual_bg_sync")
        .build()

      WorkManager.getInstance(context).enqueue(oneTimeRequest)
      Log.d(TAG, "Manual background sync triggered.")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to trigger manual background sync: ${e.message}")
    }
  }
}
