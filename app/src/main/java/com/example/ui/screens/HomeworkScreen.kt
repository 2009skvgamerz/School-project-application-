package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.sp
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
          color = MaterialTheme.colorScheme.onSurface
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

    // Homework Item List with Animated Content
    AnimatedContent(
      targetState = selectedTab,
      transitionSpec = {
        if (targetState > initialState) {
          (slideInHorizontally(
            initialOffsetX = { (it * 0.2f).toInt() },
            animationSpec = tween(280, easing = FastOutSlowInEasing)
          ) + fadeIn(animationSpec = tween(260)))
            .togetherWith(
              slideOutHorizontally(
                targetOffsetX = { -(it * 0.2f).toInt() },
                animationSpec = tween(240, easing = FastOutSlowInEasing)
              ) + fadeOut(animationSpec = tween(200))
            )
        } else {
          (slideInHorizontally(
            initialOffsetX = { -(it * 0.2f).toInt() },
            animationSpec = tween(280, easing = FastOutSlowInEasing)
          ) + fadeIn(animationSpec = tween(260)))
            .togetherWith(
              slideOutHorizontally(
                targetOffsetX = { (it * 0.2f).toInt() },
                animationSpec = tween(240, easing = FastOutSlowInEasing)
              ) + fadeOut(animationSpec = tween(200))
            )
        }
      },
      label = "homework_tab_transition",
      modifier = Modifier.fillMaxSize()
    ) { currentTabIdx ->
      val displayedList = if (currentTabIdx == 0) pendingHomeworks else completedHomeworks

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
              text = if (currentTabIdx == 0) "No pending homework! Great job!" else "No completed submissions found.",
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
          items(displayedList, key = { it.id }) { hw ->
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
}

@Composable
fun HomeworkCard(
  homework: Homework,
  userRole: UserRole,
  onSubmitClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val subjectColor = when {
    homework.subjectName.contains("Math", ignoreCase = true) -> Color(0xFF1A73E8)
    homework.subjectName.contains("Physic", ignoreCase = true) || homework.subjectName.contains("Scien", ignoreCase = true) -> Color(0xFF0D9488)
    homework.subjectName.contains("Chem", ignoreCase = true) -> Color(0xFF7C3AED)
    homework.subjectName.contains("Eng", ignoreCase = true) -> Color(0xFFD97706)
    else -> MaterialTheme.colorScheme.primary
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
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
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            color = subjectColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = homework.subjectName,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
              color = subjectColor,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = homework.className,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
            )
          }
        }

        Surface(
          color = when(homework.status) {
            HomeworkStatus.PENDING -> SchoolGold.copy(alpha = 0.15f)
            HomeworkStatus.SUBMITTED -> Color(0xFF1A73E8).copy(alpha = 0.15f)
            HomeworkStatus.EVALUATED -> SchoolAccentGreen.copy(alpha = 0.15f)
          },
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = homework.status.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = when(homework.status) {
              HomeworkStatus.PENDING -> SchoolGoldDark
              HomeworkStatus.SUBMITTED -> Color(0xFF1A73E8)
              HomeworkStatus.EVALUATED -> SchoolAccentGreen
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Text(
        text = homework.title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        ),
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = homework.description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (homework.submissionNote.isNotBlank()) {
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Text(
              text = homework.submissionNote,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = if (homework.status == HomeworkStatus.PENDING) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
          )
          Column {
            Text(
              text = "Due: ${homework.dueDate}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (homework.status == HomeworkStatus.PENDING) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "${homework.teacherName} • ${homework.maxMarks} Marks",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        if (userRole == UserRole.STUDENT && homework.status == HomeworkStatus.PENDING) {
          Button(
            onClick = onSubmitClick,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            )
          ) {
            Text("Submit", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }
        } else if (userRole == UserRole.TEACHER) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = SchoolAccentGreen.copy(alpha = 0.12f)
          ) {
            Text(
              text = "${homework.submissionsCount}/${homework.totalStudents} Submissions",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = SchoolAccentGreen,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }
  }
}
