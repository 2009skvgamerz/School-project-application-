package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
  currentUser: User,
  studentProfile: StudentProfile?,
  teacherProfile: TeacherProfile?,
  staffProfile: StaffProfile?,
  adminProfile: AdminProfile?,
  onSwitchRole: (UserRole) -> Unit,
  modifier: Modifier = Modifier,
  onNavigateToSettings: (() -> Unit)? = null,
  onSignOut: (() -> Unit)? = null
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("profile_screen")
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Identity Header Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(CircleShape)
              .background(SchoolNavyPrimary),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = currentUser.avatarInitials,
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = currentUser.name,
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = currentUser.email,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Surface(
            color = SchoolNavyPrimary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = when(currentUser.role) {
                  UserRole.STUDENT -> Icons.Default.School
                  UserRole.TEACHER -> Icons.Default.MenuBook
                  UserRole.STAFF -> Icons.Default.Engineering
                  UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                },
                contentDescription = null,
                tint = SchoolNavyPrimary,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "${currentUser.role.label} Account",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = SchoolNavyPrimary
              )
            }
          }

          if (onNavigateToSettings != null) {
            OutlinedButton(
              onClick = onNavigateToSettings,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_open_settings_btn"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("App Settings, Themes & Version Info")
            }
          }
        }
      }
    }

    // 2. Role Switcher Card (Demo & Multi-Role support)
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = SchoolNavyPrimary)
            Text(
              text = "Switch Active School Portal Role",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = SchoolNavyPrimary
            )
          }
          Text(
            text = "Easily test and experience customized dashboards for Students, Teachers, Staff, and Administrators.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            UserRole.values().forEach { role ->
              val isSelected = currentUser.role == role
              OutlinedButton(
                onClick = { onSwitchRole(role) },
                modifier = Modifier.weight(1f).testTag("switch_to_${role.name.lowercase()}"),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = if (isSelected) SchoolNavyPrimary else Color.Transparent,
                  contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                ),
                contentPadding = PaddingValues(4.dp)
              ) {
                Text(
                  text = role.label,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                  ),
                  maxLines = 1
                )
              }
            }
          }
        }
      }
    }

    // 3. Detailed Institutional Information (Role-Specific)
    item {
      Text(
        text = "Academic & Institutional Records",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = SchoolNavyPrimary
      )
    }

    when (currentUser.role) {
      UserRole.STUDENT -> {
        studentProfile?.let { p ->
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                ProfileInfoRow(label = "Student Roll No", value = "#${p.rollNo}")
                ProfileInfoRow(label = "Current Class & Section", value = "Class ${p.grade}-${p.section}")
                ProfileInfoRow(label = "Assigned House", value = p.houseName)
                ProfileInfoRow(label = "Academic Year", value = p.academicYear)
                ProfileInfoRow(label = "Parent / Guardian Contact", value = "${p.parentName} (${p.parentPhone})")
                ProfileInfoRow(label = "Emergency Contact", value = p.emergencyContact)
              }
            }
          }
        }
      }

      UserRole.TEACHER -> {
        teacherProfile?.let { p ->
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                ProfileInfoRow(label = "Employee ID", value = p.employeeId)
                ProfileInfoRow(label = "Department", value = p.department)
                ProfileInfoRow(label = "Qualifications", value = p.qualification)
                ProfileInfoRow(label = "Teaching Subjects", value = p.teachingSubjects.joinToString(", "))
                ProfileInfoRow(label = "Assigned Classes", value = p.assignedClasses.joinToString(", "))
                ProfileInfoRow(label = "Date Joined", value = p.joiningDate)
              }
            }
          }
        }
      }

      UserRole.STAFF -> {
        staffProfile?.let { p ->
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                ProfileInfoRow(label = "Staff ID", value = p.staffId)
                ProfileInfoRow(label = "Department", value = p.department)
                ProfileInfoRow(label = "Work Shift", value = p.shiftTiming)
                ProfileInfoRow(label = "Campus Duty Location", value = p.locationArea)
                ProfileInfoRow(label = "Emergency Response Role", value = p.emergencyRole)
              }
            }
          }
        }
      }

      UserRole.ADMIN -> {
        adminProfile?.let { p ->
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                ProfileInfoRow(label = "Admin ID", value = p.adminId)
                ProfileInfoRow(label = "Designation", value = p.adminRole)
                ProfileInfoRow(label = "Office Location", value = p.officeLocation)
                ProfileInfoRow(label = "Institutional Permissions", value = p.systemPermissions.joinToString(" • "))
              }
            }
          }
        }
      }
    }

    // 4. School Details Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "St. Joseph's School Campus",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = SchoolNavyPrimary
          )
          Text(
            text = "St. Joseph's High School Campus, Museum Road, Bengaluru, Karnataka 560025\nGeneral Enquiries: +91 (080) 2221-4416 • info@stjosephs.edu\nIT Helpdesk: ithelpdesk@stjosephs.edu",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    if (onSignOut != null) {
      item {
        OutlinedButton(
          onClick = onSignOut,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("sign_out_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SchoolError
          ),
          border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(SchoolError.copy(alpha = 0.5f))
          )
        ) {
          Icon(
            imageVector = Icons.Default.Logout,
            contentDescription = "Sign Out",
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Sign Out to Login Screen",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
