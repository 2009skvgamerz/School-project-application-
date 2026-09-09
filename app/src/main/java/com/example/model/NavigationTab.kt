package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationTab(val label: String, val icon: ImageVector) {
  DASHBOARD("Dashboard", Icons.Default.Dashboard),
  TIMETABLE("Timetable", Icons.Default.CalendarMonth),
  CALENDAR("Calendar", Icons.Default.EventNote),
  BUS_TRACKING("Bus Live", Icons.Default.DirectionsBus),
  ANNOUNCEMENTS("Broadcasts", Icons.Default.Campaign),
  DIRECTORY("Directory", Icons.Default.ContactPhone),
  HOMEWORK("Homework", Icons.Default.Assignment),
  ATTENDANCE("Attendance", Icons.Default.FactCheck),
  NOTICES("Circulars", Icons.Default.Article),
  DUTIES("Duties", Icons.Default.Checklist),
  CLASSES("Classes", Icons.Default.Groups),
  MANAGEMENT("Admin DB", Icons.Default.AdminPanelSettings),
  PROFILE("Profile", Icons.Default.Person),
  SETTINGS("Settings", Icons.Default.Settings)
}
