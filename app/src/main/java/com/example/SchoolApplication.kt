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
import com.example.util.SystemNotificationHelper

class SchoolApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SystemNotificationHelper.createNotificationChannel(this)
        initFirebase()
    }

    private fun initFirebase() {
        try {
            FirebaseApp.initializeApp(this)

            if (FirebaseApp.getApps(this).isNotEmpty()) {
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

                // Subscribe to Firebase Cloud Messaging (FCM) push notification topics
                SchoolFirebaseMessagingService.subscribeToDefaultTopics()
                SchoolFirebaseMessagingService.fetchFcmToken { token ->
                    Log.d(TAG, "Initial FCM Device Token: $token")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase / AppCheck / FCM: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "SchoolApplication"
    }
}
