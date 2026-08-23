package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.theme.*

/**
 * Top-level User Profile Header component that displays:
 * 1. User's Name
 * 2. Profile Icon / Avatar with Role Badge
 * 3. Current School Session (e.g. "Academic Session 2026–2027")
 * 4. Role badge chip and active session indicator
 *
 * Designed using Material 3 for placement at the top of each role-based dashboard.
 */
@Composable
fun UserProfileHeader(
  user: User,
  modifier: Modifier = Modifier,
  schoolSession: String = "Academic Session 2026–2027",
  subtitle: String? = null,
  statusLabel: String = "Active Session",
  onProfileClick: (() -> Unit)? = null,
  testTag: String = "user_profile_header"
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag)
      .then(if (onProfileClick != null) Modifier.clickable { onProfileClick() } else Modifier),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.horizontalGradient(
            colors = listOf(
              SchoolNavyPrimary,
              Color(0xFF1E40AF),
              Color(0xFF1E3A8A)
            )
          )
        )
        .padding(18.dp)
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Top Row: Avatar + Name & Context + Role Badge & Status
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Profile Icon / Avatar
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.2f))
              .border(2.dp, SchoolGoldLight.copy(alpha = 0.8f), CircleShape)
              .testTag("${testTag}_avatar"),
            contentAlignment = Alignment.Center
          ) {
            val roleIcon: ImageVector = when (user.role) {
              UserRole.STUDENT -> Icons.Default.School
              UserRole.TEACHER -> Icons.Default.MenuBook
              UserRole.STAFF -> Icons.Default.Engineering
              UserRole.DRIVER -> Icons.Default.DirectionsBus
              UserRole.ADMIN -> Icons.Default.AdminPanelSettings
              UserRole.DEVELOPER -> Icons.Default.Terminal
            }

            Icon(
              imageVector = roleIcon,
              contentDescription = "Profile Icon for ${user.fullName}",
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }

          // User Name & Subtitle Details
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Text(
              text = "Welcome,",
              style = MaterialTheme.typography.labelMedium,
              color = Color.White.copy(alpha = 0.75f)
            )
            Text(
              text = user.fullName,
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
              ),
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("${testTag}_name")
            )

            val cleanSubtitle = subtitle
              ?.replace("Bus #Bus #", "Bus #")
              ?.replace("Bus #Bus ", "Bus #")

            if (!cleanSubtitle.isNullOrBlank()) {
              Text(
                text = cleanSubtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = SchoolGoldLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            } else {
              Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // Role Badge Chip + Status Indicator
          Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            RoleBadge(role = user.role)

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = SchoolAccentGreen.copy(alpha = 0.25f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(SchoolAccentGreen)
                )
                Text(
                  text = statusLabel,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                  ),
                  color = Color.White
                )
              }
            }
          }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

        // Bottom Row: Current School Session Pill
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.testTag("${testTag}_session")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = SchoolGoldLight,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = schoolSession,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.5.sp
                ),
                color = Color.White
              )
            }
          }

          if (onProfileClick != null) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = "View Profile",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = SchoolGoldLight
              )
              Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SchoolGoldLight,
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }
    }
  }
}
