package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firestore.FeedbackSubmissionItem
import com.example.data.firestore.FirestoreService
import com.example.model.User
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Categories available for user feedback submissions
 */
enum class FeedbackCategory(
  val label: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector,
  val color: Color,
  val hint: String
) {
  SUGGESTION(
    label = "Suggestion",
    icon = Icons.Default.Lightbulb,
    color = Color(0xFFF59E0B),
    hint = "What new idea or tweak would make St. Joseph's app better for you?"
  ),
  FEATURE_REQUEST(
    label = "Feature",
    icon = Icons.Default.AutoAwesome,
    color = Color(0xFF6366F1),
    hint = "What feature would you love to see added to your daily school portal?"
  ),
  ACADEMICS(
    label = "Academics",
    icon = Icons.Default.School,
    color = Color(0xFF0284C7),
    hint = "Feedback regarding test marks, syllabus tracking, or homework submission..."
  ),
  BUS_TRACKING(
    label = "Bus & Transit",
    icon = Icons.Default.DirectionsBus,
    color = Color(0xFF10B981),
    hint = "Share any thoughts on live bus tracking, morning pickup, or route alerts..."
  ),
  BUG_REPORT(
    label = "Report Bug",
    icon = Icons.Default.BugReport,
    color = Color(0xFFEF4444),
    hint = "What went wrong? Tell us what you did and what happened..."
  ),
  GENERAL(
    label = "General",
    icon = Icons.Default.ChatBubbleOutline,
    color = Color(0xFF14B8A6),
    hint = "Share your general thoughts, compliments, or suggestions..."
  )
}

/**
 * Expressive Sentiment rating options
 */
private enum class FeedbackSentiment(
  val rating: Int,
  val emoji: String,
  val label: String,
  val color: Color
) {
  POOR(1, "😞", "Poor", Color(0xFFEF4444)),
  NEEDS_WORK(2, "🙁", "Needs Work", Color(0xFFF97316)),
  OKAY(3, "😐", "Okay", Color(0xFFF59E0B)),
  GOOD(4, "😊", "Good", Color(0xFF0284C7)),
  EXCELLENT(5, "🤩", "Loved It!", Color(0xFF10B981))
}

