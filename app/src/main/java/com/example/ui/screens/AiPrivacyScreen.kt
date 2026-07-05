package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPrivacyScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val onDeviceAiEnabled by viewModel.onDeviceAiEnabled.collectAsStateWithLifecycle()
    val cloudAiEnabled by viewModel.cloudAiEnabled.collectAsStateWithLifecycle()
    val analyticsEnabled by viewModel.analyticsEnabled.collectAsStateWithLifecycle()
    val consentGiven by viewModel.consentGiven.collectAsStateWithLifecycle()

    var showEraseConfirm by remember { mutableStateOf(false) }
    var showExportInfo by remember { mutableStateOf(false) }
    var showWithdrawConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI & Privacy", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Hero section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Your Data is Yours", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("We do not train our public models on your personal notes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Processing",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            SettingsToggleItem(
                icon = Icons.Default.PhoneAndroid,
                title = "On-device Processing",
                subtitle = "Scan screenshot text on this device with ML Kit OCR. Turning this off pauses automatic capture processing.",
                checked = onDeviceAiEnabled,
                onCheckedChange = { viewModel.setOnDeviceAiEnabled(it) }
            )

            SettingsToggleItem(
                icon = Icons.Default.CloudQueue,
                title = "Cloud AI Enrichment",
                subtitle = "Send only extracted text (never images) to our AI provider for smarter titles, summaries and categories. Off = fully on-device, coarser results.",
                checked = cloudAiEnabled,
                onCheckedChange = { viewModel.setCloudAiEnabled(it) }
            )

            SettingsToggleItem(
                icon = Icons.Default.Analytics,
                title = "Share Analytics",
                subtitle = "Anonymous usage counts only — never OCR text, screenshot content, or note bodies.",
                checked = analyticsEnabled,
                onCheckedChange = { viewModel.setAnalyticsEnabled(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Consent (DPDP Act, 2023)",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (consentGiven) "Consent given" else "Consent not given",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = if (consentGiven) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        "You can withdraw consent at any time. Withdrawing pauses all AI processing of new captures.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (consentGiven) {
                TextButton(
                    onClick = { showWithdrawConfirm = true },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text("Withdraw Consent", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Data Management",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            // Export Data Button
            TextButton(
                onClick = { showExportInfo = true },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export My Data", color = MaterialTheme.colorScheme.primary)
            }

            // Erase Data Button
            TextButton(
                onClick = { showEraseConfirm = true },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Erase All My Data", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEraseConfirm) {
        AlertDialog(
            onDismissRequest = { showEraseConfirm = false },
            title = { Text("Erase all data?") },
            text = { Text("This permanently deletes all tasks, notes, captures, and calendar events stored on this device. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eraseAllData()
                    showEraseConfirm = false
                }) { Text("Erase Everything", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showEraseConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showWithdrawConfirm) {
        AlertDialog(
            onDismissRequest = { showWithdrawConfirm = false },
            title = { Text("Withdraw consent?") },
            text = { Text("New screenshots will no longer be processed by OCR or AI until you consent again from this screen. Existing data is kept — use \"Erase All My Data\" to remove it.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setConsentGiven(false)
                    viewModel.setOnDeviceAiEnabled(false)
                    viewModel.setCloudAiEnabled(false)
                    showWithdrawConfirm = false
                }) { Text("Withdraw", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showExportInfo) {
        AlertDialog(
            onDismissRequest = { showExportInfo = false },
            title = { Text("Export My Data") },
            text = { Text("Your tasks, notes, and captures live in this app's local database. A share-sheet/file export of your full data as JSON is planned — for now you can copy individual notes and captures via their own screen's share action.") },
            confirmButton = {
                TextButton(onClick = { showExportInfo = false }) { Text("Got it") }
            }
        )
    }
}
