package com.example.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
  networkState: com.example.util.NetworkState? = null,
  schoolName: String = "St. Joseph's School",
  schoolMotto: String = "Shine and Let Shine"
) {
  var emailOrUsername by remember {
    mutableStateOf(
      if (initialEmail.isNotBlank()) initialEmail else "student01"
    )
  }
  var password by remember { mutableStateOf(com.example.data.SchoolRepository.DEMO_PASSWORD) }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var selectedRole by remember { mutableStateOf(initialRole) }
  var rememberMe by remember { mutableStateOf(true) }
  var localValidationError by remember { mutableStateOf<String?>(null) }
  var showForgotPasswordDialog by remember { mutableStateOf(false) }
  var secretDevTapCount by remember { mutableStateOf(0) }
  var showSecretDevUnlockedSnackbar by remember { mutableStateOf(false) }

  val focusManager = LocalFocusManager.current

  // Update suggested placeholder/email when switching role if it matches defaults
  fun onRoleSelected(role: UserRole) {
    selectedRole = role
    emailOrUsername = when (role) {
      UserRole.STUDENT -> "student01"
      UserRole.TEACHER -> "teacher01"
      UserRole.STAFF -> "staff01"
      UserRole.ADMIN -> "admin01"
      UserRole.DEVELOPER -> "dev"
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
          // School Logo Avatar with White Ring & Gold Border (5-Tap secret developer trigger)
          Surface(
            modifier = Modifier
              .size(72.dp)
              .clickable {
                secretDevTapCount++
                if (secretDevTapCount >= 5) {
                  secretDevTapCount = 0
                  selectedRole = UserRole.DEVELOPER
                  emailOrUsername = "dev"
                  password = com.example.data.SchoolRepository.DEMO_PASSWORD
                  showSecretDevUnlockedSnackbar = true
                }
              },
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
                UserRole.DEVELOPER -> Color(0xFF10B981).copy(alpha = 0.15f)
              },
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(
                imageVector = when (selectedRole) {
                  UserRole.STUDENT -> Icons.Default.School
                  UserRole.TEACHER -> Icons.Default.MenuBook
                  UserRole.STAFF -> Icons.Default.Engineering
                  UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                  UserRole.DEVELOPER -> Icons.Default.Terminal
                },
                contentDescription = selectedRole.label,
                tint = when (selectedRole) {
                  UserRole.STUDENT -> RoleStudentColor
                  UserRole.TEACHER -> RoleTeacherColor
                  UserRole.STAFF -> RoleStaffColor
                  UserRole.ADMIN -> RoleAdminColor
                  UserRole.DEVELOPER -> Color(0xFF10B981)
                },
                modifier = Modifier.padding(8.dp).size(22.dp)
              )
            }
          }

          // Offline notice if disconnected
          if (networkState is com.example.util.NetworkState.Offline) {
            Surface(
              color = Color(0xFFFEF2F2),
              shape = RoundedCornerShape(10.dp),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCA5A5))
              ),
              modifier = Modifier.fillMaxWidth().testTag("login_offline_notice")
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CloudOff,
                  contentDescription = null,
                  tint = Color(0xFFDC2626),
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = "Offline Mode: Offline local authentication active. Quick role logins available.",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                  color = Color(0xFF991B1B)
                )
              }
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
              UserRole.values().filter { it != UserRole.DEVELOPER }.forEach { role ->
                val isSelected = selectedRole == role
                val chipColor = when (role) {
                  UserRole.STUDENT -> RoleStudentColor
                  UserRole.TEACHER -> RoleTeacherColor
                  UserRole.STAFF -> RoleStaffColor
                  UserRole.ADMIN -> RoleAdminColor
                  UserRole.DEVELOPER -> Color(0xFF10B981)
                }

                FilterChip(
                  selected = isSelected,
                  onClick = { onRoleSelected(role) },
                  label = {
                    Text(
                      text = role.displayName,
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 10.5.sp
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

        Spacer(modifier = Modifier.height(16.dp))
      }
    }

    // 12. INSPIRATIONAL QUOTE LOADING OVERLAY
    AnimatedVisibility(
      visible = isLoading,
      enter = fadeIn(animationSpec = tween(250)),
      exit = fadeOut(animationSpec = tween(250))
    ) {
      LoginQuoteLoadingOverlay(selectedRole = selectedRole)
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

/**
 * Inspiring loading overlay shown during authentication with the school spirit quote:
 * "If I not who will make my school shine"
 */
@Composable
fun LoginQuoteLoadingOverlay(
  selectedRole: UserRole,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse_and_glow")
  
  val glowScale by infiniteTransition.animateFloat(
    initialValue = 0.94f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_scale"
  )

  val shimmerAlpha by infiniteTransition.animateFloat(
    initialValue = 0.7f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "shimmer_alpha"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color(0xF206142A),
            Color(0xF50F2C59),
            Color(0xF7081A36)
          )
        )
      )
      .clickable(enabled = false) {} // Absorb touches
      .testTag("login_loading_overlay"),
    contentAlignment = Alignment.Center
  ) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = Color(0xFF0F264A).copy(alpha = 0.95f)
      ),
      border = CardDefaults.outlinedCardBorder().copy(
        brush = Brush.linearGradient(
          listOf(SchoolGold.copy(alpha = 0.8f), SchoolGoldLight.copy(alpha = 0.3f))
        )
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
      modifier = Modifier
        .fillMaxWidth(0.9f)
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
      ) {
        // Glowing School Star / Crest
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(80.dp)
        ) {
          // Outer halo
          Box(
            modifier = Modifier
              .size(80.dp)
              .scale(glowScale)
              .clip(CircleShape)
              .background(SchoolGold.copy(alpha = 0.15f))
          )
          // Progress Ring
          CircularProgressIndicator(
            modifier = Modifier.size(68.dp),
            color = SchoolGold,
            strokeWidth = 3.5.dp,
            trackColor = Color.White.copy(alpha = 0.15f)
          )
          // Center Emblem
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(SchoolNavyPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.School,
              contentDescription = null,
              tint = SchoolGold,
              modifier = Modifier.size(26.dp)
            )
          }
        }

        // Quote Section
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Golden Quote icon
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = SchoolGold.copy(alpha = shimmerAlpha),
            modifier = Modifier.size(22.dp)
          )

          // THE QUOTE
          Text(
            text = "“If I not who will make my school shine”",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontStyle = FontStyle.Italic,
              lineHeight = 26.sp,
              letterSpacing = 0.3.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_quote_text")
          )

          Text(
            text = "— St. Joseph's School Motto & Pride",
            style = MaterialTheme.typography.labelSmall.copy(
              letterSpacing = 1.sp,
              fontWeight = FontWeight.SemiBold
            ),
            color = SchoolGoldLight,
            textAlign = TextAlign.Center
          )
        }

        HorizontalDivider(
          color = Color.White.copy(alpha = 0.12f),
          modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Loading Status Text & Role Indicator
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "Signing in as ${selectedRole.displayName}",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
            Surface(
              color = SchoolGold.copy(alpha = 0.2f),
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "PORTAL ACCESS",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 8.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = SchoolGoldLight
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              )
            }
          }

          Text(
            text = "Verifying credentials & preparing your dashboard...",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}
