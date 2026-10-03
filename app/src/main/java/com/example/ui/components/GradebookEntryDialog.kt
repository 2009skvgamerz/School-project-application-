package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.SchoolClass
import com.example.ui.theme.*

data class StudentMarkEntry(
  val studentId: String,
  val studentName: String,
  val rollNo: Int,
  var marks: Double,
  var maxMarks: Double = 100.0,
  var gradeLetter: String = "A"
)

/**
 * GradebookEntryDialog
 *
 * Teacher-facing marks and grade entry console for classroom evaluations.
 * Enables teachers to:
 * - Select subject (Mathematics, Physics, Chemistry, etc.)
 * - Select examination milestone (Unit Test 1, Quarterly Exam, Mid-Term, Half-Yearly)
 * - Enter test scores with automatic grade letter calculation (A+, A, B+, B, C, F)
 * - Save directly into classroom records feeding student GPA displays
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradebookEntryDialog(
  schoolClass: SchoolClass,
  onDismiss: () -> Unit,
  onSaveMarks: (subject: String, exam: String, marksList: List<StudentMarkEntry>) -> Unit
) {
  var selectedSubject by remember { mutableStateOf("Physics") }
  var selectedExam by remember { mutableStateOf("Unit Test 2 (Term 1)") }

  val subjects = listOf("Physics", "Mathematics", "Chemistry", "Computer Science", "English Language")
  val examTypes = listOf("Unit Test 1", "Quarterly Exam", "Unit Test 2 (Term 1)", "Mid-Term", "Half-Yearly")

  // Mock initial roster of students for this class
  var studentMarks by remember {
    mutableStateOf(
      listOf(
        StudentMarkEntry("std_1", "Alex Johnson", 1, 94.0, 100.0, "A+"),
        StudentMarkEntry("std_2", "Keerthivasan", 2, 98.0, 100.0, "A+"),
        StudentMarkEntry("std_3", "Priya Sharma", 3, 91.5, 100.0, "A+"),
        StudentMarkEntry("std_4", "Rohan Mehta", 4, 85.0, 100.0, "A"),
        StudentMarkEntry("std_5", "Sneha Patel", 5, 88.0, 100.0, "A"),
        StudentMarkEntry("std_6", "Vikram Singh", 6, 76.5, 100.0, "B+"),
        StudentMarkEntry("std_7", "Ananya Iyer", 7, 93.0, 100.0, "A+"),
        StudentMarkEntry("std_8", "Deepak Verma", 8, 82.0, 100.0, "A")
      )
    )
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .fillMaxHeight(0.88f)
        .testTag("gradebook_entry_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Header
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
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF059669).copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Assessment,
                contentDescription = null,
                tint = Color(0xFF059669),
                modifier = Modifier.size(22.dp)
              )
            }
            Column {
              Text(
                text = "Teacher Gradebook & Marks Entry",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${schoolClass.name}-${schoolClass.section} • Class Teacher: ${schoolClass.classTeacherName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Subject & Exam Selectors
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Subject Dropdown
          var subjectDropdownExpanded by remember { mutableStateOf(false) }
          Box(modifier = Modifier.weight(1f)) {
            OutlinedCard(
              onClick = { subjectDropdownExpanded = true },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("SUBJECT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                  Text(selectedSubject, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
              }
            }

            DropdownMenu(
              expanded = subjectDropdownExpanded,
              onDismissRequest = { subjectDropdownExpanded = false }
            ) {
              subjects.forEach { subj ->
                DropdownMenuItem(
                  text = { Text(subj) },
                  onClick = {
                    selectedSubject = subj
                    subjectDropdownExpanded = false
                  }
                )
              }
            }
          }

          // Exam Dropdown
          var examDropdownExpanded by remember { mutableStateOf(false) }
          Box(modifier = Modifier.weight(1.2f)) {
            OutlinedCard(
              onClick = { examDropdownExpanded = true },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("EVALUATION", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                  Text(selectedExam, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 1)
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
              }
            }

            DropdownMenu(
              expanded = examDropdownExpanded,
              onDismissRequest = { examDropdownExpanded = false }
            ) {
              examTypes.forEach { exam ->
                DropdownMenuItem(
                  text = { Text(exam) },
                  onClick = {
                    selectedExam = exam
                    examDropdownExpanded = false
                  }
                )
              }
            }
          }
        }

        // Student Marks Entry Roster
        Text(
          text = "STUDENT MARKS ROSTER (MAX 100)",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(studentMarks, key = { it.studentId }) { item ->
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
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
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  modifier = Modifier.weight(1.5f)
                ) {
                  Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text("#${item.rollNo}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                  }

                  Text(
                    text = item.studentName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }

                // Marks Entry Field
                var marksText by remember(item.marks) { mutableStateOf(item.marks.toString()) }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  OutlinedTextField(
                    value = marksText,
                    onValueChange = { newText ->
                      marksText = newText
                      val parsed = newText.toDoubleOrNull()
                      if (parsed != null && parsed in 0.0..100.0) {
                        item.marks = parsed
                        item.gradeLetter = when {
                          parsed >= 90.0 -> "A+"
                          parsed >= 80.0 -> "A"
                          parsed >= 70.0 -> "B+"
                          parsed >= 60.0 -> "B"
                          parsed >= 50.0 -> "C"
                          else -> "F"
                        }
                      }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(80.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedContainerColor = MaterialTheme.colorScheme.surface,
                      unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                  )

                  Surface(
                    color = when (item.gradeLetter) {
                      "A+" -> Color(0xFF059669).copy(alpha = 0.15f)
                      "A" -> Color(0xFF2563EB).copy(alpha = 0.15f)
                      "B+" -> Color(0xFFD97706).copy(alpha = 0.15f)
                      else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.width(36.dp)
                  ) {
                    Text(
                      text = item.gradeLetter,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = when (item.gradeLetter) {
                        "A+" -> Color(0xFF059669)
                        "A" -> Color(0xFF2563EB)
                        "B+" -> Color(0xFFD97706)
                        else -> MaterialTheme.colorScheme.onSurface
                      },
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(vertical = 6.dp)
                    )
                  }
                }
              }
            }
          }
        }

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Cancel")
          }

          Button(
            onClick = {
              onSaveMarks(selectedSubject, selectedExam, studentMarks)
              onDismiss()
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF059669),
              contentColor = Color.White
            ),
            modifier = Modifier
              .weight(1.5f)
              .testTag("submit_gradebook_btn")
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save & Update GPA", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
