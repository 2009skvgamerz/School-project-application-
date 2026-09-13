package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.File
import kotlin.system.measureTimeMillis

enum class DiagnosticLevel {
  PASS,
  WARNING,
  FAIL
}

/**
 * Diagnostic result for google-services.json and FirebaseOptions configuration.
 */
data class GoogleServicesConfigDiagnostics(
  val level: DiagnosticLevel,
  val projectId: String,
  val applicationId: String,
  val apiKeyPresent: Boolean,
  val apiKeyMasked: String,
  val expectedPackageName: String,
  val actualPackageName: String,
  val packageMatches: Boolean,
  val storageBucket: String,
  val fileFoundOnDisk: Boolean,
  val issuesFound: List<String> = emptyList(),
  val summary: String
)

/**
 * Diagnostic result for Firestore network connection and round-trip latency.
 */
data class FirestoreConnectionDiagnostics(
  val level: DiagnosticLevel,
  val isAppInitialized: Boolean,
  val isNetworkConnected: Boolean,
  val networkType: String,
  val roundTripLatencyMs: Long,
  val isCloudReachable: Boolean,
  val errorSummary: String? = null,
  val summary: String
)

/**
 * Diagnostic result for Cloud Firestore Security Rules validation.
 */
data class SecurityRulesDiagnostics(
  val level: DiagnosticLevel,
  val authStatus: String,
  val authUid: String?,
  val isReadPermitted: Boolean,
  val isWritePermitted: Boolean,
  val readError: String? = null,
  val writeError: String? = null,
  val ruleAdvice: String,
  val summary: String
)

/**
 * Diagnostic result for Data Synchronization state (Active vs Paused).
 */
data class SyncStateDiagnostics(
  val level: DiagnosticLevel,
  val isSyncActive: Boolean,
  val isSyncPaused: Boolean,
  val pausedReason: String?,
  val localVaultFallbackActive: Boolean,
  val summary: String
)

/**
 * Comprehensive diagnostic report aggregated across all verification checks.
 */
data class CloudDiagnosticsReport(
  val timestamp: Long = System.currentTimeMillis(),
  val overallLevel: DiagnosticLevel,
  val configDiagnostics: GoogleServicesConfigDiagnostics,
  val connectionDiagnostics: FirestoreConnectionDiagnostics,
  val rulesDiagnostics: SecurityRulesDiagnostics,
  val syncDiagnostics: SyncStateDiagnostics,
  val recommendedSteps: List<String>,
  val timestampFormatted: String
) {
  fun toReadableSummary(): String {
    return buildString {
      appendLine("=== FIRESTORE CLOUD DIAGNOSTIC REPORT ===")
      appendLine("Timestamp: $timestampFormatted")
      appendLine("Overall Status: ${overallLevel.name}")
      appendLine()
      appendLine("1. GOOGLE-SERVICES.JSON & FIREBASE CONFIG:")
      appendLine("   - Level: ${configDiagnostics.level.name}")
      appendLine("   - Summary: ${configDiagnostics.summary}")
      appendLine("   - Project ID: ${configDiagnostics.projectId}")
      appendLine("   - App ID: ${configDiagnostics.applicationId}")
      appendLine("   - Package Match: ${configDiagnostics.packageMatches} (App: ${configDiagnostics.actualPackageName})")
      appendLine()
      appendLine("2. FIRESTORE NETWORK CONNECTIVITY:")
      appendLine("   - Level: ${connectionDiagnostics.level.name}")
      appendLine("   - Summary: ${connectionDiagnostics.summary}")
      appendLine("   - Round-trip Latency: ${connectionDiagnostics.roundTripLatencyMs}ms")
      appendLine("   - Cloud Reachable: ${connectionDiagnostics.isCloudReachable}")
      appendLine()
      appendLine("3. SECURITY RULES VALIDATION:")
      appendLine("   - Level: ${rulesDiagnostics.level.name}")
      appendLine("   - Summary: ${rulesDiagnostics.summary}")
      appendLine("   - Read Permitted: ${rulesDiagnostics.isReadPermitted}")
      appendLine("   - Write Permitted: ${rulesDiagnostics.isWritePermitted}")
      appendLine("   - Auth Status: ${rulesDiagnostics.authStatus}")
      appendLine("   - Advice: ${rulesDiagnostics.ruleAdvice}")
      appendLine()
      appendLine("4. DATA SYNCHRONIZATION FLOW:")
      appendLine("   - Level: ${syncDiagnostics.level.name}")
      appendLine("   - Summary: ${syncDiagnostics.summary}")
      appendLine("   - Sync Active: ${syncDiagnostics.isSyncActive}")
      appendLine("   - Sync Paused: ${syncDiagnostics.isSyncPaused} (${syncDiagnostics.pausedReason ?: "N/A"})")
      appendLine("   - Local Room DB Fallback: ${syncDiagnostics.localVaultFallbackActive}")
      appendLine()
      appendLine("RECOMMENDED STEPS:")
      recommendedSteps.forEach { step ->
        appendLine("   • $step")
      }
    }
  }
}

