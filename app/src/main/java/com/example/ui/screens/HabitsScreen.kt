package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HabitEntity
import com.example.data.HabitLogEntity
import com.example.ui.AppViewModel
import java.time.LocalDate

// ─── Habit type metadata ─────────────────────────────────────────────────────

data class HabitTypeInfo(
    val key: String,
    val label: String,
    val emoji: String,
    val defaultMetricUnit: String,  // empty = no metric
    val metricLabel: String         // e.g. "Distance (km)", "Duration (min)"
)

val HABIT_TYPES = listOf(
    HabitTypeInfo("Generic",    "Generic",    "⭐", "",    ""),
    HabitTypeInfo("Running",    "Running",    "🏃", "km",  "Distance (km)"),
    HabitTypeInfo("Cycling",    "Cycling",    "🚴", "km",  "Distance (km)"),
    HabitTypeInfo("Meditation", "Meditation", "🧘", "min", "Duration (min)"),
    HabitTypeInfo("Strength",   "Strength",   "💪", "reps","Total reps"),
    HabitTypeInfo("Yoga",       "Yoga",       "🌿", "min", "Duration (min)"),
    HabitTypeInfo("Hydration",  "Hydration",  "💧", "ml",  "Volume (ml)"),
    HabitTypeInfo("Reading",    "Reading",    "📚", "min", "Duration (min)"),
    HabitTypeInfo("Sleep",      "Sleep",      "😴", "min", "Duration (min)")
)

fun habitTypeInfo(key: String) = HABIT_TYPES.find { it.key == key } ?: HABIT_TYPES.first()

fun streakBadge(streak: Int): String? = when {
    streak >= 100 -> "🏆 100"
    streak >= 60  -> "🥇 60"
    streak >= 30  -> "🥈 30"
    streak >= 14  -> "🥉 14"
    streak >= 7   -> "🌟 7"
    streak >= 3   -> "🔥 3"
    else          -> null
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onNavigateToTracking: (Int) -> Unit
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentHabitLogs.collectAsStateWithLifecycle()
    val habitsCompletedToday by viewModel.habitsCompletedTodayCount.collectAsStateWithLifecycle()
    val totalActive by viewModel.totalActiveHabitsCount.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedHabitForMetric by remember { mutableStateOf<HabitEntity?>(null) }
    var selectedHabitForDetail by remember { mutableStateOf<HabitEntity?>(null) }

    val todayEpochDay = remember { LocalDate.now().toEpochDay() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Tracker", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Habit") }
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
            // ── Stats overview card ──────────────────────────────────────────
            item {
                HabitStatsCard(
                    completedToday = habitsCompletedToday,
                    totalActive = totalActive,
                    habits = habits
                )
            }

            if (habits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No habits yet.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tap + to add your first habit — running, meditation, hydration, and more.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(habits, key = { it.id }) { habit ->
                    val logsForHabit = remember(recentLogs, habit.id) { recentLogs.filter { it.habitId == habit.id } }
                    val completedToday = logsForHabit.any { it.epochDay == todayEpochDay }
                    HabitCard(
                        habit = habit,
                        recentLogs = logsForHabit,
                        completedToday = completedToday,
                        onToggleActive = { viewModel.toggleHabitActive(habit) },
                        onDelete = { viewModel.deleteHabit(habit) },
                        onComplete = {
                            val info = habitTypeInfo(habit.habitType)
                            if (info.defaultMetricUnit.isNotEmpty() && habit.dailyGoalValue > 0f) {
                                selectedHabitForMetric = habit
                            } else {
                                viewModel.completeHabitToday(habit)
                            }
                        },
                        onCardClick = { selectedHabitForDetail = habit }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // ── Add habit dialog ─────────────────────────────────────────────────────
    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, intervalHours, habitType, metricUnit, goalValue, weeklyTarget ->
                viewModel.addHabit(title, intervalHours, habitType, metricUnit, goalValue, weeklyTarget)
                showAddDialog = false
            }
        )
    }

    // ── Metric entry dialog ──────────────────────────────────────────────────
    selectedHabitForMetric?.let { habit ->
        MetricEntryDialog(
            habit = habit,
            onDismiss = { selectedHabitForMetric = null },
            onConfirm = { value, note ->
                viewModel.completeHabitToday(habit, value, note)
                selectedHabitForMetric = null
            },
            onTrackAutomatically = {
                selectedHabitForMetric = null
                onNavigateToTracking(habit.id)
            }
        )
    }

    // ── Detail bottom sheet ──────────────────────────────────────────────────
    selectedHabitForDetail?.let { habit ->
        val logsForDetail = remember(recentLogs, habit.id) { recentLogs.filter { it.habitId == habit.id } }
        HabitDetailSheet(
            habit = habit,
            logs = logsForDetail,
            onDismiss = { selectedHabitForDetail = null }
        )
    }
}

