package com.example.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.UserSession
import com.example.ui.components.StatCard
import com.example.viewmodel.SchoolViewModel

@Composable
fun AdminDashboard(
    user: UserSession,
    schoolViewModel: SchoolViewModel,
    onNavigateToAttendance: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onNavigateToManagement: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Executive Administration",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Principal & Chief Academic Officer • St. Joseph's H.S.S.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "TOTAL ENROLLMENT",
                    value = "1,240",
                    subtitle = "Across Grades 6 - 12",
                    icon = Icons.Default.School,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "FACULTY STRENGTH",
                    value = "64 Staff",
                    subtitle = "98% Daily Present",
                    icon = Icons.Default.Badge,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "TODAY'S ATTENDANCE",
                    value = "96.2%",
                    subtitle = "+1.4% vs Last Week",
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "FEE COLLECTION",
                    value = "91.4%",
                    subtitle = "Term 1 Target Met",
                    icon = Icons.Default.AccountBalanceWallet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Administrative Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onNavigateToAttendance,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Institutional Attendance Audit")
                    }
                    FilledTonalButton(
                        onClick = onNavigateToClasses,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Class, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Classes, Rosters & Sections")
                    }
                    OutlinedButton(
                        onClick = onNavigateToManagement,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.SettingsSuggest, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Database & System Configuration")
                    }
                }
            }
        }
    }
}
