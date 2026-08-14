package com.example.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserRole
import com.example.ui.theme.*

@Composable
fun LoginScreen(
  onLogin: (username: String, password: String) -> Unit,
  onQuickRoleLogin: (UserRole) -> Unit,
  errorMessage: String? = null,
  isLoading: Boolean = false,
  modifier: Modifier = Modifier
) {
  var username by remember { mutableStateOf("student01") }
  var password by remember { mutableStateOf("password123") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var selectedQuickRole by remember { mutableStateOf(UserRole.STUDENT) }
  val focusManager = LocalFocusManager.current

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
      // Top Hero Banner with School Emblem
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(240.dp)
          .background(
            brush = Brush.verticalGradient(
              listOf(SchoolNavyDark, SchoolNavyPrimary)
            )
          )
      ) {
        // Background illustration
        Image(
          painter = painterResource(id = R.drawable.school_banner),
          contentDescription = "School Campus",
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
          contentScale = ContentScale.Crop,
          alpha = 0.35f
        )

        // Overlay brand info
        Column(
          modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .background(Color.White)
              .padding(3.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.school_logo),
              contentDescription = "School Logo",
              modifier = Modifier.fillMaxSize().clip(CircleShape),
              contentScale = ContentScale.Crop
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "ST. JOSEPH'S SCHOOL",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            ),
            color = Color.White
          )

          Surface(
            color = SchoolGold.copy(alpha = 0.25f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "SHINE AND LET SHINE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
              ),
              color = SchoolGoldLight,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
          }
        }
      }

      // Login Card form
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .offset(y = (-20).dp)
          .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Text(
            text = "Sign In to Portal",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = SchoolNavyPrimary
          )

          Text(
            text = "Select a demo role or sign in with your school account ID.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // Demo Persona Quick Selector
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "1-Tap Demo Roles (Science Expo):",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              UserRole.values().forEach { role ->
                val isSelected = selectedQuickRole == role
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    selectedQuickRole = role
                    username = "${role.name.lowercase()}01"
                    password = "password123"
                  },
                  label = {
                    Text(
                      text = role.displayName,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                      )
                    )
                  },
                  modifier = Modifier.weight(1f).testTag("quick_role_${role.name.lowercase()}")
                )
              }
            }
          }

          if (errorMessage != null) {
            Surface(
              color = MaterialTheme.colorScheme.errorContainer,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ErrorOutline,
                  contentDescription = "Error",
                  tint = MaterialTheme.colorScheme.error
                )
                Text(
                  text = errorMessage,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onErrorContainer
                )
              }
            }
          }

          // Username Field
          OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("School ID / Username") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Person, contentDescription = "Username")
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("username_input"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = RoundedCornerShape(12.dp)
          )

          // Password Field
          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Lock, contentDescription = "Password")
            },
            trailingIcon = {
              IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                  imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                  contentDescription = "Toggle Password"
                )
              }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("password_input"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
              focusManager.clearFocus()
              onLogin(username, password)
            }),
            shape = RoundedCornerShape(12.dp)
          )

          // Sign In Button
          Button(
            onClick = {
              focusManager.clearFocus()
              onLogin(username, password)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("login_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary),
            enabled = !isLoading
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
              )
            } else {
              Icon(imageVector = Icons.Default.Login, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Sign In as ${selectedQuickRole.displayName}", style = MaterialTheme.typography.labelLarge)
            }
          }

          // Direct Expo Launch Button
          OutlinedButton(
            onClick = {
              onQuickRoleLogin(selectedQuickRole)
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("fast_expo_login_btn"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.RocketLaunch,
              contentDescription = null,
              tint = SchoolGold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Instant Expo Demo Login (${selectedQuickRole.displayName})")
          }
        }
      }

      // Footer Help Info
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = "Demo Accounts: student01, teacher01, staff01, admin01",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Password: password123 (or use Instant Demo Login)",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}
