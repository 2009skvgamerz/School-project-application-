package com.example.ui.screens

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

@Composable
fun AttendanceScreen(
  userRole: UserRole,
  studentProfile: StudentProfile?,
  attendanceRecords: List<AttendanceRecord>,
  selectedClass: String,
  onSelectClass: (String) -> Unit,
  onUpdateStatus: (String, AttendanceStatus) -> Unit,
  onMarkAllPresent: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showSuccessSnackbar by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("attendance_screen")
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    if (userRole == UserRole.STUDENT) {
      // STUDENT ATTENDANCE VIEW
      val profile = studentProfile ?: return
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        // Overall Attendance Card
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
                  .size(80.dp)
                  .clip(CircleShape)
                  .background(SchoolAccentGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "${profile.attendancePercentage}%",
                    style = MaterialTheme.typography.titleLarge.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 18.sp
                    ),
                    color = SchoolAccentGreen
                  )
                  Text(
                    text = "Present",
                    style = MaterialTheme.typography.labelSmall,
                    color = SchoolAccentGreen
                  )
                }
              }

              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "Overall Attendance Record",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "Required Minimum: 75%",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                  color = SchoolAccentGreen.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = "Status: Eligible for Examinations",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SchoolAccentGreen,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }

        // Summary Breakdown
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            StatCard(
              title = "Present Days",
              value = "82 Days",
              subtitle = "Out of 86 total",
              icon = Icons.Default.CheckCircle,
              accentColor = SchoolAccentGreen,
              modifier = Modifier.weight(1f)
            )
            StatCard(
              title = "Absent Days",
              value = "3 Days",
              subtitle = "1 Medical leave",
              icon = Icons.Default.Cancel,
              accentColor = Color(0xFFDC2626),
              modifier = Modifier.weight(1f)
            )
          }
        }

        // Subject-wise Breakdown
        item {
          Text(
            text = "Subject-Wise Attendance",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = SchoolNavyPrimary
          )
        }

        val subjectAttendance = listOf(
          Triple("Physics", "96.4%", 0xFF2563EB),
          Triple("Mathematics", "95.0%", 0xFF059669),
          Triple("Chemistry", "92.8%", 0xFF7C3AED),
          Triple("English Literature", "98.0%", 0xFFD97706),
          Triple("Computer Science", "94.5%", 0xFF0284C7),
          Triple("Physical Education", "100%", 0xFF10B981)
        )

        items(subjectAttendance) { (sub, pct, colorHex) ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(colorHex))
                )
                Text(
                  text = sub,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
              }

              Surface(
                color = Color(colorHex).copy(alpha = 0.12f),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = pct,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color(colorHex),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }
          }
        }
      }
    } else {
      // TEACHER / ADMIN / STAFF MARK ATTENDANCE VIEW
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Class Attendance Register",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = SchoolNavyPrimary
          )
          Text(
            text = "Date: Today • $selectedClass",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = {
            onMarkAllPresent()
            showSuccessSnackbar = true
          },
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("mark_all_present_btn")
        ) {
          Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("All Present", style = MaterialTheme.typography.labelSmall)
        }
      }

      // Class Selector Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("Class 10-A", "Class 10-B", "Class 9-A", "Class 11-Sci").forEach { cls ->
          FilterChip(
            selected = selectedClass == cls,
            onClick = { onSelectClass(cls) },
            label = { Text(cls) }
          )
        }
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
                text = "Attendance saved successfully for $selectedClass",
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

      // Students Attendance Roster
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(attendanceRecords) { rec ->
          AttendanceStudentRow(
            record = rec,
            onStatusChange = { newStatus -> onUpdateStatus(rec.studentId, newStatus) }
          )
        }
      }

      Button(
        onClick = { showSuccessSnackbar = true },
        modifier = Modifier.fillMaxWidth().testTag("save_attendance_btn"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(imageVector = Icons.Default.Save, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Save & Sync Attendance Register")
      }
    }
  }
}

@Composable
fun AttendanceStudentRow(
  record: AttendanceRecord,
  onStatusChange: (AttendanceStatus) -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
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
          if (record.notes.isNotBlank()) {
            Text(
              text = record.notes,
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFDC2626)
            )
          }
        }
      }

      // Toggle status buttons (P, A, L)
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(
          AttendanceStatus.PRESENT,
          AttendanceStatus.ABSENT,
          AttendanceStatus.LATE
        ).forEach { status ->
          val isSelected = record.status == status
          Surface(
            color = if (isSelected) Color(status.colorHex) else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .clickable { onStatusChange(status) }
          ) {
            Text(
              text = status.label.take(1),
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
              ),
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }
    }
  }
}
