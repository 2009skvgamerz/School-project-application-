package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DirectoryCategory
import com.example.model.DirectoryContact
import com.example.model.UserRole
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavyDark
import com.example.ui.theme.SchoolNavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryScreen(
  contacts: List<DirectoryContact>,
  userRole: UserRole,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<DirectoryCategory?>(null) }
  var selectedContactForDetails by remember { mutableStateOf<DirectoryContact?>(null) }

  val isStudentViewer = userRole == UserRole.STUDENT

  val filteredContacts = remember(contacts, searchQuery, selectedCategory) {
    contacts.filter { contact ->
      val matchesSearch = searchQuery.isBlank() ||
        contact.name.contains(searchQuery, ignoreCase = true) ||
        contact.designation.contains(searchQuery, ignoreCase = true) ||
        contact.departmentOrGrade.contains(searchQuery, ignoreCase = true) ||
        contact.email.contains(searchQuery, ignoreCase = true)

      val matchesCategory = selectedCategory == null || contact.category == selectedCategory

      matchesSearch && matchesCategory
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("directory_screen")
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 1. HERO HEADER WITH PRIVACY POLICY BADGE
      Card(
        modifier = Modifier.fillMaxWidth().testTag("directory_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              brush = Brush.horizontalGradient(
                colors = listOf(SchoolNavyDark, SchoolNavyPrimary)
              )
            )
            .padding(16.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.ContactPhone,
                  contentDescription = null,
                  tint = SchoolGold,
                  modifier = Modifier.size(24.dp)
                )
              }
              Column {
                Text(
                  text = "Institutional Directory",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = Color.White
                )
                Text(
                  text = "Verified Faculty, Staff, Transport & Helpline Contacts",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.8f)
                )
              }
            }

            // PRIVACY NOTICE PILL
            Surface(
              color = if (isStudentViewer) Color(0xFF1E3A8A).copy(alpha = 0.6f) else Color(0xFF065F46).copy(alpha = 0.6f),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = if (isStudentViewer) Icons.Default.Security else Icons.Default.VerifiedUser,
                  contentDescription = null,
                  tint = if (isStudentViewer) SchoolGold else Color(0xFF34D399),
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = if (isStudentViewer) {
                    "Privacy Shield Active: Student phone numbers are protected. Staff & Faculty numbers remain reachable."
                  } else {
                    "Full Access Mode: Authorized personnel directory view with complete emergency contacts."
                  },
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                  color = Color.White
                )
              }
            }
          }
        }
      }

      // 2. SEARCH BAR
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search by name, department, role...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear search")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth().testTag("directory_search_input")
      )

      // 3. CATEGORY FILTER CHIPS
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("directory_category_filters")
      ) {
        item(key = "dir_cat_all") {
          FilterChip(
            selected = selectedCategory == null,
            onClick = { selectedCategory = null },
            label = { Text("All (${contacts.size})") },
            leadingIcon = {
              if (selectedCategory == null) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
              }
            }
          )
        }

        items(DirectoryCategory.entries.filter { it != DirectoryCategory.ALL }, key = { it.name }) { category ->
          val count = contacts.count { it.category == category }
          val isSelected = selectedCategory == category

          FilterChip(
            selected = isSelected,
            onClick = { selectedCategory = if (isSelected) null else category },
            label = { Text("${category.label} ($count)") },
            leadingIcon = {
              Icon(
                imageVector = when (category) {
                  DirectoryCategory.HELPLINE -> Icons.Default.Emergency
                  DirectoryCategory.FACULTY -> Icons.Default.School
                  DirectoryCategory.ADMINISTRATION -> Icons.Default.AccountBalance
                  DirectoryCategory.STAFF -> Icons.Default.Engineering
                  DirectoryCategory.TRANSPORT -> Icons.Default.DirectionsBus
                  DirectoryCategory.STUDENTS -> Icons.Default.Group
                  DirectoryCategory.ALL -> Icons.Default.ContactPhone
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) SchoolNavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          )
        }
      }

      // 4. CONTACTS LIST
      if (filteredContacts.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PersonOff,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(48.dp)
            )
            Text(
              text = "No contacts found matching criteria",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag("directory_contacts_lazy_column"),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredContacts, key = { it.id }) { contact ->
            DirectoryContactCard(
              contact = contact,
              isStudentViewer = isStudentViewer,
              onClick = { selectedContactForDetails = contact },
              onCall = {
                if (!contact.isStudent || !isStudentViewer) {
                  launchDialer(context, contact.phoneNumber)
                }
              },
              onEmail = {
                launchEmail(context, contact.email, contact.name)
              }
            )
          }
        }
      }
    }
  }

  // Contact Detail Bottom Dialog
  selectedContactForDetails?.let { contact ->
    DirectoryContactDetailDialog(
      contact = contact,
      isStudentViewer = isStudentViewer,
      onDismiss = { selectedContactForDetails = null },
      onCall = { launchDialer(context, contact.phoneNumber) },
      onEmail = { launchEmail(context, contact.email, contact.name) }
    )
  }
}

