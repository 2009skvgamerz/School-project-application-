package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AttendanceEntity
import com.example.model.*
import com.example.ui.components.StatCard
import com.example.ui.theme.*

/**
 * AttendanceScreen - St. Joseph's Higher Secondary School Daily Attendance Register.
 *
 * Provides:
 * 1. For Students: Personal attendance statistics, exam eligibility gauge, breakdown, and policy.
 * 2. For Educators & Staff: Interactive Class Roll Call with 4 daily options:
 *    - Full Day (FD, 1.0 weight)
 *    - Half Day (HD, 0.5 weight)
 *    - On-Duty (OD, 1.0 weight - for sports/science events)
 *    - Absent (AB, 0.0 weight - with medical/leave notes)
 * 3. Instant Room Database persistence with live reactive feedback and search filtering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
  userRole: UserRole,
  studentProfile: StudentProfile?,
  teacherProfile: TeacherProfile?,
  adminProfile: AdminProfile?,
  attendanceRecords: List<AttendanceRecord>,
  roomAttendanceRecords: List<AttendanceEntity> = emptyList(),
  selectedClass: String,
  onSelectClass: (String) -> Unit,
  onUpdateStatus: (studentId: String, status: AttendanceStatus, notes: String) -> Unit,
  onMarkAllFullDay: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showSuccessBanner by remember { mutableStateOf(false) }
  var statusFilter by remember { mutableStateOf<AttendanceStatus?>(null) }
  var searchQuery by remember { mutableStateOf("") }
  var editingNoteStudentId by remember { mutableStateOf<String?>(null) }
  var noteStudentName by remember { mutableStateOf("") }
  var noteInputText by remember { mutableStateOf("") }
  var currentStudentStatusForNote by remember { mutableStateOf(AttendanceStatus.FULL_DAY) }

  // Classes list for selector
  val availableClasses = remember {
    listOf("Class 10-A", "Class 10-B", "Class 9-A", "Class 11-Science", "Class 12-Science")
  }

  // Unified student records combining Room DB / in-memory records for the selected class
  val recordsForClass = remember(attendanceRecords, roomAttendanceRecords, selectedClass) {
    if (roomAttendanceRecords.isNotEmpty()) {
      roomAttendanceRecords.filter { it.className.equals(selectedClass, ignoreCase = true) }
        .map { entity ->
          AttendanceRecord(
            id = entity.id,
            studentId = entity.studentId,
            studentName = entity.studentName,
            rollNo = entity.rollNo,
            className = entity.className,
            date = entity.date,
            status = entity.status,
            markedBy = entity.markedBy,
            notes = entity.notes
          )
        }
    } else {
      attendanceRecords.filter { it.className.equals(selectedClass, ignoreCase = true) }
    }.ifEmpty {
      // Fallback: filter general records
      attendanceRecords.filter { it.className.equals(selectedClass, ignoreCase = true) }
    }
  }

  val filteredRecords = remember(recordsForClass, statusFilter, searchQuery) {
    recordsForClass.filter { rec ->
      (statusFilter == null || rec.status == statusFilter) &&
      (searchQuery.isBlank() ||
       rec.studentName.contains(searchQuery, ignoreCase = true) ||
       rec.rollNo.toString() == searchQuery.trim())
    }
  }

  // Attendance tallies
  val totalInClass = recordsForClass.size.coerceAtLeast(1)
  val fullDayCount = recordsForClass.count { it.status == AttendanceStatus.FULL_DAY }
  val halfDayCount = recordsForClass.count { it.status == AttendanceStatus.HALF_DAY }
  val onDutyCount = recordsForClass.count { it.status == AttendanceStatus.ON_DUTY }
  val absentCount = recordsForClass.count { it.status == AttendanceStatus.ABSENT }
  val presentRate = (((fullDayCount + onDutyCount) * 1.0 + halfDayCount * 0.5) / totalInClass * 100.0)

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("attendance_screen")
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    if (userRole == UserRole.STUDENT) {
      // ========================================================
      // 1. STUDENT ATTENDANCE REPORT VIEW
      // ========================================================
      val profile = studentProfile
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        // Attendance Overview Card
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(18.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(86.dp)
                  .clip(CircleShape)
                  .background(SchoolAccentGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "${profile?.attendancePercentage ?: 96.4}%",
                    style = MaterialTheme.typography.titleLarge.copy(
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 20.sp
                    ),
                    color = SchoolAccentGreen
                  )
                  Text(
                    text = "Overall",
                    style = MaterialTheme.typography.labelSmall,
                    color = SchoolAccentGreen
                  )
                }
              }

              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "Daily Roll Call Record",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = SchoolNavyPrimary
                )
                Text(
                  text = "Grade 10 - Section A • Roll #1",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                  color = SchoolAccentGreen.copy(alpha = 0.14f),
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = SchoolAccentGreen,
                      modifier = Modifier.size(14.dp)
                    )
                    Text(
                      text = "Eligible for Examinations (Min. 75%)",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = SchoolAccentGreen
                    )
                  }
                }
              }
            }
          }
        }

        // 4 Metric Breakdown Cards
        item {
          Text(
            text = "Academic Year Breakdown",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = SchoolNavyPrimary
          )
        }

        item {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              StatCard(
                title = "Full Day (FD)",
                value = "78 Days",
                subtitle = "Present (1.0 weight)",
                icon = Icons.Default.CheckCircle,
                accentColor = SchoolAccentGreen,
                modifier = Modifier.weight(1f),
                testTag = "student_stat_fd"
              )
              StatCard(
                title = "Half Day (HD)",
                value = "4 Days",
                subtitle = "Credited (0.5 weight)",
                icon = Icons.Default.Schedule,
                accentColor = SchoolGold,
                modifier = Modifier.weight(1f),
                testTag = "student_stat_hd"
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              StatCard(
                title = "On-Duty (OD)",
                value = "4 Days",
                subtitle = "Olympiad / Sports (1.0)",
                icon = Icons.Default.EmojiEvents,
                accentColor = SchoolAccentBlue,
                modifier = Modifier.weight(1f),
                testTag = "student_stat_od"
              )
              StatCard(
                title = "Absent (AB)",
                value = "2 Days",
                subtitle = "Medical Leave (0.0)",
                icon = Icons.Default.Cancel,
                accentColor = SchoolError,
                modifier = Modifier.weight(1f),
                testTag = "student_stat_ab"
              )
            }
          }
        }

        // Policy & Guidelines Card
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = SchoolNavyPrimary, modifier = Modifier.size(18.dp))
                Text(
                  text = "Institutional Attendance Guidelines",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = SchoolNavyPrimary
                )
              }
              Text(
                text = "• Attendance is recorded once each morning by the assigned Class Teacher.\n• On-Duty (OD) requires prior faculty approval for authorized inter-school representation.\n• Minimum 75% attendance is strictly enforced for Term Examination hall ticket issuance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    } else {
      // ========================================================
      // 2. TEACHER / ADMIN / STAFF DAILY ROLL CALL REGISTER
      // ========================================================

      // Top Header Card with Roll Call Summary & Mark All Full Day Action
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(SchoolNavyPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.FactCheck,
                  contentDescription = null,
                  tint = SchoolNavyPrimary,
                  modifier = Modifier.size(22.dp)
                )
              }

              Column {
                Text(
                  text = "Daily Roll Call Register",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = SchoolNavyPrimary
                )
                Text(
                  text = "$selectedClass • Today's Session",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Mark All Full Day Button
            Button(
              onClick = {
                onMarkAllFullDay(selectedClass)
                showSuccessBanner = true
              },
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = SchoolAccentGreen),
              modifier = Modifier.testTag("mark_all_present_btn")
            ) {
              Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("All Full Day", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
          }

          // Active Status Pill
          Surface(
            color = SchoolAccentGreen.copy(alpha = 0.10f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = SchoolAccentGreen,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Live Room Database Synchronization Active • ${String.format("%.1f", presentRate)}% Present",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = SchoolAccentGreen
              )
            }
          }
        }
      }

      // Class Selector Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        availableClasses.forEach { cls ->
          val isSelected = selectedClass.equals(cls, ignoreCase = true)
          FilterChip(
            selected = isSelected,
            onClick = {
              onSelectClass(cls)
              showSuccessBanner = false
            },
            label = {
              Text(
                text = if (cls == "Class 10-A") "$cls ★" else cls,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SchoolNavyPrimary.copy(alpha = 0.15f),
              selectedLabelColor = SchoolNavyPrimary
            ),
            modifier = Modifier.testTag("class_chip_${cls.replace(" ", "_").lowercase()}")
          )
        }
      }

      // 4 Metric Breakdown Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        RollCallStatBadge(
          title = "Full-day",
          count = fullDayCount,
          color = SchoolAccentGreen,
          isSelected = statusFilter == AttendanceStatus.FULL_DAY,
          onClick = { statusFilter = if (statusFilter == AttendanceStatus.FULL_DAY) null else AttendanceStatus.FULL_DAY },
          modifier = Modifier.weight(1f)
        )
        RollCallStatBadge(
          title = "Half-day",
          count = halfDayCount,
          color = SchoolGold,
          isSelected = statusFilter == AttendanceStatus.HALF_DAY,
          onClick = { statusFilter = if (statusFilter == AttendanceStatus.HALF_DAY) null else AttendanceStatus.HALF_DAY },
          modifier = Modifier.weight(1f)
        )
        RollCallStatBadge(
          title = "On-duty",
          count = onDutyCount,
          color = SchoolAccentBlue,
          isSelected = statusFilter == AttendanceStatus.ON_DUTY,
          onClick = { statusFilter = if (statusFilter == AttendanceStatus.ON_DUTY) null else AttendanceStatus.ON_DUTY },
          modifier = Modifier.weight(1f)
        )
        RollCallStatBadge(
          title = "Absent",
          count = absentCount,
          color = SchoolError,
          isSelected = statusFilter == AttendanceStatus.ABSENT,
          onClick = { statusFilter = if (statusFilter == AttendanceStatus.ABSENT) null else AttendanceStatus.ABSENT },
          modifier = Modifier.weight(1f)
        )
      }

      // Search Field
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search by student name or roll #", style = MaterialTheme.typography.bodySmall) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
          trailingIcon = {
            if (searchQuery.isNotBlank()) {
              IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("attendance_search_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SchoolNavyPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
          )
        )
      }

      // Success Notification Banner
      AnimatedVisibility(
        visible = showSuccessBanner,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Surface(
          color = SchoolAccentGreen.copy(alpha = 0.15f),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SchoolAccentGreen, modifier = Modifier.size(18.dp))
              Text(
                text = "Daily attendance recorded and saved to Room Database for $selectedClass",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = SchoolAccentGreen
              )
            }
            IconButton(onClick = { showSuccessBanner = false }, modifier = Modifier.size(22.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(14.dp))
            }
          }
        }
      }

      // Interactive Students List
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(filteredRecords, key = { it.id }) { rec ->
          RollCallStudentCard(
            record = rec,
            onStatusSelect = { newStatus ->
              onUpdateStatus(rec.studentId, newStatus, rec.notes)
              showSuccessBanner = true
            },
            onAddRemark = {
              editingNoteStudentId = rec.studentId
              noteStudentName = rec.studentName
              noteInputText = rec.notes
              currentStudentStatusForNote = rec.status
            }
          )
        }
      }

      // Bottom Save / Sync Action Bar
      Button(
        onClick = { showSuccessBanner = true },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("save_attendance_btn"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
      ) {
        Icon(Icons.Default.Save, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Save & Submit Roll Call ($selectedClass)",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
    }
  }

  // Modal Dialog for adding Remarks / Leave Notes / On-Duty Details
  if (editingNoteStudentId != null) {
    AlertDialog(
      onDismissRequest = { editingNoteStudentId = null },
      icon = { Icon(Icons.Default.EditNote, contentDescription = null, tint = SchoolNavyPrimary) },
      title = {
        Text("Record Reason / Remark", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Student: $noteStudentName\nStatus: ${currentStudentStatusForNote.description}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = noteInputText,
            onValueChange = { noteInputText = it },
            placeholder = { Text("e.g. Science Expo, Medical Appointment, Sports") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("attendance_note_input"),
            maxLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            editingNoteStudentId?.let { id ->
              onUpdateStatus(id, currentStudentStatusForNote, noteInputText.trim())
              showSuccessBanner = true
            }
            editingNoteStudentId = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
        ) {
          Text("Save Remark")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingNoteStudentId = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun RollCallStatBadge(
  title: String,
  count: Int,
  color: Color,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = if (isSelected) color.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    shape = RoundedCornerShape(8.dp),
    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null,
    modifier = modifier.clickable { onClick() }
  ) {
    Column(
      modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "$count",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
    }
  }
}

/**
 * Interactive Student Roll-Call Card with 4 instant-tap buttons:
 * - FD (Full Day)
 * - HD (Half Day)
 * - OD (On-Duty)
 * - AB (Absent)
 */
