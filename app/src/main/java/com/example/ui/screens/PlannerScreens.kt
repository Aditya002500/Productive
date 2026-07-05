package com.example.ui.screens

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.YearMonth
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import com.example.data.EventEntity
import com.example.data.HabitLogEntity
import com.example.data.calendar.GoogleCalendarService
import com.example.ui.theme.accents
import com.example.ui.AppViewModel
import com.example.ui.components.TexturedBackground
import com.example.ui.components.CaptureFlowBottomNavigation
import kotlinx.coroutines.launch

/** A quick-fill preset for the Add Event dialog. colorIndex maps into the dialog's eventPalette. */
data class EventTemplate(val title: String, val timeRange: String, val colorIndex: Int)

val EVENT_TEMPLATES = listOf(
    EventTemplate("Meeting", "10:00 - 11:00 AM", 0),
    EventTemplate("Workout", "6:00 - 7:00 AM", 3),
    EventTemplate("Study Session", "2:00 - 4:00 PM", 2),
    EventTemplate("Meal", "12:30 - 1:00 PM", 1)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: AppViewModel,
    initialTab: String = "day",
    onNavigateBottomBar: (String) -> Unit
) {
    var activeSubTab by remember { mutableStateOf(initialTab) } // "day", "week", "month"
    val events by viewModel.events.collectAsStateWithLifecycle()

    var showAddEventDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val calendarService = remember { GoogleCalendarService() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isImportingCalendar by remember { mutableStateOf(false) }
    var pendingImportActivity by remember { mutableStateOf<Activity?>(null) }

    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val currentActivity = pendingImportActivity
        if (result.resultCode == Activity.RESULT_OK && currentActivity != null) {
            coroutineScope.launch {
                try {
                    val authResult = calendarService.authorize(currentActivity)
                    val token = authResult.accessToken
                    if (token != null) {
                        val importedEvents = calendarService.fetchUpcomingEvents(token)
                        val count = viewModel.importGoogleCalendarEvents(importedEvents)
                        snackbarHostState.showSnackbar("Imported $count event(s) from Google Calendar.")
                    } else {
                        snackbarHostState.showSnackbar("Google Calendar authorization was not completed.")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("GoogleCalendarImport", "Import failed", e)
                    snackbarHostState.showSnackbar("Couldn't import from Google Calendar: ${e.message}")
                } finally {
                    isImportingCalendar = false
                }
            }
        } else {
            isImportingCalendar = false
        }
    }

    fun runGoogleCalendarImport() {
        val currentActivity = activity ?: return
        pendingImportActivity = currentActivity
        coroutineScope.launch {
            isImportingCalendar = true
            try {
                val authResult = calendarService.authorize(currentActivity)
                val token = authResult.accessToken
                val pendingIntent = authResult.pendingIntent
                when {
                    token != null -> {
                        val importedEvents = calendarService.fetchUpcomingEvents(token)
                        val count = viewModel.importGoogleCalendarEvents(importedEvents)
                        snackbarHostState.showSnackbar("Imported $count event(s) from Google Calendar.")
                        isImportingCalendar = false
                    }
                    authResult.hasResolution() && pendingIntent != null -> {
                        // isImportingCalendar stays true until consentLauncher's callback finishes.
                        consentLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
                    }
                    else -> {
                        snackbarHostState.showSnackbar("Couldn't authorize Google Calendar access.")
                        isImportingCalendar = false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("GoogleCalendarImport", "Import failed", e)
                snackbarHostState.showSnackbar("Couldn't import from Google Calendar: ${e.message}")
                isImportingCalendar = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(
                    onClick = { if (!isImportingCalendar) runGoogleCalendarImport() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    if (isImportingCalendar) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = "Import from Google Calendar")
                    }
                }
                FloatingActionButton(
                    onClick = { showAddEventDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Segmented Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("day" to "Day", "week" to "Week", "month" to "Month").forEach { (tabKey, tabLabel) ->
                    val isSelected = activeSubTab == tabKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { activeSubTab = tabKey }
                            .testTag("planner_tab_$tabKey"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub Tab Views
            Box(modifier = Modifier.weight(1f)) {
                if (activeSubTab == "day") {
                    DayView(events = events, viewModel = viewModel, selectedDate = selectedDate, onDateSelected = { selectedDate = it })
                } else if (activeSubTab == "week") {
                    WeekView(events = events, selectedDate = selectedDate, onDateSelected = { selectedDate = it })
                } else if (activeSubTab == "month") {
                    MonthView(events = events, selectedDate = selectedDate, onDateSelected = { selectedDate = it })
                }
            }
        }
    }

    // CREATE EVENT DIALOG
    if (showAddEventDialog) {
        var title by remember { mutableStateOf("") }
        var timeRange by remember { mutableStateOf("10:00 - 11:00 AM") }
        var location by remember { mutableStateOf("") }
        var day by remember(selectedDate) { mutableStateOf(selectedDate.dayOfMonth.toString()) }
        var monthName by remember(selectedDate) { mutableStateOf(selectedDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)) }
        var selectedColorIndex by remember { mutableStateOf(0) }

        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = { Text("Add Event", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Quick Templates:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EVENT_TEMPLATES.forEach { template ->
                            AssistChip(
                                onClick = {
                                    title = template.title
                                    timeRange = template.timeRange
                                    selectedColorIndex = template.colorIndex
                                },
                                label = { Text(template.title) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = timeRange,
                        onValueChange = { timeRange = it },
                        label = { Text("Time (e.g. 10:00 - 11:00 AM)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (e.g. Hall B, Meet)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = day,
                            onValueChange = { day = it },
                            label = { Text("Day (Number)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = monthName,
                            onValueChange = { monthName = it },
                            label = { Text("Month") },
                            modifier = Modifier.weight(1.5f)
                        )
                    }

                    Text("Color Tag:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    val eventPalette = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.accents.warning,
                        MaterialTheme.accents.info,
                        MaterialTheme.colorScheme.error
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        eventPalette.forEachIndexed { idx, color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        width = if (selectedColorIndex == idx) 2.dp else 0.dp,
                                        color = if (selectedColorIndex == idx) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorIndex = idx }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val parsedDay = day.toIntOrNull() ?: 24
                            viewModel.addEvent(
                                title = title,
                                timeRange = timeRange,
                                location = location,
                                day = parsedDay,
                                monthName = monthName,
                                color = selectedColorIndex
                            )
                            showAddEventDialog = false
                        }
                    }
                ) {
                    Text("Save Event")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// 1. DAY VIEW
@Composable
fun DayView(events: List<EventEntity>, viewModel: AppViewModel, selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val monthName = selectedDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val dayEvents = events.filter { it.monthName.equals(monthName, ignoreCase = true) && it.day == selectedDate.dayOfMonth }
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentHabitLogs.collectAsStateWithLifecycle()
    val selectedEpochDay = remember(selectedDate) { selectedDate.toEpochDay() }
    val dayHabitLogs = remember(recentLogs, selectedEpochDay) { recentLogs.filter { it.epochDay == selectedEpochDay } }
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    // ponytail: dueDate/date are frozen relative labels ("Today"/"Tomorrow"), not real dates —
    // matching must use the same label, and a label goes stale the day after it's set.
    val selectedDateLabel = remember(selectedDate) { selectedDate.toDisplayLabel() }
    val dayTasks = remember(tasks, selectedDateLabel) { tasks.filter { it.dueDate == selectedDateLabel } }
    val dayNotes = remember(notes, selectedDateLabel) { notes.filter { it.date == selectedDateLabel } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        // Horizontal calendar day strip
        val daysStrip = (-3..3).map { selectedDate.plusDays(it.toLong()) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysStrip.forEach { date ->
                val isSelected = date == selectedDate
                val name = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val num = date.dayOfMonth
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .width(48.dp)
                        .height(72.dp)
                        .padding(horizontal = 4.dp)
                        .clickable { onDateSelected(date) },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = num.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Agenda for ${selectedDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${selectedDate.dayOfMonth}",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hourly rows pulling from Room
        if (dayEvents.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                dayEvents.forEach { event ->
                    val containerColor = when (event.color) {
                        0 -> MaterialTheme.colorScheme.primaryContainer
                        1 -> MaterialTheme.colorScheme.primaryContainer
                        2 -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                    val accentColor = when (event.color) {
                        0 -> MaterialTheme.colorScheme.primary
                        1 -> MaterialTheme.colorScheme.secondary
                        2 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                    val doneAlpha = if (event.isDone) 0.5f else 1f

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = event.timeRange.substringBefore(" -").substringBefore(" PM").substringBefore(" AM"),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = doneAlpha)
                            ),
                            modifier = Modifier.width(60.dp)
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = doneAlpha)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            border = BorderStroke(1.dp, if (event.isDone) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else accentColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = doneAlpha),
                                            textDecoration = if (event.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (event.location.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = accentColor.copy(alpha = doneAlpha), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = event.location,
                                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = doneAlpha))
                                            )
                                        }
                                    }
                                }

                                // Mark done button
                                IconButton(onClick = { viewModel.toggleEventDone(event) }) {
                                    Icon(
                                        if (event.isDone) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                        contentDescription = if (event.isDone) "Mark undone" else "Mark done",
                                        tint = if (event.isDone) MaterialTheme.colorScheme.primary else accentColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteEvent(event) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = accentColor.copy(alpha = doneAlpha), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Habit completions for this day ──────────────────────────────
        if (dayHabitLogs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Habit Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dayHabitLogs.forEach { log ->
                    val habit = habits.find { it.id == log.habitId }
                    if (habit != null) {
                        val info = habitTypeInfo(habit.habitType)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(info.emoji, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(habit.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                if (log.metricValue > 0f) {
                                    val display = if (log.metricValue == kotlin.math.floor(log.metricValue.toDouble()).toFloat()) log.metricValue.toInt().toString() else "%.1f".format(log.metricValue)
                                    Text(
                                        "$display ${habit.metricUnit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                if (log.note.isNotBlank()) {
                                    Text(
                                        "\"${log.note}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── Tasks due this day ──────────────────────────────────────────
        if (dayTasks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Tasks Due",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dayTasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            )
                            Text(
                                task.priority,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleTaskCompletion(task) },
                            enabled = !task.isCompleted
                        ) {
                            Icon(
                                if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                contentDescription = if (task.isCompleted) "Done" else "Mark done",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── Notes created this day ───────────────────────────────────────
        if (dayNotes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dayNotes.forEach { note ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(note.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        if (note.content.isNotBlank()) {
                            Text(
                                note.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        if (dayEvents.isEmpty() && dayHabitLogs.isEmpty() && dayTasks.isEmpty() && dayNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Nothing scheduled for ${selectedDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${selectedDate.dayOfMonth}.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// 2. WEEK VIEW
@Composable
fun WeekView(events: List<EventEntity>, selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val startOfWeek = selectedDate.with(DayOfWeek.MONDAY)
    val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(weekDays) { date ->
            val monthName = date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            val dayNum = date.dayOfMonth
            val dayStr = "${date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())} $dayNum"
            val dayEvents = events.filter { it.monthName.equals(monthName, ignoreCase = true) && it.day == dayNum }

            Card(
                colors = CardDefaults.cardColors(containerColor = if (date == selectedDate) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().clickable { onDateSelected(date) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = dayStr,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (dayEvents.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            dayEvents.forEach { event ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                when (event.color) {
                                                    0 -> MaterialTheme.colorScheme.primary
                                                    1 -> MaterialTheme.colorScheme.secondary
                                                    else -> MaterialTheme.colorScheme.tertiary
                                                },
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "${event.timeRange}: ${event.title}",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No events scheduled",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

// 3. MONTH VIEW
@Composable
fun MonthView(events: List<EventEntity>, selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val yearMonth = YearMonth.from(selectedDate)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = selectedDate.withDayOfMonth(1)
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7

    val monthName = selectedDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val monthEvents = events.filter { it.monthName.equals(monthName, ignoreCase = true) && it.day == selectedDate.dayOfMonth }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // Month Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onDateSelected(selectedDate.minusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
            }
            Text(
                text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            IconButton(onClick = { onDateSelected(selectedDate.plusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekday abbreviations
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayLabel ->
                Text(
                    text = dayLabel,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Month grid
        Column {
            val totalCells = daysInMonth + startOffset
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val index = row * 7 + col
                        val dayNumber = index - startOffset + 1

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNumber in 1..daysInMonth) {
                                val isSelected = dayNumber == selectedDate.dayOfMonth
                                val hasEvents = events.any { it.day == dayNumber && it.monthName.equals(monthName, ignoreCase = true) }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else if (hasEvents) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else Color.Transparent
                                        )
                                        .clickable { onDateSelected(selectedDate.withDayOfMonth(dayNumber)) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected || hasEvents) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        if (hasEvents && !isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Day Agenda Details
        Text(
            text = "Agenda for ${selectedDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${selectedDate.dayOfMonth}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (monthEvents.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                monthEvents.forEach { event ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = event.timeRange,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        } else {
            Text(
                text = "No events scheduled for this day.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}
