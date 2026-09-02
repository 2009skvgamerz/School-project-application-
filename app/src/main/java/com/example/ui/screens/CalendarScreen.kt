package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalendarCategory
import com.example.model.CalendarEvent
import com.example.model.UserRole
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavyDark
import com.example.ui.theme.SchoolNavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
  events: List<CalendarEvent>,
  userRole: UserRole,
  onAddEvent: (CalendarEvent) -> Unit,
  onToggleReminder: (String) -> Boolean,
  initialSelectedEventId: String? = null,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf<CalendarCategory?>(null) }
  var isMonthView by remember { mutableStateOf(false) }
  var selectedDateNumber by remember { mutableStateOf(20) } // August 20
  var showAddEventDialog by remember { mutableStateOf(false) }
  var selectedEventForDetail by remember { mutableStateOf<CalendarEvent?>(null) }
  var reminderFeedbackMessage by remember { mutableStateOf<String?>(null) }

  // Deep Link listener to open event detail automatically
  LaunchedEffect(initialSelectedEventId, events) {
    if (initialSelectedEventId != null) {
      val match = events.find { it.id == initialSelectedEventId }
      if (match != null) {
        selectedEventForDetail = match
      }
    }
  }

  val filteredEvents = remember(events, selectedCategory) {
    if (selectedCategory == null || selectedCategory == CalendarCategory.ALL) {
      events
    } else {
      events.filter { it.category == selectedCategory }
    }
  }

  val canManageCalendar = userRole != UserRole.STUDENT

  Scaffold(
    floatingActionButton = {
      if (canManageCalendar) {
        ExtendedFloatingActionButton(
          onClick = { showAddEventDialog = true },
          icon = { Icon(Icons.Default.Add, contentDescription = "Add Calendar Event") },
          text = { Text("Add Event") },
          containerColor = SchoolNavyPrimary,
          contentColor = Color.White,
          modifier = Modifier.testTag("add_calendar_event_fab")
        )
      }
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. HERO HEADER CARD
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("calendar_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              brush = Brush.horizontalGradient(
                colors = listOf(SchoolNavyDark, SchoolNavyPrimary)
              )
            )
            .padding(18.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = SchoolGold,
                    modifier = Modifier.size(22.dp)
                  )
                }
                Column {
                  Text(
                    text = "Academic Calendar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                  )
                  Text(
                    text = "August - September 2026",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                  )
                }
              }

              // View Mode Toggle (Agenda vs Month)
              FilledTonalButton(
                onClick = { isMonthView = !isMonthView },
                colors = ButtonDefaults.filledTonalButtonColors(
                  containerColor = Color.White.copy(alpha = 0.2f),
                  contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("calendar_toggle_view_btn")
              ) {
                Icon(
                  imageVector = if (isMonthView) Icons.Default.ViewAgenda else Icons.Default.CalendarViewMonth,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isMonthView) "Agenda" else "Month",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
              }
            }

            // Quick Stats Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val holidaysCount = events.count { it.isHoliday }
              val examsCount = events.count { it.category == CalendarCategory.EXAM }

              CalendarMiniStatBadge(
                title = "Total Events",
                value = "${events.size}",
                icon = Icons.Default.Event,
                modifier = Modifier.weight(1f)
              )
              CalendarMiniStatBadge(
                title = "Exams",
                value = "$examsCount",
                icon = Icons.Default.Quiz,
                modifier = Modifier.weight(1f)
              )
              CalendarMiniStatBadge(
                title = "Holidays",
                value = "$holidaysCount",
                icon = Icons.Default.Celebration,
                modifier = Modifier.weight(1f)
              )
              CalendarMiniStatBadge(
                title = "FCM Push",
                value = "Active",
                icon = Icons.Default.CloudSync,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }

      // 2. Interactive Month Grid (if Month View is selected)
      AnimatedVisibility(
        visible = isMonthView,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
      ) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth().testTag("calendar_month_grid_card")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "August 2026",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = SchoolNavyPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Day Headers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
              listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                  text = day,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = if (day == "Sun") Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.weight(1f),
                  textAlign = TextAlign.Center
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))

            // 31 Days Grid
            val daysInAugust = (1..31).toList()
            LazyVerticalGrid(
              columns = GridCells.Fixed(7),
              modifier = Modifier.height(180.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              // Saturday offset for Aug 1 2026 (Aug 1 is Sat -> 6 empty slots)
              items(6) {
                Box(modifier = Modifier.size(28.dp))
              }
              items(daysInAugust, key = { "day_$it" }) { dayNum ->
                val isSelected = dayNum == selectedDateNumber
                val hasEvent = dayNum in listOf(15, 20, 25, 29)
                val isHoliday = dayNum == 15

                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                      when {
                        isSelected -> SchoolNavyPrimary
                        isHoliday -> Color(0xFFDC2626).copy(alpha = 0.15f)
                        hasEvent -> SchoolGold.copy(alpha = 0.25f)
                        else -> Color.Transparent
                      }
                    )
                    .clickable { selectedDateNumber = dayNum },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "$dayNum",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (isSelected || hasEvent) FontWeight.Bold else FontWeight.Normal,
                      fontSize = 11.sp
                    ),
                    color = when {
                      isSelected -> Color.White
                      isHoliday -> Color(0xFFDC2626)
                      else -> MaterialTheme.colorScheme.onSurface
                    }
                  )
                }
              }
            }
          }
        }
      }

      // 3. Category Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("calendar_category_filters")
      ) {
        item(key = "cat_all") {
          FilterChip(
            selected = selectedCategory == null || selectedCategory == CalendarCategory.ALL,
            onClick = { selectedCategory = null },
            label = { Text("All (${events.size})") },
            leadingIcon = {
              if (selectedCategory == null || selectedCategory == CalendarCategory.ALL) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
              }
            }
          )
        }

        items(CalendarCategory.entries.filter { it != CalendarCategory.ALL }, key = { it.name }) { category ->
          val count = events.count { it.category == category }
          val isSelected = selectedCategory == category

          FilterChip(
            selected = isSelected,
            onClick = { selectedCategory = if (isSelected) null else category },
            label = { Text("${category.label} ($count)") },
            leadingIcon = {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(Color(category.colorHex))
              )
            }
          )
        }
      }

      // Reminder Feedback Toast Banner
      AnimatedVisibility(visible = reminderFeedbackMessage != null) {
        reminderFeedbackMessage?.let { msg ->
          Surface(
            color = SchoolNavyPrimary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SchoolNavyPrimary, modifier = Modifier.size(18.dp))
              Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = SchoolNavyPrimary,
                modifier = Modifier.weight(1f)
              )
              IconButton(onClick = { reminderFeedbackMessage = null }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }

      // 4. Events Agenda List
      if (filteredEvents.isEmpty()) {
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
              imageVector = Icons.Default.EventBusy,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(48.dp)
            )
            Text(
              text = "No events found in this category",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag("calendar_events_lazy_column"),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(filteredEvents, key = { it.id }) { event ->
            CalendarEventCard(
              event = event,
              onClick = { selectedEventForDetail = event },
              onToggleReminder = {
                val newState = onToggleReminder(event.id)
                reminderFeedbackMessage = if (newState) {
                  "Reminder set for '${event.title}'"
                } else {
                  "Reminder removed for '${event.title}'"
                }
              }
            )
          }
        }
      }
    }
  }

  // Event Detail Dialog
  selectedEventForDetail?.let { event ->
    CalendarEventDetailDialog(
      event = event,
      onDismiss = { selectedEventForDetail = null },
      onToggleReminder = {
        val newState = onToggleReminder(event.id)
        selectedEventForDetail = event.copy(hasReminder = newState)
        reminderFeedbackMessage = if (newState) "Reminder set for '${event.title}'" else "Reminder removed"
      }
    )
  }

  // Add Event Dialog
  if (showAddEventDialog) {
    AddCalendarEventDialog(
      onDismiss = { showAddEventDialog = false },
      onConfirm = { newEvent ->
        onAddEvent(newEvent)
        showAddEventDialog = false
        reminderFeedbackMessage = "New event '${newEvent.title}' scheduled successfully"
      }
    )
  }
}

