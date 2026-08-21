package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun StudentIdCard(
  profile: StudentProfile,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column {
      // Top header of ID Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            brush = Brush.horizontalGradient(
              listOf(SchoolNavyPrimary, Color(0xFF1E3A8A))
            )
          )
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(2.dp),
              contentAlignment = Alignment.Center
            ) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .clip(CircleShape)
                  .background(SchoolNavyPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.School,
                  contentDescription = "School Logo",
                  tint = SchoolGold,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Column {
              Text(
                text = "ST. JOSEPH'S SCHOOL",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                ),
                color = Color.White
              )
              Text(
                text = "STUDENT IDENTITY CARD • 2026-2027",
                style = MaterialTheme.typography.labelSmall,
                color = SchoolGoldLight
              )
            }
          }

          Surface(
            color = Color(0xFF2563EB),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = "STUDENT",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }
      }

      // Body of ID Card
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Photo Avatar Placeholder with badge
        Column(
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(SchoolNavyPrimary.copy(alpha = 0.1f))
              .border(2.dp, SchoolNavyPrimary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Face,
              contentDescription = "Student Photo",
              tint = SchoolNavyPrimary,
              modifier = Modifier.size(54.dp)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            color = SchoolGold.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "Roll: #${profile.rollNo}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = SchoolGoldDark,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        // Details Column
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = profile.user.fullName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Class: ${profile.grade}-${profile.section}  |  Adm: ${profile.admissionNo}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "House: ${profile.houseName}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF2563EB),
            fontWeight = FontWeight.Medium
          )
          Text(
            text = "Blood Group: ${profile.bloodGroup}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Emergency: ${profile.parentPhone}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // Footer with motto and bar code simulation
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "\"Shine and Let Shine\"",
          style = MaterialTheme.typography.labelSmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.QrCode,
            contentDescription = "Digital Code",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = profile.admissionNo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun StaffIdCard(
  user: User,
  employeeId: String,
  department: String,
  designation: String,
  role: UserRole,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column {
      // Header
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            brush = Brush.horizontalGradient(
              listOf(SchoolNavyPrimary, Color(role.badgeColor))
            )
          )
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(2.dp),
              contentAlignment = Alignment.Center
            ) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .clip(CircleShape)
                  .background(SchoolNavyPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.School,
                  contentDescription = "School Logo",
                  tint = SchoolGold,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Column {
              Text(
                text = "ST. JOSEPH'S SCHOOL",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
              )
              Text(
                text = "FACULTY & STAFF IDENTITY CARD",
                style = MaterialTheme.typography.labelSmall,
                color = SchoolGoldLight
              )
            }
          }

          Surface(
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = role.displayName.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }
      }

      // Body
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(role.badgeColor).copy(alpha = 0.1f))
            .border(2.dp, Color(role.badgeColor), RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when(role) {
              UserRole.TEACHER -> Icons.Default.MenuBook
              UserRole.STAFF -> Icons.Default.Engineering
              UserRole.ADMIN -> Icons.Default.AdminPanelSettings
              else -> Icons.Default.Badge
            },
            contentDescription = "Staff Photo",
            tint = Color(role.badgeColor),
            modifier = Modifier.size(50.dp)
          )
        }

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = user.fullName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = designation,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = Color(role.badgeColor)
          )
          Text(
            text = "Emp ID: $employeeId",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Dept: $department",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Email: ${user.email}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Official Institution Pass",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = SchoolAccentGreen,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "Authorized Personnel",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = SchoolAccentGreen
          )
        }
      }
    }
  }
}
