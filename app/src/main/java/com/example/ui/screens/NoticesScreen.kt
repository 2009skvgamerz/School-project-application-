package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.components.NoticeCard
import com.example.ui.theme.SchoolNavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen(
  notices: List<Notice>,
  selectedCategory: NoticeCategory?,
  onSelectCategory: (NoticeCategory?) -> Unit,
  onNoticeClick: (Notice) -> Unit,
  onOpenCreateNoticeDialog: () -> Unit,
  canCreateNotice: Boolean,
  isRefreshing: Boolean = false,
  onRefresh: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val filteredNotices = if (selectedCategory == null) {
    notices
  } else {
    notices.filter { it.category == selectedCategory }
  }

  PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    modifier = modifier.fillMaxSize().testTag("notices_pull_refresh")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .testTag("notices_screen")
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "School Bulletins & Circulars",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = SchoolNavyPrimary
          )
          Text(
            text = "Official updates, events and emergency circulars",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (canCreateNotice) {
          Button(
            onClick = onOpenCreateNoticeDialog,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("create_notice_top_btn")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Publish", style = MaterialTheme.typography.labelSmall)
          }
        }
      }

      // Category Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          FilterChip(
            selected = selectedCategory == null,
            onClick = { onSelectCategory(null) },
            label = { Text("All (${notices.size})") }
          )
        }

        items(NoticeCategory.values()) { category ->
          val count = notices.count { it.category == category }
          FilterChip(
            selected = selectedCategory == category,
            onClick = { onSelectCategory(category) },
            label = { Text("${category.label} ($count)") },
            leadingIcon = {
              Surface(
                color = Color(category.colorHex),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.size(8.dp)
              ) {}
            }
          )
        }
      }

      // List of Notices
      if (filteredNotices.isEmpty()) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsOff,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(40.dp)
            )
            Text(
              text = "No circulars found in this category.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(filteredNotices) { notice ->
            NoticeCard(
              notice = notice,
              onNoticeClick = onNoticeClick
            )
          }
        }
      }
    }
  }
}
