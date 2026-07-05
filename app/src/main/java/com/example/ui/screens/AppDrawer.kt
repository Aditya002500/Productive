package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawerContent(
    userName: String,
    userEmail: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToFocusTimer: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onSignOut: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    var infoDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    ModalDrawerSheet {
        Column(modifier = Modifier.padding(24.dp)) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(userName, style = MaterialTheme.typography.titleMedium)
            Text(userEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text("Settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            selected = false,
            onClick = { onCloseDrawer(); onNavigateToSettings() },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("Profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            selected = false,
            onClick = { onCloseDrawer(); onNavigateToProfile() },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("Habits & Reminders") },
            icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
            selected = false,
            onClick = { onCloseDrawer(); onNavigateToHabits() },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("Focus Timer") },
            icon = { Icon(Icons.Default.Timer, contentDescription = null) },
            selected = false,
            onClick = { onCloseDrawer(); onNavigateToFocusTimer() },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("Compete with Friends") },
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
            selected = false,
            onClick = { onCloseDrawer(); onNavigateToFriends() },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text("Support Us") },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            selected = false,
            onClick = {
                infoDialog = "Support Us" to "Thank you for considering supporting CaptureFlow! " +
                    "This app is built and maintained independently. You can support future development " +
                    "by upgrading to CaptureFlow Pro from Settings, or by sharing the app with friends."
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("Terms & Conditions") },
            icon = { Icon(Icons.Default.Gavel, contentDescription = null) },
            selected = false,
            onClick = {
                infoDialog = "Terms & Conditions" to "By using CaptureFlow, you agree to use the app " +
                    "responsibly and in accordance with applicable laws. Content you capture, note, or " +
                    "schedule remains yours. The app is provided as-is without warranty of any kind."
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("Privacy Policy") },
            icon = { Icon(Icons.Default.PrivacyTip, contentDescription = null) },
            selected = false,
            onClick = {
                infoDialog = "Privacy Policy" to "CaptureFlow stores your tasks, notes, and captures " +
                    "locally on your device. Data sent to AI features is used only to generate the " +
                    "requested summaries and is not used to train models or shared with third parties."
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text("Sign Out") },
            icon = { Icon(Icons.Default.Logout, contentDescription = null) },
            selected = false,
            onClick = { onCloseDrawer(); onSignOut() },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }

    infoDialog?.let { (title, body) ->
        AlertDialog(
            onDismissRequest = { infoDialog = null },
            title = { Text(title) },
            text = { Text(body) },
            confirmButton = {
                TextButton(onClick = { infoDialog = null }) { Text("Close") }
            }
        )
    }
}
