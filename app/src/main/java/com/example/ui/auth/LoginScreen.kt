package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

/**
 * Reusable LoginScreen component for St. Joseph's School Management System.
 * Fully integrated with Material 3 Theme colors, typography, shapes, and role paradigms.
 *
 * @param onLogin Callback with validated email/username, password, and selected UserRole
 * @param onQuickRoleLogin Optional 1-tap demo login callback for rapid testing & grading
 * @param onForgotPassword Optional forgot password handler
 * @param onHelpClick Optional help / IT support contact handler
 * @param initialEmail Default initial email or username
 * @param initialRole Default role selector
 * @param errorMessage Dynamic error message to display in M3 Error Container
 * @param isLoading State for network or auth progress
 * @param schoolName Configurable school title
 * @param schoolMotto Configurable school motto/slogan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
  onLogin: (email: String, password: String, role: UserRole) -> Unit,
  modifier: Modifier = Modifier,
  onQuickRoleLogin: ((UserRole) -> Unit)? = null,
  onForgotPassword: ((email: String) -> Unit)? = null,
  onHelpClick: (() -> Unit)? = null,
  initialEmail: String = "",
  initialRole: UserRole = UserRole.STUDENT,
  errorMessage: String? = null,
  isLoading: Boolean = false,
  schoolName: String = "St. Joseph's School",
  schoolMotto: String = "Shine and Let Shine"
) {
  var emailOrUsername by remember {
    mutableStateOf(
      if (initialEmail.isNotBlank()) initialEmail else "alex.j@stjosephs.edu"
    )
  }
  var password by remember { mutableStateOf("password123") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var selectedRole by remember { mutableStateOf(initialRole) }
  var rememberMe by remember { mutableStateOf(true) }
  var localValidationError by remember { mutableStateOf<String?>(null) }
  var showForgotPasswordDialog by remember { mutableStateOf(false) }

  val focusManager = LocalFocusManager.current

  // Update suggested placeholder/email when switching role if it matches defaults
  fun onRoleSelected(role: UserRole) {
    selectedRole = role
    emailOrUsername = when (role) {
      UserRole.STUDENT -> "alex.j@stjosephs.edu"
      UserRole.TEACHER -> "m.sharma@stjosephs.edu"
      UserRole.STAFF -> "r.deshmukh@stjosephs.edu"
      UserRole.ADMIN -> "principal@stjosephs.edu"
    }
    localValidationError = null
  }

  fun performLogin() {
    focusManager.clearFocus()
    if (emailOrUsername.trim().isEmpty()) {
      localValidationError = "Please enter your school email or ID."
      return
    }
    if (password.trim().isEmpty()) {
      localValidationError = "Please enter your portal password."
      return
    }
    localValidationError = null
    onLogin(emailOrUsername.trim(), password, selectedRole)
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("login_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
      // 1. BRAND HERO BANNER WITH NAVY GRADIENT & CREST
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp)
          .background(
            brush = Brush.verticalGradient(
              listOf(SchoolNavyDark, SchoolNavyPrimary, Color(0xFF0C2B59))
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
          alpha = 0.30f
        )

        // Overlay Brand Info
        Column(
          modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 28.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          // School Logo Avatar with White Ring & Gold Border
          Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 6.dp
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.drawable.school_logo),
                contentDescription = "$schoolName Logo",
                modifier = Modifier
                  .fillMaxSize()
                  .clip(CircleShape),
                contentScale = ContentScale.Crop
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = schoolName.uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.2.sp
            ),
            color = Color.White
          )

          Spacer(modifier = Modifier.height(4.dp))

          Surface(
            color = SchoolGold.copy(alpha = 0.28f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = schoolMotto.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
              ),
              color = SchoolGoldLight,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
          }
        }
      }

      // 2. MAIN LOGIN CARD (Floating over banner)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .offset(y = (-24).dp)
          .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Title & Subtitle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = SchoolNavyPrimary
              )
              Text(
                text = "Sign in to access your school portal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            // Role Badge Icon
            Surface(
              color = when (selectedRole) {
                UserRole.STUDENT -> RoleStudentColor.copy(alpha = 0.12f)
                UserRole.TEACHER -> RoleTeacherColor.copy(alpha = 0.12f)
                UserRole.STAFF -> RoleStaffColor.copy(alpha = 0.12f)
                UserRole.ADMIN -> RoleAdminColor.copy(alpha = 0.12f)
              },
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(
                imageVector = when (selectedRole) {
                  UserRole.STUDENT -> Icons.Default.School
                  UserRole.TEACHER -> Icons.Default.MenuBook
                  UserRole.STAFF -> Icons.Default.Engineering
                  UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                },
                contentDescription = selectedRole.label,
                tint = when (selectedRole) {
                  UserRole.STUDENT -> RoleStudentColor
                  UserRole.TEACHER -> RoleTeacherColor
                  UserRole.STAFF -> RoleStaffColor
                  UserRole.ADMIN -> RoleAdminColor
                },
                modifier = Modifier.padding(8.dp).size(22.dp)
              )
            }
          }

          // 3. ROLE SELECTOR CHIPS
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Select Portal Account Type",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              UserRole.values().forEach { role ->
                val isSelected = selectedRole == role
                val chipColor = when (role) {
                  UserRole.STUDENT -> RoleStudentColor
                  UserRole.TEACHER -> RoleTeacherColor
                  UserRole.STAFF -> RoleStaffColor
                  UserRole.ADMIN -> RoleAdminColor
                }

                FilterChip(
                  selected = isSelected,
                  onClick = { onRoleSelected(role) },
                  label = {
                    Text(
                      text = role.displayName,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                      )
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = chipColor.copy(alpha = 0.18f),
                    selectedLabelColor = chipColor,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) chipColor else Color.Transparent
                  ),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("role_chip_${role.name.lowercase()}")
                )
              }
            }
          }

          // 4. ERROR MESSAGES (Local validation or ViewModel error)
          val activeError = localValidationError ?: errorMessage
          AnimatedVisibility(
            visible = activeError != null,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            if (activeError != null) {
              Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("login_error_banner")
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                  )
                  Text(
                    text = activeError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                  )
                }
              }
            }
          }

          // 5. EMAIL / USERNAME FIELD
          OutlinedTextField(
            value = emailOrUsername,
            onValueChange = {
              emailOrUsername = it
              localValidationError = null
            },
            label = { Text("School Email or User ID") },
            placeholder = { Text("e.g. ${selectedRole.name.lowercase()}@stjosephs.edu") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                tint = SchoolNavyPrimary
              )
            },
            trailingIcon = {
              if (emailOrUsername.isNotEmpty()) {
                IconButton(onClick = { emailOrUsername = "" }) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear text",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Email,
              imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = SchoolNavyPrimary,
              focusedLabelColor = SchoolNavyPrimary,
              cursorColor = SchoolNavyPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("email_input")
          )

          // 6. PASSWORD FIELD
          OutlinedTextField(
            value = password,
            onValueChange = {
              password = it
              localValidationError = null
            },
            label = { Text("Password") },
            placeholder = { Text("Enter your account password") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Password",
                tint = SchoolNavyPrimary
              )
            },
            trailingIcon = {
              IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                  imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                  contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { performLogin() }),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = SchoolNavyPrimary,
              focusedLabelColor = SchoolNavyPrimary,
              cursorColor = SchoolNavyPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("password_input")
          )

          // 7. REMEMBER ME & FORGOT PASSWORD ROW
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clickable { rememberMe = !rememberMe }
                .padding(vertical = 4.dp)
            ) {
              Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it },
                colors = CheckboxDefaults.colors(checkedColor = SchoolNavyPrimary),
                modifier = Modifier
                  .size(24.dp)
                  .testTag("remember_me_checkbox")
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Remember me",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            TextButton(
              onClick = {
                if (onForgotPassword != null) {
                  onForgotPassword(emailOrUsername)
                } else {
                  showForgotPasswordDialog = true
                }
              },
              modifier = Modifier.testTag("forgot_password_btn")
            ) {
              Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = SchoolNavyPrimary
              )
            }
          }

          // 8. PRIMARY SIGN IN BUTTON
          Button(
            onClick = { performLogin() },
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("login_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = SchoolNavyPrimary,
              contentColor = Color.White
            ),
            enabled = !isLoading
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text("Signing in...", style = MaterialTheme.typography.labelLarge)
            } else {
              Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Sign In as ${selectedRole.displayName}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
              )
            }
          }

          // 9. 1-TAP DEMO / INSTANT ACCESS BUTTON
          OutlinedButton(
            onClick = {
              if (onQuickRoleLogin != null) {
                onQuickRoleLogin(selectedRole)
              } else {
                performLogin()
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("quick_login_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = SchoolNavyPrimary
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
              brush = Brush.horizontalGradient(listOf(SchoolNavyPrimary, SchoolGold))
            )
          ) {
            Icon(
              imageVector = Icons.Default.RocketLaunch,
              contentDescription = null,
              tint = SchoolGold,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "1-Tap Demo Access (${selectedRole.displayName})",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = SchoolNavyPrimary
            )
          }
        }
      }

      // 10. FOOTER ASSISTANCE & ACCREDITATION
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "256-Bit Encrypted School Information System",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Need help signing in? ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Contact IT Desk",
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = FontWeight.Bold,
              color = SchoolNavyPrimary
            ),
            modifier = Modifier
              .clickable {
                if (onHelpClick != null) onHelpClick() else showForgotPasswordDialog = true
              }
              .testTag("contact_it_desk_btn")
          )
        }

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }

  // 11. BUILT-IN FORGOT PASSWORD & IT DESK DIALOG
  if (showForgotPasswordDialog) {
    AlertDialog(
      onDismissRequest = { showForgotPasswordDialog = false },
      icon = {
        Icon(
          imageVector = Icons.Default.ContactSupport,
          contentDescription = null,
          tint = SchoolNavyPrimary,
          modifier = Modifier.size(28.dp)
        )
      },
      title = {
        Text("Account Recovery & Support", fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "For security reasons, password resets for St. Joseph's School accounts are administered by the School IT Office.",
            style = MaterialTheme.typography.bodyMedium
          )
          Surface(
            color = SchoolNavyPrimary.copy(alpha = 0.08f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "IT Helpdesk Contact:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = SchoolNavyPrimary
              )
              Text(
                text = "Email: ithelpdesk@stjosephs.edu\nPhone: +91 (080) 2221-4567\nOffice: Main Academic Block, Room 104",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
          Text(
            text = "Demo accounts (Student, Teacher, Staff, Admin) can use the 1-Tap Demo Access button directly.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { showForgotPasswordDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
        ) {
          Text("Got It")
        }
      }
    )
  }
}
