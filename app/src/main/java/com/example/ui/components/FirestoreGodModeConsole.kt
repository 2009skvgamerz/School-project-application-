package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavigationTab
import com.example.model.UserRole
import com.example.viewmodel.SchoolViewModel
import com.example.data.firestore.CloudDiagnosticsReport
import com.example.data.firestore.DiagnosticLevel
import com.example.data.firestore.FirestoreRetryEvent
import com.example.data.firestore.FirestoreRetryPolicy
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirestoreGodModeConsole(
  viewModel: SchoolViewModel,
  onNavigateToTab: ((NavigationTab) -> Unit)? = null,
  onRoleSwitched: ((UserRole) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val selectedCollection by viewModel.selectedFirestoreCollection.collectAsState()
  val firestoreDocs by viewModel.firestoreDocs.collectAsState()
  val isLoading by viewModel.isFirestoreLoading.collectAsState()
  val operationMessage by viewModel.firestoreOperationMessage.collectAsState()
  val allFeedback by viewModel.allFeedbackSubmissions.collectAsState()
  val godModeOverrideActive by viewModel.godModeOverrideActive.collectAsState()
  val cloudHealth by viewModel.cloudConnectionHealth.collectAsState()
  val diagnosticReport by viewModel.diagnosticReport.collectAsState()
  val isDiagnosing by viewModel.isDiagnosing.collectAsState()
  val lastRetryEvent by viewModel.lastRetryEvent.collectAsState()

  val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
  val context = androidx.compose.ui.platform.LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // Dialog states
  var docToDelete by remember { mutableStateOf<Pair<String, String>?>(null) } // collection, docId
  var showPurgeDialog by remember { mutableStateOf(false) }
  var showNuclearWipeDialog by remember { mutableStateOf(false) }
  var showRulesDialog by remember { mutableStateOf(false) }
  var showDiagnosticsDialog by remember { mutableStateOf(false) }
  var rulesCopied by remember { mutableStateOf(false) }

  // Retry simulator state
  var isTestingRetry by remember { mutableStateOf(false) }
  var retrySimulationLogs by remember { mutableStateOf<List<String>>(emptyList()) }

  // Custom document deleter inputs
  var customCollection by remember { mutableStateOf("") }
  var customDocId by remember { mutableStateOf("") }
  var showCustomDeleter by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. GOD MODE BANNER & UNIVERSAL BYPASS TOGGLE
    Surface(
      color = Color(0xFF0F172A),
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .background(
            Brush.horizontalGradient(
              listOf(
                Color(0xFF0F172A),
                Color(0xFF064E3B).copy(alpha = 0.45f),
                Color(0xFF0F172A)
              )
            )
          )
          .padding(14.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                color = Color(0xFF10B981).copy(alpha = 0.25f),
                shape = CircleShape,
                modifier = Modifier.size(34.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
              Column {
                Text(
                  text = "DEV GOD MODE: ROOT PRIVILEGES",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                  ),
                  color = Color(0xFF10B981)
                )
                Text(
                  text = "Direct Cloud Firestore Deletion & Global App Access",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF94A3B8)
                )
              }
            }

            // Universal Bypass Toggle
            FilterChip(
              selected = godModeOverrideActive,
              onClick = { viewModel.toggleGodModeOverride() },
              label = {
                Text(
                  text = if (godModeOverrideActive) "BYPASS ON" else "BYPASS OFF",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.25f),
                selectedLabelColor = Color(0xFF10B981),
                containerColor = Color(0xFF334155),
                labelColor = Color(0xFF94A3B8)
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = if (godModeOverrideActive) Color(0xFF10B981) else Color(0xFF475569),
                selectedBorderColor = Color(0xFF10B981),
                enabled = true,
                selected = godModeOverrideActive
              ),
              modifier = Modifier.testTag("dev_god_mode_bypass_toggle")
            )
          }

          // Operation Feedback Banner
          operationMessage?.let { msg ->
            Surface(
              color = if (msg.contains("failed", ignoreCase = true) || msg.contains("Wipe failed", ignoreCase = true)) {
                Color(0xFF7F1D1D)
              } else if (msg.contains("Vault", ignoreCase = true) || msg.contains("restricted", ignoreCase = true)) {
                Color(0xFF78350F)
              } else {
                Color(0xFF064E3B)
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  modifier = Modifier.weight(1f),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                  Text(
                    text = msg,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = Color.White
                  )
                }
                IconButton(
                  onClick = { viewModel.clearFirestoreOperationMessage() },
                  modifier = Modifier.size(20.dp)
                ) {
                  Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(14.dp))
                }
              }
            }
          }
        }
      }
    }

    // 1.5. FIREBASE CLOUD & DEVICE SYNC DIAGNOSTICS CARD
    Surface(
      color = Color(0xFF1E293B),
      shape = RoundedCornerShape(14.dp),
      border = BorderStroke(
        1.dp,
        if (cloudHealth?.isCloudAccessible == true) Color(0xFF10B981).copy(alpha = 0.6f)
        else if (cloudHealth?.isPermissionRestricted == true) Color(0xFFF59E0B).copy(alpha = 0.6f)
        else Color(0xFF38BDF8).copy(alpha = 0.6f)
      ),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = if (cloudHealth?.isCloudAccessible == true) Icons.Default.CloudDone
              else if (cloudHealth?.isPermissionRestricted == true) Icons.Default.CloudQueue
              else Icons.Default.CloudSync,
              contentDescription = null,
              tint = if (cloudHealth?.isCloudAccessible == true) Color(0xFF10B981)
              else if (cloudHealth?.isPermissionRestricted == true) Color(0xFFF59E0B)
              else Color(0xFF38BDF8),
              modifier = Modifier.size(20.dp)
            )
            Column {
              Text(
                text = "SEAMLESS CLOUD & DEVICE FLOW",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  letterSpacing = 0.5.sp
                ),
                color = if (cloudHealth?.isCloudAccessible == true) Color(0xFF10B981)
                else if (cloudHealth?.isPermissionRestricted == true) Color(0xFFF59E0B)
                else Color(0xFF38BDF8)
              )
              Text(
                text = if (cloudHealth?.isCloudAccessible == true) "Live Cloud Firestore Active & Realtime Synced"
                else if (cloudHealth?.isPermissionRestricted == true) "Device Local Vault Active (Cloud Rules Restricted)"
                else "Bi-Directional Resilient Sync Engine Initialized",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
              )
            }
          }

          IconButton(
            onClick = {
              viewModel.checkFirestoreConnectionHealth()
              viewModel.loadFirestoreCollectionDocs(selectedCollection)
            },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Sync,
              contentDescription = "Test Connection",
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Project & Auth Meta
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "Project: st-joseph-school-app",
              style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
              color = Color(0xFF94A3B8),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          Surface(
            color = if (cloudHealth?.isAuthActive == true) Color(0xFF064E3B) else Color(0xFF334155),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = if (cloudHealth?.isAuthActive == true) "Auth: Active (${cloudHealth?.authUid?.take(8)}...)" else "Auth: Initializing...",
              style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
              color = if (cloudHealth?.isAuthActive == true) Color(0xFF10B981) else Color(0xFF94A3B8),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        // Action Buttons: Quick Rules Helper & Re-Test
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = { showRulesDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
            border = BorderStroke(1.dp, Color(0xFF0284C7)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Fix Cloud Rules", style = MaterialTheme.typography.labelSmall)
          }

          Button(
            onClick = {
              viewModel.checkFirestoreConnectionHealth()
              viewModel.loadFirestoreCollectionDocs(selectedCollection)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Test & Re-Sync", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }

    // 1.6. FIRESTORE DIAGNOSTICS & VERIFICATION SUITE
    Surface(
      color = Color(0xFF1E293B),
      shape = RoundedCornerShape(14.dp),
      border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth().testTag("firestore_diagnostics_suite_card")
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Analytics,
              contentDescription = null,
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(20.dp)
            )
            Column {
              Text(
                text = "FIRESTORE DIAGNOSTIC SUITE",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  letterSpacing = 0.5.sp
                ),
                color = Color(0xFF38BDF8)
              )
              Text(
                text = "google-services.json • Security Rules • Connectivity • Sync",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
              )
            }
          }

          // Diagnostic overall level badge
          val overallBadgeColor = when (diagnosticReport?.overallLevel) {
            DiagnosticLevel.PASS -> Color(0xFF10B981)
            DiagnosticLevel.WARNING -> Color(0xFFF59E0B)
            DiagnosticLevel.FAIL -> Color(0xFFEF4444)
            null -> Color(0xFF64748B)
          }
          val overallBadgeText = when (diagnosticReport?.overallLevel) {
            DiagnosticLevel.PASS -> "HEALTHY"
            DiagnosticLevel.WARNING -> "ATTENTION"
            DiagnosticLevel.FAIL -> "ISSUE DETECTED"
            null -> "READY TO RUN"
          }

          Surface(
            color = overallBadgeColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, overallBadgeColor.copy(alpha = 0.4f))
          ) {
            Text(
              text = overallBadgeText,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = overallBadgeColor
              ),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        // Action: Run diagnostics button
        Button(
          onClick = { viewModel.runCloudDiagnostics() },
          enabled = !isDiagnosing,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().testTag("run_firestore_diagnostics_btn")
        ) {
          if (isDiagnosing) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Running Comprehensive Probes...", style = MaterialTheme.typography.labelMedium)
          } else {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Run Diagnostic Verification", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
          }
        }

        // Diagnostic 4-Pillar Overview Cards if report is available
        diagnosticReport?.let { report ->
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            DiagnosticSummaryRow(
              icon = Icons.Default.Description,
              title = "google-services.json Config",
              subtitle = report.configDiagnostics.summary,
              level = report.configDiagnostics.level
            )
            DiagnosticSummaryRow(
              icon = Icons.Default.CloudQueue,
              title = "Firestore Connectivity",
              subtitle = "${report.connectionDiagnostics.summary} (${report.connectionDiagnostics.roundTripLatencyMs}ms latency)",
              level = report.connectionDiagnostics.level
            )
            DiagnosticSummaryRow(
              icon = Icons.Default.Security,
              title = "Security Rules Permissions",
              subtitle = report.rulesDiagnostics.summary,
              level = report.rulesDiagnostics.level
            )
            DiagnosticSummaryRow(
              icon = Icons.Default.Sync,
              title = "Data Synchronization Flow",
              subtitle = report.syncDiagnostics.summary,
              level = report.syncDiagnostics.level
            )
          }

          // Button to open complete granular details dialog
          OutlinedButton(
            onClick = { showDiagnosticsDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
            border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("view_full_diagnostic_details_btn")
          ) {
            Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("View Full Diagnostic Report & Remediation Steps", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }

    // 1.7. EXPONENTIAL BACKOFF RETRY ENGINE MONITOR
    Surface(
      color = Color(0xFF1E293B),
      shape = RoundedCornerShape(14.dp),
      border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth().testTag("backoff_retry_monitor_card")
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.HourglassTop,
              contentDescription = null,
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(20.dp)
            )
            Column {
              Text(
                text = "EXPONENTIAL BACKOFF RETRY ENGINE",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  letterSpacing = 0.5.sp
                ),
                color = Color(0xFFF59E0B)
              )
              Text(
                text = "Auto-retry transient failures with randomized jitter",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
              )
            }
          }

          Surface(
            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f))
          ) {
            Text(
              text = "ACTIVE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFF59E0B)
              ),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        // Engine specs pill row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(6.dp)) {
              Text("Base Delay", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFF64748B))
              Text("500 ms", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), color = Color(0xFFF59E0B))
            }
          }
          Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(6.dp)) {
              Text("Multiplier", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFF64748B))
              Text("2.0x (Exp)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), color = Color(0xFF38BDF8))
            }
          }
          Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(6.dp)) {
              Text("Max Delay", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFF64748B))
              Text("10,000 ms", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), color = Color(0xFFE2E8F0))
            }
          }
          Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(6.dp)) {
              Text("Max Retries", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFF64748B))
              Text("5 attempts", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), color = Color(0xFF10B981))
            }
          }
        }

        // Live Last Retry Event Feed
        lastRetryEvent?.let { event ->
          Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "LATEST RETRY EVENT",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp
                  ),
                  color = Color(0xFFF59E0B)
                )
                Text(
                  text = "Attempt ${event.attempt} of ${event.maxAttempts}",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color.White
                )
              }
              Text(
                text = "Op: ${event.operationName}",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                color = Color(0xFFE2E8F0)
              )
              Text(
                text = "Backoff: ${event.delayMs}ms delay • Error: ${event.errorSummary}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color(0xFF94A3B8)
              )
            }
          }
        } ?: run {
          Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.6f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "No active retry event in queue. Operations completing or queued in local Room DB.",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = Color(0xFF64748B),
              modifier = Modifier.padding(8.dp)
            )
          }
        }

        // Simulator button to demonstrate exponential backoff in action
        Button(
          onClick = {
            if (!isTestingRetry) {
              isTestingRetry = true
              retrySimulationLogs = listOf("Starting backoff retry test sequence...")
              coroutineScope.launch {
                var attempt = 0
                try {
                  FirestoreRetryPolicy.executeWithRetry(
                    operationName = "SimulationTestOperation",
                    maxAttempts = 3,
                    initialDelayMs = 400,
                    onRetry = { retryEv ->
                      retrySimulationLogs = retrySimulationLogs + "⏳ Attempt ${retryEv.attempt}/3: Transient drop (${retryEv.errorSummary}) -> Backoff wait ${retryEv.delayMs}ms"
                    }
                  ) {
                    attempt++
                    if (attempt < 3) {
                      throw com.google.firebase.firestore.FirebaseFirestoreException(
                        "Simulated transient network drop",
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE
                      )
                    }
                    "Success"
                  }
                  retrySimulationLogs = retrySimulationLogs + "✅ Retry simulation passed: Successfully executed on attempt 3 after exponential backoff!"
                } catch (e: Exception) {
                  retrySimulationLogs = retrySimulationLogs + "❌ Simulation completed with: ${e.message}"
                } finally {
                  isTestingRetry = false
                }
              }
            }
          },
          enabled = !isTestingRetry,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().testTag("simulate_retry_btn")
        ) {
          if (isTestingRetry) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simulating Backoff Retries...", style = MaterialTheme.typography.labelSmall)
          } else {
            Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Simulate Exponential Backoff Retry", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }
        }

        // Live Simulation Logs
        if (retrySimulationLogs.isNotEmpty()) {
          Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
              retrySimulationLogs.forEach { log ->
                Text(
                  text = log,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                  ),
                  color = if (log.contains("✅")) Color(0xFF10B981)
                  else if (log.contains("⏳")) Color(0xFFF59E0B)
                  else Color(0xFF94A3B8)
                )
              }
            }
          }
        }
      }
    }

    // 2. OMNI JUMP MATRIX (ACCESS ANYTHING ACROSS THE APP)
    Surface(
      color = Color(0xFF1E293B),
      shape = RoundedCornerShape(14.dp),
      border = BorderStroke(1.dp, Color(0xFF334155)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.OpenInNew,
              contentDescription = null,
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "OMNI JUMP: ACCESS ANY SCREEN",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
              ),
              color = Color(0xFF38BDF8)
            )
          }
          Text(
            text = "Zero Restrictions",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8)
          )
        }

        Text(
          text = "Tap any module to instantly navigate with master developer permissions:",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFFCBD5E1)
        )

        // Screen Quick Jump Chips
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          item {
            OmniNavButton("📝 Homework", Color(0xFF2563EB)) {
              onNavigateToTab?.invoke(NavigationTab.HOMEWORK)
            }
          }
          item {
            OmniNavButton("📋 Attendance", Color(0xFF10B981)) {
              onNavigateToTab?.invoke(NavigationTab.ATTENDANCE)
            }
          }
          item {
            OmniNavButton("📢 Notices", Color(0xFFF59E0B)) {
              onNavigateToTab?.invoke(NavigationTab.NOTICES)
            }
          }
          item {
            OmniNavButton("🏛️ Management", Color(0xFF8B5CF6)) {
              onNavigateToTab?.invoke(NavigationTab.MANAGEMENT)
            }
          }
          item {
            OmniNavButton("🏫 Classes", Color(0xFFEC4899)) {
              onNavigateToTab?.invoke(NavigationTab.CLASSES)
            }
          }
          item {
            OmniNavButton("📅 Timetable", Color(0xFF06B6D4)) {
              onNavigateToTab?.invoke(NavigationTab.TIMETABLE)
            }
          }
          item {
            OmniNavButton("📆 Calendar", Color(0xFF14B8A6)) {
              onNavigateToTab?.invoke(NavigationTab.CALENDAR)
            }
          }
          item {
            OmniNavButton("🚌 Live GPS", Color(0xFFF97316)) {
              onNavigateToTab?.invoke(NavigationTab.BUS_TRACKING)
            }
          }
          item {
            OmniNavButton("🚨 Broadcasts", Color(0xFFEF4444)) {
              onNavigateToTab?.invoke(NavigationTab.ANNOUNCEMENTS)
            }
          }
          item {
            OmniNavButton("👥 Directory", Color(0xFF6366F1)) {
              onNavigateToTab?.invoke(NavigationTab.DIRECTORY)
            }
          }
          item {
            OmniNavButton("💼 Duties", Color(0xFF64748B)) {
              onNavigateToTab?.invoke(NavigationTab.DUTIES)
            }
          }
          item {
            OmniNavButton("⚙️ Settings", Color(0xFF475569)) {
              onNavigateToTab?.invoke(NavigationTab.SETTINGS)
            }
          }
        }
      }
    }

    // 3. FIRESTORE POWER CONTROLS (PURGE, WIPE, RE-SEED)
    Surface(
      color = Color(0xFF1E293B),
      shape = RoundedCornerShape(14.dp),
      border = BorderStroke(1.dp, Color(0xFF334155)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CloudSync,
              contentDescription = null,
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "FIRESTORE CLOUD CONTROLLER",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              ),
              color = Color(0xFFF59E0B)
            )
          }

          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              color = Color(0xFFF59E0B),
              strokeWidth = 2.dp
            )
          }
        }

        // Action Buttons Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Purge Selected Collection Button
          Button(
            onClick = { showPurgeDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("dev_purge_collection_btn")
          ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Purge '$selectedCollection'",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          // Nuclear Wipe All Button
          Button(
            onClick = { showNuclearWipeDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("dev_nuclear_wipe_btn")
          ) {
            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "☢️ Nuclear Wipe DB",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Re-Seed Cloud DB Button
          FilledTonalButton(
            onClick = { viewModel.pushLocalSeedToFirestore() },
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = Color(0xFF0D9488).copy(alpha = 0.25f),
              contentColor = Color(0xFF2DD4BF)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("dev_reseed_firestore_btn")
          ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "🚀 Push / Re-Seed Cloud",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
          }

          // Custom Doc Deleter Toggle
          OutlinedButton(
            onClick = { showCustomDeleter = !showCustomDeleter },
            border = BorderStroke(1.dp, Color(0xFF64748B)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("dev_toggle_custom_deleter_btn")
          ) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (showCustomDeleter) "Hide Custom" else "Targeted Deleter",
              style = MaterialTheme.typography.labelSmall
            )
          }
        }

        // Targeted / Custom Document Deleter Form
        AnimatedVisibility(visible = showCustomDeleter) {
          Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFF475569)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "Targeted Document Deleter (Any Collection & Doc ID):",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF38BDF8)
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = customCollection,
                  onValueChange = { customCollection = it },
                  placeholder = { Text("Collection (e.g. feedback)", fontSize = 12.sp) },
                  singleLine = true,
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF475569)
                  ),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("custom_deleter_col_input")
                )
                OutlinedTextField(
                  value = customDocId,
                  onValueChange = { customDocId = it },
                  placeholder = { Text("Doc ID", fontSize = 12.sp) },
                  singleLine = true,
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF475569)
                  ),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("custom_deleter_doc_input")
                )
              }
              Button(
                onClick = {
                  val c = customCollection.trim().ifBlank { selectedCollection }
                  val id = customDocId.trim()
                  if (id.isNotBlank()) {
                    viewModel.deleteFirestoreDoc(c, id)
                    customDocId = ""
                  }
                },
                enabled = customDocId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("custom_deleter_submit_btn")
              ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Target Document from Firestore")
              }
            }
          }
        }
      }
    }

    // 4. COLLECTION BROWSER & LIVE DOCUMENT LIST
    Surface(
      color = Color(0xFF1E293B),
      shape = RoundedCornerShape(14.dp),
      border = BorderStroke(1.dp, Color(0xFF334155)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = null,
              tint = Color(0xFF10B981),
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "CLOUD COLLECTIONS",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              ),
              color = Color(0xFF10B981)
            )
          }

          IconButton(
            onClick = { viewModel.loadFirestoreCollectionDocs(selectedCollection) },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Refresh Docs",
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Collection Filter Chips
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(viewModel.firestoreCollections) { col ->
            val isSelected = selectedCollection == col
            FilterChip(
              selected = isSelected,
              onClick = { viewModel.selectFirestoreCollection(col) },
              label = {
                Text(
                  text = col,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF10B981),
                selectedLabelColor = Color.Black,
                containerColor = Color(0xFF0F172A),
                labelColor = Color(0xFF94A3B8)
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = Color(0xFF475569),
                selectedBorderColor = Color(0xFF10B981),
                enabled = true,
                selected = isSelected
              ),
              modifier = Modifier.testTag("firestore_chip_$col")
            )
          }
        }

        Divider(color = Color(0xFF334155))

        // Document List Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Documents in '$selectedCollection' (${firestoreDocs.size})",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
          )

          if (selectedCollection == "feedback" && allFeedback.isNotEmpty()) {
            TextButton(
              onClick = { viewModel.clearAllFeedbackData() },
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Clear Feedback Vault", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444))
            }
          }
        }

        // Live Document Items
        if (firestoreDocs.isEmpty()) {
          Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp)
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(28.dp)
              )
              Text(
                text = "No live documents in '$selectedCollection'",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFF94A3B8)
              )
              Text(
                text = "Tap 'Push / Re-Seed Cloud' above to populate this collection.",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B)
              )
            }
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            firestoreDocs.forEach { doc ->
              Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("firestore_doc_card_${doc.id}")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      // Source badge: Cloud vs Local Vault
                      Surface(
                        color = if (doc.isFromCloud) Color(0xFF0369A1).copy(alpha = 0.4f) else Color(0xFF78350F).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (doc.isFromCloud) Color(0xFF38BDF8) else Color(0xFFF59E0B))
                      ) {
                        Text(
                          text = if (doc.isFromCloud) "☁️ CLOUD" else "💾 LOCAL VAULT",
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                          ),
                          color = if (doc.isFromCloud) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                          modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                      }

                      Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(4.dp)
                      ) {
                        Text(
                          text = doc.id,
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                          ),
                          color = Color(0xFF38BDF8),
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                      if (doc.fields.isNotEmpty()) {
                        Text(
                          text = "${doc.fields.size} fields",
                          style = MaterialTheme.typography.labelSmall,
                          color = Color(0xFF64748B)
                        )
                      }
                    }

                    Text(
                      text = doc.summary,
                      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                      color = Color(0xFFE2E8F0),
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis
                    )

                    if (doc.timestamp > 0) {
                      val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(doc.timestamp))
                      Text(
                        text = "Time: $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                      )
                    }
                  }

                  // Direct Delete Document Button
                  IconButton(
                    onClick = {
                      docToDelete = Pair(selectedCollection, doc.id)
                    },
                    modifier = Modifier
                      .size(36.dp)
                      .testTag("dev_delete_doc_${doc.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.DeleteForever,
                      contentDescription = "Delete Document",
                      tint = Color(0xFFEF4444),
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // -------------------------------------------------------------
  // CONFIRMATION DIALOGS
  // -------------------------------------------------------------

  // 1. Single Document Delete Dialog
  docToDelete?.let { (col, id) ->
    AlertDialog(
      onDismissRequest = { docToDelete = null },
      containerColor = Color(0xFF0F172A),
      titleContentColor = Color(0xFFEF4444),
      textContentColor = Color(0xFFE2E8F0),
      icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFEF4444)) },
      title = { Text("Delete Document from Cloud Firestore?") },
      text = {
        Text("Are you sure you want to permanently delete document '$id' from collection '$col'? This deletion will immediately synchronize with all clients.")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteFirestoreDoc(col, id)
            docToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
          Text("Delete Document")
        }
      },
      dismissButton = {
        TextButton(onClick = { docToDelete = null }) {
          Text("Cancel", color = Color(0xFF94A3B8))
        }
      }
    )
  }

  // 2. Collection Purge Dialog
  if (showPurgeDialog) {
    AlertDialog(
      onDismissRequest = { showPurgeDialog = false },
      containerColor = Color(0xFF0F172A),
      titleContentColor = Color(0xFFEF4444),
      textContentColor = Color(0xFFE2E8F0),
      icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444)) },
      title = { Text("Purge Entire Collection '$selectedCollection'?") },
      text = {
        Text("⚠️ GOD MODE WARNING: This will permanently delete ALL documents stored in Firestore collection '$selectedCollection'. Are you sure?")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.purgeFirestoreCollection(selectedCollection)
            showPurgeDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
          Text("Purge Collection Now")
        }
      },
      dismissButton = {
        TextButton(onClick = { showPurgeDialog = false }) {
          Text("Cancel", color = Color(0xFF94A3B8))
        }
      }
    )
  }

  // 3. Nuclear Wipe Dialog
  if (showNuclearWipeDialog) {
    AlertDialog(
      onDismissRequest = { showNuclearWipeDialog = false },
      containerColor = Color(0xFF0F172A),
      titleContentColor = Color(0xFFEF4444),
      textContentColor = Color(0xFFE2E8F0),
      icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444)) },
      title = { Text("☢️ NUCLEAR GOD MODE WIPE?") },
      text = {
        Text("This will wipe EVERY single document across ALL collections in Cloud Firestore (feedback, notices, homework, attendance, users, announcements, events, fcm). Only use this for factory reset or full purge.")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.wipeEntireFirestoreDatabase()
            showNuclearWipeDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D))
        ) {
          Text("Execute Nuclear Wipe")
        }
      },
      dismissButton = {
        TextButton(onClick = { showNuclearWipeDialog = false }) {
          Text("Cancel", color = Color(0xFF94A3B8))
        }
      }
    )
  }

  // 4. Cloud Firestore Security Rules Helper Dialog
  if (showRulesDialog) {
    val sampleRules = """rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}"""
    AlertDialog(
      onDismissRequest = {
        showRulesDialog = false
        rulesCopied = false
      },
      containerColor = Color(0xFF0F172A),
      titleContentColor = Color(0xFF38BDF8),
      textContentColor = Color(0xFFE2E8F0),
      icon = { Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF38BDF8)) },
      title = { Text("Cloud Firestore Security Rules") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "The app has a resilient dual-layer architecture: all data is always preserved in the Device Local Vault. If Cloud Firestore rejects operations with 'PERMISSION_DENIED', configure these rules in Firebase Console:",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1)
          )

          Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = sampleRules,
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
              ),
              color = Color(0xFF38BDF8),
              modifier = Modifier.padding(10.dp)
            )
          }

          Text(
            text = "Steps: Firebase Console -> Firestore Database -> Rules tab -> Paste -> Click 'Publish'.",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8)
          )

          if (rulesCopied) {
            Text(
              text = "✅ Rules copied to clipboard! Ready to paste into Firebase Console.",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFF10B981)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(sampleRules))
            rulesCopied = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (rulesCopied) "Copied!" else "Copy Rules")
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showRulesDialog = false
            rulesCopied = false
          }
        ) {
          Text("Close", color = Color(0xFF94A3B8))
        }
      }
    )
  }

  // 5. Full Granular Diagnostics Report Dialog
  if (showDiagnosticsDialog) {
    val report = diagnosticReport
    AlertDialog(
      onDismissRequest = { showDiagnosticsDialog = false },
      containerColor = Color(0xFF0F172A),
      titleContentColor = Color(0xFF38BDF8),
      textContentColor = Color(0xFFE2E8F0),
      icon = { Icon(Icons.Default.Analytics, contentDescription = null, tint = Color(0xFF38BDF8)) },
      title = {
        Text(
          text = "Firestore Verification Report",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      },
      text = {
        if (report != null) {
          LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Overall Status: ${report.overallLevel.name}",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  ),
                  color = when (report.overallLevel) {
                    DiagnosticLevel.PASS -> Color(0xFF10B981)
                    DiagnosticLevel.WARNING -> Color(0xFFF59E0B)
                    DiagnosticLevel.FAIL -> Color(0xFFEF4444)
                  }
                )
                Text(
                  text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(report.timestamp)),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF64748B)
                )
              }
            }

            // Pillar 1: Config
            item {
              DiagnosticSectionCard(
                title = "1. google-services.json & Client",
                items = listOf(
                  DiagnosticDisplayItem("Project ID", report.configDiagnostics.projectId.ifEmpty { "Detected in config" }, report.configDiagnostics.level),
                  DiagnosticDisplayItem("App ID", report.configDiagnostics.applicationId.ifEmpty { "Mobile SDK configured" }, report.configDiagnostics.level),
                  DiagnosticDisplayItem("Package Match", "${report.configDiagnostics.actualPackageName} (Matches: ${report.configDiagnostics.packageMatches})", if (report.configDiagnostics.packageMatches) DiagnosticLevel.PASS else DiagnosticLevel.WARNING),
                  DiagnosticDisplayItem("Config Summary", report.configDiagnostics.summary, report.configDiagnostics.level)
                )
              )
            }

            // Pillar 2: Connectivity
            item {
              DiagnosticSectionCard(
                title = "2. Firestore Connection & Latency",
                items = listOf(
                  DiagnosticDisplayItem("App Initialized", if (report.connectionDiagnostics.isAppInitialized) "Yes" else "No", if (report.connectionDiagnostics.isAppInitialized) DiagnosticLevel.PASS else DiagnosticLevel.FAIL),
                  DiagnosticDisplayItem("Network Connection", report.connectionDiagnostics.networkType, if (report.connectionDiagnostics.isNetworkConnected) DiagnosticLevel.PASS else DiagnosticLevel.FAIL),
                  DiagnosticDisplayItem("Latency", "${report.connectionDiagnostics.roundTripLatencyMs} ms", report.connectionDiagnostics.level),
                  DiagnosticDisplayItem("Cloud Reachable", if (report.connectionDiagnostics.isCloudReachable) "Connected" else "Unreachable", report.connectionDiagnostics.level)
                )
              )
            }

            // Pillar 3: Security Rules
            item {
              DiagnosticSectionCard(
                title = "3. Security Rules Validation",
                items = listOf(
                  DiagnosticDisplayItem(
                    "Auth Status",
                    report.rulesDiagnostics.authStatus,
                    if (report.rulesDiagnostics.authUid != null || (report.rulesDiagnostics.isReadPermitted && report.rulesDiagnostics.isWritePermitted)) DiagnosticLevel.PASS else DiagnosticLevel.WARNING
                  ),
                  DiagnosticDisplayItem("Read Permitted", if (report.rulesDiagnostics.isReadPermitted) "Allowed (200 OK)" else "Denied: ${report.rulesDiagnostics.readError}", if (report.rulesDiagnostics.isReadPermitted) DiagnosticLevel.PASS else DiagnosticLevel.FAIL),
                  DiagnosticDisplayItem("Write Permitted", if (report.rulesDiagnostics.isWritePermitted) "Allowed (200 OK)" else "Denied: ${report.rulesDiagnostics.writeError}", if (report.rulesDiagnostics.isWritePermitted) DiagnosticLevel.PASS else DiagnosticLevel.FAIL),
                  DiagnosticDisplayItem("Rule Advice", report.rulesDiagnostics.ruleAdvice, report.rulesDiagnostics.level)
                )
              )
            }

            // Pillar 4: Data Sync
            item {
              DiagnosticSectionCard(
                title = "4. Device & Cloud Data Flow",
                items = listOf(
                  DiagnosticDisplayItem("Sync Active", if (report.syncDiagnostics.isSyncActive) "Yes" else "Paused", report.syncDiagnostics.level),
                  DiagnosticDisplayItem("Sync Paused", if (report.syncDiagnostics.isSyncPaused) "Paused (${report.syncDiagnostics.pausedReason})" else "Realtime Sync Active", if (report.syncDiagnostics.isSyncPaused) DiagnosticLevel.WARNING else DiagnosticLevel.PASS),
                  DiagnosticDisplayItem("Local Vault Fallback", if (report.syncDiagnostics.localVaultFallbackActive) "Active (Room SQLite Secure)" else "Normal", DiagnosticLevel.PASS),
                  DiagnosticDisplayItem("Sync Summary", report.syncDiagnostics.summary, report.syncDiagnostics.level)
                )
              )
            }

            // Recommendations
            if (report.recommendedSteps.isNotEmpty()) {
              item {
                Surface(
                  color = Color(0xFF020617),
                  shape = RoundedCornerShape(8.dp),
                  border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                      text = "RECOMMENDED ACTIONS:",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                      ),
                      color = Color(0xFFF59E0B)
                    )
                    report.recommendedSteps.forEach { rec ->
                      Text(
                        text = "• $rec",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFFCBD5E1)
                      )
                    }
                  }
                }
              }
            }
          }
        } else {
          Text("No diagnostic report currently generated. Run verification to generate.", color = Color(0xFF94A3B8))
        }
      },
      confirmButton = {
        Button(
          onClick = {
            report?.let {
              clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(it.toReadableSummary()))
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Copy Report")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDiagnosticsDialog = false }) {
          Text("Close", color = Color(0xFF94A3B8))
        }
      }
    )
  }
}

