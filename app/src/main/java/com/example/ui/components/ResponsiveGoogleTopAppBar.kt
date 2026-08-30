package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.NavigationTab
import com.example.ui.theme.*
import com.example.util.NetworkState

/**
 * Search category item for Google-style multi-category search
 */
data class SearchResultItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val category: String,
  val icon: ImageVector,
  val targetTab: NavigationTab,
  val accentColor: Color
)

/**
 * Responsive Google Material 3 TopAppBar with:
 * 1. School Branding & Drawer toggle
 * 2. Google Expressive Pill Search Bar (Responsive: compact toggleable / expanded inline)
 * 3. Profile Avatar with Google Account-style modal sheet
 * 4. Role quick-switch & Notification badges
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveGoogleTopAppBar(
  currentUser: User,
  currentTab: NavigationTab,
  networkState: NetworkState,
  unreadNotificationsCount: Int,
  roleColor: Color,
  onNavigationIconClick: () -> Unit,
  onOpenRoleSwitcher: () -> Unit,
  onOpenNotificationCenter: () -> Unit,
  onOpenDeveloperTerminal: () -> Unit,
  onNavigateToTab: (NavigationTab) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isSearchActive by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategoryFilter by remember { mutableStateOf("All") }
  var showGoogleAccountDialog by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current

  // Built-in Quick Search Index across ERP
  val allSearchItems = remember {
    listOf(
      SearchResultItem("1", "Mathematics Assignment - Calculus", "Class 12-A • Due Oct 15", "Homework", Icons.Default.Assignment, NavigationTab.HOMEWORK, Color(0xFF1A73E8)),
      SearchResultItem("2", "Physics Laboratory Record", "Class 12-A • Due Oct 18", "Homework", Icons.Default.Assignment, NavigationTab.HOMEWORK, Color(0xFF0D9488)),
      SearchResultItem("3", "Chemistry Organic Reactions", "Class 12-B • Due Oct 20", "Homework", Icons.Default.Assignment, NavigationTab.HOMEWORK, Color(0xFF7C3AED)),
      SearchResultItem("4", "Annual Sports Day 2026 Circular", "All Students & Parents • Ground 1", "Notices", Icons.Default.Campaign, NavigationTab.ANNOUNCEMENTS, Color(0xFFEA580C)),
      SearchResultItem("5", "Term 1 Examination Schedule", "Classes 9 to 12 • Auditing Hall", "Notices", Icons.Default.Article, NavigationTab.NOTICES, Color(0xFFDC2626)),
      SearchResultItem("6", "Bus Route #4 (South City)", "Driver: Rajesh Kumar • Live GPS Active", "Transport", Icons.Default.DirectionsBus, NavigationTab.BUS_TRACKING, Color(0xFFF59E0B)),
      SearchResultItem("7", "Dr. Robert Vance - Principal", "Principal Office • Room 101", "Directory", Icons.Default.Person, NavigationTab.DIRECTORY, Color(0xFFD97706)),
      SearchResultItem("8", "Mrs. Sarah Jenkins - Mathematics HOD", "Staff Room B • Dept Head", "Directory", Icons.Default.School, NavigationTab.DIRECTORY, Color(0xFF1A73E8)),
      SearchResultItem("9", "Monday Class Schedule", "Period 1 to 7 • Room 204", "Timetable", Icons.Default.CalendarMonth, NavigationTab.TIMETABLE, Color(0xFF059669)),
      SearchResultItem("10", "Fee Ledger & Digital Receipts", "Term 1 Paid • Term 2 Due Nov 30", "Accounts", Icons.Default.ReceiptLong, NavigationTab.MANAGEMENT, Color(0xFF10B981))
    )
  }

  val searchCategories = listOf("All", "Homework", "Notices", "Timetable", "Directory", "Transport")

  val filteredResults = remember(searchQuery, selectedCategoryFilter) {
    allSearchItems.filter { item ->
      val matchesCategory = (selectedCategoryFilter == "All" || item.category.equals(selectedCategoryFilter, ignoreCase = true))
      val matchesQuery = searchQuery.isBlank() ||
        item.title.contains(searchQuery, ignoreCase = true) ||
        item.subtitle.contains(searchQuery, ignoreCase = true) ||
        item.category.contains(searchQuery, ignoreCase = true)
      matchesCategory && matchesQuery
    }
  }

  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val isExpandedLayout = maxWidth >= 680.dp

    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp,
      shadowElevation = 3.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("responsive_google_top_app_bar")
    ) {
      if (isSearchActive && !isExpandedLayout) {
        // Full Mobile Search Bar Overlay
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          IconButton(
            onClick = {
              isSearchActive = false
              searchQuery = ""
              focusManager.clearFocus()
            },
            modifier = Modifier.testTag("close_search_btn")
          ) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
              Text(
                "Search homework, circulars, teachers...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear search",
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("mobile_search_text_field")
          )
        }
      } else {
        // Standard Responsive TopAppBar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Left: Drawer Navigation & Branding
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.then(if (isExpandedLayout) Modifier.widthIn(max = 240.dp) else Modifier.weight(1f, fill = false))
          ) {
            IconButton(
              onClick = onNavigationIconClick,
              modifier = Modifier
                .size(42.dp)
                .testTag("navigation_drawer_toggle_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open Navigation Drawer",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }

            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                  brush = Brush.linearGradient(
                    listOf(SchoolNavyPrimary, Color(0xFF1E3A8A))
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = SchoolGold,
                modifier = Modifier.size(22.dp)
              )
            }

            Column(verticalArrangement = Arrangement.Center) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "St. Joseph's",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.2.sp
                  ),
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                NetworkStatusBarBadge(
                  networkState = networkState,
                  modifier = Modifier.testTag("top_bar_network_status_badge")
                )
              }
              Text(
                text = currentTab.label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // Center: Google Expressive Search Bar (Expanded Desktop/Tablet layout or quick pill)
          if (isExpandedLayout) {
            Surface(
              onClick = { isSearchActive = true },
              shape = RoundedCornerShape(28.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
              modifier = Modifier
                .widthIn(min = 280.dp, max = 460.dp)
                .height(44.dp)
                .testTag("expanded_search_bar_pill")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
                Text(
                  text = if (searchQuery.isNotBlank()) searchQuery else "Search homework, circulars, classes...",
                  style = MaterialTheme.typography.bodyMedium,
                  color = if (searchQuery.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )
                if (searchQuery.isNotBlank()) {
                  IconButton(
                    onClick = { searchQuery = "" },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Clear,
                      contentDescription = "Clear",
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }

          // Right: Action Icons + Google Account Profile Avatar
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            // Search button on compact mobile screens
            if (!isExpandedLayout) {
              IconButton(
                onClick = { isSearchActive = true },
                modifier = Modifier
                  .size(42.dp)
                  .testTag("open_search_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "Search",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Role Switcher Button
            IconButton(
              onClick = onOpenRoleSwitcher,
              modifier = Modifier
                .size(42.dp)
                .testTag("switch_role_top_bar_btn")
            ) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                    if (currentUser.role == UserRole.DEVELOPER)
                      Color(0xFF10B981).copy(alpha = 0.15f)
                    else
                      MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (currentUser.role == UserRole.DEVELOPER) Icons.Default.Terminal else Icons.Default.SwapHoriz,
                  contentDescription = "Switch Role",
                  tint = if (currentUser.role == UserRole.DEVELOPER) Color(0xFF059669) else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            // Developer God Mode Terminal Shortcut
            if (currentUser.role == UserRole.DEVELOPER) {
              IconButton(
                onClick = onOpenDeveloperTerminal,
                modifier = Modifier
                  .size(42.dp)
                  .testTag("dev_terminal_top_bar_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.Terminal,
                  contentDescription = "Developer God Mode",
                  tint = Color(0xFF10B981)
                )
              }
            }

            // Notification Bell with Badge
            IconButton(
              onClick = onOpenNotificationCenter,
              modifier = Modifier
                .size(42.dp)
                .testTag("notifications_bell_btn")
            ) {
              BadgedBox(
                badge = {
                  if (unreadNotificationsCount > 0) {
                    Badge(
                      containerColor = Color(0xFFDC2626),
                      contentColor = Color.White
                    ) {
                      Text(
                        if (unreadNotificationsCount > 9) "9+" else "$unreadNotificationsCount",
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
              ) {
                Icon(
                  imageVector = if (unreadNotificationsCount > 0) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                  contentDescription = "Notifications",
                  tint = if (unreadNotificationsCount > 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Google Account Profile Avatar Button
            Surface(
              onClick = { showGoogleAccountDialog = true },
              shape = CircleShape,
              color = Color.Transparent,
              border = BorderStroke(2.dp, roleColor.copy(alpha = 0.8f)),
              modifier = Modifier
                .size(38.dp)
                .testTag("profile_top_bar_avatar_btn")
            ) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    brush = Brush.radialGradient(
                      listOf(roleColor, roleColor.copy(alpha = 0.85f))
                    )
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = currentUser.avatarInitials,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                  ),
                  color = Color.White
                )
              }
            }
          }
        }
      }
    }
  }

  // Active Search Results Modal Overlay / Sheet
  if (isSearchActive) {
    Dialog(
      onDismissRequest = {
        isSearchActive = false
        searchQuery = ""
      },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Surface(
        modifier = Modifier
          .fillMaxSize()
          .testTag("search_results_overlay"),
        color = MaterialTheme.colorScheme.surface
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Top Search Bar inside Modal
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            IconButton(
              onClick = {
                isSearchActive = false
                searchQuery = ""
              }
            ) {
              Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }

            TextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text("Search homework, notices, timetable, bus...") },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary
                )
              },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = { searchQuery = "" }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                  }
                }
              },
              singleLine = true,
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
              keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
              colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
              ),
              shape = RoundedCornerShape(24.dp),
              modifier = Modifier.weight(1f)
            )
          }

          // Category Filter Chips
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(searchCategories) { cat ->
              val isSelected = selectedCategoryFilter == cat
              FilterChip(
                selected = isSelected,
                onClick = { selectedCategoryFilter = cat },
                label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
              )
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

          // Results List
          if (filteredResults.isEmpty()) {
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
                  imageVector = Icons.Default.SearchOff,
                  contentDescription = null,
                  modifier = Modifier.size(48.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                  text = "No results found for \"$searchQuery\"",
                  style = MaterialTheme.typography.titleSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "Try searching by subject, teacher name, or class",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
              }
            }
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              contentPadding = PaddingValues(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              item {
                Text(
                  text = "${filteredResults.size} Results Found",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              items(filteredResults) { item ->
                Card(
                  onClick = {
                    onNavigateToTab(item.targetTab)
                    isSearchActive = false
                    searchQuery = ""
                  },
                  shape = RoundedCornerShape(16.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(item.accentColor.copy(alpha = 0.12f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.accentColor,
                        modifier = Modifier.size(22.dp)
                      )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        Surface(
                          color = item.accentColor.copy(alpha = 0.1f),
                          shape = RoundedCornerShape(6.dp)
                        ) {
                          Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = item.accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                          )
                        }
                        Text(
                          text = item.title,
                          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                      }
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }

                    Icon(
                      imageVector = Icons.Default.ChevronRight,
                      contentDescription = "Go",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Google Account-Style Profile Dialog Sheet
  if (showGoogleAccountDialog) {
    GoogleAccountProfileDialog(
      currentUser = currentUser,
      roleColor = roleColor,
      onDismiss = { showGoogleAccountDialog = false },
      onOpenRoleSwitcher = {
        showGoogleAccountDialog = false
        onOpenRoleSwitcher()
      },
      onNavigateToTab = { tab ->
        showGoogleAccountDialog = false
        onNavigateToTab(tab)
      },
      onSignOut = {
        showGoogleAccountDialog = false
        onSignOut()
      }
    )
  }
}

/**
 * Google Account Style Modal Dialog with:
 * - Profile avatar, name, email
 * - Role Switcher integration
 * - Quick shortcuts to ID Card, Notifications, and Settings
 * - Sign Out
 */
