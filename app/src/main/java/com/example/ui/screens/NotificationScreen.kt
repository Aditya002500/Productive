package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.components.EmptyStateView

data class NotificationItem(
    val id: Int,
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val isUnread: Boolean,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val dummyNotifications = listOf(
        NotificationItem(1, "Subscription Expiring", "Your Pro trial ends in 2 days.", "2h ago", Icons.Default.Warning, true, Color(0xFFF59E0B)),
        NotificationItem(2, "Task Reminder", "Project Deadline is approaching.", "4h ago", Icons.Default.NotificationsActive, true, Color(0xFF059669)),
        NotificationItem(3, "New Feature", "Check out the new AI Summary capabilities!", "1d ago", Icons.Default.Info, false, Color(0xFF0D9488))
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        if (dummyNotifications.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.NotificationsActive,
                title = "No Notifications",
                subtitle = "You're all caught up!",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                items(dummyNotifications) { notification ->
                    NotificationRow(notification = notification)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun NotificationRow(notification: NotificationItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notification.isUnread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
            .clickable { /* TODO: handle click */ }
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(notification.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(notification.icon, contentDescription = null, tint = notification.color)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = notification.title,
                    fontWeight = if (notification.isUnread) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = notification.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (notification.isUnread) MaterialTheme.colorScheme.onBackground else Color.Gray
            )
        }
        if (notification.isUnread) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}
