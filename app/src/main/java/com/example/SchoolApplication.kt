package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class SchoolApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }

    private fun initFirebase() {
        try {
            FirebaseApp.initializeApp(this)

            if (FirebaseApp.getApps(this).isNotEmpty()) {
                val firebaseAppCheck = FirebaseAppCheck.getInstance()
                if (BuildConfig.DEBUG) {
                    firebaseAppCheck.installAppCheckProviderFactory(
                        DebugAppCheckProviderFactory.getInstance()
                    )
                    Log.d(TAG, "Firebase AppCheck initialized with DebugAppCheckProviderFactory")
                } else {
                    firebaseAppCheck.installAppCheckProviderFactory(
                        PlayIntegrityAppCheckProviderFactory.getInstance()
                    )
                    Log.d(TAG, "Firebase AppCheck initialized with PlayIntegrityAppCheckProviderFactory")
                }

                FirebaseFirestore.getInstance().apply {
                    firestoreSettings = FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(false)
                        .build()
                }
                Log.d(TAG, "Firebase Firestore initialized with direct cloud synchronization")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase / AppCheck: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "SchoolApplication"
    }
}
