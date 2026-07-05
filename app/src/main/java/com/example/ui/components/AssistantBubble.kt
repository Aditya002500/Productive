package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.ChatMessage
import com.example.ui.AppViewModel

/**
 * Floating chat entry point shown on every authenticated screen. Collapsed, it's a small FAB;
 * tapping it expands the same composable in place into a compact chat panel (no navigation).
 */
@Composable
fun AssistantBubble(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    if (expanded) {
        AssistantPanel(viewModel = viewModel, onClose = { expanded = false }, modifier = modifier)
    } else {
        FloatingActionButton(onClick = { expanded = true }, modifier = modifier) {
            Icon(Icons.Default.SmartToy, contentDescription = "Assistant")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssistantPanel(
    viewModel: AppViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.assistantMessages.collectAsStateWithLifecycle()
    val isReplying by viewModel.isAssistantReplying.collectAsStateWithLifecycle()
    val consentGiven by viewModel.consentGiven.collectAsStateWithLifecycle()
    val cloudAiEnabled by viewModel.cloudAiEnabled.collectAsStateWithLifecycle()
    val messagesUsedToday by viewModel.aiChatMessagesUsedToday.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()

    val quotaExhausted = !isPremium && messagesUsedToday >= AppViewModel.DAILY_AI_CHAT_LIMIT
    val disabledReason = when {
        !consentGiven || !cloudAiEnabled -> "Enable Cloud AI and data consent in Settings to chat with the Assistant."
        quotaExhausted -> "You've reached today's limit of ${AppViewModel.DAILY_AI_CHAT_LIMIT} messages. Try again tomorrow."
        else -> null
    }

    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Card(
        modifier = modifier.width(320.dp).height(420.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Assistant", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            HorizontalDivider()

            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Ask about your tasks, notes, habits, or how to use the app.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { message -> ChatBubble(message) }
                    if (isReplying) item { TypingIndicatorBubble() }
                }
            }

            Text(
                disabledReason ?: "${(AppViewModel.DAILY_AI_CHAT_LIMIT - messagesUsedToday).coerceAtLeast(0)} messages left today",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = if (disabledReason != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask the Assistant") },
                    enabled = disabledReason == null && !isReplying,
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 3
                )
                IconButton(
                    onClick = {
                        viewModel.sendAssistantMessage(input.trim())
                        input = ""
                    },
                    enabled = disabledReason == null && !isReplying && input.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 240.dp)
                .background(
                    if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                message.text,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun TypingIndicatorBubble() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text("...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}
