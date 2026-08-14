package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun HomeworkScreen(
  userRole: UserRole,
  homeworks: List<Homework>,
  onSubmitHomework: (Homework) -> Unit,
  onOpenAssignDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(0) }
  val pendingHomeworks = homeworks.filter { it.status == HomeworkStatus.PENDING }
  val completedHomeworks = homeworks.filter { it.status != HomeworkStatus.PENDING }

  val displayedList = if (selectedTab == 0) pendingHomeworks else completedHomeworks

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("homework_screen")
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = if (userRole == UserRole.STUDENT) "Homework & Assignments" else "Classroom Assignments",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = SchoolNavyPrimary
        )
        Text(
          text = "${homeworks.size} assignments listed",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (userRole == UserRole.TEACHER || userRole == UserRole.ADMIN) {
        Button(
          onClick = onOpenAssignDialog,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("assign_hw_top_btn")
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("New HW", style = MaterialTheme.typography.labelSmall)
        }
      }
    }

    // Tabs for Pending vs Submitted/Completed
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      divider = {}
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Text(
            text = "Pending (${pendingHomeworks.size})",
            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
          )
        }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Text(
            text = "Submitted / Done (${completedHomeworks.size})",
            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
          )
        }
      )
    }

    // Homework Item List
    if (displayedList.isEmpty()) {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AssignmentTurnedIn,
            contentDescription = null,
            tint = SchoolAccentGreen,
            modifier = Modifier.size(40.dp)
          )
          Text(
            text = if (selectedTab == 0) "No pending homework! Great job!" else "No completed submissions found.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(displayedList) { hw ->
          HomeworkCard(
            homework = hw,
            userRole = userRole,
            onSubmitClick = { onSubmitHomework(hw) }
          )
        }
      }
    }
  }
}

@Composable
fun HomeworkCard(
  homework: Homework,
  userRole: UserRole,
  onSubmitClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
            color = SchoolNavyPrimary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = homework.subjectName,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = SchoolNavyPrimary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          Text(
            text = homework.className,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          color = when(homework.status) {
            HomeworkStatus.PENDING -> SchoolGold.copy(alpha = 0.15f)
            HomeworkStatus.SUBMITTED -> Color(0xFF2563EB).copy(alpha = 0.15f)
            HomeworkStatus.EVALUATED -> SchoolAccentGreen.copy(alpha = 0.15f)
          },
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = homework.status.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = when(homework.status) {
              HomeworkStatus.PENDING -> SchoolGoldDark
              HomeworkStatus.SUBMITTED -> Color(0xFF2563EB)
              HomeworkStatus.EVALUATED -> SchoolAccentGreen
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Text(
        text = homework.title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = homework.description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (homework.submissionNote.isNotBlank()) {
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = SchoolNavyPrimary)
            Text(
              text = homework.submissionNote,
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Due: ${homework.dueDate}",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (homework.status == HomeworkStatus.PENDING) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Teacher: ${homework.teacherName}  •  ${homework.maxMarks} Marks",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (userRole == UserRole.STUDENT && homework.status == HomeworkStatus.PENDING) {
          Button(
            onClick = onSubmitClick,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Submit", style = MaterialTheme.typography.labelSmall)
          }
        } else if (userRole == UserRole.TEACHER) {
          Text(
            text = "${homework.submissionsCount}/${homework.totalStudents} Submitted",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = SchoolAccentGreen
          )
        }
      }
    }
  }
}
