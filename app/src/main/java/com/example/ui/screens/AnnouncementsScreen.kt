package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnnouncementAudience
import com.example.model.AnnouncementPriority
import com.example.model.SchoolAnnouncement
import com.example.model.UserRole
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavyDark
import com.example.ui.theme.SchoolNavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(
  announcements: List<SchoolAnnouncement>,
  userRole: UserRole,
  onAddAnnouncement: (SchoolAnnouncement) -> Unit,
  onAcknowledgeAnnouncement: (String) -> Unit,
  initialSelectedAnnouncementId: String? = null,
  modifier: Modifier = Modifier
) {
  var selectedFilter by remember { mutableStateOf<AnnouncementPriority?>(null) }
  var showBroadcastDialog by remember { mutableStateOf(false) }
  var selectedAnnouncementForDetail by remember { mutableStateOf<SchoolAnnouncement?>(null) }
  var feedbackSnackbarMessage by remember { mutableStateOf<String?>(null) }
  var currentlyPlayingAudioId by remember { mutableStateOf<String?>(null) }

  // Deep Link handler to automatically open announcement detail
  LaunchedEffect(initialSelectedAnnouncementId, announcements) {
    if (initialSelectedAnnouncementId != null) {
      val match = announcements.find { it.id == initialSelectedAnnouncementId }
      if (match != null) {
        selectedAnnouncementForDetail = match
      }
    }
  }

  val filteredAnnouncements = remember(announcements, selectedFilter) {
    if (selectedFilter == null) {
      announcements
    } else {
      announcements.filter { it.priority == selectedFilter }
    }
  }

  val canPublishBroadcast = userRole != UserRole.STUDENT

  Scaffold(
    floatingActionButton = {
      if (canPublishBroadcast) {
        ExtendedFloatingActionButton(
          onClick = { showBroadcastDialog = true },
          icon = { Icon(Icons.Default.Campaign, contentDescription = "Broadcast Alert") },
          text = { Text("New Broadcast") },
          containerColor = SchoolNavyPrimary,
          contentColor = Color.White,
          modifier = Modifier.testTag("broadcast_announcement_fab")
        )
      }
    },
    modifier = modifier.fillMaxSize().testTag("announcements_screen")
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. HERO BANNER
      Card(
        modifier = Modifier.fillMaxWidth().testTag("announcements_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              brush = Brush.horizontalGradient(
                colors = listOf(SchoolNavyDark, SchoolNavyPrimary)
              )
            )
            .padding(18.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = SchoolGold, modifier = Modifier.size(24.dp))
              }
              Column {
                Text(
                  text = "School Broadcast Center",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = Color.White
                )
                Text(
                  text = "Official Campus PA & Emergency Alerts",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.8f)
                )
              }
            }

            // Stat pills
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val urgentCount = announcements.count { it.priority == AnnouncementPriority.URGENT }
              val totalCount = announcements.size

              Surface(
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(Icons.Default.Campaign, contentDescription = null, tint = SchoolGold, modifier = Modifier.size(16.dp))
                  Text(
                    text = "$totalCount Active",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                  )
                }
              }

              Surface(
                color = if (urgentCount > 0) Color(0xFFDC2626).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                  Text(
                    text = "$urgentCount Urgent",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                  )
                }
              }

              Surface(
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(Icons.Default.CloudSync, contentDescription = null, tint = SchoolGold, modifier = Modifier.size(16.dp))
                  Text(
                    text = "FCM Live",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                  )
                }
              }
            }
          }
        }
      }

      // Feedback banner
      AnimatedVisibility(visible = feedbackSnackbarMessage != null) {
        feedbackSnackbarMessage?.let { msg ->
          Surface(
            color = SchoolNavyPrimary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SchoolNavyPrimary, modifier = Modifier.size(18.dp))
              Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = SchoolNavyPrimary,
                modifier = Modifier.weight(1f)
              )
              IconButton(onClick = { feedbackSnackbarMessage = null }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }

      // 2. FILTER CHIPS
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("announcements_filter_row")
      ) {
        item(key = "filter_all") {
          FilterChip(
            selected = selectedFilter == null,
            onClick = { selectedFilter = null },
            label = { Text("All (${announcements.size})") },
            leadingIcon = {
              if (selectedFilter == null) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
              }
            }
          )
        }

        items(AnnouncementPriority.entries, key = { it.name }) { priority ->
          val count = announcements.count { it.priority == priority }
          val isSelected = selectedFilter == priority

          FilterChip(
            selected = isSelected,
            onClick = { selectedFilter = if (isSelected) null else priority },
            label = { Text("${priority.label} ($count)") },
            leadingIcon = {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(Color(priority.colorHex))
              )
            }
          )
        }
      }

      // 3. ANNOUNCEMENTS LIST
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("announcements_lazy_column"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(filteredAnnouncements, key = { it.id }) { announcement ->
          AnnouncementCard(
            announcement = announcement,
            isPlayingAudio = currentlyPlayingAudioId == announcement.id,
            onToggleAudio = {
              currentlyPlayingAudioId = if (currentlyPlayingAudioId == announcement.id) null else announcement.id
            },
            onAcknowledge = {
              onAcknowledgeAnnouncement(announcement.id)
              feedbackSnackbarMessage = "Broadcast acknowledged: ${announcement.title.take(30)}..."
            },
            onClick = {
              selectedAnnouncementForDetail = announcement
            }
          )
        }
      }
    }
  }

  // Announcement Detail Dialog (for Deep Linking or Card Tap)
  selectedAnnouncementForDetail?.let { detail ->
    AnnouncementDetailDialog(
      announcement = detail,
      onDismiss = { selectedAnnouncementForDetail = null },
      onAcknowledge = {
        onAcknowledgeAnnouncement(detail.id)
        selectedAnnouncementForDetail = null
        feedbackSnackbarMessage = "Broadcast acknowledged!"
      }
    )
  }

  // Publish Dialog
  if (showBroadcastDialog) {
    PublishAnnouncementDialog(
      onDismiss = { showBroadcastDialog = false },
      onConfirm = { newAnn ->
        onAddAnnouncement(newAnn)
        showBroadcastDialog = false
        feedbackSnackbarMessage = "📢 Broadcast alert sent to all student & parent portals!"
      }
    )
  }
}

