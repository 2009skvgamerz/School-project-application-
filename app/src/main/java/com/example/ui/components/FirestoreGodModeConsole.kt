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
import java.text.SimpleDateFormat
import java.util.*

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

  // Deletion confirmation dialog states
  var docToDelete by remember { mutableStateOf<Pair<String, String>?>(null) } // collection, docId
  var showPurgeDialog by remember { mutableStateOf(false) }
  var showNuclearWipeDialog by remember { mutableStateOf(false) }

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