@Composable
fun DirectoryContactCard(
  contact: DirectoryContact,
  isStudentViewer: Boolean,
  onClick: () -> Unit,
  onCall: () -> Unit,
  onEmail: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isMasked = contact.isStudent && isStudentViewer
  val isEmergency = contact.category == DirectoryCategory.HELPLINE

  Card(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isEmergency) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
    modifier = modifier.fillMaxWidth().testTag("directory_contact_card_${contact.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Avatar Icon
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(
            when (contact.category) {
              DirectoryCategory.HELPLINE -> Color(0xFFDC2626).copy(alpha = 0.15f)
              DirectoryCategory.FACULTY -> SchoolNavyPrimary.copy(alpha = 0.12f)
              DirectoryCategory.ADMINISTRATION -> SchoolGold.copy(alpha = 0.25f)
              DirectoryCategory.TRANSPORT -> Color(0xFF059669).copy(alpha = 0.15f)
              DirectoryCategory.STUDENTS -> Color(0xFF6366F1).copy(alpha = 0.15f)
              DirectoryCategory.STAFF -> Color(0xFFD97706).copy(alpha = 0.15f)
              DirectoryCategory.ALL -> SchoolNavyPrimary.copy(alpha = 0.12f)
            }
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = when (contact.category) {
            DirectoryCategory.HELPLINE -> Icons.Default.Emergency
            DirectoryCategory.FACULTY -> Icons.Default.School
            DirectoryCategory.ADMINISTRATION -> Icons.Default.AccountBalance
            DirectoryCategory.TRANSPORT -> Icons.Default.DirectionsBus
            DirectoryCategory.STUDENTS -> Icons.Default.Person
            DirectoryCategory.STAFF -> Icons.Default.Engineering
            DirectoryCategory.ALL -> Icons.Default.ContactPhone
          },
          contentDescription = null,
          tint = when (contact.category) {
            DirectoryCategory.HELPLINE -> Color(0xFFDC2626)
            DirectoryCategory.FACULTY -> SchoolNavyPrimary
            DirectoryCategory.ADMINISTRATION -> SchoolNavyDark
            DirectoryCategory.TRANSPORT -> Color(0xFF059669)
            DirectoryCategory.STUDENTS -> Color(0xFF4F46E5)
            DirectoryCategory.STAFF -> Color(0xFFB45309)
            DirectoryCategory.ALL -> SchoolNavyPrimary
          },
          modifier = Modifier.size(22.dp)
        )
      }

      // Details Body
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = contact.name,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Text(
          text = contact.designation,
          style = MaterialTheme.typography.bodySmall.copy(
            color = SchoolNavyPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = if (isMasked) Icons.Default.Lock else Icons.Default.Phone,
            contentDescription = null,
            tint = if (isMasked) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(13.dp)
          )
          Text(
            text = if (isMasked) "Hidden (Student Privacy)" else contact.phoneNumber,
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              color = if (isMasked) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = if (isMasked) FontWeight.Bold else FontWeight.Normal
            )
          )
        }
      }

      // Quick Action Buttons
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        // Call Action
        if (isMasked) {
          Surface(
            color = Color(0xFFF3F4F6),
            shape = CircleShape,
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.Lock, contentDescription = "Privacy Protected", tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
          }
        } else {
          IconButton(
            onClick = onCall,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(if (isEmergency) Color(0xFFDC2626) else SchoolNavyPrimary)
              .testTag("call_contact_${contact.id}")
          ) {
            Icon(Icons.Default.Phone, contentDescription = "Call Contact", tint = Color.White, modifier = Modifier.size(18.dp))
          }
        }

        // Email Action
        IconButton(
          onClick = onEmail,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag("email_contact_${contact.id}")
        ) {
          Icon(Icons.Default.MailOutline, contentDescription = "Email Contact", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

@Composable
fun DirectoryContactDetailDialog(
  contact: DirectoryContact,
  isStudentViewer: Boolean,
  onDismiss: () -> Unit,
  onCall: () -> Unit,
  onEmail: () -> Unit
) {
  val isMasked = contact.isStudent && isStudentViewer

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(SchoolNavyPrimary.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Person, contentDescription = null, tint = SchoolNavyPrimary)
        }
        Column {
          Text(text = contact.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
          Text(text = contact.category.label, style = MaterialTheme.typography.labelSmall, color = SchoolNavyPrimary)
        }
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DetailRowItem(icon = Icons.Default.Badge, label = "Designation", value = contact.designation)
        DetailRowItem(icon = Icons.Default.CorporateFare, label = "Department / Grade", value = contact.departmentOrGrade)
        DetailRowItem(icon = Icons.Default.MeetingRoom, label = "Room / Bay", value = contact.roomOrLocation)
        DetailRowItem(icon = Icons.Default.Email, label = "Official Email", value = contact.email)

        if (isMasked) {
          Surface(
            color = Color(0xFFFEF2F2),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
              Text(
                text = "Phone numbers of peer students are masked to safeguard personal privacy.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color(0xFF991B1B))
              )
            }
          }
        } else {
          DetailRowItem(icon = Icons.Default.Phone, label = "Phone Number", value = contact.phoneNumber)
          contact.parentContact?.let { parentPhone ->
            DetailRowItem(icon = Icons.Default.FamilyRestroom, label = "Parent Emergency Phone", value = parentPhone)
          }
          contact.bloodGroup?.let { bg ->
            DetailRowItem(icon = Icons.Default.Bloodtype, label = "Blood Group", value = bg)
          }
        }
      }
    },
    confirmButton = {
      if (!isMasked) {
        Button(
          onClick = onCall,
          colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
        ) {
          Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Call Now")
        }
      } else {
        Button(
          onClick = onEmail,
          colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
        ) {
          Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Send Email")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

@Composable
private fun DetailRowItem(icon: ImageVector, label: String, value: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = SchoolNavyPrimary, modifier = Modifier.size(16.dp))
    Text(text = "$label: ", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(1f),
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
  }
}

private fun launchDialer(context: Context, phoneNumber: String) {
  try {
    val intent = Intent(Intent.ACTION_DIAL).apply {
      data = Uri.parse("tel:$phoneNumber")
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
  } catch (_: Exception) {}
}

private fun launchEmail(context: Context, emailAddress: String, recipientName: String) {
  try {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
      data = Uri.parse("mailto:$emailAddress")
      putExtra(Intent.EXTRA_SUBJECT, "Query regarding St. Joseph Matriculation Hr. Sec. School")
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
  } catch (_: Exception) {}
}