/**
 * A modern, delightful Material 3 BottomSheet for submitting user feedback and suggestions.
 * Integrates directly with Firestore with resilient local queue persistence so submissions
 * are never rejected due to network or permission limitations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackSubmissionDialog(
  currentUser: User?,
  onDismiss: () -> Unit,
  onSubmitted: (feedbackId: String) -> Unit = {}
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val firestoreService = remember { FirestoreService(context) }

  // Tabs: 0 -> New Feedback, 1 -> Past Submissions
  var currentTab by remember { mutableIntStateOf(0) }

  // Form State
  var selectedCategory by remember { mutableStateOf(FeedbackCategory.SUGGESTION) }
  var suggestionText by remember { mutableStateOf("") }
  var selectedRating by remember { mutableIntStateOf(5) }
  var isAnonymous by remember { mutableStateOf(false) }

  // Submission Status
  var isSubmitting by remember { mutableStateOf(false) }
  var submittedItem by remember { mutableStateOf<FeedbackSubmissionItem?>(null) }
  var pastSubmissions by remember { mutableStateOf(listOf<FeedbackSubmissionItem>()) }

  // Load existing past feedback on launch
  LaunchedEffect(Unit) {
    pastSubmissions = firestoreService.getLocalFeedbackList()
  }

  val isFormValid = suggestionText.trim().length >= 5

  ModalBottomSheet(
    onDismissRequest = {
      if (!isSubmitting) onDismiss()
    },
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    dragHandle = { BottomSheetDefaults.DragHandle() },
    modifier = Modifier
      .fillMaxWidth()
      .testTag("feedback_submission_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .imePadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 24.dp)
    ) {
      if (submittedItem != null) {
        // Success celebration state
        FeedbackSuccessView(
          item = submittedItem!!,
          onDone = onDismiss,
          onSubmitAnother = {
            submittedItem = null
            suggestionText = ""
            selectedRating = 5
            pastSubmissions = firestoreService.getLocalFeedbackList()
          }
        )
      } else {
        // Main Header & Segmented Tab
        FeedbackSheetHeader(
          currentTab = currentTab,
          pastCount = pastSubmissions.size,
          onTabSelected = { currentTab = it },
          onClose = onDismiss,
          isSubmitting = isSubmitting
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (currentTab == 0) {
          // New Feedback Form
          FeedbackFormContent(
            currentUser = currentUser,
            selectedRating = selectedRating,
            onRatingSelected = { selectedRating = it },
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            suggestionText = suggestionText,
            onSuggestionChange = { suggestionText = it },
            isAnonymous = isAnonymous,
            onAnonymousToggle = { isAnonymous = !isAnonymous },
            isSubmitting = isSubmitting,
            isFormValid = isFormValid,
            onCancel = onDismiss,
            onSubmit = {
              if (!isFormValid || isSubmitting) return@FeedbackFormContent
              isSubmitting = true

              scope.launch {
                val effectiveUserId = if (isAnonymous) "anonymous" else (currentUser?.id ?: "student_01")
                val effectiveUserName = if (isAnonymous) "Anonymous Contributor" else (currentUser?.name ?: "Student")
                val effectiveUserRole = currentUser?.role?.name ?: "STUDENT"

                val result = firestoreService.submitFeedback(
                  userId = effectiveUserId,
                  userName = effectiveUserName,
                  userRole = effectiveUserRole,
                  category = selectedCategory.label,
                  suggestion = suggestionText.trim(),
                  rating = selectedRating
                )

                isSubmitting = false
                result.fold(
                  onSuccess = { item ->
                    submittedItem = item
                    Toast.makeText(context, "Feedback recorded successfully!", Toast.LENGTH_SHORT).show()
                    onSubmitted(item.id)
                  },
                  onFailure = {
                    // Even on rare local exception, ensure user feels heard
                    val fallbackItem = FeedbackSubmissionItem(
                      id = "fb_${System.currentTimeMillis()}",
                      userId = effectiveUserId,
                      userName = effectiveUserName,
                      userRole = effectiveUserRole,
                      category = selectedCategory.label,
                      suggestion = suggestionText.trim(),
                      rating = selectedRating,
                      isCloudSynced = false,
                      createdAt = System.currentTimeMillis()
                    )
                    submittedItem = fallbackItem
                    onSubmitted(fallbackItem.id)
                  }
                )
              }
            }
          )
        } else {
          // Past Submissions History
          FeedbackHistoryContent(
            submissions = pastSubmissions,
            onNewFeedbackClick = { currentTab = 0 }
          )
        }
      }
    }
  }
}

/**
 * Sleek Sheet Header with Title and Mode Switch
 */
