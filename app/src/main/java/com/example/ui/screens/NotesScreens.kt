package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.NoteEntity
import com.example.ui.AppViewModel
import kotlinx.coroutines.launch

/** Fixed whole-note text color palette; 0 means "use the current theme's default text color". */
private val NOTE_TEXT_COLORS = listOf(
    0,
    0xFFE53935.toInt(), // red
    0xFF1E88E5.toInt(), // blue
    0xFF43A047.toInt(), // green
    0xFFFB8C00.toInt(), // orange
    0xFF8E24AA.toInt()  // purple
)

/** Fixed note color palette; index 0 means "no color" (uses default surface color). */
val NOTE_COLORS = listOf(
    Color.Transparent,
    Color(0xFFFFF59D), // yellow
    Color(0xFFA5D6A7), // green
    Color(0xFF90CAF9), // blue
    Color(0xFFF48FB1), // pink
    Color(0xFFCE93D8), // purple
    Color(0xFFFFCC80)  // orange
)

private fun noteCardColor(colorTag: Int): Color? =
    NOTE_COLORS.getOrNull(colorTag)?.takeIf { colorTag != 0 }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: AppViewModel,
    onNavigateToEditor: (Int?) -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val allTags by viewModel.allNoteTags.collectAsStateWithLifecycle()
    var selectedTag by remember { mutableStateOf("All") }

    LaunchedEffect(allTags) {
        if (selectedTag != "All" && selectedTag !in allTags) selectedTag = "All"
    }

    val filteredNotes = remember(notes, selectedTag) {
        if (selectedTag == "All") notes
        else notes.filter { note -> note.tags.split(",").map { it.trim() }.contains(selectedTag) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditor(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            if (allTags.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (listOf("All") + allTags).forEach { tag ->
                            FilterChip(
                                selected = selectedTag == tag,
                                onClick = { selectedTag = tag },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            }
            items(filteredNotes) { note ->
                val noteTags = remember(note.tags) { note.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() } }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDetail(note.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = noteCardColor(note.colorTag)?.copy(alpha = 0.35f)
                            ?: MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(note.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            note.content,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(note.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        if (noteTags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                noteTags.take(2).forEach { tag ->
                                    AssistChip(onClick = {}, label = { Text(tag, fontSize = 11.sp) })
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Int?,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val allTags by viewModel.allNoteTags.collectAsStateWithLifecycle()
    val existingNote = remember(noteId, notes) { notes.find { it.id == noteId } }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var colorTag by remember { mutableStateOf(0) }
    var referenceImageUrls by remember { mutableStateOf(listOf<String>()) }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var newTagText by remember { mutableStateOf("") }
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }
    var fontScale by remember { mutableStateOf(1.0f) }
    var textColorArgb by remember { mutableStateOf(0) }
    var aiSummary by remember { mutableStateOf("") }
    var isSummarizing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(existingNote?.id) {
        if (existingNote != null) {
            title = existingNote.title
            content = existingNote.content
            colorTag = existingNote.colorTag
            referenceImageUrls = existingNote.referenceImageUrls.split(",").filter { it.isNotBlank() }
            tags = existingNote.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            isBold = existingNote.isBold
            isItalic = existingNote.isItalic
            fontScale = existingNote.fontScale
            textColorArgb = existingNote.textColorArgb
            aiSummary = existingNote.aiSummary
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) referenceImageUrls = referenceImageUrls + uri.toString()
    }

    val performSave: () -> Unit = save@{
        if (title.isBlank() && content.isBlank()) return@save
        viewModel.saveNote(
            id = noteId,
            title = title.ifBlank { "Untitled" },
            content = content,
            date = existingNote?.date ?: "Today",
            colorTag = colorTag,
            referenceImageUrls = referenceImageUrls,
            tags = tags,
            isBold = isBold,
            isItalic = isItalic,
            fontScale = fontScale,
            textColorArgb = textColorArgb,
            aiSummary = aiSummary
        )
    }

    BackHandler {
        performSave()
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "New Note" else "Edit Note", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { performSave(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { performSave(); onBack() }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline) },
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Color swatches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NOTE_COLORS.forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (index == 0) MaterialTheme.colorScheme.surfaceVariant else color)
                            .border(
                                width = if (colorTag == index) 3.dp else 1.dp,
                                color = if (colorTag == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable { colorTag = index },
                        contentAlignment = Alignment.Center
                    ) {
                        if (index == 0) {
                            Icon(Icons.Default.Close, contentDescription = "No color", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else if (colorTag == index) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", modifier = Modifier.size(16.dp), tint = Color.Black.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            // Text formatting toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isBold,
                    onClick = { isBold = !isBold },
                    label = { Icon(Icons.Default.FormatBold, contentDescription = "Bold", modifier = Modifier.size(18.dp)) }
                )
                FilterChip(
                    selected = isItalic,
                    onClick = { isItalic = !isItalic },
                    label = { Icon(Icons.Default.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(18.dp)) }
                )
                listOf(0.85f to "S", 1.0f to "M", 1.25f to "L", 1.5f to "XL").forEach { (scale, label) ->
                    FilterChip(
                        selected = fontScale == scale,
                        onClick = { fontScale = scale },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
                NOTE_TEXT_COLORS.forEach { argb ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (argb == 0) MaterialTheme.colorScheme.onBackground else Color(argb))
                            .border(
                                width = if (textColorArgb == argb) 3.dp else 1.dp,
                                color = if (textColorArgb == argb) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable { textColorArgb = argb },
                        contentAlignment = Alignment.Center
                    ) {
                        if (argb == 0) {
                            Text("A", fontSize = 12.sp, color = MaterialTheme.colorScheme.background, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            isSummarizing = true
                            aiSummary = viewModel.summarizeNoteContent(content)
                            isSummarizing = false
                        }
                    },
                    enabled = content.isNotBlank() && !isSummarizing
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isSummarizing) "Summarizing..." else "AI Summary", fontSize = 12.sp)
                }
            }

            if (aiSummary.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            aiSummary,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss summary",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { aiSummary = "" },
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Start typing...", fontSize = 16.sp, color = MaterialTheme.colorScheme.outline) },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = (16 * fontScale).sp,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                    color = if (textColorArgb == 0) MaterialTheme.colorScheme.onBackground else Color(textColorArgb)
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // Tags
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tags", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                if (tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEach { tag ->
                            InputChip(
                                selected = false,
                                onClick = {},
                                label = { Text(tag) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove tag",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { tags = tags - tag }
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newTagText,
                        onValueChange = { newTagText = it },
                        placeholder = { Text("Add a tag", fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        val trimmed = newTagText.trim()
                        if (trimmed.isNotEmpty() && !tags.contains(trimmed)) {
                            tags = tags + trimmed
                        }
                        newTagText = ""
                    }) {
                        Text("Add")
                    }
                }
                val suggestions = remember(allTags, tags) { allTags.filter { it !in tags } }
                if (suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestions.forEach { tag ->
                            AssistChip(
                                onClick = { tags = tags + tag },
                                label = { Text(tag, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Attachments
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Attachments", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    referenceImageUrls.forEach { url ->
                        Box(modifier = Modifier.size(72.dp)) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Reference photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable { referenceImageUrls = referenceImageUrls - url },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add photo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
