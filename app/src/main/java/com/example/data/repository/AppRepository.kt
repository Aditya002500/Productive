package com.example.data.repository

import com.example.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AppRepository(private val appDao: AppDao) {
    val allTasks: Flow<List<TaskEntity>> = appDao.getAllTasksFlow()
    val allNotes: Flow<List<NoteEntity>> = appDao.getAllNotesFlow()
    val allCaptures: Flow<List<CaptureEntity>> = appDao.getAllCapturesFlow()
    val allEvents: Flow<List<EventEntity>> = appDao.getAllEventsFlow()

    fun getSubTasksForTaskFlow(taskId: Int): Flow<List<SubTaskEntity>> =
        appDao.getSubTasksForTaskFlow(taskId)

    suspend fun getTaskById(id: Int): TaskEntity? = appDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long = appDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = appDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = appDao.deleteTask(task)

    suspend fun getSubTasksForTask(taskId: Int): List<SubTaskEntity> =
        appDao.getSubTasksForTask(taskId)

    suspend fun insertSubTask(subTask: SubTaskEntity) = appDao.insertSubTask(subTask)

    suspend fun updateSubTask(subTask: SubTaskEntity) = appDao.updateSubTask(subTask)

    suspend fun deleteSubTask(subTask: SubTaskEntity) = appDao.deleteSubTask(subTask)

    suspend fun deleteSubTasksForTask(taskId: Int) = appDao.deleteSubTasksForTask(taskId)

    suspend fun insertNote(note: NoteEntity) = appDao.insertNote(note)

    suspend fun deleteNote(note: NoteEntity) = appDao.deleteNote(note)

    suspend fun insertCapture(capture: CaptureEntity): Long = appDao.insertCapture(capture)

    suspend fun getCaptureById(id: Int): CaptureEntity? = appDao.getCaptureById(id)

    suspend fun updateCapture(capture: CaptureEntity) = appDao.updateCapture(capture)

    suspend fun deleteCapture(capture: CaptureEntity) = appDao.deleteCapture(capture)

    suspend fun insertEvent(event: EventEntity) = appDao.insertEvent(event)

    suspend fun deleteEvent(event: EventEntity) = appDao.deleteEvent(event)

    /** DPDP Act right-to-erasure: wipes all locally stored personal data. */
    suspend fun eraseAllUserData() {
        appDao.clearAllSubTasks()
        appDao.clearAllTasks()
        appDao.clearAllNotes()
        appDao.clearAllCaptures()
        appDao.clearAllEvents()
    }

    suspend fun prepopulateIfEmpty() {
        // Check if database is empty (using tasks as a proxy)
        val currentTasks = allTasks.first()
        if (currentTasks.isEmpty()) {
            // Prepopulate Tasks
            val taskId1 = appDao.insertTask(
                TaskEntity(
                    title = "Finalize Q3 Marketing Campaign Deliverables",
                    isCompleted = false,
                    priority = "High",
                    dueDate = "Oct 25",
                    dueTime = "10:00 AM",
                    notes = "Add additional context, meeting links, or random thoughts here..."
                )
            ).toInt()

            appDao.insertTask(
                TaskEntity(
                    title = "Review new brand guidelines",
                    isCompleted = false,
                    priority = "Medium",
                    dueDate = "Oct 24",
                    dueTime = "2:00 PM",
                    notes = "Internal audit of logo colors and typography alignment."
                )
            ).toInt()

            // Prepopulate Sub-tasks
            appDao.insertSubTask(SubTaskEntity(taskId = taskId1, title = "Draft initial copy for landing page", isCompleted = true))
            appDao.insertSubTask(SubTaskEntity(taskId = taskId1, title = "Review ad creatives with design team", isCompleted = false))
            appDao.insertSubTask(SubTaskEntity(taskId = taskId1, title = "Approve budget allocation for paid social", isCompleted = false))

            // Prepopulate Notes
            appDao.insertNote(
                NoteEntity(
                    title = "Meeting Notes",
                    content = "Discussed the new onboarding flow. Need to simplify the step 2 process and reduce the number of required fields. John will provide updated wireframes by Friday.",
                    date = "Oct 23"
                )
            )
            appDao.insertNote(
                NoteEntity(
                    title = "Idea",
                    content = "What if we used a drag-and-drop interface for the timeline view? Could make rescheduling much more intuitive for power users.",
                    date = "Oct 21"
                )
            )
            appDao.insertNote(
                NoteEntity(
                    title = "Groceries",
                    content = "- Oat milk\n- Coffee beans\n- Avocados\n- Sparkling water",
                    date = "Oct 20"
                )
            )

            // Prepopulate Captures
            appDao.insertCapture(
                CaptureEntity(
                    title = "Competitor Dashboard Analysis",
                    timestamp = "2 hours ago",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDWo0nXbI4jGDDhYEx368xutDv5ryO7Dz845tgSXnU5txbjfo_KPY-hd4sb-Y3106K3hiw_aptsEm1LonW0_46p4FbOY3W3bpYic4u9Ow7pkJmqNX6L1NzqQm9yZ8jyTxG9G2pqPzyWw9nOjd4_x1PXfBYL1oQ1fRbWepT-NIOclRWalIUim5UynoFNa0m_vWmtiVnO5bFf2UtC7_pRNULBhvEv-bN41UnkxY57y7steoQWhO705_rXq2-JBEAGqd3X7KH40unzj_sq",
                    category = "UI Ref",
                    extractedText = "Task Overview\nTotal Tasks: 8\nIncomplete: 2\nProgress: 75%\nFinalize Q3 Marketing Report\nReview brand guidelines"
                )
            )
            appDao.insertCapture(
                CaptureEntity(
                    title = "Q3 Software Subscriptions",
                    timestamp = "Yesterday",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDio1a8BCNTqsmXX9kFnMEYKllm2IdqM9Mj4E_AA-3BUguBB7JajMZ4gpV_rBvVt8f4ejfp5TRC-mEWT_iemFRyHTSMhbj4B0v-1isXR8DZfGj2LFvLWvDXxH8bFmgFxLVb5GNf6fA8DmPxdFNoxoWProWXkB5N-VnLdhHPbNiRDbK7i658D8Dm3CFGCR2pde6C4sU-s8O5s0ufV7ZMIAi_1KuOaocTw2LNn8nRilz52eQn9Xx4sKS4_3DZz-qq7CiJvI45waVzvB8i",
                    category = "Expense",
                    extractedText = "Invoice #1289\nDate: 2023-10-23\nAmount: $240.00\nServices: Github, Slack, AWS Cloud hosting"
                )
            )
            appDao.insertCapture(
                CaptureEntity(
                    title = "Auth Service Implementation",
                    timestamp = "Yesterday",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAv3Ikcvxs9LpXSht19aYj11keLgxDy3jV3YkneAlX43NMXAT2sQZnt9fkvB4z-1XQxb_zCW4ZQU_cAf5IWgByWjcL_SqTD3CJp47Vmq3wpjNomsNjy0s9ziMIhLL_rtsPWFcGo3wSbromsAl5z7Dr5r1b1f2kamsIE4TGN6YJkDG-4jsi2AG19IUZ9AJvNhflJihDEF68ZgdtU9LiMTetSsvhGzT4llDGYobhkyVR4fQxJlPs85aMI9SPclB3M0RTPx28Q6hvaCWpz",
                    category = "Snippet",
                    extractedText = "fun authenticateUser(token: String) {\n  val claims = jwtDecoder.decode(token)\n  val userId = claims.subject\n  logger.info(\"Authenticated user: \$userId\")\n}"
                )
            )

            // Prepopulate Calendar Events
            appDao.insertEvent(EventEntity(title = "Gym", timeRange = "7:00 AM", location = "Gym", day = 24, monthName = "October", year = 2023, color = 1))
            appDao.insertEvent(EventEntity(title = "Product Sync: CaptureFlow V2", timeRange = "9:00 - 10:00 AM", location = "Conference Room A", day = 24, monthName = "October", year = 2023, color = 0))
            appDao.insertEvent(EventEntity(title = "Lunch Break", timeRange = "12:00 PM", location = "Cafe", day = 24, monthName = "October", year = 2023, color = 2))
            appDao.insertEvent(EventEntity(title = "CS101: Data Structures", timeRange = "2:00 - 3:00 PM", location = "Hall B", day = 24, monthName = "October", year = 2023, color = 3))
            appDao.insertEvent(EventEntity(title = "Project Alpha", timeRange = "4:00 - 5:00 PM", location = "Office", day = 24, monthName = "October", year = 2023, color = 0))
            
            // Events for Nov 14 (Calendar view)
            appDao.insertEvent(EventEntity(title = "Design Sync", timeRange = "10:00 AM", location = "Conference Room A", day = 14, monthName = "November", year = 2023, color = 0))
            appDao.insertEvent(EventEntity(title = "Client Review", timeRange = "2:00 PM", location = "Google Meet", day = 14, monthName = "November", year = 2023, color = 1))
        }
    }
}
