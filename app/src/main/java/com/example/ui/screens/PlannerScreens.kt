package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.EventEntity
import com.example.ui.AppViewModel
import com.example.ui.components.TexturedBackground
import com.example.ui.components.CaptureFlowBottomNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: AppViewModel,
    initialTab: String = "day",
    onNavigateBottomBar: (String) -> Unit,
    onBack: () -> Unit
) {
    var activeSubTab by remember { mutableStateOf(initialTab) } // "day", "week", "month"
    val events by viewModel.events.collectAsStateWithLifecycle()

    var showAddEventDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CaptureFlow Planner", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddEventDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Event")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            CaptureFlowBottomNavigation(
                currentRoute = "planner",
                onNavigate = onNavigateBottomBar
            )
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
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF6B7280)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub Tab Views
            Box(modifier = Modifier.weight(1f)) {
                if (activeSubTab == "day") {
                    DayView(events = events, viewModel = viewModel)
                } else if (activeSubTab == "week") {
                    WeekView(events = events)
                } else if (activeSubTab == "month") {
                    MonthView(events = events)
                }
            }
        }
    }

    // CREATE EVENT DIALOG
    if (showAddEventDialog) {
        var title by remember { mutableStateOf("") }
        var timeRange by remember { mutableStateOf("10:00 - 11:00 AM") }
        var location by remember { mutableStateOf("") }
        var day by remember { mutableStateOf("24") }
        var monthName by remember { mutableStateOf("October") }
        var selectedColorIndex by remember { mutableStateOf(0) }

        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = { Text("Add Event", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(Color(0xFF065F46), Color(0xFF047857), Color(0xFF059669), Color(0xFF10B981)).forEachIndexed { idx, color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        width = if (selectedColorIndex == idx) 2.dp else 0.dp,
                                        color = if (selectedColorIndex == idx) Color.Black else Color.Transparent,
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
fun DayView(events: List<EventEntity>, viewModel: AppViewModel) {
    // Let's filter October 24 events specifically as our focus mockup day!
    val oct24Events = events.filter { it.monthName == "October" && it.day == 24 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        // Horizontal calendar day strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(22 to "Sun", 23 to "Mon", 24 to "Tue", 25 to "Wed", 26 to "Thu", 27 to "Fri").forEach { (num, name) ->
                val isSelected = num == 24
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
                    ),
                    modifier = Modifier
                        .width(48.dp)
                        .height(72.dp),
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
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = num.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF191C1E),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Today's Agenda",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hourly rows pulling from Room
        if (oct24Events.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                oct24Events.forEach { event ->
                    val containerColor = when (event.color) {
                        0 -> Color(0xFFD1FAE5) // Emerald light
                        1 -> Color(0xFFECFCCB) // Lime light
                        2 -> Color(0xFFDCFCE7) // Green light
                        else -> Color(0xFFCCFBF1) // Teal light
                    }

                    val accentColor = when (event.color) {
                        0 -> MaterialTheme.colorScheme.primary
                        1 -> MaterialTheme.colorScheme.secondary
                        2 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Hour column
                        Text(
                            text = event.timeRange.substringBefore(" -").substringBefore(" PM").substringBefore(" AM"),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280)
                            ),
                            modifier = Modifier.width(60.dp)
                        )

                        // Event Block Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.Center) {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF191C1E)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (event.location.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = accentColor, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = event.location,
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF4B5563))
                                            )
                                        }
                                    }
                                }

                                IconButton(onClick = { viewModel.deleteEvent(event) }) {
                                    Icon(Icons.Default.Schedule, contentDescription = "Delete", tint = accentColor)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No events scheduled for Oct 24.", color = Color.Gray)
            }
        }
    }
}

// 2. WEEK VIEW
@Composable
fun WeekView(events: List<EventEntity>) {
    val weekDays = listOf("Mon 23", "Tue 24", "Wed 25", "Thu 26", "Fri 27", "Sat 28", "Sun 29")

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(weekDays) { dayStr ->
            val dayNum = dayStr.substringAfter(" ").toIntOrNull() ?: 24
            val dayEvents = events.filter { it.day == dayNum && it.monthName == "October" }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
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
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF191C1E)),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No events scheduled",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                        )
                    }
                }
            }
        }
    }
}

// 3. MONTH VIEW
@Composable
fun MonthView(events: List<EventEntity>) {
    var selectedDay by remember { mutableStateOf(14) } // November 14 is highlighted in mockup
    val monthEvents = events.filter { it.monthName == "November" && it.day == selectedDay }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // Month Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "November 2023",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191C1E)
                )
            )
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
                        color = Color.Gray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // November grid starting on Wednesday (first 3 boxes empty)
        Column {
            val totalCells = 30 + 3 // Nov has 30 days, starts Wednesday (offset of 3)
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val index = row * 7 + col
                        val dayNumber = index - 2 // Offset by 3 empty spots (0,1,2 empty)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNumber in 1..30) {
                                val isSelected = dayNumber == selectedDay
                                val hasEvents = events.any { it.day == dayNumber && it.monthName == "November" }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else if (hasEvents) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else Color.Transparent
                                        )
                                        .clickable { selectedDay = dayNumber },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected || hasEvents) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                else Color(0xFF191C1E)
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
            text = "Agenda for Nov $selectedDay",
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
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
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
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
        }
    }
}