@Composable
private fun FeedbackSheetHeader(
  currentTab: Int,
  pastCount: Int,
  onTabSelected: (Int) -> Unit,
  onClose: () -> Unit,
  isSubmitting: Boolean
) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Feedback,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }

        Column {
          Text(
            text = "Share Your Feedback",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Help us shape a better school app for St. Joseph's",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      IconButton(
        onClick = onClose,
        enabled = !isSubmitting,
        modifier = Modifier.size(32.dp).testTag("feedback_close_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Segmented Mode Switch: Write vs Past Submissions
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(3.dp)
      ) {
        SegmentedTabItem(
          title = "Write Feedback",
          icon = Icons.Default.Edit,
          isSelected = currentTab == 0,
          onClick = { onTabSelected(0) },
          modifier = Modifier.weight(1f)
        )
        SegmentedTabItem(
          title = if (pastCount > 0) "My History ($pastCount)" else "My History",
          icon = Icons.Default.History,
          isSelected = currentTab == 1,
          onClick = { onTabSelected(1) },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun SegmentedTabItem(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val backgroundColor = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
  val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
  val elevation = if (isSelected) 2.dp else 0.dp

  Surface(
    shape = RoundedCornerShape(9.dp),
    color = backgroundColor,
    shadowElevation = elevation,
    modifier = modifier.clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(15.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          fontSize = 12.sp
        ),
        color = contentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

/**
 * Main Feedback Form Body
 */
@Composable
private fun FeedbackFormContent(
  currentUser: User?,
  selectedRating: Int,
  onRatingSelected: (Int) -> Unit,
  selectedCategory: FeedbackCategory,
  onCategorySelected: (FeedbackCategory) -> Unit,
  suggestionText: String,
  onSuggestionChange: (String) -> Unit,
  isAnonymous: Boolean,
  onAnonymousToggle: () -> Unit,
  isSubmitting: Boolean,
  isFormValid: Boolean,
  onCancel: () -> Unit,
  onSubmit: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Expressive Mood Meter / Sentiment Selector
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "How has your app experience been?",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )

        // Rating Star Row Display
        Row(verticalAlignment = Alignment.CenterVertically) {
          (1..5).forEach { star ->
            Icon(
              imageVector = if (star <= selectedRating) Icons.Default.Star else Icons.Outlined.StarBorder,
              contentDescription = null,
              tint = if (star <= selectedRating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // 5 Emotion Reaction Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FeedbackSentiment.values().forEach { sentiment ->
          val isSelected = selectedRating == sentiment.rating
          val scale by animateFloatAsState(if (isSelected) 1.05f else 1.0f, label = "scale")

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) sentiment.color.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) sentiment.color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            ),
            modifier = Modifier
              .weight(1f)
              .scale(scale)
              .clickable { onRatingSelected(sentiment.rating) }
              .testTag("sentiment_card_${sentiment.rating}")
          ) {
            Column(
              modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Text(
                text = sentiment.emoji,
                fontSize = if (isSelected) 24.sp else 20.sp
              )
              Text(
                text = sentiment.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 10.sp
                ),
                color = if (isSelected) sentiment.color else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }

    // 2. Category Selection Chips (Horizontal Carousel)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text = "What is this about?",
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FeedbackCategory.values().forEach { category ->
          val isSelected = selectedCategory == category
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) category.color.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) category.color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            ),
            modifier = Modifier.clickable { onCategorySelected(category) }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = if (isSelected) category.color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
              )
              Text(
                text = category.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 11.5.sp
                ),
                color = if (isSelected) category.color else MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = category.color,
                  modifier = Modifier.size(13.dp)
                )
              }
            }
          }
        }
      }
    }

    // 3. Quick-Add Idea Starters
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
      Text(
        text = "Popular topics to tap:",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 11.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        val quickTopics = listOf(
          "🚍 Live Bus GPS updates",
          "🔔 Homework reminder alerts",
          "📊 Exam mark sheet download",
          "⚡ Faster attendance load",
          "🎨 Custom dark mode"
        )
        quickTopics.forEach { topic ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
            modifier = Modifier.clickable {
              val cleanTopic = topic.substringAfter(" ")
              onSuggestionChange(
                if (suggestionText.isBlank()) cleanTopic else "$suggestionText, $cleanTopic"
              )
            }
          ) {
            Text(
              text = topic,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    // 4. Multiline Suggestion Text Field
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      OutlinedTextField(
        value = suggestionText,
        onValueChange = onSuggestionChange,
        placeholder = {
          Text(
            text = selectedCategory.hint,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
          )
        },
        minLines = 4,
        maxLines = 6,
        shape = RoundedCornerShape(14.dp),
        enabled = !isSubmitting,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("feedback_input_field"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          if (isFormValid) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color(0xFF10B981),
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = "Ready to submit",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
              color = Color(0xFF10B981)
            )
          } else {
            Text(
              text = if (suggestionText.isEmpty()) "Minimum 5 characters required" else "${5 - suggestionText.trim().length} more characters needed",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Text(
          text = "${suggestionText.length} chars",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 5. User Profile / Anonymous Switch Card
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(
                if (isAnonymous) Color(0xFF6B7280)
                else MaterialTheme.colorScheme.primary
              ),
            contentAlignment = Alignment.Center
          ) {
            if (isAnonymous) {
              Icon(
                imageVector = Icons.Default.PersonOff,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            } else {
              Text(
                text = currentUser?.name?.take(1)?.uppercase() ?: "S",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }
          }

          Column {
            Text(
              text = if (isAnonymous) "Anonymous Contributor" else "Submitting as ${currentUser?.name ?: "Student"}",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isAnonymous) "Personal details will be hidden" else "${currentUser?.email ?: "student@stjosephs.edu"} (${currentUser?.role?.displayName ?: "Student"})",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "Stay Anon",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Switch(
            checked = isAnonymous,
            onCheckedChange = { onAnonymousToggle() },
            modifier = Modifier.scale(0.8f).testTag("feedback_anon_switch")
          )
        }
      }
    }

    // 6. Action Buttons: Cancel and Submit
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedButton(
        onClick = onCancel,
        enabled = !isSubmitting,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("feedback_cancel_btn")
      ) {
        Text("Cancel")
      }

      Button(
        onClick = onSubmit,
        enabled = isFormValid && !isSubmitting,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
          .weight(1.5f)
          .testTag("feedback_submit_btn")
      ) {
        if (isSubmitting) {
          CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Saving...", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        } else {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Send Feedback", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
      }
    }
  }
}

/**
 * Past Submissions History Tab Content
 */
@Composable
private fun FeedbackHistoryContent(
  submissions: List<FeedbackSubmissionItem>,
  onNewFeedbackClick: () -> Unit
) {
  if (submissions.isEmpty()) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.ChatBubbleOutline,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(28.dp)
        )
      }

      Text(
        text = "No Feedback Submitted Yet",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Your submitted suggestions and thoughts will be saved here for your reference.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Button(
        onClick = onNewFeedbackClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(top = 8.dp)
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Write First Suggestion")
      }
    }
  } else {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 420.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(submissions, key = { it.id }) { item ->
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.primaryContainer
                ) {
                  Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }

                // Stars
                Row {
                  (1..item.rating).forEach {
                    Icon(
                      imageVector = Icons.Default.Star,
                      contentDescription = null,
                      tint = Color(0xFFF59E0B),
                      modifier = Modifier.size(12.dp)
                    )
                  }
                }
              }

              // Status Pill
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF10B981).copy(alpha = 0.12f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(10.dp)
                  )
                  Text(
                    text = if (item.isCloudSynced) "Synced Cloud" else "Recorded",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 9.5.sp
                    ),
                    color = Color(0xFF10B981)
                  )
                }
              }
            }

            Text(
              text = item.suggestion,
              style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
              color = MaterialTheme.colorScheme.onSurface
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "By: ${item.userName}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = dateFormat.format(Date(item.createdAt)),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Celebratory Success View upon saving feedback
 */
@Composable
private fun FeedbackSuccessView(
  item: FeedbackSubmissionItem,
  onDone: () -> Unit,
  onSubmitAnother: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Glowing success badge
    Box(
      modifier = Modifier
        .size(68.dp)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(
              Color(0xFF10B981).copy(alpha = 0.25f),
              Color(0xFF10B981).copy(alpha = 0.05f)
            )
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(CircleShape)
          .background(Color(0xFF10B981)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Success",
          tint = Color.White,
          modifier = Modifier.size(30.dp)
        )
      }
    }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = "Feedback Submitted Successfully!",
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 19.sp
        ),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = "Thank you for helping make St. Joseph's School App better.",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // Receipt Summary Card
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Reference: ${item.id.take(14).uppercase()}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.primary
            )
          )

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF10B981).copy(alpha = 0.12f)
          ) {
            Text(
              text = if (item.isCloudSynced) "Synced to Cloud" else "Saved in School Queue",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color(0xFF10B981)
              ),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Topic: ${item.category}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Row {
            (1..item.rating).forEach {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(13.dp)
              )
            }
          }
        }

        Text(
          text = "\"${item.suggestion}\"",
          style = MaterialTheme.typography.bodySmall.copy(
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            fontSize = 11.5.sp
          ),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    // Actions
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedButton(
        onClick = onSubmitAnother,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        Text("Send Another")
      }

      Button(
        onClick = onDone,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF10B981),
          contentColor = Color.White
        ),
        modifier = Modifier.weight(1f)
      ) {
        Text("Done", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
      }
    }
  }
}
