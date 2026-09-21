package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

import com.example.service.SchoolFirebaseMessagingService
import com.example.util.BackgroundSyncManager
import com.example.util.SchoolBackgroundScheduler
import com.example.util.SystemNotificationHelper
import org.osmdroid.config.Configuration

class SchoolApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize OpenStreetMap (osmdroid) configuration
        try {
            val sharedPrefs = getSharedPreferences("osmdroid_prefs", android.content.Context.MODE_PRIVATE)
            Configuration.getInstance().load(this, sharedPrefs)
            Configuration.getInstance().userAgentValue = packageName
        } catch (e: Exception) {
            // Fallback user agent
            try {
                Configuration.getInstance().userAgentValue = "StJosephSchoolHosur/1.0"
            } catch (_: Exception) {}
        }

        SystemNotificationHelper.createNotificationChannels(this)
        initFirebase()
        BackgroundSyncManager.initialize(this)
        SchoolBackgroundScheduler.cancelBackgroundAlerts(this)
    }

    private fun initFirebase() {
        try {
            FirebaseApp.initializeApp(this)

            if (FirebaseApp.getApps(this).isNotEmpty()) {
                // Prevent FirebaseMessaging from automatic background registration that causes hard failure on emulators
                try {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().isAutoInitEnabled = false
                } catch (e: Exception) {
                    Log.w(TAG, "Notice setting FCM auto-init: ${e.message}")
                }

                // Enable Firestore offline persistence for smooth local caching & background sync
                try {
                    FirebaseFirestore.getInstance().apply {
                        firestoreSettings = FirebaseFirestoreSettings.Builder()
                            .setPersistenceEnabled(true)
                            .build()
                    }
                    Log.d(TAG, "Firebase Firestore initialized with persistence enabled")
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore settings configuration notice: ${e.message}")
                }

                // Initialize default push notification topics locally
                SchoolFirebaseMessagingService.initDefaultTopicsLocally(this)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase / AppCheck / FCM: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "SchoolApplication"
        lateinit var instance: SchoolApplication
            private set
    }
}
