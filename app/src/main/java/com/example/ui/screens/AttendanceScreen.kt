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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.StatCard
import com.example.ui.theme.*

/**
 * AttendanceScreen - St. Joseph's School Daily Attendance Register.
 *
 * Rules:
 * 1. Attendance is taken ONCE PER DAY (Daily Roll Call), not period-by-period.
 * 2. Only the assigned CLASS TEACHER of that class (or Principal/Admin) has authorization to take/mark attendance.
 * 3. Daily statuses are strictly: Full Day (FD), Half Day (HD), On-Duty (OD), and Absent (AB).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
  userRole: UserRole,
  studentProfile: StudentProfile?,
  teacherProfile: TeacherProfile?,
  adminProfile: AdminProfile?,
  attendanceRecords: List<AttendanceRecord>,
  selectedClass: String,
  onSelectClass: (String) -> Unit,
  onUpdateStatus: (String, AttendanceStatus) -> Unit,
  onMarkAllFullDay: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showSuccessSnackbar by remember { mutableStateOf(false) }
  var statusFilter by remember { mutableStateOf<AttendanceStatus?>(null) }

  // Map of classes to their assigned Class Teachers in St. Joseph's School
  val classTeacherMap = remember {
    mapOf(
      "Class 10-A" to "Prof. Sarah Jenkins",
      "Class 10-B" to "Mr. David Miller",
      "Class 9-A" to "Mrs. Clara Higgins",
      "Class 9-B" to "Dr. Anita Sharma",
      "Class 11-Science" to "Dr. Rachel Green",
      "Class 12-Science" to "Mr. Kevin Ross"
    )
  }

  val assignedClassTeacher = classTeacherMap[selectedClass] ?: "Assigned Class Teacher"

  // Authorization Check: Only the designated Class Teacher for this class (or Admin) can record daily attendance
  val isClassTeacherForThisClass = when (userRole) {
    UserRole.ADMIN -> true // Principal / Administrator has institutional override authority
    UserRole.TEACHER -> {
      val teacherClass = teacherProfile?.classTeacherOf ?: "Class 10-A"
      teacherClass.equals(selectedClass, ignoreCase = true) || teacherProfile?.user?.fullName.equals(assignedClassTeacher, ignoreCase = true)
    }
    else -> false // Students & General Staff have view-only access
  }

  val filteredRecords = attendanceRecords.filter { rec ->
    (rec.className.equals(selectedClass, ignoreCase = true) || selectedClass.isEmpty()) &&
    (statusFilter == null || rec.status == statusFilter)
  }

  // Count tallies for this class
  val fullDayCount = attendanceRecords.count { it.className.equals(selectedClass, ignoreCase = true) && it.status == AttendanceStatus.FULL_DAY }
  val halfDayCount = attendanceRecords.count { it.className.equals(selectedClass, ignoreCase = true) && it.status == AttendanceStatus.HALF_DAY }
  val onDutyCount = attendanceRecords.count { it.className.equals(selectedClass, ignoreCase = true) && it.status == AttendanceStatus.ON_DUTY }
  val absentCount = attendanceRecords.count { it.className.equals(selectedClass, ignoreCase = true) && it.status == AttendanceStatus.ABSENT }
  val totalInClass = attendanceRecords.count { it.className.equals(selectedClass, ignoreCase = true) }.coerceAtLeast(1)
  val effectiveAttendancePercentage = (((fullDayCount + onDutyCount) * 1.0 + halfDayCount * 0.5) / totalInClass * 100.0)

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("attendance_screen")
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    if (userRole == UserRole.STUDENT) {
      // ========================================================
      // 1. STUDENT ATTENDANCE REPORT VIEW
      // ========================================================
      val profile = studentProfile ?: return
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        // Daily Attendance Header Card
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
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
                  .size(82.dp)
                  .clip(CircleShape)
                  .background(SchoolAccentGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "${profile.attendancePercentage}%",
                    style = MaterialTheme.typography.titleLarge.copy(
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 19.sp
                    ),
                    color = SchoolAccentGreen
                  )
                  Text(
                    text = "Term Avg",
                    style = MaterialTheme.typography.labelSmall,
                    color = SchoolAccentGreen
                  )
                }
              }

              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = "Daily Roll Call Record",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SchoolNavyPrimary
                  )
                }
                Text(
                  text = "Class Teacher: Prof. Sarah Jenkins • Grade 10-A",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                  color = SchoolAccentGreen.copy(alpha = 0.14f),
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = "✓ Eligible for Term Examinations (Min. 75% required)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SchoolAccentGreen,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }
            }
          }
        }

        // Daily Attendance Types Legend & 4 Metric Cards
        item {
          Text(
            text = "Academic Year Daily Attendance Summary",
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
                subtitle = "Full day present (1.0)",
                icon = Icons.Default.CheckCircle,
                accentColor = SchoolAccentGreen,
                modifier = Modifier.weight(1f),
                testTag = "stat_full_day"
              )
              StatCard(
                title = "Half Day (HD)",
                value = "4 Days",
                subtitle = "Half-day credited (0.5)",
                icon = Icons.Default.Schedule,
                accentColor = SchoolGold,
                modifier = Modifier.weight(1f),
                testTag = "stat_half_day"
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              StatCard(
                title = "On-Duty (OD)",
                value = "4 Days",
                subtitle = "Sports / Olympiad (1.0)",
                icon = Icons.Default.EmojiEvents,
                accentColor = SchoolAccentBlue,
                modifier = Modifier.weight(1f),
                testTag = "stat_on_duty"
              )
              StatCard(
                title = "Absent (AB)",
                value = "2 Days",
                subtitle = "Medical leave (0.0)",
                icon = Icons.Default.Cancel,
                accentColor = SchoolError,
                modifier = Modifier.weight(1f),
                testTag = "stat_absent"
              )
            }
          }
        }

        // Explanation Policy Card
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = SchoolNavyPrimary, modifier = Modifier.size(18.dp))
                Text(
                  text = "Daily Attendance Policy",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = SchoolNavyPrimary
                )
              }
              Text(
                text = "• Attendance is taken once each morning by your designated Class Teacher (Prof. Sarah Jenkins).\n• On-Duty (OD) is granted for official sports, science expo, or inter-school competitions and counts as full attendance.\n• Half-Day (HD) applies when departing early or arriving late with verified permission.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    } else {
      // ========================================================
      // 2. TEACHER / ADMIN / STAFF DAILY ATTENDANCE REGISTER
      // ========================================================

      // Top Title and Day Indicator
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Daily Attendance Register",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = SchoolNavyPrimary
          )
          Text(
            text = "Daily Roll Call • Once per Day • $selectedClass",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (isClassTeacherForThisClass) {
          Button(
            onClick = {
              onMarkAllFullDay(selectedClass)
              showSuccessSnackbar = true
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
      }

      // Class Selector Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("Class 10-A", "Class 10-B", "Class 9-A", "Class 11-Science").forEach { cls ->
          val isSelected = selectedClass == cls
          FilterChip(
            selected = isSelected,
            onClick = { onSelectClass(cls) },
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
            )
          )
        }
      }

      // Authorization Banner: Highlights whether current teacher is authorized Class Teacher
      if (isClassTeacherForThisClass) {
        Surface(
          color = SchoolAccentGreen.copy(alpha = 0.12f),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.VerifiedUser,
              contentDescription = null,
              tint = SchoolAccentGreen,
              modifier = Modifier.size(20.dp)
            )
            Column {
              Text(
                text = "Class Teacher Authorization Active",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = SchoolAccentGreen
              )
              Text(
                text = "You are the assigned Class Teacher for $selectedClass. You can mark daily roll call.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        // Restricted / View-Only Banner for Subject Teachers & Other Roles
        Surface(
          color = SchoolGold.copy(alpha = 0.14f),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth().testTag("restricted_attendance_banner")
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = SchoolGoldDark,
              modifier = Modifier.size(20.dp)
            )
            Column {
              Text(
                text = "Class Teacher Only • View-Only Mode",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = SchoolGoldDark
              )
              Text(
                text = "Only the assigned Class Teacher ($assignedClassTeacher) can record daily roll call for $selectedClass. Subject teachers cannot edit attendance.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // Daily Status Breakdown Summary Badges (Full Day, Half Day, On-Duty, Absent)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        DailyStatusFilterChip(
          label = "All (${totalInClass})",
          isSelected = statusFilter == null,
          color = SchoolNavyPrimary,
          onClick = { statusFilter = null },
          modifier = Modifier.weight(1f)
        )
        DailyStatusFilterChip(
          label = "FD (${fullDayCount})",
          isSelected = statusFilter == AttendanceStatus.FULL_DAY,
          color = SchoolAccentGreen,
          onClick = { statusFilter = if (statusFilter == AttendanceStatus.FULL_DAY) null else AttendanceStatus.FULL_DAY },
          modifier = Modifier.weight(1f)
        )
        DailyStatusFilterChip(
          label = "HD (${halfDayCount})",
          isSelected = statusFilter == AttendanceStatus.HALF_DAY,
          color = SchoolGold,
          onClick = { statusFilter = if (statusFilter == AttendanceStatus.HALF_DAY) null else AttendanceStatus.HALF_DAY },
          modifier = Modifier.weight(1f)
        )
        DailyStatusFilterChip(
          label = "OD (${onDutyCount})",
          isSelected = statusFilter == AttendanceStatus.ON_DUTY,
          color = SchoolAccentBlue,
          onClick = { statusFilter = if (statusFilter == AttendanceStatus.ON_DUTY) null else AttendanceStatus.ON_DUTY },
          modifier = Modifier.weight(1f)
        )
        DailyStatusFilterChip(
          label = "AB (${absentCount})",
          isSelected = statusFilter == AttendanceStatus.ABSENT,
          color = SchoolError,
          onClick = { statusFilter = if (statusFilter == AttendanceStatus.ABSENT) null else AttendanceStatus.ABSENT },
          modifier = Modifier.weight(1f)
        )
      }

      if (showSuccessSnackbar) {
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
              Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SchoolAccentGreen)
              Text(
                text = "Daily attendance saved & locked for $selectedClass by Class Teacher",
                style = MaterialTheme.typography.bodySmall,
                color = SchoolAccentGreen
              )
            }
            IconButton(onClick = { showSuccessSnackbar = false }, modifier = Modifier.size(24.dp)) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      // Students Daily Roll Call List
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(filteredRecords, key = { it.id }) { rec ->
          DailyAttendanceStudentRow(
            record = rec,
            isEditable = isClassTeacherForThisClass,
            onStatusChange = { newStatus ->
              if (isClassTeacherForThisClass) {
                onUpdateStatus(rec.studentId, newStatus)
              }
            }
          )
        }
      }

      // Save & Sync Button (Only enabled for authorized Class Teacher / Admin)
      if (isClassTeacherForThisClass) {
        Button(
          onClick = { showSuccessSnackbar = true },
          modifier = Modifier.fillMaxWidth().testTag("save_attendance_btn"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
        ) {
          Icon(imageVector = Icons.Default.Save, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Save & Submit Daily Roll Call ($selectedClass)")
        }
      } else {
        OutlinedButton(
          onClick = { /* No-op in view-only */ },
          enabled = false,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(imageVector = Icons.Default.Lock, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Attendance Editable by Class Teacher Only ($assignedClassTeacher)")
        }
      }
    }
  }
}

