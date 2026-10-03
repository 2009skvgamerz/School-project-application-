package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.SchoolClass
import com.example.model.TeacherProfile
import com.example.model.UserRole
import com.example.ui.components.GradebookEntryDialog
import com.example.ui.theme.SchoolAccentGreen
import com.example.ui.theme.SchoolNavyPrimary

@Composable
fun ClassesScreen(
  classes: List<SchoolClass>,
  onOpenAttendanceForClass: (String) -> Unit,
  onOpenAssignHomework: () -> Unit,
  modifier: Modifier = Modifier,
  userRole: UserRole = UserRole.TEACHER,
  teacherProfile: TeacherProfile? = null
) {
  var selectedClassForGradebook by remember { mutableStateOf<SchoolClass?>(null) }
  var gradebookStatusMessage by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("classes_screen")
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Academic Classes & Homeroom Rosters",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    gradebookStatusMessage?.let { msg ->
      Surface(
        color = Color(0xFF059669).copy(alpha = 0.15f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
          Text(msg, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = Color(0xFF059669))
        }
      }
    }

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(classes, key = { it.id }) { cls ->
        val fullClassName = "${cls.name}-${cls.section}"
        val isClassTeacherForThis = when (userRole) {
          UserRole.DEVELOPER -> true
          UserRole.ADMIN -> true
          UserRole.TEACHER -> {
            val homeroom = teacherProfile?.classTeacherOf ?: "Class 10-A"
            (teacherProfile?.isClassTeacher == true) && (
              homeroom.equals(fullClassName, ignoreCase = true) ||
              homeroom.replace("-", " ").equals(fullClassName.replace("-", " "), ignoreCase = true) ||
              teacherProfile.user.fullName.equals(cls.classTeacherName, ignoreCase = true)
            )
          }
          else -> false
        }

        Card(
          shape = RoundedCornerShape(14.dp),
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
              Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = "${cls.name} - Section ${cls.section}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  if (isClassTeacherForThis) {
                    Surface(
                      color = SchoolAccentGreen.copy(alpha = 0.15f),
                      shape = RoundedCornerShape(4.dp)
                    ) {
                      Text(
                        text = "Your Homeroom",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SchoolAccentGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
                Text(
                  text = "Room: ${cls.roomNo}  •  Class Teacher: ${cls.classTeacherName}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Surface(
                color = SchoolAccentGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = "${cls.averageAttendance}% Avg Present",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = SchoolAccentGreen,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Total Enrolled: ${cls.totalStudents} Students",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              )
              Text(
                text = "Floor 2, Academic Block",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Teacher Gradebook Marks Entry Button
              OutlinedButton(
                onClick = { selectedClassForGradebook = cls },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("gradebook_entry_btn_${cls.id}")
              ) {
                Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Marks & Grades", style = MaterialTheme.typography.labelSmall)
              }

              Spacer(modifier = Modifier.width(6.dp))

              OutlinedButton(
                onClick = onOpenAssignHomework,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(imageVector = Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Assign HW", style = MaterialTheme.typography.labelSmall)
              }

              Spacer(modifier = Modifier.width(6.dp))

              Button(
                onClick = { onOpenAttendanceForClass(fullClassName) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                colors = if (isClassTeacherForThis) {
                  ButtonDefaults.buttonColors(containerColor = SchoolAccentGreen)
                } else {
                  ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
                }
              ) {
                Icon(
                  imageVector = if (isClassTeacherForThis) Icons.Default.FactCheck else Icons.Default.Visibility,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isClassTeacherForThis) "Daily Roll Call" else "View (Read-Only)",
                  style = MaterialTheme.typography.labelSmall
                )
              }
            }
          }
        }
      }
    }
  }

  // Gradebook Dialog
  selectedClassForGradebook?.let { targetClass ->
    GradebookEntryDialog(
      schoolClass = targetClass,
      onDismiss = { selectedClassForGradebook = null },
      onSaveMarks = { subject, exam, marks ->
        gradebookStatusMessage = "Grades recorded for $subject ($exam) in ${targetClass.name}-${targetClass.section} • Student GPA updated!"
        selectedClassForGradebook = null
      }
    )
  }
}