@Composable
fun GoogleAccountProfileDialog(
  currentUser: User,
  roleColor: Color,
  onDismiss: () -> Unit,
  onOpenRoleSwitcher: () -> Unit,
  onNavigateToTab: (NavigationTab) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = 420.dp)
        .wrapContentHeight()
        .clip(RoundedCornerShape(28.dp))
        .testTag("google_account_profile_dialog"),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Dialog Top Bar: Close button & School Crest Brand
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.School,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "St. Joseph's Account",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Center Profile Card
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Big Circular Avatar
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                  brush = Brush.linearGradient(
                    listOf(roleColor, roleColor.copy(alpha = 0.8f))
                  )
                )
                .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = currentUser.avatarInitials,
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
              )
            }

            Text(
              text = currentUser.fullName,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center
            )

            Text(
              text = currentUser.email,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = roleColor.copy(alpha = 0.15f)
            ) {
              Text(
                text = "${currentUser.role.label} Account",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = roleColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
              )
            }
          }
        }

        // Role Switcher Google-style Pill Button
        OutlinedButton(
          onClick = onOpenRoleSwitcher,
          shape = RoundedCornerShape(24.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Switch ERP Role / User",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }

        // Quick Navigation Items
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          ProfileQuickActionRow(
            icon = Icons.Default.Badge,
            label = "My Profile & School ID",
            onClick = { onNavigateToTab(NavigationTab.PROFILE) }
          )
          ProfileQuickActionRow(
            icon = Icons.Default.Notifications,
            label = "Notification Center",
            onClick = { onNavigateToTab(NavigationTab.NOTICES) }
          )
          ProfileQuickActionRow(
            icon = Icons.Default.Settings,
            label = "App Settings & Theme",
            onClick = { onNavigateToTab(NavigationTab.SETTINGS) }
          )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Google-style Sign Out Button
        Button(
          onClick = onSignOut,
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Logout,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Sign out of St. Joseph's",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}

@Composable
fun ProfileQuickActionRow(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    color = Color.Transparent,
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
      )
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f)
      )
      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.size(18.dp)
      )
    }
  }
}
