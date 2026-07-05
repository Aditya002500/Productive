package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppViewModel
import kotlinx.coroutines.delay

private const val FOCUS_SESSION_SECONDS = 25 * 60

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val sessionsToday by viewModel.focusSessionsToday.collectAsStateWithLifecycle()
    var secondsRemaining by remember { mutableIntStateOf(FOCUS_SESSION_SECONDS) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning && secondsRemaining > 0) {
            delay(1000)
            secondsRemaining -= 1
        }
        if (isRunning && secondsRemaining == 0) {
            isRunning = false
            viewModel.recordFocusSessionCompleted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus Timer", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "%02d:%02d".format(secondsRemaining / 60, secondsRemaining % 60),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilledIconButton(onClick = { isRunning = !isRunning }, enabled = secondsRemaining > 0) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start"
                    )
                }
                OutlinedIconButton(onClick = {
                    isRunning = false
                    secondsRemaining = FOCUS_SESSION_SECONDS
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                }
            }
            Spacer(Modifier.height(32.dp))
            Text(
                "$sessionsToday focus session${if (sessionsToday == 1) "" else "s"} completed today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
