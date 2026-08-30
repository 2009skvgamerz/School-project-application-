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
  val roleGradient = when (user.role) {
    UserRole.STUDENT -> listOf(Color(0xFF1A73E8), Color(0xFF4285F4), Color(0xFF174EA6))
    UserRole.TEACHER -> listOf(Color(0xFF1E8E3E), Color(0xFF34A853), Color(0xFF137333))
    UserRole.STAFF -> listOf(Color(0xFF9334E6), Color(0xFFA142F4), Color(0xFF7627BB))
    UserRole.DRIVER -> listOf(Color(0xFFE8710A), Color(0xFFFA903E), Color(0xFFB05000))
    UserRole.ADMIN -> listOf(Color(0xFFEA8600), Color(0xFFF9AB00), Color(0xFFB06000))
    UserRole.DEVELOPER -> listOf(Color(0xFF0F9D58), Color(0xFF00796B), Color(0xFF004D40))
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag)
      .then(if (onProfileClick != null) Modifier.clickable { onProfileClick() } else Modifier),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.linearGradient(
            colors = roleGradient
          )
        )
        .padding(20.dp)
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
          // Google Classroom Style Avatar
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.22f))
              .border(2.5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
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
              text = "Welcome to St. Joseph's,",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
              ),
              color = Color.White.copy(alpha = 0.85f)
            )
            Text(
              text = user.fullName,
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
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
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White.copy(alpha = 0.92f),
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
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color.White.copy(alpha = 0.25f)
            ) {
              Text(
                text = user.role.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 11.sp
                ),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }

            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color.White.copy(alpha = 0.2f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF81C995))
                )
                Text(
                  text = statusLabel,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp
                  ),
                  color = Color.White
                )
              }
            }
          }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.22f))

        // Bottom Row: Current School Session Pill
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.testTag("${testTag}_session")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
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
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color.White.copy(alpha = 0.2f),
              modifier = Modifier.clickable { onProfileClick() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "View Profile",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  ),
                  color = Color.White
                )
                Icon(
                  imageVector = Icons.Default.ChevronRight,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
