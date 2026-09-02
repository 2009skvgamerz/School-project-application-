package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.util.BackgroundSyncManager

/**
 * Receiver triggered on system boot, power on, or app update.
 * Automatically activates WorkManager background sync so notifications will fire
 * whenever internet reconnects, even if the app UI is never opened.
 */
class BootReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    Log.d(TAG, "BootReceiver received action: ${intent?.action}. Starting background sync and continuous alert schedulers...")
    try {
      com.example.util.SchoolBackgroundScheduler.startContinuousBackgroundAlerts(context)
      BackgroundSyncManager.schedulePeriodicBackgroundSync(context)
      BackgroundSyncManager.enqueueImmediateSyncOnConnectivity(context)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start background schedulers on boot: ${e.message}")
    }
  }

  companion object {
    private const val TAG = "BootReceiver"
  }
}
