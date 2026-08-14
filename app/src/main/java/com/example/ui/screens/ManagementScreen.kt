package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.model.SchoolClass
import com.example.model.UserRole
import com.example.ui.theme.SchoolNavyPrimary

data class DirectoryMember(
  val name: String,
  val role: UserRole,
  val departmentOrClass: String,
  val contact: String,
  val status: String
)

@Composable
fun ManagementScreen(
  classes: List<SchoolClass>,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilterRole by remember { mutableStateOf<UserRole?>(null) }

  val sampleMembers = remember {
    listOf(
      DirectoryMember("Aarav Sharma", UserRole.STUDENT, "Class 10-A (Roll #14)", "parent.sharma@example.com", "Active"),
      DirectoryMember("Diya Patel", UserRole.STUDENT, "Class 10-A (Roll #08)", "parent.patel@example.com", "Active"),
      DirectoryMember("Rohan Gupta", UserRole.STUDENT, "Class 10-A (Roll #22)", "parent.gupta@example.com", "Medical Leave"),
      DirectoryMember("Prof. Emily Watson", UserRole.TEACHER, "Physics & Science Dept", "e.watson@grandviewacademy.edu", "Active"),
      DirectoryMember("Dr. Robert Sterling", UserRole.TEACHER, "Mathematics Dept", "r.sterling@grandviewacademy.edu", "Active"),
      DirectoryMember("Anita Desai", UserRole.TEACHER, "English Literature", "a.desai@grandviewacademy.edu", "Active"),
      DirectoryMember("Marcus Reynolds", UserRole.STAFF, "Campus Facilities & Safety", "m.reynolds@grandviewacademy.edu", "On Duty"),
      DirectoryMember("David Miller", UserRole.STAFF, "Transport & Logistics", "d.miller@grandviewacademy.edu", "On Duty"),
      DirectoryMember("Dr. Arthur Pendelton", UserRole.ADMIN, "Principal & Academic Dean", "principal@grandviewacademy.edu", "Active")
    )
  }

  val filteredMembers = sampleMembers.filter { member ->
    (selectedFilterRole == null || member.role == selectedFilterRole) &&
    (searchQuery.isBlank() || member.name.contains(searchQuery, ignoreCase = true) || member.departmentOrClass.contains(searchQuery, ignoreCase = true))
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("management_screen")
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Institutional Directory & Governance",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = SchoolNavyPrimary
    )

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      label = { Text("Search by name, class, or department") },
      leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
          }
        }
      },
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth().testTag("directory_search_input")
    )

    // Role filter chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      FilterChip(
        selected = selectedFilterRole == null,
        onClick = { selectedFilterRole = null },
        label = { Text("All (${sampleMembers.size})") }
      )
      UserRole.values().forEach { role ->
        FilterChip(
          selected = selectedFilterRole == role,
          onClick = { selectedFilterRole = role },
          label = { Text(role.label) }
        )
      }
    }

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(filteredMembers) { member ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                  when(member.role) {
                    UserRole.STUDENT -> Color(0xFF2563EB)
                    UserRole.TEACHER -> Color(0xFF059669)
                    UserRole.STAFF -> Color(0xFF7C3AED)
                    UserRole.ADMIN -> Color(0xFF1E293B)
                  }.copy(alpha = 0.15f)
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = when(member.role) {
                  UserRole.STUDENT -> Icons.Default.School
                  UserRole.TEACHER -> Icons.Default.MenuBook
                  UserRole.STAFF -> Icons.Default.Engineering
                  UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                },
                contentDescription = null,
                tint = when(member.role) {
                  UserRole.STUDENT -> Color(0xFF2563EB)
                  UserRole.TEACHER -> Color(0xFF059669)
                  UserRole.STAFF -> Color(0xFF7C3AED)
                  UserRole.ADMIN -> Color(0xFF1E293B)
                },
                modifier = Modifier.size(22.dp)
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = member.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = member.departmentOrClass,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = member.contact,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = member.status,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = SchoolNavyPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
        }
      }
    }
  }
}