@Composable
fun AnnouncementDetailDialog(
  announcement: SchoolAnnouncement,
  onDismiss: () -> Unit,
  onAcknowledge: () -> Unit
) {
  val priorityColor = Color(announcement.priority.colorHex)
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(priorityColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            if (announcement.isEmergency) Icons.Default.Emergency else Icons.Default.Campaign,
            contentDescription = null,
            tint = priorityColor,
            modifier = Modifier.size(20.dp)
          )
        }
        Text(
          text = if (announcement.isEmergency) "Emergency Notice" else "Official Broadcast",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Surface(
          color = priorityColor.copy(alpha = 0.1f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "${announcement.priority.label.uppercase()} • ${announcement.targetAudience.label}",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = priorityColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }

        Text(
          text = announcement.title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = if (announcement.isEmergency) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface
        )

        Text(
          text = announcement.content,
          style = MaterialTheme.typography.bodyMedium,
          lineHeight = 22.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Issued by: ${announcement.authorName}",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = announcement.date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onAcknowledge,
        colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
      ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (announcement.acknowledgedByCurrentUser) "Acknowledged" else "Acknowledge Notice")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

@Composable
fun AnnouncementCard(
  announcement: SchoolAnnouncement,
  isPlayingAudio: Boolean,
  onToggleAudio: () -> Unit,
  onAcknowledge: () -> Unit,
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val priorityColor = Color(announcement.priority.colorHex)
  val isEmergency = announcement.isEmergency

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isEmergency) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
    ),
    border = if (isEmergency) {
      androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFDC2626).copy(alpha = 0.6f))
    } else {
      null
    },
    elevation = CardDefaults.cardElevation(defaultElevation = if (isEmergency) 3.dp else 1.5.dp),
    modifier = modifier
      .fillMaxWidth()
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
      .testTag("announcement_card_${announcement.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Header Tags Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            color = priorityColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              if (isEmergency) {
                Icon(Icons.Default.Emergency, contentDescription = null, tint = priorityColor, modifier = Modifier.size(12.dp))
              }
              Text(
                text = announcement.priority.label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp),
                color = priorityColor
              )
            }
          }

          Surface(
            color = SchoolNavyPrimary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = announcement.targetAudience.label,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
              color = SchoolNavyPrimary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Text(
          text = announcement.timeAgo,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Title
      Text(
        text = announcement.title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = if (isEmergency) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface
      )

      // Content
      Text(
        text = announcement.content,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 20.sp
      )

      // Audio PA Broadcast Player (if available)
      if (announcement.audioDurationSec != null) {
        AudioBroadcastWidget(
          durationSec = announcement.audioDurationSec,
          isPlaying = isPlayingAudio,
          onTogglePlay = onToggleAudio
        )
      }

      // Attachment Download Strip (if available)
      announcement.attachmentName?.let { filename ->
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.AttachFile, contentDescription = null, tint = SchoolNavyPrimary, modifier = Modifier.size(16.dp))
            Text(
              text = filename,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              modifier = Modifier.weight(1f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            FilledTonalButton(
              onClick = { /* Simulated download */ },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("PDF", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // Footer Actions & Author
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Issued by: ${announcement.authorName}",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${announcement.authorRole} • ${announcement.acknowledgmentsCount} verified",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Acknowledge Button
        if (announcement.acknowledgedByCurrentUser) {
          FilledTonalButton(
            onClick = {},
            enabled = false,
            colors = ButtonDefaults.filledTonalButtonColors(
              disabledContainerColor = Color(0xFF10B981).copy(alpha = 0.15f),
              disabledContentColor = Color(0xFF047857)
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Acknowledged", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }
        } else {
          Button(
            onClick = onAcknowledge,
            colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("ack_btn_${announcement.id}")
          ) {
            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Acknowledge", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }
        }
      }
    }
  }
}

@Composable
private fun AudioBroadcastWidget(
  durationSec: Int,
  isPlaying: Boolean,
  onTogglePlay: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "audio_anim")
  val waveHeight by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "wave_height"
  )

  Surface(
    color = SchoolNavyPrimary.copy(alpha = 0.08f),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      IconButton(
        onClick = onTogglePlay,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(SchoolNavyPrimary)
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
          contentDescription = "Play Audio Broadcast",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = if (isPlaying) "Playing Campus PA Recording..." else "Voice PA Announcement",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = SchoolNavyPrimary
          )
          Text(
            text = "0:${durationSec.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Sound waveform simulation
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(3.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(24) { index ->
            val factor = if (isPlaying) ((index % 5 + 1) * 0.2f * waveHeight).coerceIn(0.2f, 1.0f) else 0.2f
            Box(
              modifier = Modifier
                .width(4.dp)
                .height((16 * factor).dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isPlaying) SchoolNavyPrimary else MaterialTheme.colorScheme.outlineVariant)
            )
          }
        }
      }
    }
  }
}

@Composable
fun PublishAnnouncementDialog(
  onDismiss: () -> Unit,
  onConfirm: (SchoolAnnouncement) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var content by remember { mutableStateOf("") }
  var selectedPriority by remember { mutableStateOf(AnnouncementPriority.HIGH) }
  var selectedAudience by remember { mutableStateOf(AnnouncementAudience.ALL_SCHOOL) }
  var isEmergency by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Campaign, contentDescription = null, tint = SchoolNavyPrimary)
        Text("Publish PA Broadcast", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Broadcast Subject *") },
          placeholder = { Text("e.g. Early Campus Closure Alert") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_broadcast_title")
        )

        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Broadcast Message Body *") },
          placeholder = { Text("Detailed notice content for students, parents and staff...") },
          maxLines = 4,
          modifier = Modifier.fillMaxWidth().testTag("input_broadcast_content")
        )

        Text(text = "Priority Level:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          AnnouncementPriority.entries.forEach { priority ->
            FilterChip(
              selected = selectedPriority == priority,
              onClick = { selectedPriority = priority },
              label = { Text(priority.label, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }

        Text(text = "Target Audience:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
          items(AnnouncementAudience.entries) { aud ->
            FilterChip(
              selected = selectedAudience == aud,
              onClick = { selectedAudience = aud },
              label = { Text(aud.label, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }

        // Emergency Siren checkbox
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { isEmergency = !isEmergency }
        ) {
          Checkbox(checked = isEmergency, onCheckedChange = { isEmergency = it })
          Spacer(modifier = Modifier.width(6.dp))
          Text("Flag as Emergency Siren Alert (High Visibility)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)))
        }

        // FCM Cloud Push Notification Indicator
        Surface(
          color = Color(0xFFEFF6FF),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(16.dp))
            Text(
              text = "FCM push will broadcast to topic: ${if (isEmergency) "#all_school" else "#announcements"}",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF1E40AF)
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank() && content.isNotBlank()) {
            val newAnnouncement = SchoolAnnouncement(
              id = "ann_${System.currentTimeMillis()}",
              title = title.trim(),
              content = content.trim(),
              priority = if (isEmergency) AnnouncementPriority.URGENT else selectedPriority,
              targetAudience = selectedAudience,
              date = "22 Aug 2026",
              timeAgo = "Just now",
              authorName = "Institutional Broadcast Desk",
              authorRole = "Administration",
              isEmergency = isEmergency,
              audioDurationSec = 30,
              acknowledgedByCurrentUser = true
            )
            onConfirm(newAnnouncement)
          }
        },
        enabled = title.isNotBlank() && content.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary),
        modifier = Modifier.testTag("btn_confirm_broadcast")
      ) {
        Text("Send Broadcast")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
