package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.ai.AiService
import com.example.data.ai.OcrService
import com.example.data.repository.AppRepository
import com.example.data.sync.CloudSyncRepository
import com.example.data.sync.EraseCloudDataWorker
import com.example.data.sync.ImageUploadWorker
import com.example.ui.theme.ThemeMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.example.widget.RecentNotesWidgetProvider
import com.example.widget.TodaysTasksWidgetProvider
import com.google.firebase.auth.userProfileChangeRequest
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val DAILY_AI_SUMMARY_LIMIT = 5
    }

    private val repository: AppRepository
    private val settingsRepository = SettingsRepository(application)
    private val ocrService = OcrService()
    private val aiService = AiService()

    // Settings state flows
    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )
    val notificationsEnabled: StateFlow<Boolean> = settingsRepository.notificationsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    val onDeviceAiEnabled: StateFlow<Boolean> = settingsRepository.onDeviceAiEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    val cloudAiEnabled: StateFlow<Boolean> = settingsRepository.cloudAiEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    val analyticsEnabled: StateFlow<Boolean> = settingsRepository.analyticsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    val consentGiven: StateFlow<Boolean> = settingsRepository.consentGiven.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    val cloudSyncEnabled: StateFlow<Boolean> = settingsRepository.cloudSyncEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    // AI summary daily quota
    val aiSummariesUsedToday: StateFlow<Int> = settingsRepository.aiSummariesUsedToday.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    val billingRepository = BillingRepository(application)
    val isPremium: StateFlow<Boolean> = billingRepository.isPremium

    // Focus timer daily session count
    val focusSessionsToday: StateFlow<Int> = settingsRepository.focusSessionsToday.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    private val _quotaExceeded = MutableStateFlow(false)
    val quotaExceeded: StateFlow<Boolean> = _quotaExceeded.asStateFlow()

    fun clearQuotaExceeded() {
        _quotaExceeded.value = false
    }

    // Database state flows
    val tasks: StateFlow<List<TaskEntity>>
    val notes: StateFlow<List<NoteEntity>>
    val captures: StateFlow<List<CaptureEntity>>
    val events: StateFlow<List<EventEntity>>
    val habits: StateFlow<List<HabitEntity>>
    /** Logs from the last 30 days, used in Habit cards for 7-day strips and stats. */
    val recentHabitLogs: StateFlow<List<HabitLogEntity>>
    /** How many active habits have been completed today. Used in Home "Habits Today" widget. */
    val habitsCompletedTodayCount: StateFlow<Int>
    /** Total number of active habits. */
    val totalActiveHabitsCount: StateFlow<Int>

    // One-shot draft carried from "Create Task" quick actions (e.g. AI Summary) into CreateTaskScreen.
    private val _taskDraftTitle = MutableStateFlow<String?>(null)
    val taskDraftTitle: StateFlow<String?> = _taskDraftTitle.asStateFlow()

    fun prefillTaskDraft(title: String) {
        _taskDraftTitle.value = title
    }

    fun consumeTaskDraft() {
        _taskDraftTitle.value = null
    }

    // User profile state
    private val _userProfileName = MutableStateFlow("Alex")
    val userProfileName: StateFlow<String> = _userProfileName.asStateFlow()

    private val _userProfileEmail = MutableStateFlow("you@example.com")
    val userProfileEmail: StateFlow<String> = _userProfileEmail.asStateFlow()

    private val _userProfilePhotoUrl = MutableStateFlow<String?>(null)
    val userProfilePhotoUrl: StateFlow<String?> = _userProfilePhotoUrl.asStateFlow()

    // Authentication (Firebase Auth — email/password + Google Sign-In)
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val cloudSync = CloudSyncRepository(appDao, application)
    private val backupRepository = BackupRepository(appDao)

    val isLoggedIn: Boolean get() = firebaseAuth.currentUser != null

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    fun clearAuthError() {
        _authError.value = null
    }

    fun signInWithEmail(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                syncProfileFromFirebaseUser()
                startSyncIfEnabled()
                onResult(true)
            } catch (e: Exception) {
                _authError.value = e.toAuthMessage()
                onResult(false)
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun registerWithEmail(name: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                firebaseAuth.currentUser?.updateProfile(
                    userProfileChangeRequest { displayName = name }
                )?.await()
                syncProfileFromFirebaseUser(fallbackName = name)
                startSyncIfEnabled()
                onResult(true)
            } catch (e: Exception) {
                _authError.value = e.toAuthMessage()
                onResult(false)
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun signInWithGoogleIdToken(idToken: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(credential).await()
                syncProfileFromFirebaseUser()
                startSyncIfEnabled()
                onResult(true)
            } catch (e: Exception) {
                _authError.value = e.toAuthMessage()
                onResult(false)
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun sendPasswordReset(email: String, onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            try {
                firebaseAuth.sendPasswordResetEmail(email).await()
                onResult(true, "Password reset email sent to $email.")
            } catch (e: Exception) {
                onResult(false, e.toAuthMessage())
            }
        }
    }

    fun signOut() {
        cloudSync.stopSync()
        firebaseAuth.signOut()
        _userProfileName.value = "Alex"
        _userProfileEmail.value = "you@example.com"
        _userProfilePhotoUrl.value = null
    }

    private fun syncProfileFromFirebaseUser(fallbackName: String? = null) {
        val user = firebaseAuth.currentUser ?: return
        val name = user.displayName?.takeIf { it.isNotBlank() }
            ?: fallbackName
            ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "User"
        _userProfileName.value = name
        _userProfileEmail.value = user.email ?: ""
        _userProfilePhotoUrl.value = user.photoUrl?.toString()
    }

    /** Starts the Firestore listeners for the current signed-in user, unless the user has turned cloud sync off. */
    private fun startSyncIfEnabled() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        if (cloudSyncEnabled.value) cloudSync.startSyncForUser(uid, viewModelScope)
    }

    fun setCloudSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setCloudSyncEnabled(enabled) }
        if (enabled) startSyncIfEnabled() else cloudSync.stopSync()
    }

    private fun Exception.toAuthMessage(): String = when (this) {
        is FirebaseAuthWeakPasswordException -> "Password is too weak — use at least 6 characters."
        is FirebaseAuthUserCollisionException -> "An account with this email already exists."
        is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
        is FirebaseAuthInvalidUserException -> "No account found for this email."
        else -> localizedMessage ?: "Something went wrong. Please try again."
    }

    // Screen states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All") // "All", "Tasks", "Notes", "Screenshots"
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    // Simulation / Capturing States
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisSuccess = MutableStateFlow<String?>(null)
    val analysisSuccess: StateFlow<String?> = _analysisSuccess.asStateFlow()

    init {
        repository = AppRepository(appDao, cloudSync) { cloudSyncEnabled.value }

        tasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        notes = repository.allNotes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        captures = repository.allCaptures.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        events = repository.allEvents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        habits = repository.allHabits.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        val thirtyDaysAgo = java.time.LocalDate.now().minusDays(30).toEpochDay()
        recentHabitLogs = repository.getHabitLogsFromFlow(thirtyDaysAgo).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        val todayEpochDay = java.time.LocalDate.now().toEpochDay()
        habitsCompletedTodayCount = combine(habits, recentHabitLogs) { habitList, logs ->
            val todayLoggedHabitIds = logs.filter { it.epochDay == todayEpochDay }.map { it.habitId }.toSet()
            habitList.count { it.isActive && todayLoggedHabitIds.contains(it.id) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

        totalActiveHabitsCount = habits.map { it.count { h -> h.isActive } }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

        DailyBriefingScheduler.schedule(getApplication())

        // If a Firebase session already exists (app relaunch), reflect it immediately.
        syncProfileFromFirebaseUser()
        viewModelScope.launch {
            // Read straight from DataStore (not the cloudSyncEnabled StateFlow, which hasn't
            // started collecting yet at this point) so a disabled preference is honored from cold start.
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null && settingsRepository.cloudSyncEnabled.first()) {
                cloudSync.startSyncForUser(uid, viewModelScope)
            }
        }

        // Firestore-confirmed profile snapshots (e.g. a photo/name change made on another device).
        viewModelScope.launch {
            cloudSync.remoteProfile.filterNotNull().collect { snap ->
                if (snap.displayName.isNotBlank()) _userProfileName.value = snap.displayName
                if (snap.email.isNotBlank()) _userProfileEmail.value = snap.email
                _userProfilePhotoUrl.value = snap.photoUrl
            }
        }
    }

    fun updateProfile(name: String, email: String) {
        _userProfileName.value = name
        _userProfileEmail.value = email
    }

    // Task operations
    fun addTask(title: String, priority: String, dueDate: String, dueTime: String, notesContent: String, subtaskTitles: List<String>) {
        viewModelScope.launch {
            val taskId = repository.insertTask(
                TaskEntity(
                    title = title,
                    isCompleted = false,
                    priority = priority,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    notes = notesContent
                )
            ).toInt()

            subtaskTitles.forEach { subTitle ->
                if (subTitle.isNotBlank()) {
                    repository.insertSubTask(
                        SubTaskEntity(
                            taskId = taskId,
                            title = subTitle,
                            isCompleted = false
                        )
                    )
                }
            }
            TodaysTasksWidgetProvider.requestUpdate(getApplication<Application>())
        }
    }

    /** Marking a task done is one-way — completed tasks can no longer be un-checked. */
    fun toggleTaskCompletion(task: TaskEntity) {
        if (task.isCompleted) return
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = true))
            TodaysTasksWidgetProvider.requestUpdate(getApplication<Application>())
        }
    }

    fun updateTaskNotes(taskId: Int, newNotes: String) {
        viewModelScope.launch {
            repository.getTaskById(taskId)?.let { task ->
                repository.updateTask(task.copy(notes = newNotes))
            }
        }
    }

    fun updateTaskPriority(task: TaskEntity, priority: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(priority = priority))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteSubTasksForTask(task.id)
            repository.deleteTask(task)
        }
    }

    // SubTask operations
    fun getSubTasksForTaskFlow(taskId: Int): Flow<List<SubTaskEntity>> {
        return repository.getSubTasksForTaskFlow(taskId)
    }

    /** Same one-way rule as [toggleTaskCompletion]: no un-checking a completed subtask. */
    fun toggleSubTaskCompletion(subTask: SubTaskEntity) {
        if (subTask.isCompleted) return
        viewModelScope.launch {
            repository.updateSubTask(subTask.copy(isCompleted = true))
        }
    }

    fun addSubTask(taskId: Int, title: String) {
        viewModelScope.launch {
            repository.insertSubTask(
                SubTaskEntity(taskId = taskId, title = title, isCompleted = false)
            )
        }
    }

    // Note operations
    fun addNote(title: String, content: String, date: String = "Oct 24", referenceImageUrl: String? = null) {
        viewModelScope.launch {
            repository.insertNote(
                NoteEntity(
                    title = title,
                    content = content,
                    date = date,
                    referenceImageUrls = referenceImageUrl?.let { listOf(it).joinToString(",") } ?: ""
                )
            )
        }
    }

    /** Creates a new note, or replaces an existing one in place when [id] is non-null. */
    fun saveNote(
        id: Int?,
        title: String,
        content: String,
        date: String = "Today",
        colorTag: Int = 0,
        referenceImageUrls: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        isBold: Boolean = false,
        isItalic: Boolean = false,
        fontScale: Float = 1.0f,
        textColorArgb: Int = 0,
        aiSummary: String = ""
    ) {
        viewModelScope.launch {
            // Preserve the existing syncId on edit — regenerating it here would orphan the old
            // Firestore doc and create a duplicate under a new id every time a note is saved.
            val existing = id?.let { repository.getNoteById(it) }
            val noteSyncId = existing?.syncId ?: UUID.randomUUID().toString()
            val context = getApplication<Application>().applicationContext
            val uid = firebaseAuth.currentUser?.uid
            val finalUrls = referenceImageUrls.mapIndexed { index, url ->
                if (!url.startsWith("content://")) {
                    url // already a locally-cached path or a synced https URL — nothing new to upload
                } else {
                    try {
                        val localFile = copyUriToPrivateFile(context, Uri.parse(url), "note_${noteSyncId}_$index.jpg")
                        if (cloudSyncEnabled.value && uid != null) {
                            enqueueImageUpload("note", noteSyncId, localFile, "users/$uid/notes/$noteSyncId/$index.jpg", index)
                        }
                        Uri.fromFile(localFile).toString()
                    } catch (e: Exception) {
                        url
                    }
                }
            }
            repository.insertNote(
                NoteEntity(
                    id = id ?: 0,
                    title = title,
                    content = content,
                    date = date,
                    colorTag = colorTag,
                    referenceImageUrls = finalUrls.joinToString(","),
                    tags = tags.joinToString(","),
                    isBold = isBold,
                    isItalic = isItalic,
                    fontScale = fontScale,
                    textColorArgb = textColorArgb,
                    aiSummary = aiSummary,
                    syncId = noteSyncId
                )
            )
            RecentNotesWidgetProvider.requestUpdate(getApplication<Application>())
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
            RecentNotesWidgetProvider.requestUpdate(getApplication<Application>())
        }
    }

    /**
     * Generates a short AI summary of a note's content, respecting the same daily quota as
     * capture analysis. Falls back to a local heuristic (first sentences) when cloud AI is
     * off or the quota is exhausted, so the button never appears to just fail silently.
     */
    suspend fun summarizeNoteContent(content: String): String {
        if (content.isBlank()) return ""
        val quotaAvailable = isPremium.value || aiSummariesUsedToday.value < DAILY_AI_SUMMARY_LIMIT
        return if (cloudAiEnabled.value && quotaAvailable) {
            aiService.summarizeNote(content).also { settingsRepository.recordAiSummaryUsed() }
        } else {
            if (cloudAiEnabled.value && !quotaAvailable) _quotaExceeded.value = true
            localNoteSummary(content)
        }
    }

    private fun localNoteSummary(content: String): String {
        val sentences = content.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        return sentences.take(2).joinToString(" ").ifBlank { content.take(140) }
    }

    val allNoteTags: StateFlow<List<String>> = notes
        .map { list -> list.flatMap { it.tags.split(",") }.map { it.trim() }.filter { it.isNotEmpty() }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCaptureTags: StateFlow<List<String>> = captures
        .map { list -> list.flatMap { it.tags.split(",") }.map { it.trim() }.filter { it.isNotEmpty() }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calendar / Event operations
    fun addEvent(title: String, timeRange: String, location: String, day: Int, monthName: String, color: Int = 0) {
        viewModelScope.launch {
            repository.insertEvent(
                EventEntity(
                    title = title,
                    timeRange = timeRange,
                    location = location,
                    day = day,
                    monthName = monthName,
                    color = color
                )
            )
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    /**
     * Imports Google Calendar events into the app's own Planner data, upserting by
     * [com.example.data.calendar.GoogleCalendarEvent.id] so re-imports update in place
     * instead of duplicating. Returns the number of events imported.
     */
    suspend fun importGoogleCalendarEvents(events: List<com.example.data.calendar.GoogleCalendarEvent>): Int {
        var count = 0
        events.forEach { gEvent ->
            val (day, monthName, year, timeRange) = parseGoogleEventSchedule(gEvent) ?: return@forEach
            val existing = repository.getEventByGoogleEventId(gEvent.id)
            val entity = EventEntity(
                id = existing?.id ?: 0,
                title = gEvent.summary,
                timeRange = timeRange,
                location = gEvent.location,
                day = day,
                monthName = monthName,
                year = year,
                color = existing?.color ?: 0,
                googleEventId = gEvent.id,
                syncId = existing?.syncId ?: java.util.UUID.randomUUID().toString()
            )
            if (existing != null) repository.updateEvent(entity) else repository.insertEvent(entity)
            count++
        }
        return count
    }

    private data class ParsedSchedule(val day: Int, val monthName: String, val year: Int, val timeRange: String)

    private fun parseGoogleEventSchedule(gEvent: com.example.data.calendar.GoogleCalendarEvent): ParsedSchedule? {
        return try {
            if (gEvent.startDateTime != null) {
                val start = java.time.OffsetDateTime.parse(gEvent.startDateTime)
                val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a")
                val timeRange = if (gEvent.endDateTime != null) {
                    val end = java.time.OffsetDateTime.parse(gEvent.endDateTime)
                    "${start.format(timeFormatter)} - ${end.format(timeFormatter)}"
                } else {
                    start.format(timeFormatter)
                }
                ParsedSchedule(
                    day = start.dayOfMonth,
                    monthName = start.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()),
                    year = start.year,
                    timeRange = timeRange
                )
            } else if (gEvent.startDate != null) {
                val start = java.time.LocalDate.parse(gEvent.startDate)
                ParsedSchedule(
                    day = start.dayOfMonth,
                    monthName = start.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()),
                    year = start.year,
                    timeRange = "All day"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Habit operations (recurring reminders + activity tracking)
    fun addHabit(
        title: String,
        intervalHours: Int,
        habitType: String = "Generic",
        metricUnit: String = "",
        dailyGoalValue: Float = 0f,
        weeklyTargetDays: Int = 7
    ) {
        viewModelScope.launch {
            val id = repository.insertHabit(
                HabitEntity(
                    title = title,
                    intervalHours = intervalHours,
                    isActive = true,
                    createdAt = System.currentTimeMillis(),
                    habitType = habitType,
                    metricUnit = metricUnit,
                    dailyGoalValue = dailyGoalValue,
                    weeklyTargetDays = weeklyTargetDays
                )
            ).toInt()
            HabitScheduler.schedule(getApplication<Application>(), HabitEntity(id = id, title = title, intervalHours = intervalHours, isActive = true))
        }
    }

    fun toggleHabitActive(habit: HabitEntity) {
        viewModelScope.launch {
            val updated = habit.copy(isActive = !habit.isActive)
            repository.updateHabit(updated)
            if (updated.isActive) HabitScheduler.schedule(getApplication<Application>(), updated) else HabitScheduler.cancel(getApplication<Application>(), updated.id)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            HabitScheduler.cancel(getApplication<Application>(), habit.id)
            repository.deleteHabit(habit)
        }
    }

    fun completeHabitToday(habit: HabitEntity, metricValue: Float = 0f, note: String = "") {
        val today = java.time.LocalDate.now().toEpochDay()
        viewModelScope.launch {
            // Insert a log entry for this completion
            repository.insertHabitLog(
                HabitLogEntity(
                    habitId = habit.id,
                    epochDay = today,
                    metricValue = metricValue,
                    note = note
                )
            )
            // Only update streak if this is the first completion today
            if (habit.lastCompletedEpochDay != today) {
                val newStreak = if (habit.lastCompletedEpochDay == today - 1) habit.currentStreak + 1 else 1
                repository.updateHabit(
                    habit.copy(
                        currentStreak = newStreak,
                        longestStreak = maxOf(newStreak, habit.longestStreak),
                        lastCompletedEpochDay = today
                    )
                )
            }
        }
    }

    fun getLogsForHabitFlow(habitId: Int) = repository.getLogsForHabitFlow(habitId)

    // ---- Live habit GPS tracking session (survives navigating away from TrackHabitScreen) ----

    private var locationTracker: LocationTracker? = null
    private var trackingTimerJob: kotlinx.coroutines.Job? = null

    val trackingHabitId = MutableStateFlow<Int?>(null)
    val trackingIsRunning = MutableStateFlow(false)
    val trackingSeconds = MutableStateFlow(0)
    val trackingDistanceMeters = MutableStateFlow(0f)
    val trackingPath = MutableStateFlow<List<com.google.android.gms.maps.model.LatLng>>(emptyList())

    /** Returns the already-running tracker for this habit, or starts a fresh session. */
    fun trackerForHabit(habitId: Int): LocationTracker {
        if (trackingHabitId.value != habitId) {
            locationTracker?.stop()
            trackingHabitId.value = habitId
            trackingSeconds.value = 0
            trackingDistanceMeters.value = 0f
            trackingPath.value = emptyList()
        }
        return locationTracker ?: LocationTracker(getApplication()).also { locationTracker = it }
    }

    fun startTrackingUpdates() {
        trackingIsRunning.value = true
        locationTracker?.start { distance, path ->
            trackingDistanceMeters.value = distance
            trackingPath.value = path
        }
        trackingTimerJob?.cancel()
        trackingTimerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                trackingSeconds.value += 1
            }
        }
    }

    fun pauseTrackingUpdates() {
        trackingIsRunning.value = false
        locationTracker?.pause()
        trackingTimerJob?.cancel()
    }

    /** Ends the session for good — call after Save or discarding the tracked habit. */
    fun endTrackingSession() {
        trackingIsRunning.value = false
        trackingTimerJob?.cancel()
        locationTracker?.stop()
        locationTracker = null
        trackingHabitId.value = null
        trackingSeconds.value = 0
        trackingDistanceMeters.value = 0f
        trackingPath.value = emptyList()
    }

    /** Event operations — toggle done state */
    fun toggleEventDone(event: EventEntity) {
        viewModelScope.launch { repository.toggleEventDone(event) }
    }

    // ---- Cloud image sync helpers ----

    private fun copyUriToPrivateFile(context: Context, uri: Uri, fileName: String): File {
        val file = File(context.filesDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: throw java.io.IOException("Could not open $uri")
        return file
    }

    private fun enqueueImageUpload(entityType: String, syncId: String, localFile: File, remoteStoragePath: String, imageIndex: Int) {
        val request = OneTimeWorkRequestBuilder<ImageUploadWorker>()
            .setInputData(
                Data.Builder()
                    .putString(ImageUploadWorker.KEY_ENTITY_TYPE, entityType)
                    .putString(ImageUploadWorker.KEY_ENTITY_SYNC_ID, syncId)
                    .putString(ImageUploadWorker.KEY_LOCAL_FILE_PATH, localFile.absolutePath)
                    .putString(ImageUploadWorker.KEY_REMOTE_STORAGE_PATH, remoteStoragePath)
                    .putInt(ImageUploadWorker.KEY_IMAGE_INDEX, imageIndex)
                    .build()
            )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        val workName = "image_upload_${entityType}_$syncId" + if (imageIndex >= 0) "_$imageIndex" else ""
        WorkManager.getInstance(getApplication<Application>()).enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, request)
    }

    /** Picks up a new profile photo: caches it locally for an instant preview, then uploads it if cloud sync is on. */
    fun updateProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            val uid = firebaseAuth.currentUser?.uid ?: return@launch
            val context = getApplication<Application>().applicationContext
            val localFile = try {
                copyUriToPrivateFile(context, uri, "profile_$uid.jpg")
            } catch (e: Exception) {
                return@launch
            }
            _userProfilePhotoUrl.value = Uri.fromFile(localFile).toString()
            if (cloudSyncEnabled.value) {
                enqueueImageUpload("profile", uid, localFile, "users/$uid/profile.jpg", -1)
            }
        }
    }

    /**
     * Local "Productivity Score": completed tasks weigh more than scheduled events
     * since finishing something is a stronger productivity signal than just planning it.
     * No per-day timestamps exist yet, so this is a lifetime total, not a weekly one.
     */
    val productivityScore: StateFlow<Int> = combine(tasks, events) { taskList, eventList ->
        taskList.count { it.isCompleted } * 10 + eventList.size * 5
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Search Filtering
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    // Screenshot capture and AI intelligence processing
    fun processCapture(presetIndex: Int) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisSuccess.value = null
            kotlinx.coroutines.delay(2000) // Simulate advanced AI processing duration

            when (presetIndex) {
                0 -> {
                    // Competitor Dashboard preset
                    val captureId = repository.allCaptures.first().size + 1
                    val title = "Analytics Dashboard UI v$captureId"
                    repository.insertCapture(
                        CaptureEntity(
                            title = title,
                            timestamp = "Just now",
                            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDWo0nXbI4jGDDhYEx368xutDv5ryO7Dz845tgSXnU5txbjfo_KPY-hd4sb-Y3106K3hiw_aptsEm1LonW0_46p4FbOY3W3bpYic4u9Ow7pkJmqNX6L1NzqQm9yZ8jyTxG9G2pqPzyWw9nOjd4_x1PXfBYL1oQ1fRbWepT-NIOclRWalIUim5UynoFNa0m_vWmtiVnO5bFf2UtC7_pRNULBhvEv-bN41UnkxY57y7steoQWhO705_rXq2-JBEAGqd3X7KH40unzj_sq",
                            category = "UI Ref",
                            extractedText = "Competitor Dashboard Analytics\nConversion Rate: 2.1%\nTotal Impressions: 2.4M\nSubtask checklist parsed.",
                            status = "Needs Review",
                            summary = "This screenshot captures a competitor's analytics dashboard. It highlights a 2.1% conversion rate which we need to compare against our internal benchmarks."
                        )
                    )
                    // Auto extract a related task
                    addTask(
                        title = "Verify Conversion Funnel Analytics UI",
                        priority = "Medium",
                        dueDate = "Oct 26",
                        dueTime = "4:00 PM",
                        notesContent = "Analyzed from $title. Focus on checking conversion rates against competitor's 2.1% benchmark.",
                        subtaskTitles = listOf("Check desktop UI styling", "Verify mobile gesture safe areas")
                    )
                    _analysisSuccess.value = "Extracted task: 'Verify Conversion Funnel Analytics UI'!"
                }
                1 -> {
                    // Software receipt preset
                    val captureId = repository.allCaptures.first().size + 1
                    val title = "SaaS Invoice #$captureId"
                    repository.insertCapture(
                        CaptureEntity(
                            title = title,
                            timestamp = "Just now",
                            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDio1a8BCNTqsmXX9kFnMEYKllm2IdqM9Mj4E_AA-3BUguBB7JajMZ4gpV_rBvVt8f4ejfp5TRC-mEWT_iemFRyHTSMhbj4B0v-1isXR8DZfGj2LFvLWvDXxH8bFmgFxLVb5GNf6fA8DmPxdFNoxoWProWXkB5N-VnLdhHPbNiRDbK7i658D8Dm3CFGCR2pde6C4sU-s8O5s0ufV7ZMIAi_1KuOaocTw2LNn8nRilz52eQn9Xx4sKS4_3DZz-qq7CiJvI45waVzvB8i",
                            category = "Expense",
                            extractedText = "RECEIPT\nOct 24, 2023\nTotal: $189.00\nSoftware subscriptions renew auto.",
                            status = "Processed",
                            summary = "An invoice for software subscriptions totaling $189.00 on Oct 24. A quick note has been automatically created for expense reporting."
                        )
                    )
                    // Auto extract a quick note about invoice
                    addNote(
                        title = "SaaS Expense Auto-Receipt",
                        content = "Processed software subscription invoice $title from screenshot intelligence. Total cost parsed: $189.00. Ready to submit to accounting.",
                        date = "Oct 24"
                    )
                    _analysisSuccess.value = "Extracted quick note: 'SaaS Expense Auto-Receipt'!"
                }
                2 -> {
                    // Auth service code snippet preset
                    val captureId = repository.allCaptures.first().size + 1
                    val title = "Security Logic v$captureId"
                    repository.insertCapture(
                        CaptureEntity(
                            title = title,
                            timestamp = "Just now",
                            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAv3Ikcvxs9LpXSht19aYj11keLgxDy3jV3YkneAlX43NMXAT2sQZnt9fkvB4z-1XQxb_zCW4ZQU_cAf5IWgByWjcL_SqTD3CJp47Vmq3wpjNomsNjy0s9ziMIhLL_rtsPWFcGo3wSbromsAl5z7Dr5r1b1f2kamsIE4TGN6YJkDG-4jsi2AG19IUZ9AJvNhflJihDEF68ZgdtU9LiMTetSsvhGzT4llDGYobhkyVR4fQxJlPs85aMI9SPclB3M0RTPx28Q6hvaCWpz",
                            category = "Snippet",
                            extractedText = "class AuthenticationHelper {\n  fun authenticateToken() {\n    // Extracted secure token validation rules\n  }\n}",
                            status = "Failed OCR",
                            summary = "Code snippet related to token authentication. OCR confidence is low due to poor contrast."
                        )
                    )
                    // Auto extract an event
                    addEvent(
                        title = "Security Code Audit: Token Auth",
                        timeRange = "5:00 - 6:00 PM",
                        location = "Meeting Room B",
                        day = 24,
                        monthName = "October",
                        color = 1
                    )
                    _analysisSuccess.value = "Extracted calendar event: 'Security Code Audit: Token Auth'!"
                }
            }
            _isAnalyzing.value = false
        }
    }

    /** Copies [imageUri] into app-private storage and enqueues its upload, then inserts the capture row. */
    private suspend fun insertCaptureWithImage(entity: CaptureEntity, imageUri: Uri): Long {
        val id = repository.insertCapture(entity)
        if (cloudSyncEnabled.value) {
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                try {
                    val context = getApplication<Application>().applicationContext
                    val localFile = copyUriToPrivateFile(context, imageUri, "capture_${entity.syncId}.jpg")
                    enqueueImageUpload("capture", entity.syncId, localFile, "users/$uid/captures/${entity.syncId}.jpg", -1)
                } catch (e: Exception) {
                    // Non-fatal — the capture is already saved locally with its original image reference.
                }
            }
        }
        return id
    }

    /**
     * Real capture pipeline: on-device OCR -> text-only cloud AI enrichment ->
     * persist an editable CaptureEntity. The original image URI and raw OCR text
     * are always preserved. Falls back gracefully if OCR or AI fail.
     */
    fun importAndProcess(imageUri: Uri, sourceType: String = "imported image") {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisSuccess.value = null

            val context = getApplication<Application>().applicationContext

            // 0) Respect the user's on-device processing preference (AI & Privacy settings).
            if (!onDeviceAiEnabled.value) {
                insertCaptureWithImage(
                    CaptureEntity(
                        title = "Imported image",
                        timestamp = "Just now",
                        imageUrl = imageUri.toString(),
                        category = "Other",
                        status = "Needs Review",
                        sourceType = sourceType,
                        confidence = 0f
                    ),
                    imageUri
                )
                _analysisSuccess.value = "On-device processing is off — saved to inbox for manual review."
                _isAnalyzing.value = false
                return@launch
            }

            // 1) OCR (on-device, free, private)
            val ocr = try {
                ocrService.extract(context, imageUri)
            } catch (e: Exception) {
                null
            }

            if (ocr == null) {
                // Could not even open/scan the image — record a failed capture.
                insertCaptureWithImage(
                    CaptureEntity(
                        title = "Imported image",
                        timestamp = "Just now",
                        imageUrl = imageUri.toString(),
                        category = "Other",
                        status = "Failed OCR",
                        sourceType = sourceType,
                        confidence = 0f
                    ),
                    imageUri
                )
                _analysisSuccess.value = "Couldn't read that image — saved for manual review."
                _isAnalyzing.value = false
                return@launch
            }

            // 2) AI enrichment (text-only; never sends the image). Only calls the
            // cloud model if the user has cloud AI enabled and the daily quota
            // isn't exhausted; otherwise stays fully on-device with a local
            // best-effort analysis.
            val quotaAvailable = isPremium.value || aiSummariesUsedToday.value < DAILY_AI_SUMMARY_LIMIT
            val analysis = if (cloudAiEnabled.value && quotaAvailable) {
                aiService.analyze(ocr.text).also { settingsRepository.recordAiSummaryUsed() }
            } else {
                if (cloudAiEnabled.value && !quotaAvailable) _quotaExceeded.value = true
                aiService.localOnlyAnalysis(ocr.text)
            }

            // Combine OCR + AI confidence; low either way => Needs Review.
            val combinedConfidence = minOf(ocr.confidence, if (analysis.confidence > 0f) analysis.confidence else ocr.confidence)
            val status = when {
                ocr.text.isBlank() -> "Failed OCR"
                analysis.needsReview || combinedConfidence < 0.5f -> "Needs Review"
                else -> "Processed"
            }

            // 3) Persist an editable record.
            insertCaptureWithImage(
                CaptureEntity(
                    title = analysis.title,
                    timestamp = "Just now",
                    imageUrl = imageUri.toString(),
                    category = analysis.category,
                    extractedText = ocr.text,
                    status = status,
                    summary = analysis.summary,
                    aiTitle = analysis.title,
                    detectedLanguage = analysis.language,
                    tags = analysis.tags.joinToString(", "),
                    entities = analysis.entitiesJson,
                    confidence = combinedConfidence,
                    sourceType = sourceType
                ),
                imageUri
            )

            _analysisSuccess.value = when (status) {
                "Failed OCR" -> "No readable text found — saved to inbox."
                "Needs Review" -> "Imported '${analysis.title}' — needs a quick review."
                else -> "Imported & organized '${analysis.title}'."
            }
            _isAnalyzing.value = false
        }
    }

    /** Persist user edits to any capture field, keeping AI outputs editable. */
    fun updateCapture(
        capture: CaptureEntity,
        title: String = capture.title,
        summary: String = capture.summary,
        category: String = capture.category,
        extractedText: String = capture.extractedText,
        tags: String = capture.tags,
        status: String = capture.status,
        isImportant: Boolean = capture.isImportant
    ) {
        viewModelScope.launch {
            repository.updateCapture(
                capture.copy(
                    title = title,
                    summary = summary,
                    category = category,
                    extractedText = extractedText,
                    tags = tags,
                    status = status,
                    isImportant = isImportant
                )
            )
        }
    }

    /** Re-run the AI enrichment on already-extracted text (e.g. after OCR edits). */
    fun reanalyzeCapture(captureId: Int) {
        viewModelScope.launch {
            val capture = repository.getCaptureById(captureId) ?: return@launch
            _isAnalyzing.value = true
            val quotaAvailable = isPremium.value || aiSummariesUsedToday.value < DAILY_AI_SUMMARY_LIMIT
            val analysis = if (cloudAiEnabled.value && quotaAvailable) {
                aiService.analyze(capture.extractedText).also { settingsRepository.recordAiSummaryUsed() }
            } else {
                if (cloudAiEnabled.value && !quotaAvailable) _quotaExceeded.value = true
                aiService.localOnlyAnalysis(capture.extractedText)
            }
            repository.updateCapture(
                capture.copy(
                    title = capture.title.ifBlank { analysis.title },
                    summary = analysis.summary,
                    category = analysis.category,
                    tags = analysis.tags.joinToString(", "),
                    entities = analysis.entitiesJson,
                    detectedLanguage = analysis.language,
                    confidence = analysis.confidence,
                    status = if (analysis.needsReview) "Needs Review" else "Processed"
                )
            )
            _isAnalyzing.value = false
            _analysisSuccess.value = "Re-analyzed with AI."
        }
    }

    fun deleteCapture(capture: CaptureEntity) {
        viewModelScope.launch { repository.deleteCapture(capture) }
    }

    fun clearAnalysisSuccess() {
        _analysisSuccess.value = null
    }

    // Settings operations
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNotificationsEnabled(enabled) }
    }

    fun setOnDeviceAiEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setOnDeviceAiEnabled(enabled) }
    }

    fun setCloudAiEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setCloudAiEnabled(enabled) }
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAnalyticsEnabled(enabled) }
    }

    fun setConsentGiven(given: Boolean) {
        viewModelScope.launch { settingsRepository.setConsentGiven(given) }
    }

    fun recordFocusSessionCompleted() {
        viewModelScope.launch { settingsRepository.recordFocusSessionCompleted() }
    }

    suspend fun exportBackup(): String = backupRepository.export()

    suspend fun importBackup(json: String) = backupRepository.import(json)

    /**
     * Erases all locally-stored personal data (captures, notes, tasks) to satisfy
     * the DPDP Act's user-facing erasure requirement. Settings/consent state is
     * preserved so re-consent isn't forced immediately after.
     */
    fun eraseAllData() {
        viewModelScope.launch {
            val uid = firebaseAuth.currentUser?.uid
            cloudSync.stopSync()
            repository.eraseAllUserData()
            if (uid != null) {
                val request = OneTimeWorkRequestBuilder<EraseCloudDataWorker>()
                    .setInputData(Data.Builder().putString(EraseCloudDataWorker.KEY_UID, uid).build())
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
                WorkManager.getInstance(getApplication<Application>())
                    .enqueueUniqueWork("erase_cloud_data_$uid", ExistingWorkPolicy.REPLACE, request)
            }
            // Sync is intentionally left stopped — restarting immediately could reconcile-pull
            // not-yet-deleted remote docs back down before EraseCloudDataWorker finishes.
        }
    }
}