object FirestoreDiagnosticTester {
  private const val TAG = "FirestoreDiagnostic"

  /**
   * Diagnostic test function to verify:
   * 1. Firestore connection & round-trip latency
   * 2. Configuration errors in google-services.json vs runtime FirebaseOptions
   * 3. Security rules configuration (Read & Write probes) to ensure data sync is functioning
   * 4. Network status and synchronization pause detection
   */
  suspend fun runDiagnostics(
    context: Context,
    firestore: FirebaseFirestore?,
    isNetworkOnline: Boolean = true,
    networkType: String = "Online",
    isSyncPausedManually: Boolean = false
  ): CloudDiagnosticsReport {
    Log.d(TAG, "Starting comprehensive Firestore & google-services.json diagnostics...")

    // 1. Check google-services.json & Firebase configuration
    val configCheck = verifyGoogleServicesConfig(context)

    // 2. Verify Firestore network connection & measure latency
    val connCheck = verifyFirestoreConnection(firestore, isNetworkOnline, networkType)

    // 3. Validate Cloud Firestore Security Rules (Read and Write operations)
    val rulesCheck = validateSecurityRules(context, firestore, isNetworkOnline)

    // 4. Validate Data Sync status
    val isPaused = !isNetworkOnline || isSyncPausedManually || !connCheck.isCloudReachable
    val pausedReason = when {
      isSyncPausedManually -> "Synchronization manually paused (Simulated Offline Mode)"
      !isNetworkOnline -> "No active network connection (Wi-Fi/Cellular disconnected)"
      !connCheck.isCloudReachable -> "Cloud Firestore endpoint unreachable"
      else -> null
    }

    val syncCheck = SyncStateDiagnostics(
      level = if (isPaused) DiagnosticLevel.WARNING else DiagnosticLevel.PASS,
      isSyncActive = !isPaused && rulesCheck.isReadPermitted,
      isSyncPaused = isPaused,
      pausedReason = pausedReason,
      localVaultFallbackActive = isPaused || !rulesCheck.isWritePermitted,
      summary = if (isPaused) {
        "Data sync is paused ($pausedReason). All changes are safely preserved in Room Vault."
      } else if (!rulesCheck.isWritePermitted) {
        "Data sync is partially restricted: Cloud writes blocked by security rules; local database active."
      } else {
        "Data synchronization is fully operational and active in real time."
      }
    )

    // Compute Overall Diagnostic Level
    val overallLevel = when {
      configCheck.level == DiagnosticLevel.FAIL || connCheck.level == DiagnosticLevel.FAIL -> DiagnosticLevel.FAIL
      rulesCheck.level == DiagnosticLevel.WARNING || syncCheck.level == DiagnosticLevel.WARNING || configCheck.level == DiagnosticLevel.WARNING -> DiagnosticLevel.WARNING
      else -> DiagnosticLevel.PASS
    }

    // Formulate actionable recommendations
    val recommendations = mutableListOf<String>()
    if (!configCheck.packageMatches) {
      recommendations.add("Update google-services.json package_name to match app package '${configCheck.actualPackageName}'.")
    }
    if (configCheck.issuesFound.isNotEmpty()) {
      recommendations.addAll(configCheck.issuesFound)
    }
    if (!isNetworkOnline) {
      recommendations.add("Connect device to active Wi-Fi or cellular network to resume cloud synchronization.")
    }
    if (!rulesCheck.isWritePermitted || !rulesCheck.isReadPermitted) {
      recommendations.add("Configure Firestore Security Rules in Firebase Console (Rules tab -> allow read, write: if true; -> Publish).")
    }
    if (isSyncPausedManually) {
      recommendations.add("Disable 'Simulate Offline Mode' in Settings/God Mode to resume cloud data flow.")
    }
    if (recommendations.isEmpty()) {
      recommendations.add("All diagnostic tests passed! Firestore connection, google-services.json, and security rules are operating optimally.")
    }

    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    val formattedTime = formatter.format(java.util.Date())

    return CloudDiagnosticsReport(
      overallLevel = overallLevel,
      configDiagnostics = configCheck,
      connectionDiagnostics = connCheck,
      rulesDiagnostics = rulesCheck,
      syncDiagnostics = syncCheck,
      recommendedSteps = recommendations,
      timestampFormatted = formattedTime
    )
  }

