package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.StudentEntity
import com.example.model.AttendanceStatus
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceScreen(
    user: UserSession,
    schoolViewModel: SchoolViewModel
) {
    val selectedClass by schoolViewModel.selectedClass.collectAsState()
    val currentDate by schoolViewModel.currentDate.collectAsState()
    val students by schoolViewModel.studentsInSelectedClass.collectAsState()
    val attendanceRecords by schoolViewModel.attendanceForCurrentClass.collectAsState()
    val searchQuery by schoolViewModel.searchQuery.collectAsState()

    // Access Control: Only Homeroom Teacher or Admin can edit
    val isAuthorized = (user.role == UserRole.ADMIN) || (user.role == UserRole.TEACHER && user.homeroomClass == selectedClass)

    val classes = listOf("10-A", "10-B", "11-A", "11-B", "12-A")

    val attendanceMap = remember(attendanceRecords) {
        attendanceRecords.associateBy { it.studentId }
    }

    val filteredStudents = remember(students, searchQuery) {
        if (searchQuery.isBlank()) students
        else students.filter { it.name.contains(searchQuery, ignoreCase = true) || it.rollNo.contains(searchQuery) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Roll Call Register",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = currentDate,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select Class Section:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(classes) { cls ->
                            FilterChip(
                                selected = cls == selectedClass,
                                onClick = { schoolViewModel.setSelectedClass(cls) },
                                label = { Text(cls) },
                                leadingIcon = if (cls == user.homeroomClass) {
                                    { Icon(Icons.Default.Home, contentDescription = "Homeroom", modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isAuthorized) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Read-Only: Only the assigned Homeroom Teacher for Class $selectedClass can take roll call.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { schoolViewModel.markAllFullDay(students, user.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark All Full Day (FD)")
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { schoolViewModel.setSearchQuery(it) },
                placeholder = { Text("Search by student name or roll #...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { schoolViewModel.setSearchQuery("") }) { Icon(Icons.Default.Clear, contentDescription = null) } }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        items(filteredStudents) { student ->
            val record = attendanceMap[student.id]
            val currentStatus = record?.status ?: AttendanceStatus.FULL_DAY

            StudentRollCallCard(
                student = student,
                status = currentStatus,
                remarks = record?.remarks ?: "",
                isEditable = isAuthorized,
                onStatusChange = { newStatus ->
                    schoolViewModel.markAttendance(
                        studentId = student.id,
                        studentName = student.name,
                        rollNo = student.rollNo,
                        status = newStatus,
                        teacherId = user.id
                    )
                }
            )
        }
    }
}

@Composable
fun StudentRollCallCard(
    student: StudentEntity,
    status: AttendanceStatus,
    remarks: String,
    isEditable: Boolean,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = student.rollNo,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${student.house} House • ${student.gender}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = status)
            }

            if (remarks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: $remarks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isEditable) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AttendanceStatusButton(
                        label = "FD",
                        color = StatusPresentGreen,
                        isSelected = status == AttendanceStatus.FULL_DAY,
                        modifier = Modifier.weight(1f)
                    ) { onStatusChange(AttendanceStatus.FULL_DAY) }

                    AttendanceStatusButton(
                        label = "HD",
                        color = StatusHalfDayAmber,
                        isSelected = status == AttendanceStatus.HALF_DAY,
                        modifier = Modifier.weight(1f)
                    ) { onStatusChange(AttendanceStatus.HALF_DAY) }

                    AttendanceStatusButton(
                        label = "OD",
                        color = StatusOnDutyBlue,
                        isSelected = status == AttendanceStatus.ON_DUTY,
                        modifier = Modifier.weight(1f)
                    ) { onStatusChange(AttendanceStatus.ON_DUTY) }

                    AttendanceStatusButton(
                        label = "AB",
                        color = StatusAbsentRed,
                        isSelected = status == AttendanceStatus.ABSENT,
                        modifier = Modifier.weight(1f)
                    ) { onStatusChange(AttendanceStatus.ABSENT) }
                }
            }
        }
    }
}

@Composable
fun AttendanceStatusButton(
    label: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
