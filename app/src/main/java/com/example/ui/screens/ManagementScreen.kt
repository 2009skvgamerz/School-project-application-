package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.model.SystemUserRecord
import com.example.model.UserRole
import com.example.ui.components.EditUserDialog
import com.example.ui.theme.SchoolNavyPrimary
import com.example.viewmodel.SchoolViewModel

@Composable
fun ManagementScreen(
  classes: List<SchoolClass>,
  modifier: Modifier = Modifier,
  viewModel: SchoolViewModel? = null
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilterRole by remember { mutableStateOf<UserRole?>(null) }
  var userToEdit by remember { mutableStateOf<SystemUserRecord?>(null) }
  var showAddUserDialog by remember { mutableStateOf(false) }

  val systemUsers by viewModel?.systemUsers?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
  val currentUser by viewModel?.currentUser?.collectAsState() ?: remember { mutableStateOf(null) }
  val isDeveloper = currentUser?.role == UserRole.DEVELOPER
  val isAdmin = currentUser?.role == UserRole.ADMIN

  val displayList = if (systemUsers.isNotEmpty()) {
    systemUsers.filter { member ->
      (selectedFilterRole == null || member.role == selectedFilterRole) &&
      (searchQuery.isBlank() ||
          member.fullName.contains(searchQuery, ignoreCase = true) ||
          member.email.contains(searchQuery, ignoreCase = true) ||
          member.designation.contains(searchQuery, ignoreCase = true) ||
          member.departmentOrGrade.contains(searchQuery, ignoreCase = true))
    }
  } else {
    emptyList()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("management_screen")
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Institutional Directory & Master Records",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = SchoolNavyPrimary
        )
        if (isDeveloper) {
          Text(
            text = "⚡ Developer God Mode: Full edit & override permissions active",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF059669)
          )
        }
      }

      if (isDeveloper && viewModel != null) {
        FilledTonalButton(
          onClick = { showAddUserDialog = true },
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
            contentColor = Color(0xFF059669)
          ),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.testTag("management_add_user_btn")
        ) {
          Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add User", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
      }
    }

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      label = { Text("Search by name, class, designation, or email") },
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
    val visibleFilterRoles = if (isDeveloper) {
      UserRole.values().toList()
    } else {
      UserRole.values().filter { it != UserRole.DEVELOPER }
    }

    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      item {
        FilterChip(
          selected = selectedFilterRole == null,
          onClick = { selectedFilterRole = null },
          label = { Text("All (${systemUsers.size})") }
        )
      }
      items(visibleFilterRoles) { role ->
        FilterChip(
          selected = selectedFilterRole == role,
          onClick = { selectedFilterRole = if (selectedFilterRole == role) null else role },
          label = { Text(role.displayName) }
        )
      }
    }

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(displayList, key = { it.id }) { member ->
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
                .background(Color(member.role.badgeColor).copy(alpha = 0.18f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = when(member.role) {
                  UserRole.STUDENT -> Icons.Default.School
                  UserRole.TEACHER -> Icons.Default.MenuBook
                  UserRole.STAFF -> Icons.Default.Engineering
                  UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                  UserRole.DEVELOPER -> Icons.Default.Terminal
                },
                contentDescription = null,
                tint = Color(member.role.badgeColor),
                modifier = Modifier.size(22.dp)
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = member.fullName,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Surface(
                  color = Color(member.role.badgeColor).copy(alpha = 0.15f),
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = member.role.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(member.role.badgeColor),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Text(
                text = "${member.designation.ifBlank { member.departmentOrGrade }} • ${member.email}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "ID: ${member.identifier} • Ph: ${member.phone}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            if (isDeveloper && viewModel != null) {
              IconButton(
                onClick = { userToEdit = member },
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF059669)),
                modifier = Modifier.testTag("management_edit_user_${member.username}")
              ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit User", modifier = Modifier.size(20.dp))
              }
            } else {
              Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "Active",
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

  // Edit User Dialog
  if (userToEdit != null && viewModel != null) {
    EditUserDialog(
      userRecord = userToEdit,
      isNewUser = false,
      onDismiss = { userToEdit = null },
      onSave = { updated ->
        viewModel.updateSystemUser(updated)
      },
      onDelete = { id ->
        viewModel.deleteSystemUser(id)
      }
    )
  }

  // Add User Dialog
  if (showAddUserDialog && viewModel != null) {
    EditUserDialog(
      userRecord = null,
      isNewUser = true,
      onDismiss = { showAddUserDialog = false },
      onSave = { newUser ->
        viewModel.addSystemUser(newUser)
      }
    )
  }
}
