package com.example.data.repository

import com.example.data.*
import com.example.data.sync.CloudSyncRepository
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val appDao: AppDao,
    private val cloudSync: CloudSyncRepository,
    private val isSyncEnabled: () -> Boolean
) {
    val allTasks: Flow<List<TaskEntity>> = appDao.getAllTasksFlow()
    val allNotes: Flow<List<NoteEntity>> = appDao.getAllNotesFlow()
    val allCaptures: Flow<List<CaptureEntity>> = appDao.getAllCapturesFlow()
    val allEvents: Flow<List<EventEntity>> = appDao.getAllEventsFlow()
    val allHabits: Flow<List<HabitEntity>> = appDao.getAllHabitsFlow()
    fun getHabitLogsFromFlow(fromEpochDay: Long): Flow<List<HabitLogEntity>> = appDao.getLogsFromFlow(fromEpochDay)

    fun getSubTasksForTaskFlow(taskId: Int): Flow<List<SubTaskEntity>> =
        appDao.getSubTasksForTaskFlow(taskId)

    suspend fun getTaskById(id: Int): TaskEntity? = appDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long {
        val stamped = task.copy(updatedAt = System.currentTimeMillis())
        val id = appDao.insertTask(stamped)
        if (isSyncEnabled()) cloudSync.pushTask(stamped.copy(id = id.toInt()), emptyList())
        return id
    }

    suspend fun updateTask(task: TaskEntity) {
        val stamped = task.copy(updatedAt = System.currentTimeMillis())
        appDao.updateTask(stamped)
        if (isSyncEnabled()) cloudSync.pushTask(stamped, appDao.getSubTasksForTask(stamped.id))
    }

    suspend fun deleteTask(task: TaskEntity) {
        appDao.deleteTask(task)
        if (isSyncEnabled()) cloudSync.deleteRemoteTask(task.syncId)
    }

    suspend fun getSubTasksForTask(taskId: Int): List<SubTaskEntity> =
        appDao.getSubTasksForTask(taskId)

    suspend fun insertSubTask(subTask: SubTaskEntity) {
        appDao.insertSubTask(subTask.copy(updatedAt = System.currentTimeMillis()))
        pushParentTaskOf(subTask.taskId)
    }

    suspend fun updateSubTask(subTask: SubTaskEntity) {
        appDao.updateSubTask(subTask.copy(updatedAt = System.currentTimeMillis()))
        pushParentTaskOf(subTask.taskId)
    }

    suspend fun deleteSubTask(subTask: SubTaskEntity) {
        appDao.deleteSubTask(subTask)
        pushParentTaskOf(subTask.taskId)
    }

    suspend fun deleteSubTasksForTask(taskId: Int) {
        appDao.deleteSubTasksForTask(taskId)
        pushParentTaskOf(taskId)
    }

    /** Sub-tasks are embedded in their parent task's Firestore doc, so any sub-task write re-pushes the whole parent. */
    private suspend fun pushParentTaskOf(taskId: Int) {
        if (!isSyncEnabled()) return
        val task = appDao.getTaskById(taskId) ?: return
        cloudSync.pushTask(task, appDao.getSubTasksForTask(taskId))
    }

    suspend fun getNoteById(id: Int): NoteEntity? = appDao.getNoteById(id)

    suspend fun insertNote(note: NoteEntity) {
        val stamped = note.copy(updatedAt = System.currentTimeMillis())
        appDao.insertNote(stamped)
        if (isSyncEnabled()) cloudSync.pushNote(stamped)
    }

    suspend fun deleteNote(note: NoteEntity) {
        appDao.deleteNote(note)
        if (isSyncEnabled()) cloudSync.deleteRemoteNote(note.syncId)
    }

    suspend fun insertCapture(capture: CaptureEntity): Long {
        val stamped = capture.copy(updatedAt = System.currentTimeMillis())
        val id = appDao.insertCapture(stamped)
        if (isSyncEnabled()) cloudSync.pushCapture(stamped.copy(id = id.toInt()))
        return id
    }

    suspend fun getCaptureById(id: Int): CaptureEntity? = appDao.getCaptureById(id)

    suspend fun updateCapture(capture: CaptureEntity) {
        val stamped = capture.copy(updatedAt = System.currentTimeMillis())
        appDao.updateCapture(stamped)
        if (isSyncEnabled()) cloudSync.pushCapture(stamped)
    }

    suspend fun deleteCapture(capture: CaptureEntity) {
        appDao.deleteCapture(capture)
        if (isSyncEnabled()) cloudSync.deleteRemoteCapture(capture.syncId)
    }

    suspend fun insertEvent(event: EventEntity) {
        val stamped = event.copy(updatedAt = System.currentTimeMillis())
        appDao.insertEvent(stamped)
        if (isSyncEnabled()) cloudSync.pushEvent(stamped)
    }

    suspend fun updateEvent(event: EventEntity) {
        val stamped = event.copy(updatedAt = System.currentTimeMillis())
        appDao.updateEvent(stamped)
        if (isSyncEnabled()) cloudSync.pushEvent(stamped)
    }

    suspend fun getEventByGoogleEventId(googleEventId: String): EventEntity? =
        appDao.getEventByGoogleEventId(googleEventId)

    suspend fun deleteEvent(event: EventEntity) {
        appDao.deleteEvent(event)
        if (isSyncEnabled()) cloudSync.deleteRemoteEvent(event.syncId)
    }

    suspend fun toggleEventDone(event: EventEntity) {
        val done = !event.isDone
        appDao.markEventDone(event.id, done)
        if (isSyncEnabled()) cloudSync.pushEvent(event.copy(isDone = done, updatedAt = System.currentTimeMillis()))
    }

    suspend fun insertHabit(habit: HabitEntity): Long {
        val stamped = habit.copy(updatedAt = System.currentTimeMillis())
        val id = appDao.insertHabit(stamped)
        if (isSyncEnabled()) cloudSync.pushHabit(stamped.copy(id = id.toInt()))
        return id
    }

    suspend fun updateHabit(habit: HabitEntity) {
        val stamped = habit.copy(updatedAt = System.currentTimeMillis())
        appDao.updateHabit(stamped)
        if (isSyncEnabled()) cloudSync.pushHabit(stamped)
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        appDao.deleteLogsForHabit(habit.id)
        appDao.deleteHabit(habit)
        if (isSyncEnabled()) cloudSync.deleteRemoteHabit(habit.syncId)
    }

    suspend fun insertHabitLog(log: HabitLogEntity): Long = appDao.insertHabitLog(log)

    fun getLogsForHabitFlow(habitId: Int): Flow<List<HabitLogEntity>> = appDao.getLogsForHabitFlow(habitId)

    suspend fun getLogsForHabit(habitId: Int): List<HabitLogEntity> = appDao.getLogsForHabit(habitId)

    suspend fun getLogsForDay(epochDay: Long): List<HabitLogEntity> = appDao.getLogsForDay(epochDay)

    suspend fun getLogsForHabitSince(habitId: Int, fromEpochDay: Long): List<HabitLogEntity> =
        appDao.getLogsForHabitSince(habitId, fromEpochDay)

    /** DPDP Act right-to-erasure: wipes all locally stored personal data. */
    suspend fun eraseAllUserData() {
        appDao.clearAllSubTasks()
        appDao.clearAllTasks()
        appDao.clearAllNotes()
        appDao.clearAllCaptures()
        appDao.clearAllEvents()
        appDao.clearAllHabitLogs()
        appDao.clearAllHabits()
    }
}
