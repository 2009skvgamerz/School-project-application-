package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.model.AttendanceStatus
import com.example.model.TeacherProfile
import com.example.ui.components.StatCard
import com.example.ui.theme.*

/**
 * TeacherClassAttendanceScreen
 *
 * Dedicated screen for Teachers to mark daily attendance for their assigned class
 * using the 4 institutional roll-call options:
 * - Full-day (FD, 1.0 weight)
 * - Half-day (HD, 0.5 weight)
 * - On-duty (OD, 1.0 weight)
 * - Absent (AB, 0.0 weight)
 *
 * Directly integrates with the Room Database Attendance records table.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassAttendanceScreen(
  teacherProfile: TeacherProfile?,
  attendanceList: List<AttendanceEntity>,
  selectedClass: String,
  onClassSelected: (String) -> Unit,
  onUpdateStatus: (studentId: String, status: AttendanceStatus, notes: String) -> Unit,
  onMarkAllFullDay: (className: String) -> Unit,
  modifier: Modifier = Modifier
) {
  var statusFilter by remember { mutableStateOf<AttendanceStatus?>(null) }
  var showSuccessBanner by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var editingStudentNoteFor by remember { mutableStateOf<AttendanceEntity?>(null) }
  var noteInputText by remember { mutableStateOf("") }

  val teacherAssignedClass = teacherProfile?.classTeacherOf ?: "Class 10-A"
  val isDesignatedClassTeacher = teacherProfile?.isClassTeacher == true &&
      (teacherAssignedClass.equals(selectedClass, ignoreCase = true) || teacherProfile.assignedClasses.any { it.equals(selectedClass, ignoreCase = true) })

  val classRecords = remember(attendanceList, selectedClass, statusFilter, searchQuery) {
    attendanceList.filter { entity ->
      (selectedClass.isEmpty() || entity.className.equals(selectedClass, ignoreCase = true)) &&
      (statusFilter == null || entity.status == statusFilter) &&
      (searchQuery.isBlank() || entity.studentName.contains(searchQuery, ignoreCase = true) || entity.rollNo.toString() == searchQuery.trim())
    }
  }

  // Attendance Metrics computed from current Room DB state
  val totalStudents = attendanceList.count { it.className.equals(selectedClass, ignoreCase = true) }.coerceAtLeast(1)
  val fullDayCount = attendanceList.count { it.className.equals(selectedClass, ignoreCase = true) && it.status == AttendanceStatus.FULL_DAY }
  val halfDayCount = attendanceList.count { it.className.equals(selectedClass, ignoreCase = true) && it.status == AttendanceStatus.HALF_DAY }
  val onDutyCount = attendanceList.count { it.className.equals(selectedClass, ignoreCase = true) && it.status == AttendanceStatus.ON_DUTY }
  val absentCount = attendanceList.count { it.className.equals(selectedClass, ignoreCase = true) && it.status == AttendanceStatus.ABSENT }

  val presentRate = (((fullDayCount + onDutyCount) * 1.0 + halfDayCount * 0.5) / totalStudents * 100.0)

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("teacher_attendance_screen")
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Header Banner & Class Teacher Credentials
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                .size(44.dp)
                .clip(CircleShape)
                .background(SchoolNavyPrimary.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.FactCheck,
                contentDescription = null,
                tint = SchoolNavyPrimary,
                modifier = Modifier.size(24.dp)
              )
            }

            Column {
              Text(
                text = "Daily Roll Call Register",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SchoolNavyPrimary
              )
              Text(
                text = "Class Teacher: ${teacherProfile?.user?.fullName ?: "Prof. Sarah Jenkins"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Quick Action: Mark All Full Day
          if (isDesignatedClassTeacher) {
            Button(
              onClick = {
                onMarkAllFullDay(selectedClass)
                showSuccessBanner = true
              },
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = SchoolAccentGreen),
              modifier = Modifier.testTag("teacher_mark_all_full_day_btn")
            ) {
              Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Mark All Full Day",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }

        // Homeroom Class Indicator & Room DB Sync Status
        Surface(
          color = if (isDesignatedClassTeacher) SchoolAccentGreen.copy(alpha = 0.12f) else SchoolGold.copy(alpha = 0.12f),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = if (isDesignatedClassTeacher) Icons.Default.Verified else Icons.Default.Info,
              contentDescription = null,
              tint = if (isDesignatedClassTeacher) SchoolAccentGreen else SchoolGoldDark,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = if (isDesignatedClassTeacher)
                "Authorized: Homeroom Class Teacher for $selectedClass • Room Database Synced"
              else
                "Subject Teacher View • Daily Roll Call is restricted to Class Teacher",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              color = if (isDesignatedClassTeacher) SchoolAccentGreen else SchoolGoldDark
            )
          }
        }
      }
    }

    // 2. Class Selector Chips
    val availableClasses = teacherProfile?.assignedClasses?.ifEmpty { null }
      ?: listOf("Class 10-A", "Class 10-B", "Class 9-A", "Class 11-Science")

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      availableClasses.forEach { cls ->
        val isSelected = selectedClass.equals(cls, ignoreCase = true)
        val isHomeroom = cls.equals(teacherAssignedClass, ignoreCase = true)

        FilterChip(
          selected = isSelected,
          onClick = { onClassSelected(cls) },
          label = {
            Text(
              text = if (isHomeroom) "$cls ★" else cls,
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

    // 3. Four Metric Cards for Daily Options
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      StatCard(
        title = "Full-day",
        value = "$fullDayCount",
        subtitle = "FD (1.0)",
        icon = Icons.Default.CheckCircle,
        accentColor = SchoolAccentGreen,
        modifier = Modifier.weight(1f),
        testTag = "stat_fd_count"
      )
      StatCard(
        title = "Half-day",
        value = "$halfDayCount",
        subtitle = "HD (0.5)",
        icon = Icons.Default.Schedule,
        accentColor = SchoolGold,
        modifier = Modifier.weight(1f),
        testTag = "stat_hd_count"
      )
      StatCard(
        title = "On-duty",
        value = "$onDutyCount",
        subtitle = "OD (1.0)",
        icon = Icons.Default.EmojiEvents,
        accentColor = SchoolAccentBlue,
        modifier = Modifier.weight(1f),
        testTag = "stat_od_count"
      )
      StatCard(
        title = "Absent",
        value = "$absentCount",
        subtitle = "AB (0.0)",
        icon = Icons.Default.Cancel,
        accentColor = SchoolError,
        modifier = Modifier.weight(1f),
        testTag = "stat_ab_count"
      )
    }

    // 4. Quick Search & Filter Chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search student name or roll #", style = MaterialTheme.typography.bodySmall) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
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

      if (searchQuery.isNotBlank()) {
        IconButton(onClick = { searchQuery = "" }) {
          Icon(Icons.Default.Clear, contentDescription = "Clear search")
        }
      }
    }

    // Status Filter Badges (All, FD, HD, OD, AB)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      TeacherStatusFilterTab(
        title = "All ($totalStudents)",
        isSelected = statusFilter == null,
        tint = SchoolNavyPrimary,
        onClick = { statusFilter = null },
        modifier = Modifier.weight(1f)
      )
      TeacherStatusFilterTab(
        title = "FD ($fullDayCount)",
        isSelected = statusFilter == AttendanceStatus.FULL_DAY,
        tint = SchoolAccentGreen,
        onClick = { statusFilter = if (statusFilter == AttendanceStatus.FULL_DAY) null else AttendanceStatus.FULL_DAY },
        modifier = Modifier.weight(1f)
      )
      TeacherStatusFilterTab(
        title = "HD ($halfDayCount)",
        isSelected = statusFilter == AttendanceStatus.HALF_DAY,
        tint = SchoolGold,
        onClick = { statusFilter = if (statusFilter == AttendanceStatus.HALF_DAY) null else AttendanceStatus.HALF_DAY },
        modifier = Modifier.weight(1f)
      )
      TeacherStatusFilterTab(
        title = "OD ($onDutyCount)",
        isSelected = statusFilter == AttendanceStatus.ON_DUTY,
        tint = SchoolAccentBlue,
        onClick = { statusFilter = if (statusFilter == AttendanceStatus.ON_DUTY) null else AttendanceStatus.ON_DUTY },
        modifier = Modifier.weight(1f)
      )
      TeacherStatusFilterTab(
        title = "AB ($absentCount)",
        isSelected = statusFilter == AttendanceStatus.ABSENT,
        tint = SchoolError,
        onClick = { statusFilter = if (statusFilter == AttendanceStatus.ABSENT) null else AttendanceStatus.ABSENT },
        modifier = Modifier.weight(1f)
      )
    }

    // Success Banner upon saving/updating
    AnimatedVisibility(visible = showSuccessBanner) {
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
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SchoolAccentGreen)
            Text(
              text = "Daily Roll Call updated & synced to Room database for $selectedClass.",
              style = MaterialTheme.typography.bodySmall,
              color = SchoolAccentGreen
            )
          }
          IconButton(onClick = { showSuccessBanner = false }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
          }
        }
      }
    }

    // 5. Student Roll Call Records List
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.weight(1f)
    ) {
      items(classRecords, key = { it.id }) { record ->
        TeacherRollCallRow(
          record = record,
          isEditable = isDesignatedClassTeacher,
          onStatusSelect = { newStatus ->
            onUpdateStatus(record.studentId, newStatus, record.notes)
            showSuccessBanner = true
          },
          onAddOrEditNote = {
            editingStudentNoteFor = record
            noteInputText = record.notes
          }
        )
      }
    }

    // 6. Bottom Submit / Confirm Button
    if (isDesignatedClassTeacher) {
      Button(
        onClick = { showSuccessBanner = true },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("teacher_submit_attendance_btn"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
      ) {
        Icon(Icons.Default.Save, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Save & Lock Daily Roll Call ($selectedClass)",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
    }
  }

  // Dialog for editing reason / On-Duty remark
  if (editingStudentNoteFor != null) {
    val student = editingStudentNoteFor!!
    AlertDialog(
      onDismissRequest = { editingStudentNoteFor = null },
      title = {
        Text("Add Remark / Reason (${student.studentName})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Record reason for On-Duty (OD), Half-Day (HD), or Absent (AB) note in Room DB.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = noteInputText,
            onValueChange = { noteInputText = it },
            placeholder = { Text("e.g. Inter-school Science Olympiad, Medical leave") },
            modifier = Modifier.fillMaxWidth().testTag("attendance_note_input"),
            maxLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onUpdateStatus(student.studentId, student.status, noteInputText.trim())
            editingStudentNoteFor = null
            showSuccessBanner = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
        ) {
          Text("Save Remark")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingStudentNoteFor = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun TeacherStatusFilterTab(
  title: String,
  isSelected: Boolean,
  tint: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = if (isSelected) tint.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    shape = RoundedCornerShape(8.dp),
    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, tint) else null,
    modifier = modifier.clickable { onClick() }
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 11.sp
      ),
      color = if (isSelected) tint else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(vertical = 6.dp),
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun TeacherRollCallRow(
  record: AttendanceEntity,
  isEditable: Boolean,
  onStatusSelect: (AttendanceStatus) -> Unit,
  onAddOrEditNote: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("roll_call_row_${record.rollNo}"),
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
          horizontalArrangement = Arrangement.spacedBy(10.dp)
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
              text = "Status: ${record.status.description}",
              style = MaterialTheme.typography.labelSmall,
              color = Color(record.status.colorHex)
            )
          }
        }

        // 4 Status Action Badges: Full-day (FD), Half-day (HD), On-duty (OD), Absent (AB)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                .clickable(enabled = isEditable) { onStatusSelect(status) }
                .testTag("teacher_status_${status.code.lowercase()}_${record.rollNo}")
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
        }
      }

      // Note/Remark Row with quick edit trigger
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        if (record.notes.isNotBlank()) {
          Surface(
            color = when (record.status) {
              AttendanceStatus.ON_DUTY -> SchoolAccentBlue.copy(alpha = 0.08f)
              AttendanceStatus.HALF_DAY -> SchoolGold.copy(alpha = 0.08f)
              AttendanceStatus.ABSENT -> SchoolError.copy(alpha = 0.08f)
              else -> SchoolNavyPrimary.copy(alpha = 0.05f)
            },
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.weight(1f, fill = false).clickable(enabled = isEditable) { onAddOrEditNote() }
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
        } else {
          Spacer(modifier = Modifier.weight(1f))
        }

        if (isEditable) {
          TextButton(
            onClick = onAddOrEditNote,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            modifier = Modifier.testTag("add_note_btn_${record.rollNo}")
          ) {
            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(14.dp), tint = SchoolNavyPrimary)
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = if (record.notes.isNotBlank()) "Edit Note" else "+ Note",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = SchoolNavyPrimary
            )
          }
        }
      }
    }
  }
}
