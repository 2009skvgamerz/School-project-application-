package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AttendanceEntity
import com.example.model.*
import com.example.ui.components.StatCard
import com.example.ui.theme.*

/**
 * AttendanceScreen - St. Joseph's Higher Secondary School Daily Attendance Register.
 *
 * Fully Dark-Mode Compliant & Re-architected with Material Design 3.
 *
 * Features:
 * 1. For Students: Personal attendance statistics, exam eligibility gauge, breakdown, and policy.
 * 2. For Educators & Staff: Interactive Class Roll Call with 4 daily options:
 *    - Full Day (FD, 1.0 weight)
 *    - Half Day (HD, 0.5 weight)
 *    - On-Duty (OD, 1.0 weight - for sports/science events)
 *    - Absent (AB, 0.0 weight - with medical/leave notes)
 * 3. Security & Access Control: ONLY the designated Homeroom Teacher can record or alter daily attendance.
 * 4. Instant Room Database persistence with live reactive feedback and search filtering.
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
  modifier: Modifier = Modifier,
  currentUser: User? = null,
  classes: List<SchoolClass> = emptyList()
) {
  var showSuccessBanner by remember { mutableStateOf(false) }
  var showLockedWarningDialog by remember { mutableStateOf<String?>(null) }
  var statusFilter by remember { mutableStateOf<AttendanceStatus?>(null) }
  var searchQuery by remember { mutableStateOf("") }
  var editingNoteStudentId by remember { mutableStateOf<String?>(null) }
  var noteStudentName by remember { mutableStateOf("") }
  var noteInputText by remember { mutableStateOf("") }
  var currentStudentStatusForNote by remember { mutableStateOf(AttendanceStatus.FULL_DAY) }

  // Classes list for selector
  val availableClasses = remember(classes) {
    if (classes.isNotEmpty()) {
      classes.map { "${it.name}-${it.section}" }.distinct()
    } else {
      listOf("Class 10-A", "Class 10-B", "Class 9-A", "Class 11-Science", "Class 12-A")
    }
  }

  // Determine the respected Homeroom Teacher for the currently selected class
  val homeroomTeacherName = remember(selectedClass, classes) {
    classes.find {
      val fullCls = "${it.name}-${it.section}"
      fullCls.equals(selectedClass, ignoreCase = true) ||
      "${it.name} ${it.section}".equals(selectedClass, ignoreCase = true) ||
      it.name.equals(selectedClass, ignoreCase = true)
    }?.classTeacherName ?: when (selectedClass.lowercase().trim()) {
      "class 10-a", "class 10 a" -> "Prof. Sarah Jenkins"
      "class 10-b", "class 10 b" -> "Mr. David Miller"
      "class 9-a", "class 9 a" -> "Mrs. Clara Higgins"
      "class 9-b", "class 9 b" -> "Dr. Anita Sharma"
      "class 11-science", "class 11 science" -> "Dr. Rachel Green"
      "class 12-science", "class 12 science" -> "Mr. Kevin Ross"
      "class 12-a", "class 12 a" -> "Prof. Sarah Jenkins"
      else -> "Assigned Homeroom Teacher"
    }
  }

  // Security Verification: ONLY the respected Homeroom Teacher for this specific class can take roll-call
  val isAuthorizedHomeroomTeacher = remember(userRole, teacherProfile, currentUser, selectedClass, homeroomTeacherName) {
    if (userRole != UserRole.TEACHER || teacherProfile == null) {
      false
    } else {
      val teacherName = currentUser?.fullName ?: teacherProfile.user.fullName
      val isNameMatch = teacherName.equals(homeroomTeacherName, ignoreCase = true)
      val isAssignedMatch = teacherProfile.classTeacherOf?.let {
        it.equals(selectedClass, ignoreCase = true) ||
        it.replace(" ", "").equals(selectedClass.replace(" ", ""), ignoreCase = true) ||
        it.replace("-", "").equals(selectedClass.replace("-", ""), ignoreCase = true)
      } ?: false
      teacherProfile.isClassTeacher && (isNameMatch || isAssignedMatch)
    }
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
      // 1. STUDENT ATTENDANCE REPORT VIEW (DARK-MODE OPTIMIZED)
      // ========================================================
      val profile = studentProfile
      val studentPercentage = profile?.attendancePercentage ?: 96.4
      val isExamEligible = studentPercentage >= 75.0

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        // Attendance Overview Card
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(18.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              // Circular Percentage Gauge
              Box(
                modifier = Modifier
                  .size(90.dp)
                  .clip(CircleShape)
                  .background(
                    if (isExamEligible) SchoolAccentGreen.copy(alpha = 0.15f)
                    else SchoolError.copy(alpha = 0.15f)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "${String.format("%.1f", studentPercentage)}%",
                    style = MaterialTheme.typography.titleLarge.copy(
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 20.sp
                    ),
                    color = if (isExamEligible) SchoolAccentGreen else SchoolError
                  )
                  Text(
                    text = "Overall",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = if (isExamEligible) SchoolAccentGreen else SchoolError
                  )
                }
              }

              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "Daily Roll Call Record",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Grade 10 - Section A • Roll #1",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                  color = if (isExamEligible) SchoolAccentGreen.copy(alpha = 0.14f) else SchoolError.copy(alpha = 0.14f),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                  ) {
                    Icon(
                      imageVector = if (isExamEligible) Icons.Default.CheckCircle else Icons.Default.Warning,
                      contentDescription = null,
                      tint = if (isExamEligible) SchoolAccentGreen else SchoolError,
                      modifier = Modifier.size(15.dp)
                    )
                    Text(
                      text = if (isExamEligible) "Eligible for Term Exams (Min. 75%)" else "Attendance Below 75% Requirement",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = if (isExamEligible) SchoolAccentGreen else SchoolError
                    )
                  }
                }
              }
            }
          }
        }

        // Academic Breakdown Header
        item {
          Text(
            text = "Academic Year Breakdown",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        // 4 Metric Breakdown Cards (Dark Mode Safe)
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

        // Subject-Wise Attendance Breakdown
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(
                text = "Subject-Wise Attendance",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )

              listOf(
                Triple("Mathematics", 98.0, SchoolNavyPrimaryDark),
                Triple("Physics", 96.5, SchoolAccentBlue),
                Triple("Chemistry", 95.0, SchoolEmerald),
                Triple("Computer Science", 100.0, SchoolGold),
                Triple("English Language", 94.0, SchoolAccentPurple)
              ).forEach { (subject, pct, color) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = subject,
                      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "${pct}%",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = color
                    )
                  }
                  LinearProgressIndicator(
                    progress = { (pct / 100.0).toFloat() },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                  )
                }
              }
            }
          }
        }

        // Institutional Attendance Guidelines
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = "Institutional Attendance Guidelines",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              Text(
                text = "• Attendance is marked daily at 08:30 AM by the assigned Homeroom Class Teacher.\n• On-Duty (OD) requires prior formal approval for official inter-school representation.\n• Minimum 75% attendance is required by the academic board for Term Examination hall ticket issuance.",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
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

      // Top Header Card with Dynamic Attendance Gauge & Homeroom Status
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
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
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(
                    if (isAuthorizedHomeroomTeacher) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else SchoolError.copy(alpha = 0.15f)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isAuthorizedHomeroomTeacher) Icons.AutoMirrored.Filled.FactCheck else Icons.Default.Lock,
                  contentDescription = null,
                  tint = if (isAuthorizedHomeroomTeacher) MaterialTheme.colorScheme.primary else SchoolError,
                  modifier = Modifier.size(24.dp)
                )
              }

              Column {
                Text(
                  text = "Daily Roll Call Register",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "$selectedClass • Homeroom: $homeroomTeacherName",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            // Mark All Full Day Button (Active for Homeroom Teacher)
            if (isAuthorizedHomeroomTeacher) {
              Button(
                onClick = {
                  onMarkAllFullDay(selectedClass)
                  showSuccessBanner = true
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = SchoolAccentGreen,
                  contentColor = Color.White
                ),
                modifier = Modifier.testTag("mark_all_present_btn")
              ) {
                Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("All Full Day", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }
            } else {
              OutlinedButton(
                onClick = {
                  showLockedWarningDialog = "Attendance taking is restricted. Only the designated Homeroom Teacher ($homeroomTeacherName) is authorized to take or alter daily roll call for $selectedClass."
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(
                  contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("mark_all_present_btn_locked")
              ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Locked", style = MaterialTheme.typography.labelSmall)
              }
            }
          }

          // Authorization Status Pill / Info Banner (Dark Mode High Contrast)
          if (isAuthorizedHomeroomTeacher) {
            Surface(
              color = SchoolAccentGreen.copy(alpha = 0.12f),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Verified,
                  contentDescription = null,
                  tint = SchoolAccentGreen,
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = "Authorized Homeroom Teacher ($homeroomTeacherName) • Live Room DB Synced • ${String.format("%.1f", presentRate)}% Present",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = SchoolAccentGreen
                )
              }
            }
          } else {
            Surface(
              color = SchoolGold.copy(alpha = 0.14f),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Lock,
                  contentDescription = null,
                  tint = SchoolGold,
                  modifier = Modifier.size(18.dp)
                )
                Column {
                  Text(
                    text = "Read-Only Mode: Attendance is Locked",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SchoolGold
                  )
                  Text(
                    text = "Only the assigned Homeroom Teacher ($homeroomTeacherName) can record or alter daily attendance for $selectedClass.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }

      // Class Selector Chips Strip
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        availableClasses.forEach { cls ->
          val isSelected = selectedClass.equals(cls, ignoreCase = true)
          val isUserHomeroomClass = teacherProfile?.classTeacherOf?.equals(cls, ignoreCase = true) == true

          FilterChip(
            selected = isSelected,
            onClick = {
              onSelectClass(cls)
              showSuccessBanner = false
            },
            label = {
              Text(
                text = if (isUserHomeroomClass) "$cls ★" else cls,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
              selectedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("class_chip_${cls.replace(" ", "_").lowercase()}")
          )
        }
      }

      // 4 Metric Breakdown Cards / Live Filter Badges (Dark Mode High Contrast)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

      // Modern Search Box
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = {
          Text(
            "Search student by name or roll #",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        leadingIcon = {
          Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
              Icon(
                Icons.Default.Close,
                contentDescription = "Clear search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("attendance_search_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface,
          focusedTextColor = MaterialTheme.colorScheme.onSurface,
          unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
      )

      // Success Notification Banner
      AnimatedVisibility(
        visible = showSuccessBanner,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Surface(
          color = SchoolAccentGreen.copy(alpha = 0.15f),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, SchoolAccentGreen.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SchoolAccentGreen, modifier = Modifier.size(18.dp))
              Text(
                text = "Daily attendance recorded & saved to Room Database for $selectedClass",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = SchoolAccentGreen
              )
            }
            IconButton(onClick = { showSuccessBanner = false }, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = SchoolAccentGreen, modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      // Interactive Students Roll Call List
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f)
      ) {
        if (filteredRecords.isEmpty()) {
          item {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  Icons.Default.PersonSearch,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(40.dp)
                )
                Text(
                  text = "No students match the criteria",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = if (searchQuery.isNotBlank()) "Try adjusting your search query \"$searchQuery\""
                  else "No students with status filter: ${statusFilter?.label}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }

        items(filteredRecords, key = { it.id }) { rec ->
          RollCallStudentCard(
            record = rec,
            isEditable = isAuthorizedHomeroomTeacher,
            onLockedAttempt = {
              showLockedWarningDialog = "Attendance marking is restricted. Only the respected Homeroom Teacher ($homeroomTeacherName) can record or modify attendance for $selectedClass."
            },
            onStatusSelect = { newStatus ->
              if (isAuthorizedHomeroomTeacher) {
                onUpdateStatus(rec.studentId, newStatus, rec.notes)
                showSuccessBanner = true
              } else {
                showLockedWarningDialog = "Only $homeroomTeacherName (Homeroom Teacher) is authorized to mark attendance for $selectedClass."
              }
            },
            onAddRemark = {
              if (isAuthorizedHomeroomTeacher) {
                editingNoteStudentId = rec.studentId
                noteStudentName = rec.studentName
                noteInputText = rec.notes
                currentStudentStatusForNote = rec.status
              } else {
                showLockedWarningDialog = "Only $homeroomTeacherName (Homeroom Teacher) is authorized to record attendance remarks for $selectedClass."
              }
            }
          )
        }
      }

      // Bottom Save / Sync Action Bar (Dark Mode Safe)
      if (isAuthorizedHomeroomTeacher) {
        Button(
          onClick = { showSuccessBanner = true },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("save_attendance_btn"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          )
        ) {
          Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Save & Submit Roll Call ($selectedClass)",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }
      } else {
        OutlinedButton(
          onClick = {
            showLockedWarningDialog = "Attendance modifications are disabled. Only $homeroomTeacherName (Homeroom Teacher) is authorized to submit roll call for $selectedClass."
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("save_attendance_btn_locked"),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        ) {
          Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Attendance Locked • Restricted to $homeroomTeacherName",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
          )
        }
      }
    }
  }

  // Security Restriction Alert Dialog (Dark Mode Theme Compliant)
  if (showLockedWarningDialog != null) {
    AlertDialog(
      onDismissRequest = { showLockedWarningDialog = null },
      containerColor = MaterialTheme.colorScheme.surface,
      icon = {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(SchoolError.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Lock, contentDescription = null, tint = SchoolError, modifier = Modifier.size(26.dp))
        }
      },
      title = {
        Text(
          text = "Homeroom Teacher Authorization Required",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )
      },
      text = {
        Column(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = showLockedWarningDialog ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Designated Homeroom Teacher: $homeroomTeacherName",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(10.dp),
              textAlign = TextAlign.Center
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showLockedWarningDialog = null },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Understood")
        }
      }
    )
  }

  // Modal Dialog for adding Remarks / Leave Notes / On-Duty Details (Dark Mode Safe)
  if (editingNoteStudentId != null) {
    AlertDialog(
      onDismissRequest = { editingNoteStudentId = null },
      containerColor = MaterialTheme.colorScheme.surface,
      icon = {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
      },
      title = {
        Text(
          "Record Reason / Remark",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "Student: $noteStudentName",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Status: ${currentStudentStatusForNote.description}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(currentStudentStatusForNote.colorHex)
              )
            }
          }

          OutlinedTextField(
            value = noteInputText,
            onValueChange = { noteInputText = it },
            placeholder = {
              Text(
                "e.g. Science Olympiad, Medical Leave, Approved OD",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("attendance_note_input"),
            maxLines = 3,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
              focusedTextColor = MaterialTheme.colorScheme.onSurface,
              unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
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
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Save Remark")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingNoteStudentId = null }) {
          Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    color = if (isSelected) color.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    shape = RoundedCornerShape(12.dp),
    border = if (isSelected) BorderStroke(1.5.dp, color) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
    modifier = modifier.clickable { onClick() }
  ) {
    Column(
      modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "$count",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
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
 *
 * When isEditable is false (non-homeroom teacher or other roles),
 * actions are locked and trigger security explanation dialog.
 */
