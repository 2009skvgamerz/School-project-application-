package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.model.*
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavyPrimary

@Composable
fun NoticeDetailDialog(
  notice: Notice,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    },
    title = {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = Color(notice.category.colorHex).copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = notice.category.label,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = Color(notice.category.colorHex),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Text(
            text = notice.date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Text(
          text = notice.title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = notice.content,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Text(
          text = "Issued by: ${notice.publisherName}",
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
          color = SchoolNavyPrimary
        )
        Text(
          text = "Designation: ${notice.publisherRole}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (notice.attachmentName != null) {
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = "PDF",
                tint = Color(0xFFDC2626)
              )
              Column {
                Text(
                  text = notice.attachmentName,
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "Official PDF Document (Tap to view)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }
  )
}

@Composable
fun AddNoticeDialog(
  onDismiss: () -> Unit,
  onAddNotice: (title: String, content: String, category: NoticeCategory, isUrgent: Boolean) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var content by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(NoticeCategory.GENERAL) }
  var isUrgent by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Publish School Notice",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Notice Title") },
          modifier = Modifier.fillMaxWidth().testTag("notice_title_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Notice Description / Circular") },
          modifier = Modifier.fillMaxWidth().testTag("notice_content_input"),
          minLines = 3,
          maxLines = 5
        )

        Text("Select Category:", style = MaterialTheme.typography.labelMedium)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(NoticeCategory.GENERAL, NoticeCategory.ACADEMIC, NoticeCategory.EVENT, NoticeCategory.SPORTS).forEach { cat ->
            FilterChip(
              selected = selectedCategory == cat,
              onClick = { selectedCategory = cat },
              label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Checkbox(
            checked = isUrgent,
            onCheckedChange = { isUrgent = it },
            modifier = Modifier.testTag("notice_urgent_checkbox")
          )
          Text("Mark as High Priority / Urgent Broadcast")
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank() && content.isNotBlank()) {
            onAddNotice(title, content, selectedCategory, isUrgent)
            onDismiss()
          }
        },
        enabled = title.isNotBlank() && content.isNotBlank(),
        modifier = Modifier.testTag("publish_notice_confirm_btn")
      ) {
        Text("Publish")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun AssignHomeworkDialog(
  onDismiss: () -> Unit,
  onAssign: (title: String, desc: String, subject: String, className: String, dueDate: String, marks: Int) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var subject by remember { mutableStateOf("Physics") }
  var className by remember { mutableStateOf("Class 10-A") }
  var dueDate by remember { mutableStateOf("Tomorrow, 09:00 AM") }
  var marksText by remember { mutableStateOf("20") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Assign New Homework",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Assignment Title") },
          modifier = Modifier.fillMaxWidth().testTag("hw_title_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Instructions & Exercises") },
          modifier = Modifier.fillMaxWidth().testTag("hw_desc_input"),
          minLines = 3
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = className,
            onValueChange = { className = it },
            label = { Text("Class") },
            modifier = Modifier.weight(1f)
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = dueDate,
            onValueChange = { dueDate = it },
            label = { Text("Due Date & Time") },
            modifier = Modifier.weight(1.5f)
          )
          OutlinedTextField(
            value = marksText,
            onValueChange = { marksText = it },
            label = { Text("Max Marks") },
            modifier = Modifier.weight(1f)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank() && description.isNotBlank()) {
            val marks = marksText.toIntOrNull() ?: 20
            onAssign(title, description, subject, className, dueDate, marks)
            onDismiss()
          }
        },
        enabled = title.isNotBlank() && description.isNotBlank(),
        modifier = Modifier.testTag("assign_hw_confirm_btn")
      ) {
        Text("Assign to Class")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun SubmitHomeworkDialog(
  homework: Homework,
  onDismiss: () -> Unit,
  onSubmit: (note: String) -> Unit
) {
  var submissionNote by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Submit Homework",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = homework.title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
          color = SchoolNavyPrimary
        )
        Text(
          text = "Subject: ${homework.subjectName}  |  Due: ${homework.dueDate}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = submissionNote,
          onValueChange = { submissionNote = it },
          label = { Text("Submission Notes / Link / Solution summary") },
          modifier = Modifier.fillMaxWidth().testTag("submission_notes_input"),
          minLines = 3,
          placeholder = { Text("Completed exercises 1 to 4 with calculations...") }
        )

        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CloudUpload,
              contentDescription = "Upload",
              tint = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Attach photo / PDF document (Simulated)",
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSubmit(if (submissionNote.isBlank()) "Submitted on time" else submissionNote)
          onDismiss()
        },
        modifier = Modifier.testTag("submit_hw_confirm_btn")
      ) {
        Text("Confirm Submission")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun AddDutyDialog(
  onDismiss: () -> Unit,
  onAddDuty: (title: String, area: String, time: String, priority: DutyPriority) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var area by remember { mutableStateOf("") }
  var time by remember { mutableStateOf("02:00 PM") }
  var priority by remember { mutableStateOf(DutyPriority.MEDIUM) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Add Campus Duty / Task",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Task Description") },
          modifier = Modifier.fillMaxWidth().testTag("duty_title_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = area,
          onValueChange = { area = it },
          label = { Text("Campus Location / Block") },
          modifier = Modifier.fillMaxWidth().testTag("duty_area_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = time,
          onValueChange = { time = it },
          label = { Text("Scheduled Time") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Text("Priority:", style = MaterialTheme.typography.labelMedium)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          DutyPriority.values().forEach { p ->
            FilterChip(
              selected = priority == p,
              onClick = { priority = p },
              label = { Text(p.label) }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank() && area.isNotBlank()) {
            onAddDuty(title, area, time, priority)
            onDismiss()
          }
        },
        enabled = title.isNotBlank() && area.isNotBlank(),
        modifier = Modifier.testTag("add_duty_confirm_btn")
      ) {
        Text("Save Task")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun QuickRoleSwitcherDialog(
  currentRole: UserRole,
  onDismiss: () -> Unit,
  onSelectRole: (UserRole) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Switch Demo Persona",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Quickly test different role views and permissions for the Science Expo demo:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val displayRoles = UserRole.values().filter { it != UserRole.DEVELOPER }

        displayRoles.forEach { role ->
          val isCurrent = role == currentRole
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onSelectRole(role)
                onDismiss()
              },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isCurrent) SchoolNavyPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
            )
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(
                  imageVector = when(role) {
                    UserRole.STUDENT -> Icons.Default.School
                    UserRole.TEACHER -> Icons.Default.MenuBook
                    UserRole.STAFF -> Icons.Default.Engineering
                    UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                    UserRole.DEVELOPER -> Icons.Default.Terminal
                  },
                  contentDescription = null,
                  tint = Color(role.badgeColor)
                )
                Column {
                  Text(
                    text = role.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                  )
                  Text(
                    text = when(role) {
                      UserRole.STUDENT -> "Keerthivasan (12-A)"
                      UserRole.TEACHER -> "Prof. Sarah Jenkins (Science)"
                      UserRole.STAFF -> "Mr. Thomas Wright (Facilities)"
                      UserRole.ADMIN -> "Dr. Arthur Pendelton (Principal)"
                      UserRole.DEVELOPER -> "Alex Rivera (Developer God Mode)"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              if (isCurrent) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Active",
                  tint = SchoolNavyPrimary
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