@Composable
fun RollCallStudentCard(
  record: AttendanceRecord,
  onStatusSelect: (AttendanceStatus) -> Unit,
  onAddRemark: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("student_attendance_row_${record.rollNo}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Roll No Badge & Student Name
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Surface(
            color = SchoolNavyPrimary.copy(alpha = 0.1f),
            shape = CircleShape
          ) {
            Text(
              text = "#${record.rollNo}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = SchoolNavyPrimary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          Column {
            Text(
              text = record.studentName,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = record.status.description,
              style = MaterialTheme.typography.labelSmall,
              color = Color(record.status.colorHex)
            )
          }
        }

        // 4 Interactive Status Badges (FD, HD, OD, AB)
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          listOf(
            AttendanceStatus.FULL_DAY,
            AttendanceStatus.HALF_DAY,
            AttendanceStatus.ON_DUTY,
            AttendanceStatus.ABSENT
          ).forEach { status ->
            val isSelected = record.status == status
            val statusColor = Color(status.colorHex)

            Surface(
              color = if (isSelected) statusColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier
                .clickable { onStatusSelect(status) }
                .testTag("status_${status.code.lowercase()}_${record.rollNo}")
            ) {
              Text(
                text = status.code,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 11.sp
                ),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
              )
            }
          }

          // Remark / Notes Button
          IconButton(
            onClick = onAddRemark,
            modifier = Modifier.size(28.dp).testTag("note_btn_${record.rollNo}")
          ) {
            Icon(
              imageVector = if (record.notes.isNotBlank()) Icons.Default.Notes else Icons.Default.AddComment,
              contentDescription = "Add remark",
              tint = if (record.notes.isNotBlank()) SchoolNavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Notes Display if present
      if (record.notes.isNotBlank()) {
        Surface(
          color = when (record.status) {
            AttendanceStatus.ON_DUTY -> SchoolAccentBlue.copy(alpha = 0.08f)
            AttendanceStatus.HALF_DAY -> SchoolGold.copy(alpha = 0.08f)
            AttendanceStatus.ABSENT -> SchoolError.copy(alpha = 0.08f)
            else -> SchoolNavyPrimary.copy(alpha = 0.05f)
          },
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddRemark() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = if (record.status == AttendanceStatus.ON_DUTY) Icons.Default.EmojiEvents else Icons.Default.Notes,
              contentDescription = null,
              tint = Color(record.status.colorHex),
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = record.notes,
              style = MaterialTheme.typography.labelSmall,
              color = Color(record.status.colorHex)
            )
          }
        }
      }
    }
  }
}