@Composable
fun RollCallStudentCard(
  record: AttendanceRecord,
  onStatusSelect: (AttendanceStatus) -> Unit,
  onAddRemark: () -> Unit,
  isEditable: Boolean = true,
  onLockedAttempt: () -> Unit = {}
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("student_attendance_row_${record.rollNo}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
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
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape
          ) {
            Text(
              text = "#${record.rollNo}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          Column {
            Text(
              text = record.studentName,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = record.status.description,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = Color(record.status.colorHex)
              )
              if (!isEditable) {
                Icon(
                  imageVector = Icons.Default.Lock,
                  contentDescription = "Read only",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                  modifier = Modifier.size(12.dp)
                )
              }
            }
          }
        }

        // 4 Status Badges (FD, HD, OD, AB)
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
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
              color = when {
                isSelected -> statusColor
                !isEditable -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .clickable {
                  if (isEditable) {
                    onStatusSelect(status)
                  } else {
                    onLockedAttempt()
                  }
                }
                .testTag("status_${status.code.lowercase()}_${record.rollNo}")
            ) {
              Text(
                text = status.code,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 11.sp
                ),
                color = when {
                  isSelected -> Color.White
                  !isEditable -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                  else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)
              )
            }
          }

          // Remark / Notes Button
          IconButton(
            onClick = {
              if (isEditable) {
                onAddRemark()
              } else {
                onLockedAttempt()
              }
            },
            modifier = Modifier
              .size(32.dp)
              .testTag("note_btn_${record.rollNo}")
          ) {
            Icon(
              imageVector = if (record.notes.isNotBlank()) Icons.Default.Notes else Icons.Default.AddComment,
              contentDescription = "Add remark",
              tint = if (record.notes.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Notes Display if present (Dark Mode High Contrast)
      if (record.notes.isNotBlank()) {
        Surface(
          color = when (record.status) {
            AttendanceStatus.ON_DUTY -> SchoolAccentBlue.copy(alpha = 0.12f)
            AttendanceStatus.HALF_DAY -> SchoolGold.copy(alpha = 0.12f)
            AttendanceStatus.ABSENT -> SchoolError.copy(alpha = 0.12f)
            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
          },
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.dp, Color(record.status.colorHex).copy(alpha = 0.25f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              if (isEditable) onAddRemark() else onLockedAttempt()
            }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = if (record.status == AttendanceStatus.ON_DUTY) Icons.Default.EmojiEvents else Icons.Default.Notes,
              contentDescription = null,
              tint = Color(record.status.colorHex),
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = record.notes,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              color = Color(record.status.colorHex)
            )
          }
        }
      }
    }
  }
}
