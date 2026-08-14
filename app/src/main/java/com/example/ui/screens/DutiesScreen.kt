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
import com.example.model.DutyPriority
import com.example.model.DutyStatus
import com.example.model.DutyTask
import com.example.ui.dashboard.DutyTaskItemCard
import com.example.ui.theme.SchoolNavyPrimary

@Composable
fun DutiesScreen(
  duties: List<DutyTask>,
  onUpdateDutyStatus: (String, DutyStatus) -> Unit,
  onOpenAddDutyDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(0) }

  val filteredDuties = when (selectedTab) {
    0 -> duties
    1 -> duties.filter { it.status == DutyStatus.PENDING || it.status == DutyStatus.IN_PROGRESS }
    else -> duties.filter { it.status == DutyStatus.COMPLETED }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("duties_screen")
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Campus Operations & Duties",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = SchoolNavyPrimary
        )
        Text(
          text = "${duties.size} operational tasks assigned today",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Button(
        onClick = onOpenAddDutyDialog,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("add_duty_btn")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add Duty", style = MaterialTheme.typography.labelSmall)
      }
    }

    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      divider = {}
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("All (${duties.size})") }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("In Progress / Pending") }
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        text = { Text("Completed") }
      )
    }

    if (filteredDuties.isEmpty()) {
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
            imageVector = Icons.Default.DoneAll,
            contentDescription = null,
            tint = SchoolNavyPrimary,
            modifier = Modifier.size(40.dp)
          )
          Text(
            text = "No duty tasks found in this section.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredDuties) { duty ->
          DutyTaskItemCard(
            duty = duty,
            onStatusChange = { newStatus -> onUpdateDutyStatus(duty.id, newStatus) }
          )
        }
      }
    }
  }
}