private data class DiagnosticDisplayItem(
  val name: String,
  val details: String,
  val level: DiagnosticLevel
)

@Composable
private fun DiagnosticSummaryRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  level: DiagnosticLevel,
  modifier: Modifier = Modifier
) {
  val levelColor = when (level) {
    DiagnosticLevel.PASS -> Color(0xFF10B981)
    DiagnosticLevel.WARNING -> Color(0xFFF59E0B)
    DiagnosticLevel.FAIL -> Color(0xFFEF4444)
  }

  Surface(
    color = Color(0xFF0F172A),
    shape = RoundedCornerShape(8.dp),
    border = BorderStroke(1.dp, levelColor.copy(alpha = 0.3f)),
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(levelColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = levelColor,
          modifier = Modifier.size(16.dp)
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = Color.White
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
          color = Color(0xFF94A3B8),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
      Surface(
        color = levelColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
      ) {
        Text(
          text = level.name,
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = levelColor
          ),
          modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
      }
    }
  }
}

@Composable
private fun DiagnosticSectionCard(
  title: String,
  items: List<DiagnosticDisplayItem>,
  modifier: Modifier = Modifier
) {
  Surface(
    color = Color(0xFF020617),
    shape = RoundedCornerShape(8.dp),
    border = BorderStroke(1.dp, Color(0xFF334155)),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp
        ),
        color = Color(0xFF38BDF8)
      )
      items.forEach { item ->
        val itemColor = when (item.level) {
          DiagnosticLevel.PASS -> Color(0xFF10B981)
          DiagnosticLevel.WARNING -> Color(0xFFF59E0B)
          DiagnosticLevel.FAIL -> Color(0xFFEF4444)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(itemColor)
            )
            Text(
              text = "${item.name}: ${item.details}",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
              color = Color(0xFFCBD5E1)
            )
          }
          Text(
            text = item.level.name,
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = itemColor
            )
          )
        }
      }
    }
  }
}

@Composable
private fun OmniNavButton(
  label: String,
  accentColor: Color,
  onClick: () -> Unit
) {
  Surface(
    color = accentColor.copy(alpha = 0.15f),
    shape = RoundedCornerShape(10.dp),
    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
    modifier = Modifier.clickable { onClick() }
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
      ),
      color = Color.White,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    )
  }
}
