package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.data.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppViewModel
import com.example.ui.components.CaptureFlowBottomNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: AppViewModel,
    onNavigateToTaskDetail: (Int) -> Unit,
    onNavigateToNoteDetail: (Int) -> Unit,
    onNavigateToCaptureDetail: (Int) -> Unit,
    onNavigateBottomBar: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val captures by viewModel.captures.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Input Row
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search tasks, notes, OCR texts...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .testTag("search_input_field"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            singleLine = true
        )

        // Filtering Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("All", "Tasks", "Notes", "Screenshots", "Events", "Habits")) { category ->
                FilterChip(
                    selected = selectedFilter == category,
                    onClick = { viewModel.setFilter(category) },
                    label = { Text(category) },
                    modifier = Modifier.testTag("filter_chip_$category")
                )
            }
        }

        // Tag filter row — tags come from both notes and captures.
        val allTags by viewModel.allNoteTags.collectAsStateWithLifecycle()
        val allCaptureTags by viewModel.allCaptureTags.collectAsStateWithLifecycle()
        val combinedTags = remember(allTags, allCaptureTags) { (allTags + allCaptureTags).distinct().sorted() }
        var selectedTag by remember { mutableStateOf("All") }
        LaunchedEffect(combinedTags) {
            if (selectedTag != "All" && selectedTag !in combinedTags) selectedTag = "All"
        }
        if (combinedTags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (listOf("All") + combinedTags).forEach { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { selectedTag = tag },
                        label = { Text(tag) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filtering and compiling matching items dynamically!
        val filteredResults = remember(searchQuery, selectedFilter, selectedTag, tasks, notes, captures, events, habits) {
            val q = searchQuery.lowercase()
            val list = mutableListOf<SearchResultItem>()

            // Tasks filter (tasks have no tags field, so the tag filter excludes them when active)
            if ((selectedFilter == "All" || selectedFilter == "Tasks") && selectedTag == "All") {
                tasks.filter {
                    it.title.lowercase().contains(q) || it.notes.lowercase().contains(q)
                }.forEach {
                    list.add(SearchResultItem.TaskResult(it))
                }
            }

            // Notes filter
            if (selectedFilter == "All" || selectedFilter == "Notes") {
                notes.filter {
                    (it.title.lowercase().contains(q) || it.content.lowercase().contains(q)) &&
                        (selectedTag == "All" || it.tags.split(",").map { t -> t.trim() }.contains(selectedTag))
                }.forEach {
                    list.add(SearchResultItem.NoteResult(it))
                }
            }

            // Screenshot captures filter
            if (selectedFilter == "All" || selectedFilter == "Screenshots") {
                captures.filter {
                    (it.title.lowercase().contains(q) || it.extractedText.lowercase().contains(q)) &&
                        (selectedTag == "All" || it.tags.split(",").map { t -> t.trim() }.contains(selectedTag))
                }.forEach {
                    list.add(SearchResultItem.CaptureResult(it))
                }
            }

            // Events filter (no tags field, excluded when a tag filter is active)
            if ((selectedFilter == "All" || selectedFilter == "Events") && selectedTag == "All") {
                events.filter {
                    it.title.lowercase().contains(q) || it.location.lowercase().contains(q)
                }.forEach {
                    list.add(SearchResultItem.EventResult(it))
                }
            }

            // Habits filter (no tags field, excluded when a tag filter is active)
            if ((selectedFilter == "All" || selectedFilter == "Habits") && selectedTag == "All") {
                habits.filter {
                    it.title.lowercase().contains(q)
                }.forEach {
                    list.add(SearchResultItem.HabitResult(it))
                }
            }

            list
        }

        // Rendered Search Results
        if (filteredResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Results (${filteredResults.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(filteredResults) { item ->
                    when (item) {
                        is SearchResultItem.TaskResult -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToTaskDetail(item.task.id) }
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TaskAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(item.task.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Task • Priority: ${item.task.priority}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        is SearchResultItem.NoteResult -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToNoteDetail(item.note.id) }
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(item.note.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(item.note.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                        is SearchResultItem.CaptureResult -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToCaptureDetail(item.capture.id) }
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(item.capture.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Screenshot Category: ${item.capture.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        is SearchResultItem.EventResult -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(item.event.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Event • ${item.event.monthName} ${item.event.day}, ${item.event.timeRange}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        is SearchResultItem.HabitResult -> {
                            val info = habitTypeInfo(item.habit.habitType)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(info.emoji, style = MaterialTheme.typography.titleLarge)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.habit.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        val subtitle = buildString {
                                            append("Habit · ${info.label}")
                                            if (item.habit.dailyGoalValue > 0f) append(" · ${item.habit.dailyGoalValue.toInt()} ${item.habit.metricUnit}/day")
                                        }
                                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (item.habit.currentStreak > 0) {
                                        Text(
                                            "🔥 ${item.habit.currentStreak}d",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No matching items found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Try a different search term or filter",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Sealed class wrapper for typed results
sealed class SearchResultItem {
    data class TaskResult(val task: TaskEntity) : SearchResultItem()
    data class NoteResult(val note: NoteEntity) : SearchResultItem()
    data class CaptureResult(val capture: CaptureEntity) : SearchResultItem()
    data class EventResult(val event: EventEntity) : SearchResultItem()
    data class HabitResult(val habit: HabitEntity) : SearchResultItem()
}
