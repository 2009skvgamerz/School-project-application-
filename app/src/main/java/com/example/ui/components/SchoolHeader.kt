package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.theme.*

@Composable
fun SchoolTopBar(
  user: User?,
  unreadNotificationCount: Int = 0,
  networkState: com.example.util.NetworkState? = null,
  onNotificationsClick: () -> Unit = {},
  onProfileClick: () -> Unit = {}
) {
  Surface(
    color = SchoolNavyPrimary,
    tonalElevation = 4.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White)
            .padding(2.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.school_logo),
            contentDescription = "St. Joseph's Emblem",
            modifier = Modifier.fillMaxSize().clip(CircleShape),
            contentScale = ContentScale.Crop
          )
        }

        Column {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "ST. JOSEPH'S SCHOOL",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              ),
              color = Color.White
            )
            if (networkState is com.example.util.NetworkState.Offline) {
              Surface(
                color = Color(0xFFDC2626),
                shape = RoundedCornerShape(4.dp)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                  )
                  Text(
                    text = "OFFLINE",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                  )
                }
              }
            }
          }
          Text(
            text = "SHINE AND LET SHINE",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Medium,
              letterSpacing = 1.sp
            ),
            color = SchoolGoldLight
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        user?.let {
          RoleBadge(role = it.role)
        }

        IconButton(
          onClick = onNotificationsClick,
          modifier = Modifier.testTag("top_bar_notifications_btn")
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            if (unreadNotificationCount > 0) {
              BadgedBox(
                badge = {
                  Badge(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White,
                    modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                  ) {
                    Text(
                      text = if (unreadNotificationCount > 9) "9+" else "$unreadNotificationCount",
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                    )
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Default.Notifications,
                  contentDescription = "Notifications ($unreadNotificationCount unread)",
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
            } else {
              Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }

        IconButton(
          onClick = onProfileClick,
          modifier = Modifier.testTag("top_bar_profile_btn")
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AccountCircle,
              contentDescription = "User Profile",
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun RoleBadge(
  role: UserRole,
  modifier: Modifier = Modifier
) {
  Surface(
    color = Color(role.badgeColor),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier
  ) {
    Text(
      text = role.displayName.uppercase(),
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
      color = Color.White,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
  }
}

@Composable
fun WelcomeGreetingBanner(
  user: User,
  subtitle: String,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    modifier = modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.horizontalGradient(
            colors = listOf(
              SchoolNavyPrimary,
              Color(0xFF1D4ED8)
            )
          )
        )
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Welcome back,",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
          )
          Text(
            text = user.fullName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = SchoolGoldLight
          )
        }

        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when(user.role) {
              UserRole.STUDENT -> Icons.Default.School
              UserRole.TEACHER -> Icons.Default.MenuBook
              UserRole.STAFF -> Icons.Default.Engineering
              UserRole.ADMIN -> Icons.Default.AdminPanelSettings
              UserRole.DEVELOPER -> Icons.Default.Terminal
            },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
          )
        }
      }
    }
  }
}
