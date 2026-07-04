package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.ai.AiService
import com.example.data.ai.OcrService
import com.example.data.repository.AppRepository
import com.example.ui.theme.ThemeMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AppViewModel(application: Application) : AndroidViewModel(application) {
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

    // Database state flows
    val tasks: StateFlow<List<TaskEntity>>
    val notes: StateFlow<List<NoteEntity>>
    val captures: StateFlow<List<CaptureEntity>>
    val events: StateFlow<List<EventEntity>>

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

    // Authentication (Firebase Auth — email/password + Google Sign-In)
    private val firebaseAuth = FirebaseAuth.getInstance()

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
        firebaseAuth.signOut()
        _userProfileName.value = "Alex"
        _userProfileEmail.value = "you@example.com"
    }

    private fun syncProfileFromFirebaseUser(fallbackName: String? = null) {
        val user = firebaseAuth.currentUser ?: return
        val name = user.displayName?.takeIf { it.isNotBlank() }
            ?: fallbackName
            ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "User"
        _userProfileName.value = name
        _userProfileEmail.value = user.email ?: ""
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
        val appDatabase = AppDatabase.getDatabase(application)
        repository = AppRepository(appDatabase.appDao())

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

        // Initialize database if empty
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }

        // If a Firebase session already exists (app relaunch), reflect it immediately.
        syncProfileFromFirebaseUser()
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
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun updateTaskNotes(taskId: Int, newNotes: String) {
        viewModelScope.launch {
            repository.getTaskById(taskId)?.let { task ->
                repository.updateTask(task.copy(notes = newNotes))
            }
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

    fun toggleSubTaskCompletion(subTask: SubTaskEntity) {
        viewModelScope.launch {
            repository.updateSubTask(subTask.copy(isCompleted = !subTask.isCompleted))
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
    fun addNote(title: String, content: String, date: String = "Oct 24") {
        viewModelScope.launch {
            repository.insertNote(NoteEntity(title = title, content = content, date = date))
        }
    }

    /** Creates a new note, or replaces an existing one in place when [id] is non-null. */
    fun saveNote(id: Int?, title: String, content: String, date: String = "Today") {
        viewModelScope.launch {
            repository.insertNote(NoteEntity(id = id ?: 0, title = title, content = content, date = date))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

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
                repository.insertCapture(
                    CaptureEntity(
                        title = "Imported image",
                        timestamp = "Just now",
                        imageUrl = imageUri.toString(),
                        category = "Other",
                        status = "Needs Review",
                        sourceType = sourceType,
                        confidence = 0f
                    )
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
                repository.insertCapture(
                    CaptureEntity(
                        title = "Imported image",
                        timestamp = "Just now",
                        imageUrl = imageUri.toString(),
                        category = "Other",
                        status = "Failed OCR",
                        sourceType = sourceType,
                        confidence = 0f
                    )
                )
                _analysisSuccess.value = "Couldn't read that image — saved for manual review."
                _isAnalyzing.value = false
                return@launch
            }

            // 2) AI enrichment (text-only; never sends the image). Only calls the
            // cloud model if the user has cloud AI enabled; otherwise stays fully
            // on-device with a local best-effort analysis.
            val analysis = if (cloudAiEnabled.value) {
                aiService.analyze(ocr.text)
            } else {
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
            repository.insertCapture(
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
                )
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
            val analysis = if (cloudAiEnabled.value) {
                aiService.analyze(capture.extractedText)
            } else {
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

    /**
     * Erases all locally-stored personal data (captures, notes, tasks) to satisfy
     * the DPDP Act's user-facing erasure requirement. Settings/consent state is
     * preserved so re-consent isn't forced immediately after.
     */
    fun eraseAllData() {
        viewModelScope.launch { repository.eraseAllUserData() }
    }
}
