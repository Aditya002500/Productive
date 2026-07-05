package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppViewModel

private data class LeaderboardEntry(val name: String, val score: Int, val isYou: Boolean = false)

// Fixed demo friends (no real backend yet) — clearly framed in the UI as a preview feature.
private val DEMO_FRIENDS = listOf(
    LeaderboardEntry("Priya", 145),
    LeaderboardEntry("Rohan", 98),
    LeaderboardEntry("Sara", 210),
    LeaderboardEntry("Dev", 60)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val myScore by viewModel.productivityScore.collectAsStateWithLifecycle()
    val userName by viewModel.userProfileName.collectAsStateWithLifecycle()

    val leaderboard = remember(myScore, userName) {
        (DEMO_FRIENDS + LeaderboardEntry(userName, myScore, isYou = true))
            .sortedByDescending { it.score }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compete with Friends", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Local leaderboard preview",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "These friends are sample data so you can see how competing works. Invite real friends coming soon.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            itemsIndexed(leaderboard) { index, entry ->
                LeaderboardRow(rank = index + 1, entry = entry)
            }
        }
    }
}

@Composable
private fun LeaderboardRow(rank: Int, entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isYou) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(rankColor(rank)),
                contentAlignment = Alignment.Center
            ) {
                if (rank <= 3) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text("$rank", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                if (entry.isYou) "${entry.name} (You)" else entry.name,
                modifier = Modifier.weight(1f),
                fontWeight = if (entry.isYou) FontWeight.Bold else FontWeight.Normal
            )
            Text("${entry.score} pts", fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun rankColor(rank: Int): Color = when (rank) {
    1 -> Color(0xFFFFB300)
    2 -> Color(0xFF9E9E9E)
    3 -> Color(0xFF8D6E63)
    else -> Color(0xFFB0BEC5)
}
