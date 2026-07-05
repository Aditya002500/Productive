package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.ui.components.CaptureFlowBottomNavigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HabitEntity
import com.example.data.TaskEntity
import com.example.ui.AppViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun priorityRank(priority: String): Int = when (priority) {
    "High" -> 0
    "Medium" -> 1
    "Low" -> 2
    else -> 3
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateBottomBar: (String) -> Unit,
    onNavigateToTaskDetail: (Int) -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToCollections: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val habitsCompletedToday by viewModel.habitsCompletedTodayCount.collectAsStateWithLifecycle()
    val totalActiveHabits by viewModel.totalActiveHabitsCount.collectAsStateWithLifecycle()
    val userName by viewModel.userProfileName.collectAsStateWithLifecycle()
    val currentDate = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    val today = remember { LocalDate.now() }
    val todayMonthName = remember(today) { today.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) }
    val todaysEvents = remember(events, todayMonthName, today) {
        events.filter { it.monthName.equals(todayMonthName, ignoreCase = true) && it.day == today.dayOfMonth }
    }
    val todaysEventsDone = remember(todaysEvents) { todaysEvents.count { it.isDone } }
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Greeting
        Column {
            Text(
                text = currentDate,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$greeting, $userName",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        SectionLabel("OVERVIEW")
        Spacer(modifier = Modifier.height(12.dp))

        // Grid (Progress & Priority)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.85f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(80.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            strokeWidth = 8.dp
                        )
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(80.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 8.dp
                        )
                        Text(
                            "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Completed: $completedCount/$totalCount", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text("Events today: ${todaysEvents.size}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            // Priority Tasks Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.85f)
                    .clickable { onNavigateToCreateTask() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Priority Tasks", 
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val priorityTasks = remember(tasks) {
                        tasks.filter { !it.isCompleted }
                            .sortedWith(compareBy({ priorityRank(it.priority) }, { it.dueDate }, { it.dueTime }))
                            .take(3)
                    }
                    if (priorityTasks.isEmpty()) {
                        Text("No priority tasks.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                    } else {
                        priorityTasks.forEach { task ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .clickable { viewModel.toggleTaskCompletion(task) }
                            ) {
                                Icon(
                                    if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                    contentDescription = "Mark done",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        SectionLabel("SCHEDULE")
        Spacer(modifier = Modifier.height(12.dp))

        // Up Next Card — the first non-done event scheduled for today, if any.
        val upNext = todaysEvents.firstOrNull { !it.isDone }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateBottomBar("planner") },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    "UP NEXT",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    upNext?.title ?: "No events scheduled today",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (upNext != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (upNext.location.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(upNext.location, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(upNext.timeRange, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tap to add one from the Planner", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Today's Timeline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (todaysEvents.isEmpty()) {
            Text(
                "No events today. Add one from the Planner tab.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                items(todaysEvents) { event ->
                    val isNext = event == upNext
                    Card(
                        modifier = Modifier
                            .width(170.dp)
                            .height(120.dp)
                            .then(
                                if (!isNext) Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                else Modifier
                            )
                            .clickable { onNavigateBottomBar("planner") },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isNext) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val textColor = if (isNext) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground
                            Text(event.timeRange, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(event.title, color = textColor.copy(alpha = 0.85f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        SectionLabel("TODAY'S PROGRESS")
        Spacer(modifier = Modifier.height(12.dp))

        // ── Habits Today cross-tab section ────────────────────────────────
        if (habits.any { it.isActive }) {
            HabitsTodayCard(
                habits = habits.filter { it.isActive },
                habitsCompletedToday = habitsCompletedToday,
                totalActive = totalActiveHabits,
                onNavigateToHabits = { /* drawer handles navigation */ }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            "Task Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        TaskBreakdownChart(
            tasks = tasks,
            eventsDone = todaysEventsDone,
            eventsTotal = todaysEvents.size,
            habitsCompletedToday = habitsCompletedToday,
            totalActiveHabits = totalActiveHabits
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Bar chart showing completed vs total for Tasks (by priority), Events (today), and Habits (today).
 */
@Composable
private fun TaskBreakdownChart(
    tasks: List<TaskEntity>,
    eventsDone: Int,
    eventsTotal: Int,
    habitsCompletedToday: Int,
    totalActiveHabits: Int
) {
    val priorities = listOf("High", "Medium", "Low")
    val completedColor = MaterialTheme.colorScheme.primary
    val pendingColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    val eventsColor = MaterialTheme.colorScheme.tertiary
    val habitsColor = MaterialTheme.colorScheme.secondary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val taskCounts = remember(tasks) {
        priorities.map { p ->
            val forPriority = tasks.filter { it.priority == p }
            forPriority.count { it.isCompleted } to forPriority.size
        }
    }

    // All bars: 3 task priority bars + Events + Habits
    val allBars: List<Triple<String, Int, Int>> = remember(taskCounts, eventsDone, eventsTotal, habitsCompletedToday, totalActiveHabits) {
        taskCounts.mapIndexed { i, (done, total) -> Triple(priorities[i], done, total) } +
        listOf(
            Triple("Events", eventsDone, eventsTotal),
            Triple("Habits", habitsCompletedToday, totalActiveHabits)
        )
    }
    val maxCount = (allBars.maxOfOrNull { it.third } ?: 0).coerceAtLeast(1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                allBars.forEachIndexed { index, (label, completed, total) ->
                    val barCompleted = when {
                        index < 3 -> completedColor
                        index == 3 -> eventsColor
                        else -> habitsColor
                    }
                    val barPending = barCompleted.copy(alpha = 0.25f)
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Canvas(
                            modifier = Modifier
                                .width(32.dp)
                                .weight(1f)
                        ) {
                            val barWidth = size.width
                            val totalHeight = size.height * (total.toFloat() / maxCount)
                            val completedHeight = size.height * (completed.toFloat() / maxCount)
                            drawRect(
                                color = barPending,
                                topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - totalHeight),
                                size = Size(barWidth, totalHeight)
                            )
                            drawRect(
                                color = barCompleted,
                                topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - completedHeight),
                                size = Size(barWidth, completedHeight)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall, color = labelColor, maxLines = 1)
                        Text("$completed/$total", style = MaterialTheme.typography.labelSmall, color = labelColor)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(completedColor, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tasks", style = MaterialTheme.typography.labelSmall, color = labelColor)
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.size(8.dp).background(eventsColor, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Events", style = MaterialTheme.typography.labelSmall, color = labelColor)
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.size(8.dp).background(habitsColor, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Habits", style = MaterialTheme.typography.labelSmall, color = labelColor)
            }
        }
    }
}

/** Cross-tab "Habits Today" summary card on HomeScreen. */
@Composable
private fun HabitsTodayCard(
    habits: List<HabitEntity>,
    habitsCompletedToday: Int,
    totalActive: Int,
    onNavigateToHabits: () -> Unit
) {
    val allDone = habitsCompletedToday == totalActive && totalActive > 0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToHabits),
        colors = CardDefaults.cardColors(
            containerColor = if (allDone) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Habits Today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (allDone) {
                    Text("All done! 🎉", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(
                        "$habitsCompletedToday / $totalActive",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Streak chips for top 4 habits
            val topHabits = habits.sortedByDescending { it.currentStreak }.take(4)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(topHabits) { habit ->
                    val info = com.example.ui.screens.habitTypeInfo(habit.habitType)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(info.emoji, fontSize = 12.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(habit.title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            if (habit.currentStreak > 0) {
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "🔥${habit.currentStreak}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