@Composable
fun DailyStatusFilterChip(
  label: String,
  isSelected: Boolean,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    shape = RoundedCornerShape(8.dp),
    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
    modifier = modifier.clickable { onClick() }
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 11.sp
      ),
      color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(vertical = 6.dp),
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
  }
}

/**
 * Row displaying student roll call with 4 daily options:
 * - Full Day (FD)
 * - Half Day (HD)
 * - On-Duty (OD)
 * - Absent (AB)
 */
@Composable
fun DailyAttendanceStudentRow(
  record: AttendanceRecord,
  isEditable: Boolean,
  onStatusChange: (AttendanceStatus) -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth().testTag("student_attendance_row_${record.rollNo}"),
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
        // Roll No & Student Name
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
              text = "Daily: ${record.status.description}",
              style = MaterialTheme.typography.labelSmall,
              color = Color(record.status.colorHex)
            )
          }
        }

        // 4 Status Action Badges (FD, HD, OD, AB)
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
                .clickable(enabled = isEditable) { onStatusChange(status) }
                .testTag("status_${status.code.lowercase()}_${record.rollNo}")
            ) {
              Text(
                text = status.code,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 11.sp
                ),
                color = if (isSelected) Color.White else if (isEditable) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      // Reason / Remarks if available (e.g. On-Duty event or medical note)
      if (record.notes.isNotBlank()) {
        Surface(
          color = when (record.status) {
            AttendanceStatus.ON_DUTY -> SchoolAccentBlue.copy(alpha = 0.08f)
            AttendanceStatus.HALF_DAY -> SchoolGold.copy(alpha = 0.08f)
            AttendanceStatus.ABSENT -> SchoolError.copy(alpha = 0.08f)
            else -> SchoolNavyPrimary.copy(alpha = 0.05f)
          },
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.fillMaxWidth()
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
              modifier = Modifier.size(14.dp)
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
