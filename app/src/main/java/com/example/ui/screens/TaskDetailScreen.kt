package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Int,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val task = tasks.find { it.id == taskId }

    val subtasks by viewModel.getSubTasksForTaskFlow(taskId).collectAsStateWithLifecycle(initialValue = emptyList())

    var newSubtaskTitle by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    // Synchronize local text with database notes once loaded
    LaunchedEffect(task) {
        task?.let {
            noteText = it.notes
        }
    }

    if (task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Task not found.")
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Task Details", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.deleteTask(task)
                            onBack()
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Priority Tag Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (task.priority == "High") Color(0xFFFFDAD6) else Color(0xFFECEEF0),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (task.priority == "High") {
                            Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Color(0xFF93000A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "${task.priority} Priority",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (task.priority == "High") Color(0xFF93000A) else Color(0xFF3B494A)
                            )
                        )
                    }
                }

                Text(
                    text = "${task.dueDate} at ${task.dueTime}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                )
            }

            // Task title
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191C1E)
                )
            )

            // Dynamic checklist (Subtasks)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Subtasks Checklist",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Subtask list
                subtasks.forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleSubTaskCompletion(sub) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    if (sub.isCompleted) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .border(
                                    2.dp,
                                    if (sub.isCompleted) MaterialTheme.colorScheme.primary else Color(0xFFBAC9C9),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (sub.isCompleted) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = sub.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (sub.isCompleted) Color.Gray else Color(0xFF191C1E)
                            )
                        )
                    }
                }

                // Add Subtask input row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSubtaskTitle,
                        onValueChange = { newSubtaskTitle = it },
                        placeholder = { Text("Add checklist item...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_subtask_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newSubtaskTitle.isNotBlank()) {
                                viewModel.addSubTask(task.id, newSubtaskTitle)
                                newSubtaskTitle = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Task Notes Editor Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Task Notes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = {
                        noteText = it
                        viewModel.updateTaskNotes(task.id, it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("task_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Write notes or details here...") }
                )
            }

            // Screenshot Attachment container (Hotlinked mockup reference)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Attached Screenshot Source",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "competitor_dashboard_v2.png",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1E)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDWo0nXbI4jGDDhYEx368xutDv5ryO7Dz845tgSXnU5txbjfo_KPY-hd4sb-Y3106K3hiw_aptsEm1LonW0_46p4FbOY3W3bpYic4u9Ow7pkJmqNX6L1NzqQm9yZ8jyTxG9G2pqPzyWw9nOjd4_x1PXfBYL1oQ1fRbWepT-NIOclRWalIUim5UynoFNa0m_vWmtiVnO5bFf2UtC7_pRNULBhvEv-bN41UnkxY57y7steoQWhO705_rXq2-JBEAGqd3X7KH40unzj_sq",
                        contentDescription = "Source screenshot",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
