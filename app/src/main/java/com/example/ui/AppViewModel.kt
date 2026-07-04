package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    // Database state flows
    val tasks: StateFlow<List<TaskEntity>>
    val notes: StateFlow<List<NoteEntity>>
    val captures: StateFlow<List<CaptureEntity>>
    val events: StateFlow<List<EventEntity>>

    // User profile state
    private val _userProfileName = MutableStateFlow("Alex")
    val userProfileName: StateFlow<String> = _userProfileName.asStateFlow()

    private val _userProfileEmail = MutableStateFlow("you@example.com")
    val userProfileEmail: StateFlow<String> = _userProfileEmail.asStateFlow()

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

    fun clearAnalysisSuccess() {
        _analysisSuccess.value = null
    }
}