@Composable
private fun CalendarMiniStatBadge(
  title: String,
  value: String,
  icon: ImageVector,
  modifier: Modifier = Modifier
) {
  Surface(
    color = Color.White.copy(alpha = 0.12f),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = SchoolGold, modifier = Modifier.size(18.dp))
      Column {
        Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
        Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.75f), maxLines = 1)
      }
    }
  }
}

@Composable
fun CalendarEventCard(
  event: CalendarEvent,
  onClick: () -> Unit,
  onToggleReminder: () -> Unit,
  modifier: Modifier = Modifier
) {
  val categoryColor = Color(event.category.colorHex)

  Card(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier.fillMaxWidth().testTag("calendar_event_card_${event.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Date Pill Block
      Box(
        modifier = Modifier
          .width(58.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(categoryColor.copy(alpha = 0.12f))
          .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = event.formattedDate.take(3).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = 10.sp
            ),
            color = categoryColor
          )
          Text(
            text = event.date.takeLast(2),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Black,
              fontSize = 18.sp
            ),
            color = categoryColor
          )
        }
      }

      // Event Body
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            color = categoryColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = event.category.label,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              ),
              color = categoryColor,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          if (event.isHoliday) {
            Surface(
              color = Color(0xFFDC2626).copy(alpha = 0.15f),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = "HOLIDAY",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                ),
                color = Color(0xFFDC2626),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        Text(
          text = event.title,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = event.time,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = event.location,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Reminder Action
      IconButton(
        onClick = onToggleReminder,
        modifier = Modifier
          .size(40.dp)
          .testTag("event_reminder_btn_${event.id}")
      ) {
        Icon(
          imageVector = if (event.hasReminder) Icons.Default.NotificationsActive else Icons.Outlined.NotificationsNone,
          contentDescription = "Toggle Reminder",
          tint = if (event.hasReminder) SchoolNavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
fun CalendarEventDetailDialog(
  event: CalendarEvent,
  onDismiss: () -> Unit,
  onToggleReminder: () -> Unit
) {
  val categoryColor = Color(event.category.colorHex)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(categoryColor.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Event, contentDescription = null, tint = categoryColor, modifier = Modifier.size(18.dp))
        }
        Text(
          text = event.title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
          color = categoryColor.copy(alpha = 0.12f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "${event.category.label} • ${event.targetGrades}",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = categoryColor),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }

        Text(
          text = event.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        DetailRow(icon = Icons.Default.CalendarMonth, label = "Date", value = event.formattedDate)
        DetailRow(icon = Icons.Default.Schedule, label = "Timing", value = event.time)
        DetailRow(icon = Icons.Default.LocationOn, label = "Location", value = event.location)
        DetailRow(icon = Icons.Default.Group, label = "Organizer", value = event.organizer)
      }
    },
    confirmButton = {
      Button(
        onClick = onToggleReminder,
        colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary)
      ) {
        Icon(
          imageVector = if (event.hasReminder) Icons.Default.NotificationsOff else Icons.Default.NotificationsActive,
          contentDescription = null,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (event.hasReminder) "Remove Reminder" else "Set Reminder")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = SchoolNavyPrimary, modifier = Modifier.size(16.dp))
    Text(text = "$label: ", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
    Text(text = value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCalendarEventDialog(
  onDismiss: () -> Unit,
  onConfirm: (CalendarEvent) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var dateStr by remember { mutableStateOf("2026-08-30") }
  var formattedDateStr by remember { mutableStateOf("Sun, 30 Aug 2026") }
  var timeStr by remember { mutableStateOf("10:00 AM - 01:00 PM") }
  var locationStr by remember { mutableStateOf("Main Auditorium") }
  var selectedCategory by remember { mutableStateOf(CalendarCategory.ACADEMIC) }
  var targetGrades by remember { mutableStateOf("All Grades (1-12)") }
  var isHoliday by remember { mutableStateOf(false) }
  var broadcastFcmPush by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.AddCircle, contentDescription = null, tint = SchoolNavyPrimary)
        Text("Schedule New Event", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Event Title *") },
          placeholder = { Text("e.g. Annual Sports Meet") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_event_title")
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description") },
          placeholder = { Text("Event details, rules or instructions...") },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth().testTag("input_event_desc")
        )

        Text(
          text = "Event Category:",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(CalendarCategory.entries.filter { it != CalendarCategory.ALL }) { cat ->
            FilterChip(
              selected = selectedCategory == cat,
              onClick = { selectedCategory = cat },
              label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = dateStr,
            onValueChange = {
              dateStr = it
              formattedDateStr = it
            },
            label = { Text("Date (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = timeStr,
            onValueChange = { timeStr = it },
            label = { Text("Timing") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        OutlinedTextField(
          value = locationStr,
          onValueChange = { locationStr = it },
          label = { Text("Location / Venue") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Holiday checkbox
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { isHoliday = !isHoliday }
        ) {
          Checkbox(
            checked = isHoliday,
            onCheckedChange = { isHoliday = it }
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Declare as Official School Holiday", style = MaterialTheme.typography.bodyMedium)
        }

        // Broadcast FCM Push Notification Checkbox
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { broadcastFcmPush = !broadcastFcmPush }
        ) {
          Checkbox(
            checked = broadcastFcmPush,
            onCheckedChange = { broadcastFcmPush = it }
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Broadcast FCM Push Notification (#events)",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SchoolNavyPrimary)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            val newEvt = CalendarEvent(
              id = "evt_usr_${System.currentTimeMillis()}",
              title = title.trim(),
              description = description.ifBlank { "Official school event scheduled on campus." },
              date = dateStr,
              formattedDate = formattedDateStr,
              time = timeStr,
              location = locationStr,
              category = selectedCategory,
              isHoliday = isHoliday,
              targetGrades = targetGrades,
              hasReminder = true
            )
            onConfirm(newEvt)
          }
        },
        enabled = title.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyPrimary),
        modifier = Modifier.testTag("btn_confirm_add_event")
      ) {
        Text("Schedule Event")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