// ─── Stats overview card ──────────────────────────────────────────────────────

@Composable
private fun HabitStatsCard(
    completedToday: Int,
    totalActive: Int,
    habits: List<HabitEntity>
) {
    val progress = if (totalActive == 0) 0f else completedToday.toFloat() / totalActive
    val best = habits.maxByOrNull { it.currentStreak }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Today's Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "$completedToday / $totalActive habits",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                if (completedToday == totalActive && totalActive > 0) {
                    Text("🎉", fontSize = 36.sp)
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(56.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            strokeWidth = 6.dp
                        )
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(56.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 6.dp
                        )
                        Text(
                            "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            if (progress > 0f) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            }
            if (best != null && best.currentStreak > 0) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "🔥 Best streak: ${best.title} — ${best.currentStreak} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ─── Habit card ───────────────────────────────────────────────────────────────

@Composable
private fun HabitCard(
    habit: HabitEntity,
    recentLogs: List<HabitLogEntity>,
    completedToday: Boolean,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit,
    onCardClick: () -> Unit
) {
    val info = habitTypeInfo(habit.habitType)
    val today = remember { LocalDate.now() }

    // 7-day completion strip
    val last7Days = remember(today) { (6 downTo 0).map { today.minusDays(it.toLong()) } }
    val completedDays = remember(recentLogs) { recentLogs.map { it.epochDay }.toSet() }

    val badge = streakBadge(habit.longestStreak)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = if (!habit.isActive)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = if (completedToday)
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type emoji badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (completedToday) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(info.emoji, fontSize = 18.sp)
                }

                Spacer(Modifier.width(12.dp))

                // Title + subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (!habit.isActive) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.onSurface
                    )
                    val subtitle = buildString {
                        append(info.label)
                        if (habit.dailyGoalValue > 0f) append(" · ${habit.dailyGoalValue.toDisplayString()} ${habit.metricUnit}")
                        append(" · Every ${habit.intervalHours}h")
                    }
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Streak badge
                if (habit.currentStreak > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔥", fontSize = 14.sp)
                        Text(
                            "${habit.currentStreak}d",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            // 7-day dot strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                last7Days.forEach { day ->
                    val done = completedDays.contains(day.toEpochDay())
                    val isToday = day == today
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(if (isToday) 12.dp else 10.dp)
                                .background(
                                    color = when {
                                        done -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    },
                                    shape = CircleShape
                                )
                                .then(
                                    if (isToday && !done)
                                        Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier
                                )
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            day.dayOfWeek.name.take(1),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Badge unlock label
                if (badge != null) {
                    Text(
                        badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    Text(
                        "Best: ${habit.longestStreak}d",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Complete button
                    FilledTonalButton(
                        onClick = onComplete,
                        enabled = !completedToday && habit.isActive,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (completedToday) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.secondaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            if (completedToday) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (completedToday) "Done!" else "Log",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    // Toggle active
                    Switch(
                        checked = habit.isActive,
                        onCheckedChange = { onToggleActive() },
                        modifier = Modifier.height(24.dp)
                    )

                    // Delete
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete habit",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ─── Metric entry dialog ──────────────────────────────────────────────────────

@Composable
private fun MetricEntryDialog(
    habit: HabitEntity,
    onDismiss: () -> Unit,
    onConfirm: (value: Float, note: String) -> Unit,
    onTrackAutomatically: () -> Unit
) {
    val info = habitTypeInfo(habit.habitType)
    var valueText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${info.emoji} Log ${habit.title}") },
        text = {
            Column {
                Text(
                    info.metricLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (habit.dailyGoalValue > 0f) {
                    Text(
                        "Goal: ${habit.dailyGoalValue.toDisplayString()} ${habit.metricUnit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (habit.habitType in listOf("Running", "Cycling")) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onTrackAutomatically) {
                        Text("📍 Track Automatically")
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text("${info.metricLabel} (${habit.metricUnit})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(valueText.toFloatOrNull() ?: 0f, note.trim()) },
                enabled = valueText.isNotBlank()
            ) { Text("Log") }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(0f, "") }) { Text("Skip") }
        }
    )
}

// ─── Habit detail sheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitDetailSheet(
    habit: HabitEntity,
    logs: List<HabitLogEntity>,
    onDismiss: () -> Unit
) {
    val info = habitTypeInfo(habit.habitType)
    val today = remember { LocalDate.now() }
    val last7Days = remember(today) { (6 downTo 0).map { today.minusDays(it.toLong()) } }
    val completedDays = remember(logs) { logs.map { it.epochDay }.toSet() }
    val totalMetric = remember(logs) { logs.sumOf { it.metricValue.toDouble() }.toFloat() }

    // Badge milestones
    val milestones = listOf(3, 7, 14, 30, 60, 100)
    val badgeEmojis = listOf("🔥", "🌟", "🥉", "🥈", "🥇", "🏆")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(info.emoji, fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(habit.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(info.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Streak stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(label = "Current", value = "${habit.currentStreak}d 🔥", modifier = Modifier.weight(1f))
                StatChip(label = "Best", value = "${habit.longestStreak}d 🏆", modifier = Modifier.weight(1f))
                if (info.defaultMetricUnit.isNotEmpty() && totalMetric > 0f) {
                    StatChip(
                        label = "Total ${info.defaultMetricUnit}",
                        value = "${totalMetric.toDisplayString()} ${habit.metricUnit}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // 7-day visual
            Text("Last 7 Days", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                last7Days.forEach { day ->
                    val done = completedDays.contains(day.toEpochDay())
                    val isToday = day == today
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (done) MaterialTheme.colorScheme.primary
                                    else if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                                .then(
                                    if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (done) Icon(Icons.Default.Check, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            day.dayOfWeek.name.take(2),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Badges
            Text("Badges", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                milestones.forEachIndexed { i, days ->
                    val unlocked = habit.longestStreak >= days
                    Box(
                        modifier = Modifier
                            .background(
                                if (unlocked) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (unlocked) badgeEmojis[i] else "🔒",
                                fontSize = 18.sp
                            )
                            Text(
                                "${days}d",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (unlocked) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Recent log history
            if (logs.isNotEmpty()) {
                Text("Recent Activity", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                logs.take(8).forEach { log ->
                    val day = LocalDate.ofEpochDay(log.epochDay)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(info.emoji, fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            day.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        if (log.metricValue > 0f) {
                            Text(
                                "${log.metricValue.toDisplayString()} ${habit.metricUnit}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (log.note.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "\"${log.note}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Add Habit Dialog ─────────────────────────────────────────────────────────

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, intervalHours: Int, habitType: String, metricUnit: String, goalValue: Float, weeklyTarget: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var intervalHours by remember { mutableStateOf(1) }
    var selectedType by remember { mutableStateOf(HABIT_TYPES.first()) }
    var goalText by remember { mutableStateOf("") }
    var weeklyTarget by remember { mutableStateOf(7) }

    val intervalOptions = listOf(1, 2, 3, 4, 6, 8, 12)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Habit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Name (e.g. Morning Run)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Type picker
                Text("Activity Type", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                val rows = HABIT_TYPES.chunked(3)
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { typeInfo ->
                            FilterChip(
                                selected = typeInfo == selectedType,
                                onClick = { selectedType = typeInfo },
                                label = { Text("${typeInfo.emoji} ${typeInfo.label}", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Pad last row if needed
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }

                // Goal (only for typed habits)
                if (selectedType.defaultMetricUnit.isNotEmpty()) {
                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { goalText = it },
                        label = { Text("Daily Goal (${selectedType.metricLabel})") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Remind interval
                Text("Remind me every:", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    intervalOptions.take(4).forEach { h ->
                        FilterChip(
                            selected = h == intervalHours,
                            onClick = { intervalHours = h },
                            label = { Text("${h}h", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    intervalOptions.drop(4).forEach { h ->
                        FilterChip(
                            selected = h == intervalHours,
                            onClick = { intervalHours = h },
                            label = { Text("${h}h", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - intervalOptions.drop(4).size) { Spacer(Modifier.weight(1f)) }
                }

                // Weekly target
                Text("Weekly target: $weeklyTarget days", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = weeklyTarget.toFloat(),
                    onValueChange = { weeklyTarget = it.toInt() },
                    valueRange = 1f..7f,
                    steps = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        title.trim(),
                        intervalHours,
                        selectedType.key,
                        selectedType.defaultMetricUnit,
                        goalText.toFloatOrNull() ?: 0f,
                        weeklyTarget
                    )
                },
                enabled = title.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

/** Pretty-print a float: show decimals only when non-zero. */
private fun Float.toDisplayString(): String =
    if (this == kotlin.math.floor(this.toDouble()).toFloat()) this.toInt().toString()
    else "%.1f".format(this)
