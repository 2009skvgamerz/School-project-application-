package com.example.ui.components

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
import com.example.model.SystemUserRecord
import com.example.model.UserRole
import com.example.ui.theme.SchoolNavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(
  userRecord: SystemUserRecord?,
  isNewUser: Boolean = false,
  onDismiss: () -> Unit,
  onSave: (SystemUserRecord) -> Unit,
  onDelete: ((String) -> Unit)? = null
) {
  var fullName by remember(userRecord) { mutableStateOf(userRecord?.fullName ?: "") }
  var username by remember(userRecord) { mutableStateOf(userRecord?.username ?: "") }
  var email by remember(userRecord) { mutableStateOf(userRecord?.email ?: "") }
  var phone by remember(userRecord) { mutableStateOf(userRecord?.phone ?: "+91 ") }
  var role by remember(userRecord) { mutableStateOf(userRecord?.role ?: UserRole.STUDENT) }
  var designation by remember(userRecord) { mutableStateOf(userRecord?.designation ?: "") }
  var identifier by remember(userRecord) { mutableStateOf(userRecord?.identifier ?: "") }
  var departmentOrGrade by remember(userRecord) { mutableStateOf(userRecord?.departmentOrGrade ?: "") }
  var sectionOrRoom by remember(userRecord) { mutableStateOf(userRecord?.sectionOrRoom ?: "") }
  var extraNotes by remember(userRecord) { mutableStateOf(userRecord?.extraNotes ?: "") }

  var roleDropdownExpanded by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = if (isNewUser) Icons.Default.PersonAdd else Icons.Default.Edit,
          contentDescription = null,
          tint = Color(0xFF059669)
        )
        Text(
          text = if (isNewUser) "Create New User Record" else "Edit User: ${userRecord?.fullName ?: ""}",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Surface(
          color = Color(0xFF10B981).copy(alpha = 0.12f),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = Color(0xFF059669),
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "Developer Root Privilege: You can modify user identities, roles, credentials, and records in real-time.",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF065F46)
            )
          }
        }

        // Full Name
        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = { Text("Full Name") },
          leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("edit_user_fullname_input")
        )

        // Username & Identifier
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("edit_user_username_input")
          )
          OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            label = { Text("ID / Reg No.") },
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("edit_user_id_input")
          )
        }

        // Role Dropdown Selector
        ExposedDropdownMenuBox(
          expanded = roleDropdownExpanded,
          onExpandedChange = { roleDropdownExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = role.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("User System Role") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = roleDropdownExpanded,
            onDismissRequest = { roleDropdownExpanded = false }
          ) {
            UserRole.values().forEach { r ->
              DropdownMenuItem(
                text = {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Surface(
                      color = Color(r.badgeColor),
                      shape = RoundedCornerShape(4.dp),
                      modifier = Modifier.size(12.dp)
                    ) {}
                    Text(r.displayName)
                  }
                },
                onClick = {
                  role = r
                  roleDropdownExpanded = false
                }
              )
            }
          }
        }

        // Email & Phone
        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email Address") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("edit_user_email_input")
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number") },
          leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("edit_user_phone_input")
        )

        // Designation / Department / Section
        OutlinedTextField(
          value = designation,
          onValueChange = { designation = it },
          label = { Text("Designation / Title") },
          leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = departmentOrGrade,
            onValueChange = { departmentOrGrade = it },
            label = { Text("Grade / Dept") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = sectionOrRoom,
            onValueChange = { sectionOrRoom = it },
            label = { Text("Section / Room") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        OutlinedTextField(
          value = extraNotes,
          onValueChange = { extraNotes = it },
          label = { Text("System & Developer Notes") },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val record = SystemUserRecord(
            id = userRecord?.id ?: "usr_${System.currentTimeMillis()}",
            username = username.ifBlank { "user_${System.currentTimeMillis()}" },
            fullName = fullName.ifBlank { "Unknown User" },
            email = email.ifBlank { "user@stjosephs.edu" },
            role = role,
            phone = phone,
            designation = designation,
            identifier = identifier,
            departmentOrGrade = departmentOrGrade,
            sectionOrRoom = sectionOrRoom,
            extraNotes = extraNotes
          )
          onSave(record)
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary),
        modifier = Modifier.testTag("save_user_button")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (isNewUser) "Create User" else "Save Changes")
      }
    },
    dismissButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!isNewUser && userRecord != null && onDelete != null && userRecord.role != UserRole.DEVELOPER) {
          TextButton(
            onClick = {
              onDelete(userRecord.id)
              onDismiss()
            },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Delete")
          }
        }
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )
}