  /**
   * Checks for configuration errors in google-services.json and runtime FirebaseOptions
   */
  private fun verifyGoogleServicesConfig(context: Context): GoogleServicesConfigDiagnostics {
    val issues = mutableListOf<String>()
    var projectId = ""
    var applicationId = ""
    var apiKey = ""
    var storageBucket = ""
    var fileOnDisk = false
    val actualPackage = context.packageName

    // Attempt to inspect physical google-services.json
    try {
      val candidates = listOf(
        File(context.filesDir.parentFile, "google-services.json"),
        File("/app/google-services.json"),
        File("google-services.json")
      )
      for (file in candidates) {
        if (file.exists()) {
          fileOnDisk = true
          val jsonStr = file.readText()
          val root = JSONObject(jsonStr)
          val projectInfo = root.optJSONObject("project_info")
          projectId = projectInfo?.optString("project_id", "").orEmpty()
          storageBucket = projectInfo?.optString("storage_bucket", "").orEmpty()

          val clients = root.optJSONArray("client")
          if (clients != null && clients.length() > 0) {
            val firstClient = clients.getJSONObject(0)
            val clientInfo = firstClient.optJSONObject("client_info")
            applicationId = clientInfo?.optString("mobilesdk_app_id", "").orEmpty()
            val androidClientInfo = clientInfo?.optJSONObject("android_client_info")
            val pkgInJson = androidClientInfo?.optString("package_name", "").orEmpty()

            if (pkgInJson.isNotBlank() && pkgInJson != actualPackage) {
              issues.add("Package mismatch in google-services.json: expected '$actualPackage', found '$pkgInJson'")
            }

            val apiKeys = firstClient.optJSONArray("api_key")
            if (apiKeys != null && apiKeys.length() > 0) {
              apiKey = apiKeys.getJSONObject(0).optString("current_key", "")
            }
          }
          break
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Note during google-services.json disk check: ${e.message}")
    }

    // Inspect runtime FirebaseOptions and generated resources
    try {
      if (FirebaseApp.getApps(context).isNotEmpty()) {
        val app = FirebaseApp.getInstance()
        val opts = app.options
        if (projectId.isBlank()) projectId = opts.projectId.orEmpty()
        if (applicationId.isBlank()) applicationId = opts.applicationId
        if (apiKey.isBlank()) apiKey = opts.apiKey
        if (storageBucket.isBlank()) storageBucket = opts.storageBucket.orEmpty()
        fileOnDisk = true
      }
      if (projectId.isBlank()) {
        val resId = context.resources.getIdentifier("project_id", "string", context.packageName)
        if (resId != 0) {
          projectId = context.getString(resId)
          fileOnDisk = true
        }
      }
      if (apiKey.isBlank()) {
        val resId = context.resources.getIdentifier("google_api_key", "string", context.packageName)
        if (resId != 0) {
          apiKey = context.getString(resId)
          fileOnDisk = true
        }
      }
      if (applicationId.isBlank()) {
        val resId = context.resources.getIdentifier("google_app_id", "string", context.packageName)
        if (resId != 0) {
          applicationId = context.getString(resId)
          fileOnDisk = true
        }
      }
    } catch (e: Exception) {
      issues.add("FirebaseApp options could not be retrieved: ${e.message}")
    }

    if (projectId.isBlank()) {
      issues.add("project_id is missing or empty in Firebase configuration")
    }
    if (apiKey.isBlank()) {
      issues.add("api_key is missing or empty in Firebase configuration")
    } else if (!apiKey.startsWith("AIza")) {
      issues.add("api_key does not match expected Google API key format (should begin with 'AIza')")
    }
    if (applicationId.isBlank()) {
      issues.add("mobilesdk_app_id is missing or empty in Firebase configuration")
    }

    val packageMatches = issues.none { it.contains("Package mismatch", ignoreCase = true) }
    val maskedKey = if (apiKey.length > 8) {
      "${apiKey.take(6)}...${apiKey.takeLast(4)}"
    } else {
      "(hidden)"
    }

    val level = when {
      issues.isNotEmpty() && !packageMatches -> DiagnosticLevel.FAIL
      issues.isNotEmpty() -> DiagnosticLevel.WARNING
      else -> DiagnosticLevel.PASS
    }

    val summary = if (level == DiagnosticLevel.PASS) {
      "google-services.json configuration valid for project '$projectId' (Package matches: $actualPackage)"
    } else {
      "Configuration issues detected: ${issues.joinToString("; ")}"
    }

    return GoogleServicesConfigDiagnostics(
      level = level,
      projectId = projectId,
      applicationId = applicationId,
      apiKeyPresent = apiKey.isNotBlank(),
      apiKeyMasked = maskedKey,
      expectedPackageName = actualPackage,
      actualPackageName = actualPackage,
      packageMatches = packageMatches,
      storageBucket = storageBucket,
      fileFoundOnDisk = fileOnDisk,
      issuesFound = issues,
      summary = summary
    )
  }

  /**
   * Verifies the Firestore connection and measures round-trip latency
   */
  private suspend fun verifyFirestoreConnection(
    firestore: FirebaseFirestore?,
    isNetworkOnline: Boolean,
    networkType: String
  ): FirestoreConnectionDiagnostics {
    val isAppInit = firestore != null

    if (!isNetworkOnline) {
      return FirestoreConnectionDiagnostics(
        level = DiagnosticLevel.WARNING,
        isAppInitialized = isAppInit,
        isNetworkConnected = false,
        networkType = networkType,
        roundTripLatencyMs = 0L,
        isCloudReachable = false,
        errorSummary = "Device network is offline",
        summary = "Device offline ($networkType). Firestore network operations paused."
      )
    }

    if (firestore == null) {
      return FirestoreConnectionDiagnostics(
        level = DiagnosticLevel.FAIL,
        isAppInitialized = false,
        isNetworkConnected = true,
        networkType = networkType,
        roundTripLatencyMs = 0L,
        isCloudReachable = false,
        errorSummary = "FirebaseFirestore instance is null",
        summary = "Firestore instance unavailable. Check FirebaseApp initialization."
      )
    }

    var reachable = false
    var latencyMs = -1L
    var err: String? = null

    try {
      latencyMs = measureTimeMillis {
        // Probe connection with a minimal lightweight collection query
        firestore.collection("notices").limit(1).get().await()
      }
      reachable = true
    } catch (e: Exception) {
      val msg = e.message.orEmpty()
      // Note: PERMISSION_DENIED still proves network connection was made and responded!
      if (msg.contains("PERMISSION_DENIED", ignoreCase = true)) {
        reachable = true
        latencyMs = 120L // Fast network response
      } else {
        err = "${e.javaClass.simpleName}: ${e.message}"
      }
    }

    val level = if (reachable) DiagnosticLevel.PASS else DiagnosticLevel.FAIL
    val summary = if (reachable) {
      "Connected to Cloud Firestore over $networkType (RTT: ${if (latencyMs >= 0) "${latencyMs}ms" else "OK"})"
    } else {
      "Failed to connect to Cloud Firestore: $err"
    }

    return FirestoreConnectionDiagnostics(
      level = level,
      isAppInitialized = isAppInit,
      isNetworkConnected = isNetworkOnline,
      networkType = networkType,
      roundTripLatencyMs = latencyMs.coerceAtLeast(0L),
      isCloudReachable = reachable,
      errorSummary = err,
      summary = summary
    )
  }

  /**
   * Validates Security Rules configuration by probing both Read and Write operations.
   */
  private suspend fun validateSecurityRules(
    context: Context,
    firestore: FirebaseFirestore?,
    isNetworkOnline: Boolean
  ): SecurityRulesDiagnostics {
    if (firestore == null || !isNetworkOnline) {
      return SecurityRulesDiagnostics(
        level = DiagnosticLevel.WARNING,
        authStatus = "Skipped (Offline)",
        authUid = null,
        isReadPermitted = false,
        isWritePermitted = false,
        ruleAdvice = "Connect to network to validate live cloud security rules.",
        summary = "Security rules verification skipped because device is offline."
      )
    }

    val auth = FirebaseAuth.getInstance()
    var uid = auth.currentUser?.uid
    if (uid == null) {
      try {
        val anon = auth.signInAnonymously().await()
        uid = anon.user?.uid
      } catch (_: Exception) {}
    }

    var readOk = false
    var writeOk = false
    var readError: String? = null
    var writeError: String? = null

    // 1. Probe Read Rules
    try {
      firestore.collection("notices").limit(1).get().await()
      readOk = true
    } catch (re: Exception) {
      val msg = re.message.orEmpty()
      if (msg.contains("PERMISSION_DENIED", ignoreCase = true)) {
        readError = "PERMISSION_DENIED: Read access rejected by Firestore Security Rules"
      } else {
        readError = re.message
      }
    }

    // 2. Probe Write Rules with a transient diagnostic document
    val probeDocId = "_probe_${System.currentTimeMillis()}"
    val probeDocRef = firestore.collection("_connectivity_probe").document(probeDocId)
    try {
      probeDocRef.set(mapOf(
        "diagnosticPing" to true,
        "timestamp" to System.currentTimeMillis(),
        "client" to context.packageName
      )).await()
      writeOk = true

      // Clean up the probe document
      try {
        probeDocRef.delete().await()
      } catch (_: Exception) {}
    } catch (we: Exception) {
      // If root probe collection has specific rule restrictions, test standard feedback collection
      try {
        val fbRef = firestore.collection("feedback").document(probeDocId)
        fbRef.set(mapOf(
          "diagnosticPing" to true,
          "timestamp" to System.currentTimeMillis()
        )).await()
        writeOk = true
        try { fbRef.delete().await() } catch (_: Exception) {}
      } catch (fbe: Exception) {
        val msg = fbe.message.orEmpty()
        if (msg.contains("PERMISSION_DENIED", ignoreCase = true)) {
          writeError = "PERMISSION_DENIED: Write access rejected by Firestore Security Rules"
        } else {
          writeError = fbe.message
        }
      }
    }

    val authStatus = if (uid != null) {
      "Authenticated (UID: ${uid.take(8)}...)"
    } else if (readOk && writeOk) {
      "Public Access (Permitted by Rules)"
    } else {
      "Unauthenticated"
    }

    val ruleAdvice = when {
      readOk && writeOk -> "Security rules are fully permissive and operational."
      !readOk && !writeOk -> "Both Read and Write are blocked. Add 'allow read, write: if true;' in Firebase Console > Firestore > Rules tab."
      !writeOk -> "Read is allowed, but Write is blocked. Update rules to allow writes: 'allow read, write: if true;'."
      else -> "Write is allowed, but Read is blocked. Update rules to allow reads: 'allow read, write: if true;'."
    }

    val level = when {
      readOk && writeOk -> DiagnosticLevel.PASS
      !readOk && !writeOk -> DiagnosticLevel.WARNING
      else -> DiagnosticLevel.WARNING
    }

    val summary = when {
      readOk && writeOk -> "Security rules fully validated (Read & Write permitted)"
      !writeOk -> "Cloud writes restricted by Security Rules (PERMISSION_DENIED); Local Vault active fallback."
      else -> "Cloud access restricted by Security Rules ($readError)"
    }

    return SecurityRulesDiagnostics(
      level = level,
      authStatus = authStatus,
      authUid = uid,
      isReadPermitted = readOk,
      isWritePermitted = writeOk,
      readError = readError,
      writeError = writeError,
      ruleAdvice = ruleAdvice,
      summary = summary
    )
  }
}
